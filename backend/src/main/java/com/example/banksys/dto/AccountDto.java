package com.example.banksys.dto;

import java.math.BigDecimal;

public class AccountDto {

    private Long id;
    private String title;
    private String number;
    private String currency;
    private BigDecimal balance;
    private BigDecimal loanDebt;
    private String ownerUsername;

    public AccountDto() {
    }

    public AccountDto(Long id,
                      String title,
                      String number,
                      String currency,
                      BigDecimal balance,
                      BigDecimal loanDebt,
                      String ownerUsername) {
        this.id = id;
        this.title = title;
        this.number = number;
        this.currency = currency;
        this.balance = balance;
        this.loanDebt = loanDebt;
        this.ownerUsername = ownerUsername;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNumber() {
        return number;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getLoanDebt() {
        return loanDebt;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }
}
