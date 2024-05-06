package com.itel.OrderManagementApplication.order;

import com.itel.OrderManagementApplication.web.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/api/v1/order")
    public PageResponse<OrderResponse> fetchOrders(
            @RequestParam(value = "max", defaultValue = "3") int max,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        List<Order> orders = orderService.fetchOrders(max, page);
        List<OrderResponse> orderResponseList =
                orders.stream().map(OrderResponse::new).collect(Collectors.toList());
        return new PageResponse<>(orderService.fetchTotalOrderCount(), page, orderResponseList);
    }
}
