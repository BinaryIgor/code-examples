package com.binaryigor.single.app.account;

import com.binaryigor.single.app.ErrorResponse;
import com.binaryigor.single.app.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private static final int GENERATE_ACCOUNTS_MAX_IN_MEMORY = 10_000;
    private static final UUID LOAD_TEST_ACCOUNT_ID1 = UUID.fromString("06f40771-6460-479a-a47c-177473e240b5");
    private static final UUID LOAD_TEST_ACCOUNT_ID2 = UUID.fromString("4db7506f-43fe-475e-afbe-842514a6223b");
    private static final int UNIQUE_NAMES = 250_000;
    private static final int MAX_VERSION = 10_000;
    private static final Random RANDOM = new Random();

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("{id}")
    Account getAccount(@PathVariable UUID id) {
        return accountRepository.accountById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account of %s id does not exist".formatted(id)));
    }

    @GetMapping
    List<Account> getAccountsByName(@RequestParam String name) {
        return accountRepository.accountsByName(name);
    }

    @PostMapping("generate-test-data")
    ResponseEntity<Void> generateTestData(@RequestParam(required = false, defaultValue = "1250000") int size) {
        Thread.startVirtualThread(() -> {
            var requiredAccountsToCreate = Stream.of(LOAD_TEST_ACCOUNT_ID1, LOAD_TEST_ACCOUNT_ID2)
                .filter(id -> accountRepository.accountById(id).isEmpty())
                .map(this::randomAccount)
                .toList();

            if (!requiredAccountsToCreate.isEmpty()) {
                accountRepository.create(requiredAccountsToCreate);
            }

            generateAndCreateRandomAccounts(size);
        });
        return ResponseEntity.accepted().build();
    }

    private void generateAndCreateRandomAccounts(int size) {
        var start = Instant.now();

        var toCreate = new LinkedList<Account>();
        var created = 0;

        for (int i = 0; i < size; i++) {
            toCreate.add(randomAccount());
            if (toCreate.size() >= GENERATE_ACCOUNTS_MAX_IN_MEMORY) {
                var toCreateSize = toCreate.size();
                log.info("Next {} accounts were generated, creating them...", toCreateSize);

                accountRepository.create(toCreate);
                toCreate.clear();

                created += toCreateSize;
                log.info("{}/{} accounts were created", created, size);
            }
        }

        if (!toCreate.isEmpty()) {
            log.info("Creating {} left accounts....", toCreate.size());
            accountRepository.create(toCreate);
            log.info("{} left accounts were created!", toCreate.size());
        }

        var duration = Duration.between(start, Instant.now());

        log.info("All accounts were created, it took: {}!", duration);
    }

    @PostMapping("execute-random-write")
    void executeRandomWrite() {
        if (RANDOM.nextBoolean()) {
            // random insert
            accountRepository.create(List.of(randomAccount()));
        } else {
            // random delete of existing account
            var accountIds = accountRepository.accountIds(10, List.of(LOAD_TEST_ACCOUNT_ID1, LOAD_TEST_ACCOUNT_ID2));
            if (!accountIds.isEmpty()) {
                var toDeleteIdx = RANDOM.nextInt(accountIds.size());
                accountRepository.delete(accountIds.get(toDeleteIdx));
            }
        }
    }

    private Account randomAccount() {
        return randomAccount(UUID.randomUUID());
    }

    private Account randomAccount(UUID id) {
        var name = "name-" + RANDOM.nextInt(UNIQUE_NAMES);
        return new Account(id, name, "email-" + id + "@email.com", Instant.now(), RANDOM.nextInt(MAX_VERSION));
    }

    @ExceptionHandler
    ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ErrorResponse.asResponseEntity(HttpStatus.NOT_FOUND, exception);
    }
}
