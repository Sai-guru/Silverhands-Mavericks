package io.bootify.silverhands.repos.catalog;

import io.bootify.silverhands.domain.catalog.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ServiceRepository extends JpaRepository<Service, UUID> {

    List<Service> findAllByProviderIdOrderByCreatedAtDesc(UUID providerId);

    @Query("""
            select service from Service service
            where (cast(:search as string) is null or lower(service.name) like lower(concat('%', cast(:search as string), '%'))
                   or lower(coalesce(service.description, '')) like lower(concat('%', cast(:search as string), '%'))
                   or lower(coalesce(service.category, '')) like lower(concat('%', cast(:search as string), '%')))
              and (cast(:category as string) is null or lower(service.category) like lower(concat('%', cast(:category as string), '%')))
              and (cast(:area as string) is null or lower(service.area) like lower(concat('%', cast(:area as string), '%')))
            """)
    List<Service> search(@Param("search") String search, @Param("category") String category,
            @Param("area") String area, Pageable pageable);

}
