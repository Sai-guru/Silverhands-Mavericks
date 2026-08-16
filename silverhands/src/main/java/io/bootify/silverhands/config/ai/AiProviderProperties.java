package io.bootify.silverhands.config.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.ai.openrouter")
public record AiProviderProperties(
        String apiKey,
        String baseUrl,
        String model,
        Double temperature,
        Integer maxTokens,
        Integer recommendationLimit,
        String siteUrl,
        String appName) {

    public AiProviderProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://openrouter.ai/api/v1";
        }
        if (model == null || model.isBlank()) {
            model = "nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free";
        }
        if (temperature == null) {
            temperature = 0.7;
        }
        if (maxTokens == null) {
            maxTokens = 4096;
        }
    }

    public int recommendationLimitOrDefault() {
        return recommendationLimit == null ? 5 : recommendationLimit;
    }

}
