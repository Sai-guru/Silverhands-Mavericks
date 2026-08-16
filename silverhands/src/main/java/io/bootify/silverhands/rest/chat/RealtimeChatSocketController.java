package io.bootify.silverhands.rest.chat;

import io.bootify.silverhands.model.dto.RealtimeChatInboundDTO;
import io.bootify.silverhands.model.dto.RealtimeChatOutboundDTO;
import io.bootify.silverhands.service.chat.RealtimeChatService;
import io.bootify.silverhands.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class RealtimeChatSocketController {

    private final RealtimeChatService realtimeChatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid final RealtimeChatInboundDTO inboundDTO,
            final Principal principal) {
        // sender identity is derived from the authenticated WebSocket session,
        // never trusted from the client payload
        if (!(principal instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new IllegalArgumentException("sender identity could not be resolved");
        }
        final var senderUser = userService.getCurrentUser(oauthToken);
        final RealtimeChatOutboundDTO outboundDTO = realtimeChatService.createAndMap(
                inboundDTO, senderUser);
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + outboundDTO.getConversationId(), outboundDTO);
    }

}
