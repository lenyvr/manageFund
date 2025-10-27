package com.fund.manageFunds.infraestructure.dto;

import com.fund.manageFunds.domain.model.NotificationOptions;
import com.fund.manageFunds.domain.model.Subscription;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubscriptionDtoRequest {

    private String clientEmail;
    private String fundName;
    private Double investAmount;
    private NotificationOptions notificationOption;
    private String informationForNotification;
    private static Double INITIAL_AMOUNT = Double.valueOf("500000");

    public Subscription toSubscriptionEntity() {
        Subscription subscription = new Subscription();
        subscription.setInitialAmount(INITIAL_AMOUNT);
        subscription.setInvestAmount(this.investAmount);
        subscription.setFundName(this.fundName);
        subscription.setClientEmail(this.clientEmail);
        subscription.setNotificationOption(this.notificationOption);
        subscription.setInformationForNotification(this.informationForNotification);
        return subscription;
    }
}
