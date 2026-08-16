package io.bootify.silverhands.service.user;

import io.bootify.silverhands.domain.user.User;
import io.bootify.silverhands.model.dto.CurrentUserDTO;
import io.bootify.silverhands.model.dto.UserDTO;
import io.bootify.silverhands.repos.user.UserRepository;
import io.bootify.silverhands.util.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    public static final String ROLE_PENDING = "ROLE_PENDING";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_PROVIDER = "PROVIDER";

    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User ensureFromOAuth(final OAuth2AuthenticationToken token) {
        final String googleId = token.getPrincipal().getAttribute("sub");
        if (googleId == null || googleId.isBlank()) {
            throw new IllegalStateException("googleId is missing from OAuth token");
        }
        final String email = token.getPrincipal().getAttribute("email");
        final String name = token.getPrincipal().getAttribute("name");
        final String picture = token.getPrincipal().getAttribute("picture");
        final OffsetDateTime now = OffsetDateTime.now();

        final Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleId);
        final Optional<User> existingByEmail = email == null || email.isBlank()
                ? Optional.empty()
                : userRepository.findByEmail(email);
        final User user = existingByGoogleId.or(() -> existingByEmail).orElseGet(User::new);

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        user.setUpdatedAt(now);
        user.setLastLoginAt(now);
        user.setGoogleId(googleId);
        user.setEmail(email != null && !email.isBlank() ? email : googleId + "@oauth.local");
        user.setName(name != null && !name.isBlank() ? name : "SilverHands User");
        user.setProfileImageUrl(picture);
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(ROLE_PENDING);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User getCurrentUser(final OAuth2AuthenticationToken token) {
        return ensureFromOAuth(token);
    }

    @Transactional
    public CurrentUserDTO chooseRole(final OAuth2AuthenticationToken token, final String requestedRole) {
        final User user = ensureFromOAuth(token);
        final String normalizedRole = normalizeRole(requestedRole);
        if (!ROLE_PENDING.equals(user.getRole()) && !normalizedRole.equals(user.getRole())) {
            throw new IllegalStateException("Role is already set and cannot be changed.");
        }
        user.setRole(normalizedRole);
        user.setUpdatedAt(OffsetDateTime.now());
        return toCurrentUserDTO(userRepository.save(user));
    }

    @Transactional
    public CurrentUserDTO getCurrentUserDetails(final OAuth2AuthenticationToken token) {
        return toCurrentUserDTO(getCurrentUser(token));
    }

    @Transactional
    public UserDTO updateProfile(final OAuth2AuthenticationToken token, final UserDTO userDTO) {
        final User user = getCurrentUser(token);
        if (userDTO.getName() != null && !userDTO.getName().isBlank()) {
            user.setName(userDTO.getName());
        }
        user.setPhone(userDTO.getPhone());
        user.setProfileImageUrl(userDTO.getProfileImageUrl());
        user.setUpdatedAt(OffsetDateTime.now());
        return toDTO(userRepository.save(user));
    }

    public UserDTO get(final UUID id) {
        return userRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findUsersByRole(final String role, final String name) {
        return userRepository.findByRole(role,
                        name == null || name.isBlank() ? null : name.trim())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private CurrentUserDTO toCurrentUserDTO(final User user) {
        final CurrentUserDTO dto = new CurrentUserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setRoleSelectionRequired(ROLE_PENDING.equals(user.getRole()));
        return dto;
    }

    private UserDTO toDTO(final User user) {
        final UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private String normalizeRole(final String role) {
        if (role == null) {
            throw new IllegalArgumentException("role is required");
        }
        final String trimmedRole = role.trim().toUpperCase();
        if (ROLE_CUSTOMER.equals(trimmedRole) || ROLE_PROVIDER.equals(trimmedRole)) {
            return trimmedRole;
        }
        throw new IllegalArgumentException("role must be CUSTOMER or PROVIDER");
    }

}
