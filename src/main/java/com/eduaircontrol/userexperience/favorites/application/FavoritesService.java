package com.eduaircontrol.userexperience.favorites.application;

import com.eduaircontrol.userexperience.favorites.domain.model.Favorite;
import com.eduaircontrol.userexperience.favorites.infrastructure.persistence.FavoriteJpaRepository;
import com.eduaircontrol.userexperience.shared.contract.EnvironmentInstitutionPort;
import com.eduaircontrol.userexperience.shared.exception.ForbiddenException;
import com.eduaircontrol.userexperience.shared.tenant.TenantContext;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Favoritos del usuario: ambientes y/o variables.
 *
 * <p>Al marcar un favorito se valida que el ambiente pertenezca a la institución
 * del token (ADR-016) usando la réplica local alimentada por los eventos de
 * classroom. Sin institución en el token (contexto de sistema) no se valida.
 */
@Service
@RequiredArgsConstructor
public class FavoritesService {

    private final FavoriteJpaRepository favoriteRepository;
    private final EnvironmentInstitutionPort environmentInstitutionPort;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Set<UUID> favoriteIds(UUID userId) {
        if (userId == null) {
            return Set.of();
        }
        return favoriteRepository.findByUserId(userId).stream()
                .map(Favorite::getClassroomId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Transactional
    public boolean toggle(UUID userId, UUID environmentId) {
        if (userId == null || environmentId == null) {
            throw new IllegalArgumentException("userId y environmentId son obligatorios");
        }
        boolean favorite = !isFavorite(userId, environmentId);
        setFavorite(userId, environmentId, favorite);
        return favorite;
    }

    @Transactional
    public void setFavorite(UUID userId, UUID environmentId, boolean favorite) {
        if (userId == null || environmentId == null) {
            return;
        }
        if (favorite) {
            requireEnvironmentInTenant(environmentId);
            favoriteRepository.findByUserIdAndClassroomId(userId, environmentId)
                    .orElseGet(() -> favoriteRepository.save(Favorite.builder()
                            .id(UUID.randomUUID())
                            .userId(userId)
                            .classroomId(environmentId)
                            .addedAt(clock.instant())
                            .build()));
            return;
        }
        favoriteRepository.deleteByUserIdAndClassroomId(userId, environmentId);
    }

    /**
     * Un ambiente de otra institución no se puede marcar como favorito (ADR-016).
     */
    private void requireEnvironmentInTenant(UUID environmentId) {
        UUID institutionId = TenantContext.institutionId();
        if (institutionId == null) {
            return; // contexto de sistema (sin institución en el token)
        }
        UUID owner = environmentInstitutionPort.institutionOf(environmentId).orElse(null);
        if (owner == null || !owner.equals(institutionId)) {
            throw new ForbiddenException("El ambiente no pertenece a tu institución");
        }
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(UUID userId, UUID environmentId) {
        return userId != null && environmentId != null
                && favoriteRepository.findByUserIdAndClassroomId(userId, environmentId).isPresent();
    }
}
