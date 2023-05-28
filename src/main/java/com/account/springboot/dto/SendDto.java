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
public class SendDto {

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