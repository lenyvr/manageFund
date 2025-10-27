package com.fund.manageFunds.application;

import com.fund.manageFunds.domain.exceptions.IsAlreadySubscribed;
import com.fund.manageFunds.domain.exceptions.NoAvailableAmount;
import com.fund.manageFunds.domain.exceptions.NoFundFound;
import com.fund.manageFunds.domain.model.Fund;
import com.fund.manageFunds.domain.model.NotificationOptions;
import com.fund.manageFunds.domain.model.Subscription;
import com.fund.manageFunds.domain.model.SubscriptionState;
import com.fund.manageFunds.domain.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionService {

    private static final Double INITIAL_AMOUNT =  Double.valueOf("500000");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final SmsService smsService;
    private final EmailService emailService;
    private final SubscriptionRepository subscriptionRepository;
    private final FundService fundService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, FundService fundService,
                               SmsService smsService, EmailService emailService) {
        this.subscriptionRepository = subscriptionRepository;
        this.fundService = fundService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    public List<Subscription> getByClientFiltered(String clientEmail, String fundName, String state){
        List<Subscription> subscriptionList = new ArrayList<>();
        if((fundName==null || fundName.isBlank() ) && (state==null || state.isBlank())){
            subscriptionList = subscriptionRepository.getByClient(clientEmail);
        } else {
            subscriptionList = subscriptionRepository.getSubscriptionsByClientFiltered(clientEmail, fundName, state);
        }

        if(subscriptionList==null || subscriptionList.isEmpty()){
            log.warn("There aren't any transactions for this client");
        }

        return subscriptionList;
    }

    public Subscription subscribeToAFund(Subscription subscription) {
        Fund fund = verifyFundExistence(subscription);
        List<Subscription> orderedSubscriptionList = orderSubscriptionByDate(
                subscriptionRepository.getSubscriptionsByClientFiltered(
                        subscription.getClientEmail(),null,
                        SubscriptionState.OPENED.name())
                );
        if(isAnyOpenInscription(subscription)){
            throw new IsAlreadySubscribed(subscription.getClientEmail(), subscription.getFundName());
        }
        checkAvailableAmount(subscription, fund, orderedSubscriptionList);

        subscription.setState(SubscriptionState.OPENED);
        subscription.setDate(getActualDateAsString());
        subscription.setResidualAMount(getAmountAvailable(orderedSubscriptionList) - subscription.getInvestAmount());
        subscription.setId(UUID.randomUUID().toString());
        Subscription subscriptionSaved = subscriptionRepository.save(subscription);
        sendNotification(subscriptionSaved);
        return subscriptionSaved;
    }

    public Subscription cancelFundSubscription(Subscription subscription){
        if(!isAnyOpenInscription(subscription)){
           String message =  String
                   .format("There isn't any opened subscription from %s into fund %s to cancel.",
                           subscription.getClientEmail(), subscription.getFundName());
            throw new IsAlreadySubscribed(message);
        }

        List<Subscription> orderedSubscriptionList =
                orderSubscriptionByDate(
                        subscriptionRepository.getSubscriptionsByClientFiltered(subscription.getClientEmail(),null,
                                SubscriptionState.OPENED.name())
                );

        subscription.setState(SubscriptionState.CANCELED);
        subscription.setDate(getActualDateAsString());
        subscription.setResidualAMount(getAmountAvailable(orderedSubscriptionList) + subscription.getInvestAmount());
        subscription.setId(UUID.randomUUID().toString());
        subscription.setInvestAmount(Double.valueOf("0.0"));
        return subscriptionRepository.save(subscription);
    }

    private void sendNotification(Subscription subscription) {
        String notificationMessage = String.format("You have been successfully subscribed to the fund %s",
                subscription.getFundName());
        if(NotificationOptions.SMS == subscription.getNotificationOption()){
            smsService.sendSMS(subscription.getInformationForNotification(), notificationMessage);
        } else if (NotificationOptions.EMAIL == subscription.getNotificationOption()) {
            String htmlBody = "<h1>Subscription confirmation</h1><p><b>" + notificationMessage + "</b></p>";
            String email = (subscription.getInformationForNotification()==null
                    || subscription.getInformationForNotification().isBlank()) ?
                    subscription.getClientEmail():subscription.getInformationForNotification();
            String subject = "Subscription confirmation";

            emailService.sendEmail(
                    email,
                    subject,
                    htmlBody,
                    notificationMessage
            );
        }
    }

    private String getActualDateAsString(){
        LocalDateTime currentDate = LocalDateTime.now();
        return currentDate.format(FORMATTER);
    }

    private boolean isAnyOpenInscription(Subscription subscription)  {
       List<Subscription> subscriptionsByClientAndFund = subscriptionRepository.getSubscriptionsByClientFiltered(subscription.getClientEmail(),
               subscription.getFundName(), null);
       List<Subscription> orderedSubscriptionsByClientAndFund = orderSubscriptionByDate(subscriptionsByClientAndFund);

        return Optional.of(orderedSubscriptionsByClientAndFund)
                .flatMap(subscriptionsList -> subscriptionsList.stream()
                        .reduce((beforeSubs, afterSubs) -> afterSubs))// last element
                .map(subscript -> SubscriptionState.OPENED == subscript.getState())
                .orElse(false);
    }

    private void checkAvailableAmount(Subscription subscription, Fund fund, List<Subscription> orderedSubscriptionList) {
        if (subscription.getInvestAmount().compareTo(fund.getMinAmount()) < 0){
            throw new NoAvailableAmount("The amount invested is less than the minimum amount required in the fund");
        }

        if(getAmountAvailable(orderedSubscriptionList).compareTo(subscription.getInvestAmount())<0){
            throw new NoAvailableAmount(subscription.getClientEmail(), fund.getName());
        }
    }


    private Double getAmountAvailable(List<Subscription> orderedSubscriptionList){
         return Optional.of(orderedSubscriptionList)
                .flatMap(subscriptionsList -> subscriptionsList.stream()
                        .reduce((beforeSubs, afterSubs) -> afterSubs))// last element
                .map(Subscription::getResidualAMount)
                .orElse(INITIAL_AMOUNT);
    }

    private static List<Subscription> orderSubscriptionByDate(List<Subscription> subscriptions) {
        List<Subscription> orderedList = new ArrayList<>();
        if(subscriptions!=null || !subscriptions.isEmpty()){
            orderedList = subscriptions.stream()
                    .sorted(Comparator.comparing(subs -> LocalDate.parse(subs.getDate(), FORMATTER)))
                    .collect(Collectors.toList());
        }
        return orderedList;
    }

    private Fund verifyFundExistence(Subscription subscription){
        Fund fund= fundService.getFund(subscription.getFundName());
        if(fund == null){
            throw new NoFundFound(subscription.getClientEmail(), subscription.getFundName());
        }

        return  fund;
    }

}
