package com.example.banksys.config;

import com.example.banksys.model.Employee;
import com.example.banksys.model.Role;
import com.example.banksys.repository.EmployeeRepository;
import com.example.banksys.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;

    public DataInitializer(RoleRepository roleRepository,
                           EmployeeRepository employeeRepository) {
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.findByName("EMPLOYEE")
                        .map(existing -> {
                            existing.setName("USER");
                            existing.setDescription("Базовый пользователь (user)");
                            return roleRepository.save(existing);
                        })
                        .orElseGet(() -> roleRepository.save(
                                new Role("USER", "Базовый пользователь (user)")
                        ))
                );

        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> roleRepository.save(
                        new Role("MANAGER", "Менеджер по работе с клиентами")
                ));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(
                        new Role("ADMIN", "Управляющий/администратор системы")
                ));

        createUserIfAbsent("Еркебулан", "employee1", "password", userRole);
        createUserIfAbsent("Аслан", "manager1", "password", userRole, managerRole);
        createUserIfAbsent("Магжан", "admin1", "admin", userRole, managerRole, adminRole);
        createUserIfAbsent("Жанибек", "demo1", "demo", userRole);
        createUserIfAbsent("Айбол", "demo2", "demo", userRole);

        System.out.println("Инициализация сотрудников и ролей завершена");
    }

    private void createUserIfAbsent(String fullName, String username, String password, Role... roles) {
        if (employeeRepository.findByUsername(username).isPresent()) {
            return;
        }
        Employee user = new Employee(fullName, username, password);
        Set<Role> assigned = new HashSet<>();
        for (Role role : roles) {
            assigned.add(role);
        }
        user.setRoles(assigned);
        employeeRepository.save(user);
    }
}
