package com.itel.OrderManagementApplication.product;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductService {

    private final List<Product> productList;

    @Autowired
    public ProductService(List<Product> productList) {
        this.productList = productList;
    }

    public Optional<Product> fetchProductById(Long id) {
        return productList.stream().filter(it -> Objects.equals(it.getId(), id)).findFirst();
    }

    List<Product> fetchProducts(int max, int page) {
        List<List<Product>> productGroups = Lists.partition(productList, max);
        if (productGroups.size() < page) {
            return Collections.emptyList();
        } else {
            return productGroups.get(page - 1);
        }
    }

    int fetchTotalProductCount() {
        return productList.size();
    }
}
