package io.bootify.silverhands.rest.ai;

import io.bootify.silverhands.model.dto.AiChatRequestDTO;
import io.bootify.silverhands.model.dto.AiChatResponseDTO;
import io.bootify.silverhands.service.ai.AiChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/ai", produces = MediaType.APPLICATION_JSON_VALUE)
public class AiChatResource {

    private final AiChatService aiChatService;

    public AiChatResource(final AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDTO> chat(
            final OAuth2AuthenticationToken authenticationToken,
            @RequestBody @Valid final AiChatRequestDTO requestDTO) {
        return ResponseEntity.ok(aiChatService.chatWithRecommendations(requestDTO));
    }

}
