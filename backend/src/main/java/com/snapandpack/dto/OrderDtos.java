package com.snapandpack.dto;

import com.snapandpack.model.CustomerOrder;
import com.snapandpack.model.OrderItem;
import com.snapandpack.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/** Request and response bodies for orders. */
public final class OrderDtos {

    private OrderDtos() {
    }

    /** One line of the cart: which product, in which colour and size, and how many. */
    public record OrderItemRequest(
            @NotNull Long productId,
            @NotBlank String color,
            @NotBlank String size,
            @Min(value = 1, message = "Quantity must be at least 1") @Max(value = 99, message = "Quantity is too large") int quantity) {
    }

    /** What checkout sends. Prices are never taken from the browser; they are read from the database. */
    public record CreateOrderRequest(
            @NotEmpty(message = "Your cart is empty") @Size(max = 50, message = "Too many different items in one order") List<@Valid OrderItemRequest> items,
            @NotBlank(message = "Enter a phone number") @Size(max = 30) String phone,
            @NotBlank(message = "Enter a delivery address") @Size(max = 500) String address) {
    }

    /** Used by staff to move an order to a new status. */
    public record UpdateStatusRequest(@NotNull(message = "Choose a status") OrderStatus status) {
    }

    /** One line of a saved order. */
    public record OrderItemResponse(String productName, String color, String size, int quantity, BigDecimal unitPrice) {

        /** Copies an order line from the database into a response. */
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getProductName(), item.getColor(), item.getSize(), item.getQuantity(),
                    item.getUnitPrice());
        }
    }

    /** A saved order as the front end sees it. */
    public record OrderResponse(Long id, Instant createdAt, OrderStatus status, BigDecimal total, String customerName,
                                String customerEmail, String phone, String address, List<OrderItemResponse> items) {

        /** Copies an order, its customer and its lines from the database into a response. */
        public static OrderResponse from(CustomerOrder order) {
            return new OrderResponse(order.getId(), order.getCreatedAt().toInstant(ZoneOffset.UTC), order.getStatus(),
                    order.getTotal(), order.getUser().getName(), order.getUser().getEmail(), order.getPhone(),
                    order.getAddress(), order.getItems().stream().map(OrderItemResponse::from).toList());
        }
    }
}
