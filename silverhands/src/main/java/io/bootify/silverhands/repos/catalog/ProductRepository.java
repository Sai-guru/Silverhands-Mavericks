package io.bootify.silverhands.repos.catalog;

import io.bootify.silverhands.domain.catalog.Product;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByProviderIdOrderByCreatedAtDesc(UUID providerId);

    @Query("""
            select product from Product product
            where (cast(:search as string) is null or lower(product.name) like lower(concat('%', cast(:search as string), '%'))
                   or lower(coalesce(product.description, '')) like lower(concat('%', cast(:search as string), '%'))
                   or lower(coalesce(product.category, '')) like lower(concat('%', cast(:search as string), '%')))
              and (cast(:category as string) is null or lower(product.category) like lower(concat('%', cast(:category as string), '%')))
              and (cast(:area as string) is null or lower(product.area) like lower(concat('%', cast(:area as string), '%')))
            """)
    List<Product> search(@Param("search") String search, @Param("category") String category,
            @Param("area") String area, Pageable pageable);

}
