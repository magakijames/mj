package com.snapandpack.controller;

import com.snapandpack.dto.OrderDtos.CreateOrderRequest;
import com.snapandpack.dto.OrderDtos.OrderResponse;
import com.snapandpack.dto.OrderDtos.UpdateStatusRequest;
import com.snapandpack.security.CurrentUser;
import com.snapandpack.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** Any signed-in user can place an order for themselves. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(Authentication authentication, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.place(CurrentUser.id(authentication), request);
    }

    @GetMapping("/mine")
    public List<OrderResponse> mine(Authentication authentication) {
        return orderService.listForCustomer(CurrentUser.id(authentication));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT')")
    public List<OrderResponse> all() {
        return orderService.listAll();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER')")
    public OrderResponse changeStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.changeStatus(id, request.status());
    }
}
