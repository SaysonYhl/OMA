package com.itel.OrderManagementApplication.web;

import com.itel.OrderManagementApplication.product.Product;
import com.itel.OrderManagementApplication.product.ProductResponse;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

public class PageResponse<T> {
    @Getter private int totalCount;

    @Getter private int pageNumber;

    @Getter private List<T> content;

    public PageResponse(int totalCount, int pageNumber, List<T> content) {
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.content = content;
    }
}
