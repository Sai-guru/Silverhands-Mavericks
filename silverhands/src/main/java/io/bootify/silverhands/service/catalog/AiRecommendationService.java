package io.bootify.silverhands.service.catalog;

import io.bootify.silverhands.domain.catalog.Service;
import io.bootify.silverhands.model.dto.RecommendedServiceDTO;
import io.bootify.silverhands.repos.catalog.ServiceRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;


// fully qualified to avoid the clash between the Service entity and the annotation
@org.springframework.stereotype.Service
public class AiRecommendationService {

    private final ServiceRepository serviceRepository;

    public AiRecommendationService(final ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendedServiceDTO> findRecommendations(final String query, final int maxResults) {
        final int safeMaxResults = Math.max(1, Math.min(maxResults, 20));
        return serviceRepository.search(query == null || query.isBlank() ? null : query.trim(),
                        null, null, PageRequest.of(0, safeMaxResults))
                .stream()
                .map(this::mapToRecommendation)
                .toList();
    }

    private RecommendedServiceDTO mapToRecommendation(final Service service) {
        final RecommendedServiceDTO recommendation = new RecommendedServiceDTO();
        recommendation.setServiceId(service.getId());
        recommendation.setProviderId(service.getProvider().getId());
        recommendation.setProviderName(service.getProvider().getName());
        recommendation.setName(service.getName());
        recommendation.setDescription(service.getDescription());
        recommendation.setCategory(service.getCategory());
        recommendation.setArea(service.getArea());
        recommendation.setPricePerHour(service.getPricePerHour());
        return recommendation;
    }

}
