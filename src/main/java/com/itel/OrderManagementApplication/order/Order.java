package com.itel.OrderManagementApplication.order;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    Long id;
    List<OrderDetails> orderDetailsList;
    double totalCost;
    LocalDateTime dateOrdered;
}
