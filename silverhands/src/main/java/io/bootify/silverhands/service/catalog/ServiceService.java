package io.bootify.silverhands.service.catalog;

import io.bootify.silverhands.domain.catalog.Service;
import io.bootify.silverhands.domain.user.User;
import io.bootify.silverhands.model.dto.ServiceDTO;
import io.bootify.silverhands.repos.catalog.ServiceRepository;
import io.bootify.silverhands.util.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


// fully qualified to avoid the clash between the Service entity and the annotation
@org.springframework.stereotype.Service
@Transactional(rollbackFor = Exception.class)
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(final ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> search(final String search, final String category, final String area) {
        return serviceRepository.search(blankToNull(search), blankToNull(category), blankToNull(area),
                        PageRequest.of(0, 100))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> findMyServices(final User provider) {
        return serviceRepository.findAllByProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceDTO get(final UUID id) {
        return serviceRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("service not found"));
    }

    public ServiceDTO create(final User provider, final ServiceDTO dto) {
        requireProvider(provider);
        final Service service = new Service();
        applyFields(service, dto);
        final OffsetDateTime now = OffsetDateTime.now();
        service.setProvider(provider);
        service.setCreatedAt(now);
        service.setUpdatedAt(now);
        return toDTO(serviceRepository.save(service));
    }

    public ServiceDTO update(final User currentUser, final UUID id, final ServiceDTO dto) {
        final Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("service not found"));
        if (!service.getProvider().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "you do not own this service");
        }
        applyFields(service, dto);
        service.setUpdatedAt(OffsetDateTime.now());
        return toDTO(serviceRepository.save(service));
    }

    public void delete(final User currentUser, final UUID id) {
        final Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("service not found"));
        if (!service.getProvider().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "you do not own this service");
        }
        serviceRepository.delete(service);
    }

    private void applyFields(final Service service, final ServiceDTO dto) {
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setCategory(dto.getCategory());
        service.setPricePerHour(dto.getPricePerHour());
        service.setArea(dto.getArea());
        service.setAvailableFrom(dto.getAvailableFrom());
        service.setAvailableTo(dto.getAvailableTo());
    }

    private void requireProvider(final User user) {
        if (!"PROVIDER".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "only providers can manage services");
        }
    }

    private ServiceDTO toDTO(final Service service) {
        final ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setCategory(service.getCategory());
        dto.setPricePerHour(service.getPricePerHour());
        dto.setArea(service.getArea());
        dto.setAvailableFrom(service.getAvailableFrom());
        dto.setAvailableTo(service.getAvailableTo());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        dto.setProviderId(service.getProvider().getId());
        dto.setProviderName(service.getProvider().getName());
        return dto;
    }

    private String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
