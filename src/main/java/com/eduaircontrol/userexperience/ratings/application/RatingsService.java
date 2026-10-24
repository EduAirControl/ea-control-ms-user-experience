package com.eduaircontrol.userexperience.ratings.application;

import com.eduaircontrol.userexperience.ratings.domain.model.ClassroomRating;
import com.eduaircontrol.userexperience.ratings.infrastructure.persistence.ClassroomRatingJpaRepository;
import com.eduaircontrol.userexperience.shared.contract.EnvironmentInstitutionPort;
import com.eduaircontrol.userexperience.shared.exception.ForbiddenException;
import com.eduaircontrol.userexperience.shared.tenant.TenantContext;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calificaciones de ambientes (escala 1-5).
 *
 * <p>Se valida que el ambiente pertenezca a la institución del token (ADR-016)
 * usando la réplica local de ambientes.
 */
@Service
@RequiredArgsConstructor
public class RatingsService {

    private final ClassroomRatingJpaRepository ratingRepository;
    private final EnvironmentInstitutionPort environmentInstitutionPort;
    private final Clock clock;

    @Transactional
    public ClassroomRating rate(UUID userId, UUID classroomId, int score, String comment) {
        requireUser(userId);
        if (classroomId == null) {
            throw new IllegalArgumentException("classroomId es obligatorio");
        }
        requireEnvironmentInTenant(classroomId);
        return ratingRepository.save(
                ClassroomRating.rate(userId, classroomId, score, comment, clock.instant()));
    }

    @Transactional(readOnly = true)
    public List<ClassroomRating> byUser(UUID userId) {
        requireUser(userId);
        return ratingRepository.findByUserIdOrderByRatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<ClassroomRating> byClassroom(UUID classroomId) {
        if (classroomId == null) {
            throw new IllegalArgumentException("classroomId es obligatorio");
        }
        return ratingRepository.findByClassroomIdOrderByRatedAtDesc(classroomId);
    }

    private void requireUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId es obligatorio");
        }
    }

    /** Un ambiente de otra institución no se puede valorar (ADR-016). */
    private void requireEnvironmentInTenant(UUID classroomId) {
        UUID institutionId = TenantContext.institutionId();
        if (institutionId == null) {
            return; // contexto de sistema (sin institución en el token)
        }
        UUID owner = environmentInstitutionPort.institutionOf(classroomId).orElse(null);
        if (owner == null || !owner.equals(institutionId)) {
            throw new ForbiddenException("El ambiente no pertenece a tu institución");
        }
    }
}
