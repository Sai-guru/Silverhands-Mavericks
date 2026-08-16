package io.bootify.silverhands.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RealtimeChatOutboundDTO {

    private UUID id;

    private UUID conversationId;

    private UUID senderUserId;

    private String senderName;

    private String message;

    private OffsetDateTime createdAt;

}
