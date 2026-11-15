package com.snapandpack.service;

import com.snapandpack.dto.OrderDtos.CreateOrderRequest;
import com.snapandpack.dto.OrderDtos.OrderItemRequest;
import com.snapandpack.dto.OrderDtos.OrderResponse;
import com.snapandpack.exception.ApiException;
import com.snapandpack.model.CustomerOrder;
import com.snapandpack.model.OrderItem;
import com.snapandpack.model.OrderStatus;
import com.snapandpack.model.Product;
import com.snapandpack.model.Role;
import com.snapandpack.model.User;
import com.snapandpack.repository.OrderRepository;
import com.snapandpack.repository.ProductRepository;
import com.snapandpack.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Placing orders, listing them and moving them through their statuses. */
@Service
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    private final AuditService audit;
    private final NotificationService notifications;

    /** Stock at or below this number after a sale triggers a "low stock" notice to staff. */
    private static final int LOW_STOCK = 5;

    /** Receives the database access this class needs, the activity record and notifications. */
    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users, AuditService audit,
                        NotificationService notifications) {
        this.orders = orders;
        this.products = products;
        this.users = users;
        this.audit = audit;
        this.notifications = notifications;
    }

    /**
     * Saves an order and takes the stock in one transaction: if any line cannot be filled,
     * nothing is saved and no stock is taken. Prices come from the database, never from the browser.
     */
    @Transactional
    public OrderResponse place(Long userId, CreateOrderRequest request) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("Account not found"));

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setPhone(request.phone().trim());
        order.setAddress(request.address().trim());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest line : request.items()) {
            Product product = products.findById(line.productId())
                    .orElseThrow(() -> ApiException.badRequest("A product in your cart is no longer sold"));
            if (!product.getColors().contains(line.color()) || !product.getSizes().contains(line.size())) {
                throw ApiException.badRequest(product.getName() + " is not sold in that colour or size");
            }
            if (products.takeStock(product.getId(), line.quantity()) == 0) {
                throw ApiException.conflict("Not enough stock left for " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setColor(line.color());
            item.setSize(line.size());
            item.setQuantity(line.quantity());
            item.setUnitPrice(product.getPrice());
            order.addItem(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));

            int left = product.getStock() - line.quantity();
            if (left <= LOW_STOCK) {
                notifications.notifyStaff(Role.STOCK_STAFF, "Low stock: " + product.getName(),
                        left == 0 ? "Sold out." : left + " left.", "admin/products.html");
            }
        }
        order.setTotal(total);
        CustomerOrder saved = orders.save(order);

        notifications.notify(user, "Order #" + saved.getId() + " received",
                "Thank you. Your order total is " + PaymentService.money(total) + ". Pay with M-Pesa from My orders to confirm it.",
                "pages/orders.html");
        notifications.notifyStaff(Role.ORDER_STAFF, "New order #" + saved.getId(),
                user.getName() + " ordered " + PaymentService.money(total) + " of goods.", "admin/orders.html");
        return OrderResponse.from(saved);
    }

    /** One customer's own orders, newest first. */
    @Transactional(readOnly = true)
    public List<OrderResponse> listForCustomer(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream().map(OrderResponse::from).toList();
    }

    /** Every order, newest first, for staff. */
    @Transactional(readOnly = true)
    public List<OrderResponse> listAll() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(OrderResponse::from).toList();
    }

    /** Moves an order to a new status, records it and tells the customer. Cancelling puts the stock back; a cancelled order stays cancelled. */
    @Transactional
    public OrderResponse changeStatus(Long orderId, OrderStatus status) {
        CustomerOrder order = orders.findById(orderId).orElseThrow(() -> ApiException.notFound("Order not found"));
        if (order.getStatus() == status) {
            return OrderResponse.from(order);
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw ApiException.badRequest("A cancelled order cannot be reopened");
        }
        if (status == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProductId() != null) {
                    products.returnStock(item.getProductId(), item.getQuantity());
                }
            }
        }
        audit.record("ORDER_STATUS_CHANGED", "Order #" + order.getId() + ": " + order.getStatus() + " to " + status);
        order.setStatus(status);
        notifications.notify(order.getUser(), "Order #" + order.getId() + " " + status.name().toLowerCase(),
                "Your order #" + order.getId() + " is now " + status.name().toLowerCase() + ".", "pages/orders.html");
        return OrderResponse.from(order);
    }
}
