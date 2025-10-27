package com.fund.manageFunds.domain.repository;

import com.fund.manageFunds.domain.model.Subscription;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.List;

public interface SubscriptionRepository {

    Subscription save(Subscription subscription);
    //List<Subscription> getAll();
    Subscription getByHashkey(Key keyToSearch);
    List<Subscription> getByClient(String clientEmail);
    List<Subscription> getSubscriptionsByClientFiltered(String clientEmail, String fundName, String state);
}
