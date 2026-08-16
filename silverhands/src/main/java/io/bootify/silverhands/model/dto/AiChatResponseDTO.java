package io.bootify.silverhands.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AiChatResponseDTO {

    private String reply;

    private String providerDataSummary;

    private String model;

    private Boolean usedAi;

    private List<RecommendedServiceDTO> recommendedServices;

}
