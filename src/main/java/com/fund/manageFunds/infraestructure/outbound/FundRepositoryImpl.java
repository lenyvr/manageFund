package com.fund.manageFunds.infraestructure.outbound;

import com.fund.manageFunds.domain.model.Fund;
import com.fund.manageFunds.domain.repository.FundRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FundRepositoryImpl implements FundRepository {

    private final DynamoDbTable<Fund> fundTable;

    public  FundRepositoryImpl(DynamoDbEnhancedClient enhancedClient) {
        this.fundTable = enhancedClient.table("Fund", TableSchema.fromBean(Fund.class));
    }

    @Override
    public List<Fund> getAll() {
        return fundTable.scan()
                .stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Fund getByHashkey(Key keyToSearch) {
        GetItemEnhancedRequest request = GetItemEnhancedRequest.builder()
                .key(keyToSearch)
                .build();
        return fundTable.getItem(request);
    }

}
