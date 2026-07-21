package com.ipillgood.server.domain.cabinet.controller;

import com.ipillgood.server.domain.cabinet.controller.docs.CabinetApi;
import com.ipillgood.server.domain.cabinet.dto.CabinetRequest;
import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
import com.ipillgood.server.domain.cabinet.service.CabinetService;
import com.ipillgood.server.global.apiPayload.ApiResponse;
import com.ipillgood.server.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cabinet")
public class CabinetController implements CabinetApi {

    private final CabinetService cabinetService;

    @Override
    @GetMapping("/product-candidates")
    public ApiResponse<CabinetResponse.ProductCandidates> getProductCandidates(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size
    ) {
        CabinetResponse.ProductCandidates response =
                cabinetService.getProductCandidates(memberId, keyword, sort, page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/products")
    public ApiResponse<CabinetResponse.ProductList> getProducts(
            @AuthenticationPrincipal Long memberId
    ) {
        CabinetResponse.ProductList response = cabinetService.getProducts(memberId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/products/{memberProductId}")
    public ApiResponse<CabinetResponse.ProductDetail> getProduct(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long memberProductId
    ) {
        CabinetResponse.ProductDetail response = cabinetService.getProduct(memberId, memberProductId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PostMapping("/products")
    public ApiResponse<CabinetResponse.AddProducts> addProducts(
            @AuthenticationPrincipal Long memberId,
            @RequestBody CabinetRequest.AddProducts request
    ) {
        CabinetResponse.AddProducts response = cabinetService.addProducts(memberId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.CREATED, response);
    }

    @Override
    @DeleteMapping("/products")
    public ApiResponse<CabinetResponse.DeleteProducts> deleteProducts(
            @AuthenticationPrincipal Long memberId,
            @RequestBody CabinetRequest.DeleteProducts request
    ) {
        CabinetResponse.DeleteProducts response = cabinetService.deleteProducts(memberId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
