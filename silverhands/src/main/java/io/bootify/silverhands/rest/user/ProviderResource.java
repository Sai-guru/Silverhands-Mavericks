package io.bootify.silverhands.rest.user;

import io.bootify.silverhands.model.dto.UserDTO;
import io.bootify.silverhands.service.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/providers", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProviderResource {

    private final UserService userService;

    public ProviderResource(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> searchProviders(
            @RequestParam(required = false) final String name) {
        return ResponseEntity.ok(userService.findUsersByRole("PROVIDER", name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getProvider(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(userService.get(id));
    }

}
