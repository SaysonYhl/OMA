package com.itel.OrderManagementApplication.product;

import com.itel.OrderManagementApplication.web.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/v1/product/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        Product product = this.productService.fetchProductById(id).get();
        return new ProductResponse(product);
    }

    @GetMapping("/api/v1/product")
    public PageResponse<ProductResponse> getProducts(
            @RequestParam(value = "max", defaultValue = "2") int max,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        List<Product> products = productService.fetchProducts(max, page);

        List<ProductResponse> productResponseList =
                products.stream().map(ProductResponse::new).collect(Collectors.toList());

        return new PageResponse<>(productService.fetchTotalProductCount(), page, productResponseList);
    }
}
