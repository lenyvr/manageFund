package com.fund.manageFunds.infraestructure.dto;

import com.fund.manageFunds.domain.model.NotificationOptions;
import com.fund.manageFunds.domain.model.Subscription;
import com.fund.manageFunds.domain.model.SubscriptionState;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubscriptionDtoResponse {

    private String clientEmail;
    private String fundName;
    private SubscriptionState state;
    private Double initialAmount;
    private Double investAmount;
    private Double residualAMount;
    private String date;
    private NotificationOptions notificationOption;
    private String informationForNotification;

    public static SubscriptionDtoResponse fromSubscriptionEntity(Subscription subscription) {
        return new SubscriptionDtoResponse(subscription.getClientEmail(),
                subscription.getFundName(), subscription.getState(), subscription.getInitialAmount(),
                subscription.getInvestAmount(), subscription.getResidualAMount(), subscription.getDate(),
                subscription.getNotificationOption(), subscription.getInformationForNotification());
    }
}
