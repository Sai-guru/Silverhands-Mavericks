package io.bootify.silverhands.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class MessageDTO {

    private UUID id;

    private UUID conversationId;

    private UUID senderUserId;

    private String senderName;

    @NotBlank
    private String message;

    private OffsetDateTime createdAt;

}
