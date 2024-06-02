package com.binaryigor.modularpattern.shared.leader;

public class SingleInstanceLeaderAwareness implements LeaderAwareness {

    @Override
    public void executeIfLeader(Runnable runnable) {
        runnable.run();
    }
}
