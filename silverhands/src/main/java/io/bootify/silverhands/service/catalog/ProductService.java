package io.bootify.silverhands.service.catalog;

import io.bootify.silverhands.domain.catalog.Product;
import io.bootify.silverhands.domain.user.User;
import io.bootify.silverhands.model.dto.ProductDTO;
import io.bootify.silverhands.repos.catalog.ProductRepository;
import io.bootify.silverhands.util.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@Transactional(rollbackFor = Exception.class)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> search(final String search, final String category, final String area) {
        return productRepository.search(blankToNull(search), blankToNull(category), blankToNull(area),
                        PageRequest.of(0, 100))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> findMyProducts(final User provider) {
        return productRepository.findAllByProviderIdOrderByCreatedAtDesc(provider.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO get(final UUID id) {
        return productRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("product not found"));
    }

    public ProductDTO create(final User provider, final ProductDTO dto) {
        requireProvider(provider);
        final Product product = new Product();
        applyFields(product, dto);
        final OffsetDateTime now = OffsetDateTime.now();
        product.setProvider(provider);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return toDTO(productRepository.save(product));
    }

    public ProductDTO update(final User currentUser, final UUID id, final ProductDTO dto) {
        final Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("product not found"));
        if (!product.getProvider().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "you do not own this product");
        }
        applyFields(product, dto);
        product.setUpdatedAt(OffsetDateTime.now());
        return toDTO(productRepository.save(product));
    }

    public void delete(final User currentUser, final UUID id) {
        final Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("product not found"));
        if (!product.getProvider().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "you do not own this product");
        }
        productRepository.delete(product);
    }

    private void applyFields(final Product product, final ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setArea(dto.getArea());
        product.setImageUrl(dto.getImageUrl());
    }

    private void requireProvider(final User user) {
        if (!"PROVIDER".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "only providers can manage products");
        }
    }

    private ProductDTO toDTO(final Product product) {
        final ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategory(product.getCategory());
        dto.setArea(product.getArea());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setProviderId(product.getProvider().getId());
        dto.setProviderName(product.getProvider().getName());
        return dto;
    }

    private String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
