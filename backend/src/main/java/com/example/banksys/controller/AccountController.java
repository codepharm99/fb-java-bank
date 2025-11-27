package com.example.banksys.controller;

import com.example.banksys.dto.AccountDto;
import com.example.banksys.dto.LoanRequest;
import com.example.banksys.dto.TransferRequest;
import com.example.banksys.dto.TransferByUserRequest;
import com.example.banksys.dto.TransferHistoryItem;
import com.example.banksys.service.AccountDemoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountDemoService accountDemoService;

    public AccountController(AccountDemoService accountDemoService) {
        this.accountDemoService = accountDemoService;
    }

    @GetMapping
    public List<AccountDto> getAccounts() {
        return accountDemoService.getAccounts();
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            return ResponseEntity.ok(accountDemoService.transfer(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/transfer/by-user")
    public ResponseEntity<?> transferByUser(@RequestBody TransferByUserRequest request) {
        try {
            return ResponseEntity.ok(accountDemoService.transferByUser(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/transfers")
    public List<TransferHistoryItem> history(@RequestParam(name = "user", required = false) String username) {
        return accountDemoService.getHistory(username);
    }

    @PostMapping("/loan")
    public ResponseEntity<?> takeLoan(@RequestBody LoanRequest request) {
        try {
            return ResponseEntity.ok(accountDemoService.takeLoan(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
