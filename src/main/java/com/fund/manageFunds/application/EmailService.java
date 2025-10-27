package com.fund.manageFunds.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailService {

    private final SesClient sesClient;
    private final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    public void sendEmail(String clientEmail, String subject, String htmlBody, String textBody) {
        if(!isValid(clientEmail)){
            log.warn("Email can't be send because the client email is not valid");
            return;
        }
        try {
            String sender = "lenicet.villadiego@gmail.com";
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(sender)
                    .destination(Destination.builder().toAddresses(clientEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .text(Content.builder().data(textBody).build())
                                    .html(Content.builder().data(htmlBody).build())
                                    .build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);

            log.info("Email sent. Message ID: {}", response.messageId());
        } catch (SesException e) {
            log.error("It was an error trying to send email to the mail {}, error: {}",
                    clientEmail, e.awsErrorDetails().errorMessage());
        }
    }

    private boolean isValid(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = PATTERN.matcher(email);
        return matcher.matches();
    }
}