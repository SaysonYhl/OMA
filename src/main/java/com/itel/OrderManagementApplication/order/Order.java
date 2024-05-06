package com.itel.OrderManagementApplication.order;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    @Getter Long id;
    @Getter List<OrderDetails> orderDetailsList;
    @Getter OrderStatus status;
    @Getter double totalCost;
    @Getter LocalDateTime dateOrdered;

}
