package io.bootify.silverhands.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ServiceDTO {

    private UUID id;

    @NotBlank
    private String name;

    private String description;

    private String category;

    @NotNull
    @Positive
    private BigDecimal pricePerHour;

    @NotBlank
    private String area;

    private LocalTime availableFrom;

    private LocalTime availableTo;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private UUID providerId;

    private String providerName;

}
