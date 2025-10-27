package com.fund.manageFunds.application;

import com.fund.manageFunds.domain.exceptions.NoFundFound;
import com.fund.manageFunds.domain.model.Fund;
import com.fund.manageFunds.domain.repository.FundRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundService {

    final FundRepository fundRepository;

    public FundService(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    public List<Fund> getAll(){
        return fundRepository.getAll();
    }

    public Fund getFund(String name){
        Fund fund = fundRepository.getByHashkey(Fund.getHashKey(name));
        if(fund == null){
            throw new NoFundFound("Fund doesn't exist");
        }
        return fund;
    }
}
