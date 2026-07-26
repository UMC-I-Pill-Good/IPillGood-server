package com.ipillgood.server.domain.product.service;

import com.ipillgood.server.domain.ingredient.entity.EffectKeyword;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.repository.EffectKeywordRepository;
import com.ipillgood.server.domain.product.code.ProductErrorCode;
import com.ipillgood.server.domain.product.converter.ProductConverter;
import com.ipillgood.server.domain.product.dto.ProductResponse;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.domain.product.exception.ProductException;
import com.ipillgood.server.domain.product.repository.ProductIngredientRepository;
import com.ipillgood.server.domain.product.repository.ProductRepository;
import com.ipillgood.server.domain.review.dto.ProductReviewResponse;
import com.ipillgood.server.domain.review.service.ProductReviewService;
import com.ipillgood.server.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final EffectKeywordRepository effectKeywordRepository;
    private final ProductReviewService reviewService;
    private final S3Service s3Service;

    public ProductResponse.ProductInfo getProductInfo(Long productId) {
        Product product = getProduct(productId);

        List<Ingredient> includedIngredients = productIngredientRepository
                .findIngredientsByProduct(productId);

        ProductReviewResponse.ReviewSummary reviewSummary = reviewService.getReviewSummary(product);
        return ProductConverter.toProductInfo(product, includedIngredients, reviewSummary, s3Service::getPublicUrl);
    }

    public ProductResponse.ProductIngredientsInfo getProductIngredients(Long productId) {
        getProduct(productId);
        List<Ingredient> includedIngredients = productIngredientRepository.findIngredientsByProduct(productId);

        Map<Long, List<String>> keywordsByIngredient = effectKeywordRepository
                .findByIngredient_IdInOrderByIdAsc(includedIngredients.stream()
                        .map(Ingredient::getId).toList())
                .stream().collect(Collectors.groupingBy(
                        ek -> ek.getIngredient().getId(),
                        Collectors.mapping(EffectKeyword::getKeyword, Collectors.toList())
                ));

        return ProductConverter.toProductIngredientsInfo(
                productId,
                includedIngredients,
                keywordsByIngredient,
                s3Service::getPublicUrl
        );
    }

    private Product getProduct(Long productId) {
        return productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
