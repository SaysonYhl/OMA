package com.itel.OrderManagementApplication.order;

import lombok.Getter;

public class OrderDetails {

    @Getter long orderId;
    @Getter long productId;
    @Getter int quantity;
}
