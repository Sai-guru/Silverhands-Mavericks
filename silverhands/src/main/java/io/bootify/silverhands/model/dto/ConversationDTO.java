package io.bootify.silverhands.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ConversationDTO {

    private UUID id;

    private UUID customerId;

    private String customerName;

    private UUID providerId;

    private String providerName;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}
