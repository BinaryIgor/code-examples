package com.binaryigor.htmxproductionsetup.history.db;

import com.binaryigor.htmxproductionsetup.history.HistoryRepository;
import com.binaryigor.htmxproductionsetup.shared.DemoData;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryHistoryRepository implements HistoryRepository {

    private final Map<UUID, List<LocalDate>> usersHistory = new ConcurrentHashMap<>();

    public InMemoryHistoryRepository() {
        var today = LocalDate.now();
        usersHistory.put(DemoData.USER1_ID,
                List.of(today.minusDays(1),
                        today.minusDays(2),
                        today.minusDays(3),
                        today.minusDays(7)));
    }

    @Override
    public List<LocalDate> days(UUID userId) {
        return usersHistory.getOrDefault(userId, List.of());
    }
}
