package com.itel.OrderManagementApplication.order;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

class OrderResponse {
    @Getter
    private final Long id;
    @Getter private final List<OrderDetailsResponse> orderDetails;
    @Getter private final Double totalCost;
    @Getter private final LocalDateTime dateOrdered;

    OrderResponse(Order order) {
        this.id = order.getId();
        this.totalCost = order.getTotalCost();
        this.dateOrdered = order.getDateOrdered();
        this.orderDetails =
                order.getOrderDetailsList().stream()
                        .map(OrderDetailsResponse::new)
                        .collect(Collectors.toList());
    }

    class OrderDetailsResponse {
        @Getter private final Long productId;
        @Getter private final Integer quantity;

        OrderDetailsResponse(OrderDetails orderDetails) {
            this.productId = orderDetails.getProductId();
            this.quantity = orderDetails.getQuantity();
        }
    }

}
