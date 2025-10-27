package com.fund.manageFunds.infraestructure.inbound;

import com.fund.manageFunds.application.FundService;
import com.fund.manageFunds.infraestructure.dto.FundDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/funds")
public class FundController {

    private final FundService fundService;

    public FundController(FundService fundService) {
        this.fundService = fundService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<FundDto>> exists(){
        return ResponseEntity.ok(fundService.getAll().stream().map(FundDto::fromFundEntity).collect(Collectors.toList()));
    }

    @GetMapping("/getByName")
    public ResponseEntity<FundDto> getOne(@RequestParam String fundName){
        return ResponseEntity
                .ok(FundDto.fromFundEntity(fundService.getFund(fundName)));
    }
}
