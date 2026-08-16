package io.bootify.silverhands.repos.chat;

import io.bootify.silverhands.domain.chat.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);

}
