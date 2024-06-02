package com.binaryigor.modularpattern.shared.db;

import java.util.function.Supplier;

public interface Transactions {

    void execute(Runnable transaction);

     <T> T executeAndReturn(Supplier<T> transaction);
}
