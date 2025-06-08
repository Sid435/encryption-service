package com.encrypt.encryption_service.service.email_service;

import java.util.Map;

public interface EmailTemplate {
    EmailContent getFileEmailTemplate(Map<String, Object> templateModel);
}
