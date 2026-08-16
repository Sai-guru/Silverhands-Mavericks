package io.bootify.silverhands.service.ai;

import io.bootify.silverhands.config.ai.AiProviderProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class AiTextClientService {

    private final AiProviderProperties aiProviderProperties;
    private final RestClient restClient;

    public AiTextClientService(final AiProviderProperties aiProviderProperties) {
        this.aiProviderProperties = aiProviderProperties;
        this.restClient = RestClient.builder().build();
    }

    public String generateText(final String prompt) {
        if (aiProviderProperties.apiKey() == null || aiProviderProperties.apiKey().isBlank()) {
            return null;
        }
        return generateWithOpenRouter(prompt);
    }

    private String generateWithOpenRouter(final String prompt) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiProviderProperties.model());
        payload.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        payload.put("temperature", aiProviderProperties.temperature());
        payload.put("max_tokens", aiProviderProperties.maxTokens());

        final RestClient.RequestBodySpec request = restClient.post()
                .uri(aiProviderProperties.baseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + aiProviderProperties.apiKey())
                .contentType(MediaType.APPLICATION_JSON);
        if (aiProviderProperties.siteUrl() != null && !aiProviderProperties.siteUrl().isBlank()) {
            request.header("HTTP-Referer", aiProviderProperties.siteUrl());
        }
        if (aiProviderProperties.appName() != null && !aiProviderProperties.appName().isBlank()) {
            request.header("X-Title", aiProviderProperties.appName());
        }
        final Map<?, ?> response = request
                .body(payload)
                .retrieve()
                .body(Map.class);
        return extractOpenRouterText(response);
    }

    private String extractOpenRouterText(final Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        final Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        final Object firstChoiceObj = choices.getFirst();
        if (!(firstChoiceObj instanceof Map<?, ?> firstChoice)) {
            return null;
        }
        final Object messageObj = firstChoice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            return null;
        }
        final Object contentObj = message.get("content");
        if (contentObj instanceof String text) {
            return text;
        }
        return null;
    }

}
