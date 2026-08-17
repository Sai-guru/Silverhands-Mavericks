package io.bootify.silverhands.service.ai;

import io.bootify.silverhands.config.ai.AiProviderProperties;
import io.bootify.silverhands.model.dto.AiChatRequestDTO;
import io.bootify.silverhands.model.dto.AiChatResponseDTO;
import io.bootify.silverhands.model.dto.RecommendedServiceDTO;
import io.bootify.silverhands.service.catalog.AiRecommendationService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiRecommendationService aiRecommendationService;
    private final AiTextClientService aiTextClientService;
    private final AiProviderProperties aiProviderProperties;

    public AiChatResponseDTO chatWithRecommendations(final AiChatRequestDTO requestDTO) {
        final List<RecommendedServiceDTO> recommendations =
                aiRecommendationService.findRecommendations(requestDTO.getMessage(),
                        aiProviderProperties.recommendationLimitOrDefault());

        final String providerDataSummary = recommendations.isEmpty()
                ? "No matching services found in current data."
                : recommendations.stream()
                        .map(item -> item.getName() + " | " + item.getArea())
                        .collect(Collectors.joining("\n"));

        final String aiReply = aiTextClientService.generateText(buildPrompt(requestDTO, recommendations));

        final AiChatResponseDTO responseDTO = new AiChatResponseDTO();
        responseDTO.setRecommendedServices(recommendations);
        responseDTO.setProviderDataSummary(providerDataSummary);
        responseDTO.setModel(aiProviderProperties.model());
        if (aiReply == null || aiReply.isBlank()) {
            responseDTO.setUsedAi(false);
            responseDTO.setReply("I found " + recommendations.size()
                    + " matching service(s). Please check the recommendedServices list.");
        } else {
            responseDTO.setUsedAi(true);
            responseDTO.setReply(aiReply);
        }
        return responseDTO;
    }

    private String buildPrompt(final AiChatRequestDTO requestDTO,
            final List<RecommendedServiceDTO> recommendations) {
        return """
                You are SilverHands assistant.
                User input language: %s
                Response language: %s
                Input type: %s
                User request: %s

                Use only the service data below to answer. Do not invent unavailable services or providers.
                Service data:
                %s

                Reply with:
                1) short understanding of request
                2) top matching services/providers summary
                3) one next-step suggestion for the user
                """
                .formatted(requestDTO.getInputLanguage(), requestDTO.getOutputLanguage(),
                        requestDTO.getInputType(), requestDTO.getMessage(),
                        recommendations.stream()
                                .map(item -> "- " + item.getName() + " | providerId: "
                                        + item.getProviderId() + " | serviceId: "
                                        + item.getServiceId() + " | " + item.getArea()
                                        + " | rate: " + item.getPricePerHour() + " per hour")
                                .collect(Collectors.joining("\n")));
    }

}
