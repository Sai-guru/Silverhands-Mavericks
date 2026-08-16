package io.bootify.silverhands.rest.catalog;

import io.bootify.silverhands.model.dto.ServiceDTO;
import io.bootify.silverhands.service.catalog.ServiceService;
import io.bootify.silverhands.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/services", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServiceResource {

    private final ServiceService serviceService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> searchServices(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final String category,
            @RequestParam(required = false) final String area,
            @RequestParam(required = false) final Boolean mine,
            final OAuth2AuthenticationToken token) {
        if (Boolean.TRUE.equals(mine)) {
            return ResponseEntity.ok(serviceService.findMyServices(userService.getCurrentUser(token)));
        }
        return ResponseEntity.ok(serviceService.search(search, category, area));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getService(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(serviceService.get(id));
    }

    @PostMapping
    public ResponseEntity<ServiceDTO> createService(
            @RequestBody @Valid final ServiceDTO serviceDTO,
            final OAuth2AuthenticationToken token) {
        final ServiceDTO created = serviceService.create(userService.getCurrentUser(token),
                serviceDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> updateService(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final ServiceDTO serviceDTO,
            final OAuth2AuthenticationToken token) {
        return ResponseEntity.ok(serviceService.update(userService.getCurrentUser(token), id,
                serviceDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable(name = "id") final UUID id,
            final OAuth2AuthenticationToken token) {
        serviceService.delete(userService.getCurrentUser(token), id);
        return ResponseEntity.noContent().build();
    }

}
