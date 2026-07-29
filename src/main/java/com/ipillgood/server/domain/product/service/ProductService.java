package com.ipillgood.server.domain.product.service;

import com.ipillgood.server.domain.cabinet.service.CabinetService;
import com.ipillgood.server.domain.ingredient.entity.EffectKeyword;
import com.ipillgood.server.domain.ingredient.entity.Ingredient;
import com.ipillgood.server.domain.ingredient.entity.IngredientCombination;
import com.ipillgood.server.domain.ingredient.entity.enums.CombinationType;
import com.ipillgood.server.domain.ingredient.repository.EffectKeywordRepository;
import com.ipillgood.server.domain.ingredient.repository.IngredientCombinationRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final EffectKeywordRepository effectKeywordRepository;
    private final IngredientCombinationRepository ingredientCombinationRepository;
    private final ProductReviewService reviewService;
    private final CabinetService cabinetService;
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

    public ProductResponse.ProductCombinations getProductCombinations(Long memberId, Long productId) {
        getProduct(productId);

        Set<Long> productIngredientIds = productIngredientRepository.findIngredientsByProduct(productId).stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        Set<Long> ownedIngredientIds = cabinetService.getOwnedIngredients(memberId).stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        Set<Ingredient> goodIngredients = collectOwnedPartners(
                productIngredientIds, ownedIngredientIds, CombinationType.GOOD);
        Set<Ingredient> cautionIngredients = collectOwnedPartners(
                productIngredientIds, ownedIngredientIds, CombinationType.CAUTION);

        return ProductConverter.toProductCombinations(
                productId,
                cabinetService.getOwnedProductCount(memberId),
                goodIngredients,
                cautionIngredients
        );
    }

    public ProductResponse.ProductPurchaseCautionCheck getCautionCombinations(Long memberId, Long productId) {
        Product product = getProduct(productId);

        Set<Long> productIngredientIds = productIngredientRepository.findIngredientsByProduct(productId).stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        Set<Long> ownedIngredientIds = cabinetService.getOwnedIngredients(memberId).stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        List<ProductResponse.ProductPurchaseCautionCheck.CautionCombination> conflicts =
                collectCautionConflicts(productIngredientIds, ownedIngredientIds);

        return ProductConverter.toProductPurchaseCautionCheck(product, conflicts);
    }

    private List<ProductResponse.ProductPurchaseCautionCheck.CautionCombination> collectCautionConflicts(
            Set<Long> productIngredientIds,
            Set<Long> ownedIngredientIds
    ) {
        if (productIngredientIds.isEmpty() || ownedIngredientIds.isEmpty()) {
            return List.of();
        }
        return ingredientCombinationRepository
                .findWithIngredientsByIngredientIdInAndType(productIngredientIds, CombinationType.CAUTION)
                .stream()
                .map(combination -> toCautionCombination(combination, productIngredientIds, ownedIngredientIds))
                .filter(Objects::nonNull)
                .toList();
    }

    private ProductResponse.ProductPurchaseCautionCheck.CautionCombination toCautionCombination(
            IngredientCombination combination,
            Set<Long> productIngredientIds,
            Set<Long> ownedIngredientIds
    ) {
        Ingredient a = combination.getIngredientA();
        Ingredient b = combination.getIngredientB();

        Ingredient productSide;
        Ingredient ownedSide;
        if (productIngredientIds.contains(a.getId()) && ownedIngredientIds.contains(b.getId())) {
            productSide = a;
            ownedSide = b;
        } else if (productIngredientIds.contains(b.getId()) && ownedIngredientIds.contains(a.getId())) {
            productSide = b;
            ownedSide = a;
        } else {
            return null;
        }

        return ProductResponse.ProductPurchaseCautionCheck.CautionCombination.builder()
                .type(combination.getType())
                .currentIngredientId(ownedSide.getId())
                .currentIngredientName(ownedSide.getName())
                .purchaseProductIngredientId(productSide.getId())
                .purchaseIngredientName(productSide.getName())
                .reason(combination.getReason())
                .build();
    }

    private Set<Ingredient> collectOwnedPartners(
            Set<Long> productIngredientIds,
            Set<Long> ownedIngredientIds,
            CombinationType type
    ) {
        if (productIngredientIds.isEmpty()) {
            return Set.of();
        }
        return ingredientCombinationRepository
                .findWithIngredientsByIngredientIdInAndType(productIngredientIds, type).stream()
                .map(combination -> combination.getPartnerOf(productIngredientIds))
                .filter(partner -> ownedIngredientIds.contains(partner.getId()))
                .filter(partner -> !productIngredientIds.contains(partner.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Product getProduct(Long productId) {
        return productRepository.findActiveById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }
}
