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
        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseGet(() -> roleRepository.save(
                        new Role("EMPLOYEE", "Базовый сотрудник банка")
                ));

        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> roleRepository.save(
                        new Role("MANAGER", "Менеджер по работе с клиентами")
                ));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(
                        new Role("ADMIN", "Управляющий/администратор системы")
                ));

        if (employeeRepository.findByUsername("employee1").isEmpty()) {
            Employee emp = new Employee("Иван Сотрудник", "employee1", "password");
            Set<Role> roles = new HashSet<>();
            roles.add(employeeRole);
            emp.setRoles(roles);
            employeeRepository.save(emp);
        }

        if (employeeRepository.findByUsername("manager1").isEmpty()) {
            Employee manager = new Employee("Мария Менеджер", "manager1", "password");
            Set<Role> roles = new HashSet<>();
            roles.add(employeeRole);
            roles.add(managerRole);
            manager.setRoles(roles);
            employeeRepository.save(manager);
        }

        if (employeeRepository.findByUsername("admin1").isEmpty()) {
            Employee admin = new Employee("Антон Управляющий", "admin1", "admin");
            Set<Role> roles = new HashSet<>();
            roles.add(employeeRole);
            roles.add(managerRole);
            roles.add(adminRole);
            admin.setRoles(roles);
            employeeRepository.save(admin);
        }

        System.out.println("Инициализация сотрудников и ролей завершена");
    }
}