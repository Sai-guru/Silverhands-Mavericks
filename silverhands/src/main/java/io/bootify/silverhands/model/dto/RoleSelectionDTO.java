package io.bootify.silverhands.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RoleSelectionDTO {

    @NotBlank
    private String role;

}
