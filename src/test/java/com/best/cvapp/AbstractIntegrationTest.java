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
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;
import java.util.Properties;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(AbstractIntegrationTest.NoOpMailTestConfig.class)
public abstract class AbstractIntegrationTest{

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cvapp_test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

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