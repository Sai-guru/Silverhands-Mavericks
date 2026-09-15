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

        // Extract a clean keyword from the message for better DB matching
        final String searchKeyword = extractKeyword(requestDTO.getMessage());

        final List<RecommendedServiceDTO> recommendations =
                aiRecommendationService.findRecommendations(
                        searchKeyword,
                        aiProviderProperties.recommendationLimitOrDefault());

        final String providerDataSummary = recommendations.isEmpty()
                ? "No matching services found in current data."
                : recommendations.stream()
                        .map(item -> item.getName() + " | " + item.getArea())
                        .collect(Collectors.joining("\n"));

        final String systemPrompt = buildSystemPrompt();
        final String userPrompt = buildUserPrompt(requestDTO, recommendations);
        final String aiReply = aiTextClientService.generateText(systemPrompt, userPrompt);

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

    /**
     * Pulls out the most useful 1-3 word keyword from a natural language message.
     * "I need someone to fix my sink" → "plumber sink fix"
     * We do this by stripping stop words and keeping nouns/action words.
     * Simple approach: just strip common filler and send the rest — the DB LIKE
     * query handles partial matches already.
     */
    private String extractKeyword(final String message) {
        if (message == null || message.isBlank()) return null;
        // Remove common stop words so LIKE search actually finds something
        final String cleaned = message.toLowerCase()
                .replaceAll("\\b(i|need|want|looking for|someone|a|an|the|to|my|me|please|hi|hello|can|you|find|get|help|fix|do|is|are|have|with|in|at|for|of|and|or|but)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        // Take first 3 meaningful words only — LIKE '%...' works better with short terms
        final String[] words = cleaned.split(" ");
        final StringBuilder keyword = new StringBuilder();
        int count = 0;
        for (final String word : words) {
            if (word.length() > 2) {
                if (!keyword.isEmpty()) keyword.append(" ");
                keyword.append(word);
                if (++count == 3) break;
            }
        }
        return keyword.isEmpty() ? message.trim() : keyword.toString();
    }

    private String buildSystemPrompt() {
        return """
                You are the SilverHands AI assistant — a helpful, friendly assistant embedded in the SilverHands home services portal.

                About SilverHands:
                - SilverHands is a platform that connects customers with local service providers for home and personal services.
                - Services include: cleaning, plumbing, cooking, electrical work, tutoring, nursing, carpentry, painting, and more.
                - Every provider on the platform has registered their services with details like area, category, phone number, and availability.
                - Customers can browse providers, chat with them directly, and book their services.

                Your job:
                - Help customers find the right service provider based on what they need.
                - When the user describes a need, look at the service data provided to you and recommend relevant providers.
                - Always mention the provider name, service name, area, and phone number when recommending.
                - If no matching service is in the data, say so honestly — do not invent providers.
                - Keep answers short, warm, and conversational. No bullet-numbered lists unless listing multiple providers.
                - If the user asks something unrelated to services (e.g. general questions), answer helpfully but gently steer back to the portal's purpose.
                - Respond in the same language the user wrote in.
                """;
    }

    private String buildUserPrompt(final AiChatRequestDTO requestDTO,
            final List<RecommendedServiceDTO> recommendations) {

        final String serviceData = recommendations.isEmpty()
                ? "No services found in the database for this query."
                : recommendations.stream()
                        .map(item -> "- Provider: " + item.getProviderName()
                                + " | Service: " + item.getName()
                                + " | Category: " + item.getCategory()
                                + " | Area: " + item.getArea()
                                + " | Phone: " + item.getPhoneNumber()
                                + (item.getDescription() != null ? " | Info: " + item.getDescription() : ""))
                        .collect(Collectors.joining("\n"));

        // Include conversation history if present
        final String history = (requestDTO.getHistory() == null || requestDTO.getHistory().isBlank())
                ? ""
                : "Previous conversation:\n" + requestDTO.getHistory() + "\n\n";

        return history
                + "User message: " + requestDTO.getMessage() + "\n\n"
                + "Available services from our database:\n" + serviceData + "\n\n"
                + "Reply in: " + requestDTO.getOutputLanguage();
    }

}