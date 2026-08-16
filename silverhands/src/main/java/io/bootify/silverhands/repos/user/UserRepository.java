package io.bootify.silverhands.repos.user;

import io.bootify.silverhands.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByEmail(String email);

    @Query("""
            select user from User user
            where user.role = :role
              and (cast(:name as string) is null or lower(user.name) like lower(concat('%', cast(:name as string), '%')))
            order by user.name
            """)
    List<User> findByRole(@Param("role") String role, @Param("name") String name);

}
