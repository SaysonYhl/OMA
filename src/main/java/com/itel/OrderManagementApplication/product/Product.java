package com.itel.OrderManagementApplication.product;

import lombok.Getter;

public class Product {

    @Getter Long id;
    @Getter String name;
    @Getter String description;
    @Getter int productQuantity;
    @Getter double unitPrice;

    public Product(Long id, String name, String description, int productQuantity, double unitPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.productQuantity = productQuantity;
        this.unitPrice = unitPrice;
    }
}
