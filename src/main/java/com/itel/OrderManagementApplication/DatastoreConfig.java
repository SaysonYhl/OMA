package com.itel.OrderManagementApplication;

import com.itel.OrderManagementApplication.product.Product;
import org.junit.jupiter.api.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatastoreConfig {

    @Bean
    public List<Product> getProductList() {
        Product product1 =
                new Product(1L, "Product A", "This is a description for product A", 1000, 149.99);
        Product product2 =
                new Product(2L, "Product B", "This is a description for product B", 300, 450.00);
        Product product3 =
                new Product(3L, "Product C", "This is a description for product C", 4000, 1499.99);
        List<Product> productList = new ArrayList<>();
        productList.add(product1);
        productList.add(product2);
        productList.add(product3);
        return productList;
    }

    @Bean
    public List<Order> getOrderList() {
        return new ArrayList<>();
    }
}
