package com.itel.OrderManagementApplication.product;

import lombok.Getter;

import java.util.Currency;

public class ProductResponse {

    @Getter private Long id;
    @Getter private String name;
    @Getter private String description;
    @Getter private int productQuantity;
    @Getter private double unitPrice;
    @Getter private Currency currency;

    ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.productQuantity = product.getProductQuantity();
        this.unitPrice = product.getUnitPrice();
        this.currency = Currency.getInstance("PHP");
    }
}
