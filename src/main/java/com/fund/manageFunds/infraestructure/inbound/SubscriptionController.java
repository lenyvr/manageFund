package com.fund.manageFunds.infraestructure.inbound;

import com.fund.manageFunds.application.SmsService;
import com.fund.manageFunds.application.SubscriptionService;
import com.fund.manageFunds.domain.model.Subscription;
import com.fund.manageFunds.infraestructure.dto.SubscriptionDtoRequest;
import com.fund.manageFunds.infraestructure.dto.SubscriptionDtoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/getByClientFiltered")
    public ResponseEntity<List<SubscriptionDtoResponse>> getAllSubscriptions(@RequestParam String clientEmail,
                                                                             @RequestParam(required = false) String state,
                                                                             @RequestParam(required = false) String fundName){
        return ResponseEntity
                .ok(subscriptionService.getByClientFiltered(clientEmail, fundName, state).stream().map(SubscriptionDtoResponse::fromSubscriptionEntity)
                .collect(Collectors.toList()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionDtoResponse> subscribe(@RequestBody SubscriptionDtoRequest subscriptionDtoRequest){
        Subscription subscription = subscriptionService.subscribeToAFund(subscriptionDtoRequest.toSubscriptionEntity());
        return ResponseEntity.ok(SubscriptionDtoResponse.fromSubscriptionEntity(subscription));
    }

    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionDtoResponse> cancel(@RequestBody SubscriptionDtoRequest subscriptionDtoRequest){
        Subscription subscription = subscriptionService.cancelFundSubscription(subscriptionDtoRequest.toSubscriptionEntity());
        return ResponseEntity.ok(SubscriptionDtoResponse.fromSubscriptionEntity(subscription));
    }

}
