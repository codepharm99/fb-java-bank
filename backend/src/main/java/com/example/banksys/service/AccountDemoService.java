package com.example.banksys.service;

import com.example.banksys.dto.AccountDto;
import com.example.banksys.dto.LoanRequest;
import com.example.banksys.dto.LoanResponse;
import com.example.banksys.dto.TransferRequest;
import com.example.banksys.dto.TransferResponse;
import com.example.banksys.dto.TransferByUserRequest;
import com.example.banksys.dto.TransferHistoryItem;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AccountDemoService {

    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, Long> userAccounts = new ConcurrentHashMap<>();
    private final List<TransferHistoryItem> history = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idSequence = new AtomicLong(1);

    @PostConstruct
    public void seedDemoAccounts() {
        if (!accounts.isEmpty()) {
            return;
        }
        addAccountForUser("employee1", "Основной счёт", "KZT", new BigDecimal("750000.00"), BigDecimal.ZERO);
        addAccountForUser("manager1", "Основной счёт", "KZT", new BigDecimal("820000.50"), new BigDecimal("60000.00"));
        addAccountForUser("admin1", "Основной счёт", "KZT", new BigDecimal("1250000.00"), BigDecimal.ZERO);
        addAccountForUser("demo1", "Основной счёт", "KZT", new BigDecimal("350000.00"), BigDecimal.ZERO);
        addAccountForUser("demo2", "Основной счёт", "KZT", new BigDecimal("270000.00"), new BigDecimal("15000.00"));
    }

    public List<AccountDto> getAccounts() {
        List<AccountDto> result = new ArrayList<>();
        accounts.values().stream()
                .sorted(Comparator.comparing(Account::getId))
                .forEach(acc -> result.add(toDto(acc)));
        return result;
    }

    public synchronized TransferResponse transfer(TransferRequest request) {
        validateTransfer(request);
        Account from = requireAccount(request.getFromAccountId());
        Account to = requireAccount(request.getToAccountId());

        BigDecimal amount = normalize(request.getAmount());
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств на счёте отправителя");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        history.add(new TransferHistoryItem(
                from.getOwnerUsername(),
                to.getOwnerUsername(),
                amount,
                from.getCurrency(),
                request.getDescription(),
                LocalDateTime.now()
        ));

        return new TransferResponse(
                "ok",
                "Перевод выполнен",
                toDto(from),
                toDto(to)
        );
    }

    public TransferResponse transferByUser(TransferByUserRequest request) {
        validateTransferByUser(request);
        Account from = requireAccountByUsername(request.getFromUsername());
        Account to = requireAccountByUsername(request.getToUsername());

        TransferRequest converted = new TransferRequest();
        converted.setFromAccountId(from.getId());
        converted.setToAccountId(to.getId());
        converted.setAmount(request.getAmount());
        converted.setDescription(request.getDescription());
        return transfer(converted);
    }

    public synchronized LoanResponse takeLoan(LoanRequest request) {
        validateLoan(request);
        Account account = requireAccount(request.getAccountId());

        BigDecimal amount = normalize(request.getAmount());
        int term = Optional.ofNullable(request.getTermMonths()).orElse(12);
        if (term <= 0) {
            term = 12;
        }

        BigDecimal rate = Optional.ofNullable(request.getRate()).orElse(new BigDecimal("0.12"));
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            rate = BigDecimal.ZERO;
        }

        BigDecimal totalToRepay = amount.multiply(BigDecimal.ONE.add(rate))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = totalToRepay.divide(
                BigDecimal.valueOf(term),
                2,
                RoundingMode.HALF_UP
        );

        account.setBalance(account.getBalance().add(amount));
        account.setLoanDebt(account.getLoanDebt().add(totalToRepay));

        history.add(new TransferHistoryItem(
                "BANK",
                account.getOwnerUsername(),
                amount,
                account.getCurrency(),
                "Кредит: пополнение по кредиту",
                LocalDateTime.now()
        ));

        return new LoanResponse(
                "ok",
                "Кредит одобрен и зачислен на счёт",
                monthlyPayment,
                totalToRepay,
                toDto(account)
        );
    }

    public List<TransferHistoryItem> getHistory(String username) {
        synchronized (history) {
            if (username == null || username.isBlank()) {
                return new ArrayList<>(history);
            }
            List<TransferHistoryItem> filtered = new ArrayList<>();
            for (TransferHistoryItem item : history) {
                if (username.equalsIgnoreCase(item.getFromUsername())
                        || username.equalsIgnoreCase(item.getToUsername())) {
                    filtered.add(item);
                }
            }
            return filtered;
        }
    }

    private void validateTransfer(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Некорректный запрос перевода");
        }
        if (request.getFromAccountId() == null || request.getToAccountId() == null) {
            throw new IllegalArgumentException("Нужно указать счета отправителя и получателя");
        }
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Счета отправителя и получателя не должны совпадать");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть больше нуля");
        }
    }

    private void validateLoan(LoanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Некорректный запрос по кредиту");
        }
        if (request.getAccountId() == null) {
            throw new IllegalArgumentException("Нужно выбрать счёт для зачисления кредита");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(new BigDecimal("1000")) < 0) {
            throw new IllegalArgumentException("Минимальная сумма кредита — 1000");
        }
    }

    private void validateTransferByUser(TransferByUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Некорректный запрос перевода");
        }
        if (request.getFromUsername() == null || request.getToUsername() == null) {
            throw new IllegalArgumentException("Нужно указать имена отправителя и получателя");
        }
        if (request.getFromUsername().equalsIgnoreCase(request.getToUsername())) {
            throw new IllegalArgumentException("Отправитель и получатель не должны совпадать");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть больше нуля");
        }
    }

    private Account requireAccount(Long accountId) {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Счёт не найден: " + accountId);
        }
        return account;
    }

    private Account requireAccountByUsername(String username) {
        Long accountId = userAccounts.get(username);
        if (accountId == null) {
            throw new IllegalArgumentException("Счёт для пользователя не найден: " + username);
        }
        return requireAccount(accountId);
    }

    private Account addAccount(String title,
                               String number,
                               String currency,
                               BigDecimal balance,
                               BigDecimal loanDebt,
                               String ownerUsername) {
        long id = idSequence.getAndIncrement();
        Account account = new Account(id, title, number, currency, normalize(balance), normalize(loanDebt), ownerUsername);
        accounts.put(id, account);
        return account;
    }

    private Account addAccountForUser(String username,
                                      String title,
                                      String currency,
                                      BigDecimal balance,
                                      BigDecimal loanDebt) {
        Account account = addAccount(title, generateAccountNumber(), currency, balance, loanDebt, username);
        userAccounts.put(username, account.getId());
        return account;
    }

    private AccountDto toDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getTitle(),
                account.getNumber(),
                account.getCurrency(),
                normalize(account.getBalance()),
                normalize(account.getLoanDebt()),
                account.getOwnerUsername()
        );
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String generateAccountNumber() {
        long id = idSequence.get();
        return String.format("KZ%018d", id);
    }

    private static class Account {
        private final Long id;
        private final String title;
        private final String number;
        private final String currency;
        private final String ownerUsername;
        private BigDecimal balance;
        private BigDecimal loanDebt;

        private Account(Long id,
                        String title,
                        String number,
                        String currency,
                        BigDecimal balance,
                        BigDecimal loanDebt,
                        String ownerUsername) {
            this.id = id;
            this.title = title;
            this.number = number;
            this.currency = currency;
            this.balance = balance;
            this.loanDebt = loanDebt;
            this.ownerUsername = ownerUsername;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getNumber() {
            return number;
        }

        public String getCurrency() {
            return currency;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public BigDecimal getLoanDebt() {
            return loanDebt;
        }

        public String getOwnerUsername() {
            return ownerUsername;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

        public void setLoanDebt(BigDecimal loanDebt) {
            this.loanDebt = loanDebt;
        }
    }
}
