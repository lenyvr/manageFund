package com.fund.manageFunds.application;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.Map;

@Service
@Slf4j
public class SmsService {

    private final SnsClient snsClient;
    private final String PATRON_E164 = "^\\+\\d{1,15}$";

    public SmsService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void sendSMS(String phoneNumber, String message) {
        if(!verifyPhoneNumber(phoneNumber)){
            return;
        }

        String smsType = "AWS.SNS.SMS.SMSType";
        String smsTypeValue = "Transactional";
        String dataType = "String";
        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .messageAttributes(Map.of(
                            smsType,
                            software.amazon.awssdk.services.sns.model.MessageAttributeValue.builder()
                                    .stringValue(smsTypeValue)
                                    .dataType(dataType)
                                    .build()
                    ))
                    .build();


            PublishResponse result = snsClient.publish(request);

            log.info("successful sending SMS, Message ID: {}", result.messageId());
        } catch (SnsException e) {
            log.info("Error sending SMS, error: {}", e.awsErrorDetails().errorMessage());
        }
    }

    private boolean verifyPhoneNumber(String phoneNumber) {
        boolean phoneNumberMatches;
        String phoneNumberNotValidMessage = "Isn't possible sent SMS because phoneNumber is not valid";
        if (phoneNumber == null || !phoneNumber.startsWith("+")) {
            phoneNumberMatches = false;
        }
        // 1. Quitar todos los caracteres no numéricos ni '+'
        String phoneNumberCleaned = phoneNumber.replaceAll("[\\s\\-().]", "");

        // 2. Aplicar el patrón estricto al string limpio
        phoneNumberMatches = phoneNumberCleaned.matches(PATRON_E164);

        if(!phoneNumberMatches){
            log.warn(phoneNumberNotValidMessage);
        }

        return  phoneNumberMatches;
    }


}
