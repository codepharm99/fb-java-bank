package com.example.banksys.controller;

import com.example.banksys.dto.EmployeeDto;
import com.example.banksys.dto.LoginRequest;
import com.example.banksys.dto.LoginResponse;
import com.example.banksys.mapper.EmployeeMapper;
import com.example.banksys.model.Employee;
import com.example.banksys.repository.EmployeeRepository;
import com.example.banksys.service.AuthTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // на время разработки, чтобы Next.js спокойно ходил
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final AuthTokenService authTokenService;

    public AuthController(EmployeeRepository employeeRepository,
                          AuthTokenService authTokenService) {
        this.employeeRepository = employeeRepository;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("username and password are required");
        }

        Optional<Employee> optionalEmployee =
                employeeRepository.findByUsername(request.getUsername());

        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        Employee employee = optionalEmployee.get();

        if (!employee.getPassword().equals(request.getPassword()) || !employee.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        String token = authTokenService.generateToken(employee.getId());
        EmployeeDto dto = EmployeeMapper.toDto(employee);

        LoginResponse response = new LoginResponse(token, dto);
        return ResponseEntity.ok(response);
    }
}