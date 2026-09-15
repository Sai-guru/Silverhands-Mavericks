package io.bootify.silverhands.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AiChatRequestDTO {

    @NotBlank
    private String message;

    @NotBlank
    private String inputLanguage;

    @NotBlank
    private String outputLanguage;

    @NotBlank
    private String inputType;

    // Optional: last N turns of conversation, formatted as "User: ...\nAI: ...\n"
    // Frontend builds this string and sends it. Null is fine for first message.
    private String history;

}
