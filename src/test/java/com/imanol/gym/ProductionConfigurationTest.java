package com.imanol.gym;

import com.imanol.gym.config.CorsConfig;
import com.imanol.gym.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

class ProductionConfigurationTest {

    private static final String VALID_SECRET =
            "Z3ltLW1hbmFnZW1lbnQtYXBpLXRlc3Qtc2VjcmV0LWtleS1mb3ItdGVzdHM=";

    @Test
    void jwtConfigurationRejectsMissingAndInvalidSecrets() {
        IllegalStateException missing = assertThrows(
                IllegalStateException.class,
                () -> new JwtService("", 3600000)
        );
        assertTrue(missing.getMessage().contains("must not be blank"));

        IllegalStateException invalid = assertThrows(
                IllegalStateException.class,
                () -> new JwtService("not-base64", 3600000)
        );
        assertTrue(invalid.getMessage().contains("valid Base64"));

        assertDoesNotThrow(() -> new JwtService(VALID_SECRET, 3600000));
    }

    @Test
    void productionPropertiesRequireSecretAndDisableSqlLogging() throws Exception {
        Properties production = loadProperties("application-prod.properties");
        Properties defaults = loadProperties("application.properties");

        assertEquals("${JWT_SECRET}", production.getProperty("app.jwt.secret"));
        assertEquals("false", production.getProperty("spring.jpa.show-sql"));
        assertFalse(defaults.getProperty("app.jwt.secret", "").contains(":"));
    }

    @Test
    void corsAllowsOnlyConfiguredOriginsWithoutCredentials() {
        CorsConfig config = new CorsConfig("http://localhost:3000");
        var source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();

        var allowed = source.getCorsConfiguration(request);
        assertNotNull(allowed);
        assertEquals("http://localhost:3000", allowed.checkOrigin("http://localhost:3000"));
        assertNull(allowed.checkOrigin("https://untrusted.example"));
        assertFalse(allowed.getAllowCredentials());
        assertFalse(allowed.getAllowedOrigins().contains("*"));

        CorsConfig emptyConfig = new CorsConfig("");
        assertNull(emptyConfig.corsConfigurationSource()
                .getCorsConfiguration(request)
                .checkOrigin("http://localhost:3000"));
    }

    @Test
    void productionJarContainsOnlyProductionMigrations() throws Exception {
        Path jarPath = Path.of("target/gym-management-api-1.0.0.jar");
        boolean packagedArtifactIsCurrent = Files.exists(jarPath)
                && Files.getLastModifiedTime(jarPath).toMillis()
                >= Files.getLastModifiedTime(Path.of("pom.xml")).toMillis();
        if (packagedArtifactIsCurrent) {
            try (JarFile jar = new JarFile(jarPath.toFile())) {
                for (int version = 1; version <= 8; version++) {
                    int migrationVersion = version;
                    assertTrue(jar.stream().anyMatch(entry ->
                            entry.getName().matches(
                                    "BOOT-INF/classes/db/migration/V"
                                            + migrationVersion + "__.*\\.sql")));
                }
                assertFalse(jar.stream().anyMatch(entry ->
                        entry.getName().contains("V900__seed_local_nutrition.sql")
                                || entry.getName().contains("V901__seed_local_workout_plan.sql")
                                || entry.getName().startsWith(
                                "BOOT-INF/classes/db/migration/dev/")));
            }
        } else {
            for (int version = 1; version <= 8; version++) {
                assertTrue(Files.exists(Path.of(
                        "target/classes/db/migration/V" + version
                                + "__create_" + migrationName(version) + ".sql"
                )));
            }
        }
        assertFalse(Files.exists(Path.of(
                "target/classes/db/migration/V900__seed_local_nutrition.sql")));
        assertFalse(Files.exists(Path.of(
                "target/classes/db/migration/V901__seed_local_workout_plan.sql")));
    }

    private String migrationName(int version) {
        return switch (version) {
            case 1 -> "exercise_categories";
            case 2 -> "exercises";
            case 3 -> "workout_templates";
            case 4 -> "workout_template_exercises";
            case 5 -> "users";
            case 6 -> "trainer_clients";
            case 7 -> "workout_plans";
            case 8 -> "nutrition";
            default -> throw new IllegalArgumentException("Unexpected migration");
        };
    }

    private Properties loadProperties(String name) throws Exception {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream(name)) {
            assertNotNull(input);
            properties.load(input);
        }
        return properties;
    }
}
