package com.ipillgood.server.domain.product.controller;

import com.ipillgood.server.domain.product.code.ProductSuccessCode;
import com.ipillgood.server.domain.product.controller.docs.ProductApi;
import com.ipillgood.server.domain.product.dto.ProductResponse;
import com.ipillgood.server.domain.product.service.ProductService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
public class ProductController implements ProductApi {

    private final ProductService productService;

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse.ProductInfo> getProductInfo(
            @PathVariable Long productId
    ){
        ProductResponse.ProductInfo resDto = productService.getProductInfo(productId);
        return ApiResponse.onSuccess(ProductSuccessCode.PRODUCT_VIEW_SUCCESS, resDto);
    }
}
