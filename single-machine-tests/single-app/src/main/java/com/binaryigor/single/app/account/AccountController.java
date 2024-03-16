package com.binaryigor.single.app.account;

import com.binaryigor.single.app.shared.ErrorResponse;
import com.binaryigor.single.app.shared.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("{id}")
    Account getAccount(@PathVariable UUID id) {
        return accountRepository.accountById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account of %s id does not exist".formatted(id)));
    }

    @PostMapping("generate-random")
    List<UUID> generateRandom(@RequestParam(required = false, defaultValue = "10") Integer size) {
        var accounts = Stream.generate(() -> {
                    var id = UUID.randomUUID();
                    return new Account(id, "name-" + id, "email-" + id + "@email.com", Instant.now());
                })
                .limit(size)
                .toList();

        accountRepository.create(accounts, 100);

        return accounts.stream().map(Account::id).toList();
    }

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ErrorResponse.asResponseEntity(HttpStatus.NOT_FOUND, exception);
    }
}
