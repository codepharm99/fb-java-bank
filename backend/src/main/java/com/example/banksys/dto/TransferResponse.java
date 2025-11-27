package com.example.banksys.dto;

public class TransferResponse {

    private String status;
    private String message;
    private AccountDto fromAccount;
    private AccountDto toAccount;

    public TransferResponse() {
    }

    public TransferResponse(String status,
                            String message,
                            AccountDto fromAccount,
                            AccountDto toAccount) {
        this.status = status;
        this.message = message;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public AccountDto getFromAccount() {
        return fromAccount;
    }

    public AccountDto getToAccount() {
        return toAccount;
    }
}
