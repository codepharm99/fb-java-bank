package com.example.banksys.controller;

import com.example.banksys.dto.EmployeeDto;
import com.example.banksys.mapper.EmployeeMapper;
import com.example.banksys.model.Employee;
import com.example.banksys.repository.EmployeeRepository;
import com.example.banksys.service.AuthTokenService;
//import org.springframework.util.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final AuthTokenService authTokenService;

    public EmployeeController(EmployeeRepository employeeRepository,
                              AuthTokenService authTokenService) {
        this.employeeRepository = employeeRepository;
        this.authTokenService = authTokenService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        Long employeeId = authTokenService.parseEmployeeId(token);

        if (employeeId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        EmployeeDto dto = EmployeeMapper.toDto(optionalEmployee.get());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/users")
    public List<EmployeeDto> listUsers() {
        return employeeRepository.findAll()
                .stream()
                .map(EmployeeMapper::toDto)
                .collect(Collectors.toList());
    }
}
