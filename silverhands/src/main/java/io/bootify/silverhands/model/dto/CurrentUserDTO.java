package io.bootify.silverhands.model.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CurrentUserDTO {

    private UUID id;

    private String email;

    private String name;

    private String profileImageUrl;

    private String phone;

    // CUSTOMER, PROVIDER or ROLE_PENDING
    private String role;

    private Boolean roleSelectionRequired;

}
