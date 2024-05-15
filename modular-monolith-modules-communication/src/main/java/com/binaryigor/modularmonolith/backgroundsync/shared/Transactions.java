package com.binaryigor.modularmonolith.backgroundsync.shared;

public interface Transactions {
    Transactions DELEGATE = Runnable::run;

    void execute(Runnable transaction);
}
