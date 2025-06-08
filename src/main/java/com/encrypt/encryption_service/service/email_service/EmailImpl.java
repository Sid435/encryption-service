package com.encrypt.encryption_service.service.email_service;

import com.encrypt.encryption_service.config.AppConfig;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailImpl implements EmailDao{
    private final AppConfig config;
    private final JavaMailSender mailSender;
    private final EmailTemplate emailTemplate;

    public void sendFileEmail(String fileName, byte[] file, String toEmail){
        EmailContent emailContent = emailTemplate.getFileEmailTemplate(Map.of("encryptedFileName", fileName));
        sendEmail(toEmail, emailContent, fileName, file);
    }


    @Override
    public void sendEmail(String toEmail, EmailContent emailContent, String attachmentName, byte[] attachment) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(config.getEmailSender());
            helper.setTo(toEmail);
            helper.setSubject(emailContent.subject());
            helper.setText(emailContent.body(), true);

            helper.addAttachment(attachmentName.endsWith(".pdf") ? attachmentName : attachmentName + ".pdf",
                    new ByteArrayResource(attachment),
                    "application/pdf");

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
