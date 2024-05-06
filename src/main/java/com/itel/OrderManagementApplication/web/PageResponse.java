package com.itel.OrderManagementApplication.web;

import lombok.Getter;

import java.util.List;

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
