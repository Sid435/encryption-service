package com.encrypt.encryption_service.service.email_service;

public interface EmailDao {
    void sendEmail(String to, EmailContent content, String attachmentName, byte[] attachment);
}
