package com.best.cvapp;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AbstractIntegrationTest.NoOpMailTestConfig.class)
public abstract class AbstractIntegrationTest{

    protected String storeKnownVerificationToken(JdbcTemplate jdbcTemplate, String email) {
        String rawToken = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                UPDATE email_verification_tokens t
                SET token = ?
                FROM users u
                WHERE t.user_id = u.id AND u.email = ?
                """, DigestUtils.sha256Hex(rawToken), email);
        return rawToken;
    }

    protected String storeKnownPasswordResetToken(JdbcTemplate jdbcTemplate, String email) {
        String rawToken = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                UPDATE password_reset_tokens
                SET token = ?
                WHERE email = ?
                """, DigestUtils.sha256Hex(rawToken), email);
        return rawToken;
    }

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cvapp_test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        postgres.start();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class NoOpMailTestConfig {

        @Bean
        @Primary
        JavaMailSender javaMailSender() {
            return new JavaMailSender() {

                @Override
                public MimeMessage createMimeMessage() {
                    return new MimeMessage(Session.getInstance(new Properties()));
                }

                @Override
                public MimeMessage createMimeMessage(InputStream contentStream) {
                    return createMimeMessage();
                }

                @Override
                public void send(MimeMessage mimeMessage) {
                    // no-op for tests
                }

                @Override
                public void send(MimeMessage... mimeMessages) {
                    // no-op for tests
                }

                @Override
                public void send(MimeMessagePreparator mimeMessagePreparator) {
                    // no-op for tests
                }

                @Override
                public void send(MimeMessagePreparator... mimeMessagePreparators) {
                    // no-op for tests
                }

                @Override
                public void send(SimpleMailMessage simpleMessage) {
                    // no-op for tests
                }

                @Override
                public void send(SimpleMailMessage... simpleMessages) {
                    // no-op for tests
                }
            };
        }
    }
}
