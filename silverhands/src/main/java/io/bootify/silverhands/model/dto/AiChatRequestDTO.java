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

}
