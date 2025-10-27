package com.fund.manageFunds.domain.repository;


import com.fund.manageFunds.domain.model.Fund;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.List;

public interface FundRepository {
    List<Fund> getAll();
    Fund getByHashkey(Key keyToSearch);
}
