package com.example.banksys.mapper;

import com.example.banksys.dto.EmployeeDto;
import com.example.banksys.model.Employee;
import com.example.banksys.model.Role;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeMapper {

    public static EmployeeDto toDto(Employee employee) {
        List<String> roles = employee.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new EmployeeDto(
                employee.getId(),
                employee.getFullName(),
                employee.getUsername(),
                roles
        );
    }
}