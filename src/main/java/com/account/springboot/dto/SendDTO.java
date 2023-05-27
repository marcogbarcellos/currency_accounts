package com.account.springboot.dto;

import com.account.springboot.models.CurrencyEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SendDTO {

    @NotEmpty
    @Email
    private String fromEmail;

    @NotEmpty
    @Email
    private String toEmail;

    @NotEmpty
    private CurrencyEnum currency;

    @NotEmpty
    private String amount;


}