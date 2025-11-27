package com.example.banksys.dto;

import java.util.List;

public class EmployeeDto {

    private Long id;
    private String fullName;
    private String username;
    private List<String> roles;

    public EmployeeDto() {
    }

    public EmployeeDto(Long id, String fullName, String username, List<String> roles) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }
}