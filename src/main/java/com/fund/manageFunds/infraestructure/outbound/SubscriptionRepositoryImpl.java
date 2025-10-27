package com.fund.manageFunds.infraestructure.outbound;

import com.fund.manageFunds.domain.model.Subscription;
import com.fund.manageFunds.domain.repository.SubscriptionRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final DynamoDbTable<Subscription> subscriptionTable;

    public  SubscriptionRepositoryImpl(DynamoDbEnhancedClient enhancedClient) {
        this.subscriptionTable = enhancedClient.table("Subscription", TableSchema.fromBean(Subscription.class));
    }



    @Override
    public List<Subscription> getSubscriptionsByClientFiltered(String clientEmail, String fundName, String state){
        QueryConditional pkConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(clientEmail).build()
        );

        StringBuilder filterLogic = new StringBuilder();
        Map<String, AttributeValue> filterValues = new HashMap<>();
        Map<String, String> filterNames = new HashMap<>();

        if(fundName!= null && !fundName.isBlank()){
            filterLogic.append("#fundName = :valFundName");
            filterValues.put(":valFundName", AttributeValue.builder().s(fundName).build());
            filterNames.put("#fundName", "fundName");
        }

        if(fundName!= null && !fundName.isBlank() && state!=null && !state.isBlank()){
            filterLogic.append(" AND ");
        }

        if(state!=null && !state.isBlank()){
            filterLogic.append("#state = :valState");
            filterValues.put(":valState", AttributeValue.builder().s(state).build());
            filterNames.put("#state", "state");
        }

        Expression filterExpression = Expression.builder()
                .expression(filterLogic.toString())
                .expressionNames(filterNames)
                .expressionValues(filterValues)
                .build();

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .queryConditional(pkConditional)
                .build();

        return subscriptionTable.query(request)
                .items()
                .stream()
                .toList();
    }

    @Override
    public Subscription save(Subscription subscription) {
      subscriptionTable.putItem(subscription);
      return getByHashkey(subscription.getHashKey(subscription.getClientEmail(), subscription.getId()));
    }

    /*
    @Override
    public List<Subscription> getAll() {
        return subscriptionTable.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }*/

    @Override
    public Subscription getByHashkey(Key keyToSearch) {
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                .key(keyToSearch)
                .build();
        return subscriptionTable.getItem(request);
    }

    @Override
    public List<Subscription> getByClient(String clientEmail) {
        QueryConditional pkConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(clientEmail).build()
        );

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(pkConditional)
                .build();

        return subscriptionTable.query(request)
                .items()
                .stream()
                .toList();
    }
}
