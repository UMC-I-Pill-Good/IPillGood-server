package com.ipillgood.server.global.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipillgood.server.global.ai.dto.GeminiRecommendationResult;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Gemini 2.5 Flash generateContent REST API 호출 클라이언트
@Component
public class GeminiClient {

    private static final String RESPONSE_SCHEMA_TYPE_OBJECT = "OBJECT";
    private static final String RESPONSE_SCHEMA_TYPE_ARRAY = "ARRAY";
    private static final String RESPONSE_SCHEMA_TYPE_STRING = "STRING";
    private static final String RESPONSE_SCHEMA_TYPE_INTEGER = "INTEGER";

    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;

    public GeminiClient(GeminiProperties geminiProperties) {
        this.geminiProperties = geminiProperties;
        this.restClient = RestClient.builder()
                .baseUrl(geminiProperties.baseUrl())
                .build();
    }

    public GeminiRecommendationResult generateRecommendation(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", buildResponseSchema()
                )
        );

        Map<String, Object> response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={apiKey}",
                        geminiProperties.model(), geminiProperties.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        String jsonText = extractText(response);
        try {
            return objectMapper.readValue(jsonText, GeminiRecommendationResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini 응답 파싱에 실패했습니다.", e);
        }
    }

    private Map<String, Object> buildResponseSchema() {
        return Map.of(
                "type", RESPONSE_SCHEMA_TYPE_OBJECT,
                "properties", Map.of(
                        "healthSummary", Map.of("type", RESPONSE_SCHEMA_TYPE_STRING),
                        "recommendations", Map.of(
                                "type", RESPONSE_SCHEMA_TYPE_ARRAY,
                                "items", Map.of(
                                        "type", RESPONSE_SCHEMA_TYPE_OBJECT,
                                        "properties", Map.of(
                                                "ingredientId", Map.of("type", RESPONSE_SCHEMA_TYPE_INTEGER),
                                                "aiReason", Map.of("type", RESPONSE_SCHEMA_TYPE_STRING)
                                        ),
                                        "required", List.of("ingredientId", "aiReason")
                                )
                        )
                ),
                "required", List.of("healthSummary", "recommendations")
        );
    }

    private String extractText(Map<String, Object> response) {
        JsonNode root = objectMapper.valueToTree(response);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini 응답에서 결과 텍스트를 찾을 수 없습니다.");
        }
        return textNode.asText();
    }
}
