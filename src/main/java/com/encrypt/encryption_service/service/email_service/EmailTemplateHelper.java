package com.encrypt.encryption_service.service.email_service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailTemplateHelper implements EmailTemplate{

    private final SpringTemplateEngine templateEngine;

    @Override
    public EmailContent getFileEmailTemplate(Map<String, Object> templateModel) {
        EMAIL_TYPE template = EMAIL_TYPE.FILE_SENDING;
        String formattedHtml = getEmailContent(templateModel, template);
        String subject = template.getSubject();
        return new EmailContent(subject, formattedHtml);
    }

    private String getEmailContent(Map<String, Object> templateModel, EMAIL_TYPE emailType){
        Context thmeContext = new Context();
        thmeContext.setVariables(templateModel);
        return templateEngine.process(emailType.getTemplateName(), thmeContext);

    }
}
