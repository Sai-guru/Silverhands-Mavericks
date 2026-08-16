package io.bootify.silverhands.service.chat;

import io.bootify.silverhands.domain.user.User;
import io.bootify.silverhands.model.dto.MessageDTO;
import io.bootify.silverhands.model.dto.RealtimeChatInboundDTO;
import io.bootify.silverhands.model.dto.RealtimeChatOutboundDTO;
import java.util.UUID;
import org.springframework.stereotype.Service;


@Service
public class RealtimeChatService {

    private final MessageService messageService;

    public RealtimeChatService(final MessageService messageService) {
        this.messageService = messageService;
    }

    public RealtimeChatOutboundDTO createAndMap(final RealtimeChatInboundDTO inboundDTO,
            final User senderUser) {
        final MessageDTO savedMessage = messageService.createMessage(senderUser,
                inboundDTO.getConversationId(), inboundDTO.getMessage());
        final RealtimeChatOutboundDTO outboundDTO = new RealtimeChatOutboundDTO();
        outboundDTO.setId(savedMessage.getId());
        outboundDTO.setConversationId(savedMessage.getConversationId());
        outboundDTO.setSenderUserId(savedMessage.getSenderUserId());
        outboundDTO.setSenderName(savedMessage.getSenderName());
        outboundDTO.setMessage(savedMessage.getMessage());
        outboundDTO.setCreatedAt(savedMessage.getCreatedAt());
        return outboundDTO;
    }

}
