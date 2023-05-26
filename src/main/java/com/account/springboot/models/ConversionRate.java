package com.account.springboot.models;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Currency;

@Builder
@Data
@EqualsAndHashCode
@ToString
public class ConversionRate implements Comparable<ConversionRate> {
    private LocalDate date;
    private String rate;
    private Currency currency;

    @Override
    public int compareTo(ConversionRate o) {
        return o.getDate().compareTo(this.getDate());
    }
}