package io.bootify.silverhands.rest.user;

import io.bootify.silverhands.model.dto.UserDTO;
import io.bootify.silverhands.service.user.UserService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


// customer discovery is only available to providers
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerResource {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> searchCustomers(
            @RequestParam(required = false) final String name) {
        return ResponseEntity.ok(userService.findUsersByRole("CUSTOMER", name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getCustomer(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(userService.get(id));
    }

}
