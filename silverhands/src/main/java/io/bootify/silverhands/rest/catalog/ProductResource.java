package io.bootify.silverhands.rest.catalog;

import io.bootify.silverhands.model.dto.ProductDTO;
import io.bootify.silverhands.service.catalog.ProductService;
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
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductResource {

    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final String category,
            @RequestParam(required = false) final String area,
            @RequestParam(required = false) final Boolean mine,
            final OAuth2AuthenticationToken token) {
        if (Boolean.TRUE.equals(mine)) {
            return ResponseEntity.ok(productService.findMyProducts(userService.getCurrentUser(token)));
        }
        return ResponseEntity.ok(productService.search(search, category, area));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(productService.get(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @RequestBody @Valid final ProductDTO productDTO,
            final OAuth2AuthenticationToken token) {
        final ProductDTO created = productService.create(userService.getCurrentUser(token),
                productDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final ProductDTO productDTO,
            final OAuth2AuthenticationToken token) {
        return ResponseEntity.ok(productService.update(userService.getCurrentUser(token), id,
                productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable(name = "id") final UUID id,
            final OAuth2AuthenticationToken token) {
        productService.delete(userService.getCurrentUser(token), id);
        return ResponseEntity.noContent().build();
    }

}
