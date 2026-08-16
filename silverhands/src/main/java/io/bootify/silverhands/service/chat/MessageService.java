package io.bootify.silverhands.service.chat;

import io.bootify.silverhands.domain.chat.Conversation;
import io.bootify.silverhands.domain.chat.Message;
import io.bootify.silverhands.domain.user.User;
import io.bootify.silverhands.model.dto.MessageDTO;
import io.bootify.silverhands.repos.chat.MessageRepository;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;

    @Transactional(readOnly = true)
    public List<MessageDTO> findMessages(final User currentUser, final UUID conversationId) {
        conversationService.getOwnedConversation(currentUser, conversationId);
        return messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // sender identity is resolved server-side; the sender must participate in the conversation
    public MessageDTO createMessage(final User sender, final UUID conversationId, final String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("message content is required");
        }
        final Conversation conversation = conversationService.getOwnedConversation(sender,
                conversationId);
        final Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setMessage(content.trim());
        message.setCreatedAt(OffsetDateTime.now());
        final Message saved = messageRepository.save(message);
        conversationService.touch(conversation);
        return toDTO(saved);
    }

    private MessageDTO toDTO(final Message message) {
        final MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderUserId(message.getSender().getId());
        dto.setSenderName(message.getSender().getName());
        dto.setMessage(message.getMessage());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

}
