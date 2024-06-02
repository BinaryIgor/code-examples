package com.binaryigor.modularpattern.shared.db;

import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

public class SpringTransactions implements Transactions {

    private final TransactionTemplate transactionTemplate;

    public SpringTransactions(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void execute(Runnable transaction) {
        transactionTemplate.execute(status -> {
            transaction.run();
            return null;
        });
    }

    @Override
    public <T> T executeAndReturn(Supplier<T> transaction) {
        return transactionTemplate.execute(status -> transaction.get());
    }
}
