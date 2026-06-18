package com.best.cvapp.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendCompanyInvite(String toEmail, String token) {
        String link = frontendUrl + "/accept-invite?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("You're invited to join CVApp as a Company");
        message.setText("Click the link below to set up your account:\n\n" + link +
                "\n\nThis link expires in 48 hours.");
        mailSender.send(message);
    }
}