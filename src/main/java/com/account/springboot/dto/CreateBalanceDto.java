package com.account.springboot.dto;

import com.account.springboot.models.CurrencyEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateBalanceDto {

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private CurrencyEnum currency;
}