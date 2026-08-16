package io.bootify.silverhands.rest.user;

import io.bootify.silverhands.model.dto.UserDTO;
import io.bootify.silverhands.service.user.UserService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserResource {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(userService.get(id));
    }

}
