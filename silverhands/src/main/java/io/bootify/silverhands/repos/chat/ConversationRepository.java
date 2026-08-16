package io.bootify.silverhands.repos.chat;

import io.bootify.silverhands.domain.chat.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findAllByCustomerIdOrProviderIdOrderByUpdatedAtDesc(UUID customerId,
            UUID providerId);

    Optional<Conversation> findFirstByCustomerIdAndProviderId(UUID customerId, UUID providerId);

}
