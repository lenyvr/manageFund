package com.fund.manageFunds.domain.model;


import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class Fund {
    @Setter
    private String name;
    @Getter
    @Setter
    private String category;
    @Getter
    @Setter
    private Double minAmount;

    @DynamoDbPartitionKey
    public String getName() {
        return name;
    }

    public static Key getHashKey(String nameValue){
        return Key.builder()
                .partitionValue(nameValue)
                .build();
    }

}
