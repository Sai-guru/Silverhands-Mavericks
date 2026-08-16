package io.bootify.silverhands.service.chat;

import io.bootify.silverhands.domain.chat.Conversation;
import io.bootify.silverhands.domain.user.User;
import io.bootify.silverhands.model.dto.ConversationDTO;
import io.bootify.silverhands.repos.chat.ConversationRepository;
import io.bootify.silverhands.repos.user.UserRepository;
import io.bootify.silverhands.util.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@Transactional(rollbackFor = Exception.class)
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationService(final ConversationRepository conversationRepository,
            final UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> findMyConversations(final User currentUser) {
        return conversationRepository
                .findAllByCustomerIdOrProviderIdOrderByUpdatedAtDesc(currentUser.getId(),
                        currentUser.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationDTO get(final User currentUser, final UUID id) {
        return toDTO(getOwnedConversation(currentUser, id));
    }

    // returns the existing conversation for the pair, or creates one
    public ConversationDTO getOrCreateWith(final User currentUser, final UUID otherUserId) {
        if (currentUser.getId().equals(otherUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "cannot start a conversation with yourself");
        }
        final User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("user not found"));
        final boolean iAmCustomer = "CUSTOMER".equals(currentUser.getRole());
        final User customer = iAmCustomer ? currentUser : other;
        final User provider = iAmCustomer ? other : currentUser;
        if (!"CUSTOMER".equals(customer.getRole()) || !"PROVIDER".equals(provider.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "conversations connect a customer with a provider");
        }
        final Conversation conversation = conversationRepository
                .findFirstByCustomerIdAndProviderId(customer.getId(), provider.getId())
                .orElseGet(() -> {
                    final Conversation created = new Conversation();
                    created.setCustomer(customer);
                    created.setProvider(provider);
                    final OffsetDateTime now = OffsetDateTime.now();
                    created.setCreatedAt(now);
                    created.setUpdatedAt(now);
                    return conversationRepository.save(created);
                });
        return toDTO(conversation);
    }

    public Conversation getOwnedConversation(final User currentUser, final UUID id) {
        final Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("conversation not found"));
        final UUID userId = currentUser.getId();
        if (!conversation.getCustomer().getId().equals(userId)
                && !conversation.getProvider().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "you are not a participant in this conversation");
        }
        return conversation;
    }

    public void touch(final Conversation conversation) {
        conversation.setUpdatedAt(OffsetDateTime.now());
        conversationRepository.save(conversation);
    }

    private ConversationDTO toDTO(final Conversation conversation) {
        final ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setCustomerId(conversation.getCustomer().getId());
        dto.setCustomerName(conversation.getCustomer().getName());
        dto.setProviderId(conversation.getProvider().getId());
        dto.setProviderName(conversation.getProvider().getName());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        return dto;
    }

}
