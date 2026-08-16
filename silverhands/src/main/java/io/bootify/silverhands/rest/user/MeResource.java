package io.bootify.silverhands.rest.user;

import io.bootify.silverhands.model.dto.CurrentUserDTO;
import io.bootify.silverhands.model.dto.RoleSelectionDTO;
import io.bootify.silverhands.model.dto.UserDTO;
import io.bootify.silverhands.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/me", produces = MediaType.APPLICATION_JSON_VALUE)
public class MeResource {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<CurrentUserDTO> getCurrentUser(
            final OAuth2AuthenticationToken authenticationToken) {
        return ResponseEntity.ok(userService.getCurrentUserDetails(authenticationToken));
    }

    @PostMapping("/role")
    public ResponseEntity<CurrentUserDTO> chooseRole(
            final OAuth2AuthenticationToken authenticationToken,
            @RequestBody @Valid final RoleSelectionDTO roleSelectionDTO) {
        try {
            return ResponseEntity.ok(userService.chooseRole(authenticationToken,
                    roleSelectionDTO.getRole()));
        } catch (final IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (final IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    @PutMapping
    public ResponseEntity<UserDTO> updateProfile(
            final OAuth2AuthenticationToken authenticationToken,
            @RequestBody final UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateProfile(authenticationToken, userDTO));
    }

}
