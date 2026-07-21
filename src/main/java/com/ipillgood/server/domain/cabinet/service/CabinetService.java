package com.ipillgood.server.domain.cabinet.service;

import com.ipillgood.server.domain.cabinet.code.CabinetErrorCode;
import com.ipillgood.server.domain.cabinet.converter.CabinetConverter;
import com.ipillgood.server.domain.cabinet.dto.CabinetRequest;
import com.ipillgood.server.domain.cabinet.dto.CabinetResponse;
import com.ipillgood.server.domain.cabinet.entity.MemberProduct;
import com.ipillgood.server.domain.cabinet.exception.CabinetException;
import com.ipillgood.server.domain.cabinet.repository.CabinetAddedProductRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductCandidateRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductCandidateTagRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductDetailRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductIngredientKeywordRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetProductRow;
import com.ipillgood.server.domain.cabinet.repository.CabinetReviewPromptRow;
import com.ipillgood.server.domain.cabinet.repository.MemberProductRepository;
import com.ipillgood.server.domain.intake.entity.MemberActiveProduct;
import com.ipillgood.server.domain.intake.repository.MemberActiveProductRepository;
import com.ipillgood.server.domain.member.entity.Member;
import com.ipillgood.server.domain.member.repository.MemberRepository;
import com.ipillgood.server.domain.product.entity.Product;
import com.ipillgood.server.domain.product.repository.ProductRepository;
import com.ipillgood.server.global.apiPayload.code.GeneralErrorCode;
import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CabinetService {

    private static final int DEFAULT_PRODUCT_CANDIDATE_PAGE = 0;
    private static final int DEFAULT_PRODUCT_CANDIDATE_SIZE = 20;
    private static final int MAX_PRODUCT_CANDIDATE_SIZE = 100;
    private static final int MAX_PRODUCT_CANDIDATE_KEYWORD_LENGTH = 100;
    private static final int REVIEW_PROMPT_DUE_DAYS = 30;

    private final MemberRepository memberRepository;
    private final MemberProductRepository memberProductRepository;
    private final MemberActiveProductRepository memberActiveProductRepository;
    private final ProductRepository productRepository;

    @Value("${app.storage.public-base-url:https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com}")
    private String storagePublicBaseUrl;

    public CabinetResponse.ProductCandidates getProductCandidates(
            Long memberId,
            String keyword,
            String sort,
            String page,
            String size
    ) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        ProductCandidateSearchCondition condition =
                validateProductCandidateSearchCondition(keyword, sort, page, size);
        PageRequest pageRequest = PageRequest.of(condition.page(), condition.size());
        Page<CabinetProductCandidateRow> candidatePage = findProductCandidatePage(memberId, condition, pageRequest);

        List<Long> productIds = candidatePage.getContent()
                .stream()
                .map(CabinetProductCandidateRow::productId)
                .toList();
        Map<Long, List<String>> tagsByProductId = findProductCandidateTags(productIds);

        return CabinetConverter.toProductCandidates(
                condition.keyword(),
                condition.sort().name(),
                condition.page(),
                condition.size(),
                candidatePage.getTotalElements(),
                candidatePage.hasNext(),
                candidatePage.getContent(),
                tagsByProductId,
                storagePublicBaseUrl
        );
    }

    public CabinetResponse.ProductList getProducts(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        List<CabinetProductRow> products = memberProductRepository.findActiveCabinetProducts(memberId);
        return CabinetConverter.toProductList(member.getNickname(), products, storagePublicBaseUrl);
    }

    public CabinetResponse.ReviewPrompts getDueReviewPrompts(Long memberId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        LocalDate dueStartedOn = LocalDate.now().minusDays(REVIEW_PROMPT_DUE_DAYS);
        List<CabinetReviewPromptRow> reviewPrompts =
                memberProductRepository.findDueReviewPrompts(memberId, dueStartedOn);
        return CabinetConverter.toReviewPrompts(reviewPrompts);
    }

    @Transactional
    public CabinetResponse.ReviewPromptDismissed dismissReviewPrompt(Long memberId, Long activeProductId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);
        validateActiveProductId(activeProductId);

        MemberActiveProduct activeProduct = memberActiveProductRepository
                .findActiveReviewPromptDismissTarget(memberId, activeProductId)
                .orElseThrow(() -> new CabinetException(CabinetErrorCode.REVIEW_PROMPT_NOT_FOUND));
        activeProduct.dismissReviewPrompt(LocalDateTime.now());

        return CabinetConverter.toReviewPromptDismissed(activeProduct);
    }

    public CabinetResponse.ProductDetail getProduct(Long memberId, Long memberProductId) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);
        validateMemberProductId(memberProductId);

        CabinetProductDetailRow product = memberProductRepository
                .findActiveCabinetProductDetail(memberId, memberProductId)
                .orElseThrow(() -> new CabinetException(CabinetErrorCode.MEMBER_PRODUCT_NOT_FOUND));
        List<CabinetProductIngredientKeywordRow> ingredients =
                memberProductRepository.findProductIngredientKeywordRows(memberId, memberProductId);

        return CabinetConverter.toProductDetail(product, ingredients, LocalDate.now(), storagePublicBaseUrl);
    }

    @Transactional
    public CabinetResponse.AddProducts addProducts(Long memberId, CabinetRequest.AddProducts request) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        List<Long> productIds = validateAddProductIds(request);
        List<Product> products = productRepository.findActiveByIdIn(productIds);
        validateAllProductsExist(productIds, products);
        validateNotAlreadyOwned(memberId, productIds);

        Map<Long, Product> productsById = toProductsById(products);
        LocalDateTime addedAt = LocalDateTime.now();
        List<MemberProduct> memberProducts = productIds.stream()
                .map(productId -> MemberProduct.builder()
                        .member(member)
                        .product(productsById.get(productId))
                        .addedAt(addedAt)
                        .build())
                .toList();

        List<MemberProduct> savedMemberProducts = memberProductRepository.saveAll(memberProducts);
        List<Long> memberProductIds = savedMemberProducts.stream()
                .map(MemberProduct::getId)
                .toList();

        List<CabinetAddedProductRow> addedProductRows =
                memberProductRepository.findAddedProductsByIds(memberId, memberProductIds);
        if (addedProductRows.size() != memberProductIds.size()) {
            throw new CabinetException(CabinetErrorCode.ADD_TARGET_PRODUCT_NOT_FOUND);
        }

        Map<Long, Integer> productOrder = toProductOrder(productIds);
        List<CabinetAddedProductRow> orderedRows = addedProductRows.stream()
                .sorted(Comparator.comparingInt(row -> productOrder.get(row.productId())))
                .toList();
        return CabinetConverter.toAddProducts(orderedRows, storagePublicBaseUrl);
    }

    @Transactional
    public CabinetResponse.DeleteProducts deleteProducts(Long memberId, CabinetRequest.DeleteProducts request) {
        Member member = getMember(memberId);
        validateOnboardingCompleted(member);

        List<Long> memberProductIds = validateDeleteMemberProductIds(request);
        List<MemberProduct> memberProducts =
                memberProductRepository.findActiveProductsForDelete(memberId, memberProductIds);
        validateAllMemberProductsExist(memberProductIds, memberProducts);

        Map<Long, MemberProduct> memberProductsById = toMemberProductsById(memberProducts);
        List<MemberProduct> orderedMemberProducts = memberProductIds.stream()
                .map(memberProductsById::get)
                .toList();
        List<MemberActiveProduct> activeProducts =
                memberActiveProductRepository.findActiveByMemberProductIds(memberId, memberProductIds);
        Map<Long, MemberActiveProduct> activeProductsByMemberProductId =
                toActiveProductsByMemberProductId(activeProducts);

        CabinetResponse.DeleteProducts response =
                CabinetConverter.toDeleteProducts(orderedMemberProducts, activeProductsByMemberProductId);
        LocalDateTime deletedAt = LocalDateTime.now();
        LocalDate stoppedOn = LocalDate.now();

        orderedMemberProducts.forEach(memberProduct -> memberProduct.markDeleted(deletedAt));
        activeProducts.forEach(activeProduct -> activeProduct.markStopped(stoppedOn));

        return response;
    }

    private ProductCandidateSearchCondition validateProductCandidateSearchCondition(
            String keyword,
            String sort,
            String page,
            String size
    ) {
        String normalizedKeyword = normalizeProductCandidateKeyword(keyword);
        ProductCandidateSort candidateSort = parseProductCandidateSort(sort);
        int parsedPage = parseProductCandidatePage(page);
        int parsedSize = parseProductCandidateSize(size);
        String searchKeyword = normalizedKeyword == null
                ? null
                : normalizedKeyword.toLowerCase(Locale.ROOT);

        return new ProductCandidateSearchCondition(
                normalizedKeyword,
                searchKeyword,
                candidateSort,
                parsedPage,
                parsedSize
        );
    }

    private String normalizeProductCandidateKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String normalizedKeyword = keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return null;
        }
        if (normalizedKeyword.length() > MAX_PRODUCT_CANDIDATE_KEYWORD_LENGTH) {
            throw new CabinetException(CabinetErrorCode.PRODUCT_CANDIDATE_SEARCH_CONDITION_INVALID);
        }
        return normalizedKeyword;
    }

    private ProductCandidateSort parseProductCandidateSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return ProductCandidateSort.REVIEW_COUNT_DESC;
        }

        try {
            return ProductCandidateSort.valueOf(sort.trim());
        } catch (IllegalArgumentException e) {
            throw new CabinetException(CabinetErrorCode.PRODUCT_CANDIDATE_SEARCH_CONDITION_INVALID);
        }
    }

    private int parseProductCandidatePage(String page) {
        if (page == null || page.isBlank()) {
            return DEFAULT_PRODUCT_CANDIDATE_PAGE;
        }

        int parsedPage = parseInteger(page);
        if (parsedPage < 0) {
            throw new CabinetException(CabinetErrorCode.PRODUCT_CANDIDATE_SEARCH_CONDITION_INVALID);
        }
        return parsedPage;
    }

    private int parseProductCandidateSize(String size) {
        if (size == null || size.isBlank()) {
            return DEFAULT_PRODUCT_CANDIDATE_SIZE;
        }

        int parsedSize = parseInteger(size);
        if (parsedSize < 1 || parsedSize > MAX_PRODUCT_CANDIDATE_SIZE) {
            throw new CabinetException(CabinetErrorCode.PRODUCT_CANDIDATE_SEARCH_CONDITION_INVALID);
        }
        return parsedSize;
    }

    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new CabinetException(CabinetErrorCode.PRODUCT_CANDIDATE_SEARCH_CONDITION_INVALID);
        }
    }

    private Page<CabinetProductCandidateRow> findProductCandidatePage(
            Long memberId,
            ProductCandidateSearchCondition condition,
            PageRequest pageRequest
    ) {
        return switch (condition.sort()) {
            case REVIEW_COUNT_DESC -> memberProductRepository.findProductCandidatesOrderByReviewCountDesc(
                    memberId,
                    condition.searchKeyword(),
                    pageRequest
            );
            case RATING_DESC -> memberProductRepository.findProductCandidatesOrderByRatingDesc(
                    memberId,
                    condition.searchKeyword(),
                    pageRequest
            );
        };
    }

    private Map<Long, List<String>> findProductCandidateTags(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, List<String>> tagsByProductId = new LinkedHashMap<>();
        for (CabinetProductCandidateTagRow tagRow : memberProductRepository.findProductCandidateTags(productIds)) {
            if (tagRow.keyword() == null || tagRow.keyword().isBlank()) {
                continue;
            }

            List<String> tags = tagsByProductId.computeIfAbsent(tagRow.productId(), productId -> new ArrayList<>());
            if (!tags.contains(tagRow.keyword())) {
                tags.add(tagRow.keyword());
            }
        }
        return tagsByProductId;
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.UNAUTHORIZED));
    }

    private void validateOnboardingCompleted(Member member) {
        if (member.getOnboardingCompletedAt() == null) {
            throw new CabinetException(CabinetErrorCode.ONBOARDING_NOT_COMPLETED);
        }
    }

    private List<Long> validateAddProductIds(CabinetRequest.AddProducts request) {
        if (request == null || request.productIds() == null || request.productIds().isEmpty()) {
            throw new CabinetException(CabinetErrorCode.ADD_PRODUCT_LIST_INVALID);
        }

        List<Long> productIds = request.productIds();
        Set<Long> uniqueProductIds = new HashSet<>();
        for (Long productId : productIds) {
            if (productId == null || productId < 1 || !uniqueProductIds.add(productId)) {
                throw new CabinetException(CabinetErrorCode.ADD_PRODUCT_LIST_INVALID);
            }
        }
        return productIds;
    }

    private List<Long> validateDeleteMemberProductIds(CabinetRequest.DeleteProducts request) {
        if (request == null || request.memberProductIds() == null || request.memberProductIds().isEmpty()) {
            throw new CabinetException(CabinetErrorCode.MEMBER_PRODUCT_ID_INVALID);
        }

        List<Long> memberProductIds = request.memberProductIds();
        Set<Long> uniqueMemberProductIds = new HashSet<>();
        for (Long memberProductId : memberProductIds) {
            if (memberProductId == null || memberProductId < 1 || !uniqueMemberProductIds.add(memberProductId)) {
                throw new CabinetException(CabinetErrorCode.MEMBER_PRODUCT_ID_INVALID);
            }
        }
        return memberProductIds;
    }

    private void validateMemberProductId(Long memberProductId) {
        if (memberProductId == null || memberProductId < 1) {
            throw new CabinetException(CabinetErrorCode.MEMBER_PRODUCT_ID_INVALID);
        }
    }

    private void validateActiveProductId(Long activeProductId) {
        if (activeProductId == null || activeProductId < 1) {
            throw new CabinetException(CabinetErrorCode.REVIEW_PROMPT_ID_INVALID);
        }
    }

    private void validateAllProductsExist(List<Long> productIds, List<Product> products) {
        if (products.size() != productIds.size()) {
            throw new CabinetException(CabinetErrorCode.ADD_TARGET_PRODUCT_NOT_FOUND);
        }
    }

    private void validateAllMemberProductsExist(List<Long> memberProductIds, List<MemberProduct> memberProducts) {
        if (memberProducts.size() != memberProductIds.size()) {
            throw new CabinetException(CabinetErrorCode.MEMBER_PRODUCT_NOT_FOUND);
        }
    }

    private void validateNotAlreadyOwned(Long memberId, List<Long> productIds) {
        List<Long> ownedProductIds = memberProductRepository.findActiveOwnedProductIds(memberId, productIds);
        if (!ownedProductIds.isEmpty()) {
            throw new CabinetException(CabinetErrorCode.PRODUCT_ALREADY_OWNED);
        }
    }

    private Map<Long, Product> toProductsById(List<Product> products) {
        Map<Long, Product> productsById = new HashMap<>();
        products.forEach(product -> productsById.put(product.getId(), product));
        return productsById;
    }

    private Map<Long, MemberProduct> toMemberProductsById(List<MemberProduct> memberProducts) {
        Map<Long, MemberProduct> memberProductsById = new HashMap<>();
        memberProducts.forEach(memberProduct -> memberProductsById.put(memberProduct.getId(), memberProduct));
        return memberProductsById;
    }

    private Map<Long, MemberActiveProduct> toActiveProductsByMemberProductId(
            List<MemberActiveProduct> activeProducts
    ) {
        Map<Long, MemberActiveProduct> activeProductsByMemberProductId = new HashMap<>();
        activeProducts.forEach(activeProduct ->
                activeProductsByMemberProductId.put(activeProduct.getMemberProduct().getId(), activeProduct)
        );
        return activeProductsByMemberProductId;
    }

    private Map<Long, Integer> toProductOrder(List<Long> productIds) {
        Map<Long, Integer> productOrder = new HashMap<>();
        for (int index = 0; index < productIds.size(); index++) {
            productOrder.put(productIds.get(index), index);
        }
        return productOrder;
    }

    private enum ProductCandidateSort {
        REVIEW_COUNT_DESC,
        RATING_DESC
    }

    private record ProductCandidateSearchCondition(
            String keyword,
            String searchKeyword,
            ProductCandidateSort sort,
            int page,
            int size
    ) {
    }
}
