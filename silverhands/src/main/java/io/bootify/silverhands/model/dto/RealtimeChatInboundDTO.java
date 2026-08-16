package io.bootify.silverhands.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RealtimeChatInboundDTO {

    @NotNull
    private UUID conversationId;

    @NotBlank
    private String message;

}
