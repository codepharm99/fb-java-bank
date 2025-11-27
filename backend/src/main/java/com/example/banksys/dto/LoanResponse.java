package com.example.banksys.dto;

import java.math.BigDecimal;

public class LoanResponse {

    private String status;
    private String message;
    private BigDecimal monthlyPayment;
    private BigDecimal totalToRepay;
    private AccountDto account;

    public LoanResponse() {
    }

    public LoanResponse(String status,
                        String message,
                        BigDecimal monthlyPayment,
                        BigDecimal totalToRepay,
                        AccountDto account) {
        this.status = status;
        this.message = message;
        this.monthlyPayment = monthlyPayment;
        this.totalToRepay = totalToRepay;
        this.account = account;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getMonthlyPayment() {
        return monthlyPayment;
    }

    public BigDecimal getTotalToRepay() {
        return totalToRepay;
    }

    public AccountDto getAccount() {
        return account;
    }
}
