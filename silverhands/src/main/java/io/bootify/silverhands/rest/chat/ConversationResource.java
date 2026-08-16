package io.bootify.silverhands.rest.chat;

import io.bootify.silverhands.model.dto.ConversationDTO;
import io.bootify.silverhands.model.dto.MessageDTO;
import io.bootify.silverhands.service.chat.ConversationService;
import io.bootify.silverhands.service.chat.MessageService;
import io.bootify.silverhands.service.user.UserService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConversationResource {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;

    public ConversationResource(final ConversationService conversationService,
            final MessageService messageService, final UserService userService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getMyConversations(
            final OAuth2AuthenticationToken token) {
        return ResponseEntity.ok(conversationService.findMyConversations(
                userService.getCurrentUser(token)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDTO> getConversation(@PathVariable(name = "id") final UUID id,
            final OAuth2AuthenticationToken token) {
        return ResponseEntity.ok(conversationService.get(userService.getCurrentUser(token), id));
    }

    // get-or-create a conversation with the given user; the pair keeps a single conversation
    @PostMapping
    public ResponseEntity<ConversationDTO> startConversation(
            @RequestBody final Map<String, UUID> body,
            final OAuth2AuthenticationToken token) {
        final UUID otherUserId = body.get("otherUserId");
        if (otherUserId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        final ConversationDTO conversation = conversationService.getOrCreateWith(
                userService.getCurrentUser(token), otherUserId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable(name = "id") final UUID id,
            final OAuth2AuthenticationToken token) {
        return ResponseEntity.ok(messageService.findMessages(userService.getCurrentUser(token), id));
    }

}
