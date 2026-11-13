package com.snapandpack.service;

import com.snapandpack.dto.StatsResponse;
import com.snapandpack.model.OrderStatus;
import com.snapandpack.model.Role;
import com.snapandpack.repository.OrderRepository;
import com.snapandpack.repository.ProductRepository;
import com.snapandpack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final ProductRepository products;
    private final OrderRepository orders;
    private final UserRepository users;

    public DashboardService(ProductRepository products, OrderRepository orders, UserRepository users) {
        this.products = products;
        this.orders = orders;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        long customers = users.countByRole(Role.CUSTOMER);
        return new StatsResponse(products.count(), orders.count(), customers, users.count() - customers,
                orders.sumTotalExcluding(OrderStatus.CANCELLED));
    }
}
