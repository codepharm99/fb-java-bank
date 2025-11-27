package com.example.banksys.dto;

public class LoginResponse {

    private String token;
    private EmployeeDto employee;

    public LoginResponse() {
    }

    public LoginResponse(String token, EmployeeDto employee) {
        this.token = token;
        this.employee = employee;
    }

    public String getToken() {
        return token;
    }

    public EmployeeDto getEmployee() {
        return employee;
    }
}