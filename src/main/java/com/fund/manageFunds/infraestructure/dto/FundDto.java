package com.fund.manageFunds.infraestructure.dto;
import com.fund.manageFunds.domain.model.Fund;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FundDto {
    private String name;
    private String category;
    private Double minAmount;

    public static FundDto fromFundEntity(Fund fundEntity) {
        return new FundDto(fundEntity.getName(), fundEntity.getCategory(), fundEntity.getMinAmount());
    }
}
