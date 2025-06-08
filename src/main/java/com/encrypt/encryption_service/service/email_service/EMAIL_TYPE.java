package com.encrypt.encryption_service.service.email_service;

import lombok.Getter;

@Getter
public enum EMAIL_TYPE {
    FILE_SENDING("Encypted File Shared!", "file.html");

    private final String subject;
    private final String templateName;

    EMAIL_TYPE(String subject, String templateName) {
        this.subject = subject;
        this.templateName = templateName;
    }
}
