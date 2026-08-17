package io.bootify.silverhands.model.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RecommendedServiceDTO {

    private UUID serviceId;

    private UUID providerId;

    private String providerName;

    private String name;

    private String description;

    private String category;

    private String area;

    private String phoneNumber;

}
