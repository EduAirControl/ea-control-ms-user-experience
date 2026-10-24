package com.eduaircontrol.userexperience;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Aislamiento por institucion (ADR-016): un usuario solo puede marcar como
 * favorito o valorar ambientes de su institucion. La institucion del ambiente sale
 * de la replica local alimentada por los eventos de classroom.
 */
class InstitutionIsolationTest extends PostgresTestBase {

    private static final UUID INSTITUTION_A = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID INSTITUTION_B = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID ENV_A = UUID.fromString("aaaaaaaa-0000-4000-8000-0000000000a1");
    private static final UUID ENV_B = UUID.fromString("bbbbbbbb-0000-4000-8000-0000000000b1");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    private MockMvc mockMvc;
    private UUID userId;
    private String tokenInA;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        userId = UUID.randomUUID();
        tokenInA = com.eduaircontrol.userexperience.shared.security.TestTokenMint.mint(
                userId, "iso-" + userId.toString().substring(0, 8) + "@test.com", List.of("USER"),
                INSTITUTION_A, null);

        jdbc.update("DELETE FROM user_experience.favorites WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM user_experience.classroom_ratings WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM user_experience.educational_environment "
                + "WHERE educational_environment_id IN (?, ?)", ENV_A, ENV_B);

        jdbc.update("""
                insert into user_experience.educational_environment
                    (educational_environment_id, code, name, institution_id)
                values (?, 'ISO-A', 'Ambiente A', ?)
                """, ENV_A, INSTITUTION_A);
        jdbc.update("""
                insert into user_experience.educational_environment
                    (educational_environment_id, code, name, institution_id)
                values (?, 'ISO-B', 'Ambiente B', ?)
                """, ENV_B, INSTITUTION_B);
    }

    @Test
    void canFavoriteAnEnvironmentOfOwnInstitution() throws Exception {
        mockMvc.perform(post("/api/v1/favorites/{id}/toggle", ENV_A)
                        .header("Authorization", "Bearer " + tokenInA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));
    }

    @Test
    void cannotFavoriteAnEnvironmentOfAnotherInstitution() throws Exception {
        mockMvc.perform(post("/api/v1/favorites/{id}/toggle", ENV_B)
                        .header("Authorization", "Bearer " + tokenInA))
                .andExpect(status().isForbidden());
    }

    @Test
    void canRateAnEnvironmentOfOwnInstitution() throws Exception {
        mockMvc.perform(post("/api/v1/classrooms/{id}/ratings", ENV_A)
                        .header("Authorization", "Bearer " + tokenInA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5));
    }

    @Test
    void cannotRateAnEnvironmentOfAnotherInstitution() throws Exception {
        mockMvc.perform(post("/api/v1/classrooms/{id}/ratings", ENV_B)
                        .header("Authorization", "Bearer " + tokenInA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5}"))
                .andExpect(status().isForbidden());
    }
}
