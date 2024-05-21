package com.binaryigor.modularpattern.shared.leader;

public interface LeaderAwareness {
    void executeIfLeader(Runnable runnable);
}
