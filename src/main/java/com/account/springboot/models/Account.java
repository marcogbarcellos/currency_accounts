package com.account.springboot.models;

import com.account.springboot.dto.AccountInDto;
import com.account.springboot.dto.AccountOutDto;
import com.account.springboot.dto.CreateBalanceDTO;
import com.account.springboot.exceptions.CustomException;
import com.account.springboot.exceptions.ErrorCode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString
public class Account {
    private String email;
    private ConcurrentMap<CurrencyEnum, Balance> balances;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public Account(AccountInDto dto) {
        this.balances = new ConcurrentHashMap<>();
        this.email = dto.getEmail();
        LocalDate now = LocalDate.now();
        this.createdAt = now;
        this.updatedAt = now;

    }

    public AccountOutDto toDTO() {
        Map<CurrencyEnum, BigDecimal> dtoBalances = balances.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getAmount()));
        return AccountOutDto.builder()
                .email(this.email)
                .balances(dtoBalances)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public void addBalance(CurrencyEnum currency, BigDecimal yearlyInterestRate) {
        LocalDate now = LocalDate.now();
        balances.put(currency, Balance.builder()
                .currency(currency)
                .amount(new BigDecimal("10")) // starting balance with some value form testing purpose
                .yearlyInterestRate(yearlyInterestRate) // considering the interest is 1.5% per year
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    public void updateBalance(CurrencyEnum currency, BigDecimal amount) {
        Balance balance = balances.get(currency);
        if (balance == null) {
            throw new CustomException(ErrorCode.NO_SUCH_CURRENCY);
        }

        synchronized (balance) {
            BigDecimal currentAmount = balance.getAmount();
            BigDecimal newAmount = currentAmount.add(amount);

            if (newAmount.compareTo(BigDecimal.ZERO) >= 0) {
                balance.setAmount(newAmount);
            } else {
                throw new CustomException(ErrorCode.INSUFFICIENT_AMOUNT);
            }
            updatedAt = LocalDate.now();
        }
    }

}