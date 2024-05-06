package com.itel.OrderManagementApplication.order;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {

    private final List<Order> orderList;

    @Autowired
    public OrderService(List<Order> orderList) {
        this.orderList = orderList;
    }

    List<Order> fetchOrders(int max, int page) {
        List<Order> checkedOutOrders =
                this.orderList.stream()
                        .filter(it -> it.getStatus() == OrderStatus.CHECKED_OUT)
                        .collect(Collectors.toList());
        List<List<Order>> orderGroups = Lists.partition(checkedOutOrders, max);
        if (orderGroups.size() < page) {
            return Collections.emptyList();
        } else {
            return orderGroups.get(page - 1);
        }
    }

    int fetchTotalOrderCount() {
        return (int)
                this.orderList.stream().filter(it -> it.getStatus() == OrderStatus.CHECKED_OUT).count();
    }
}
