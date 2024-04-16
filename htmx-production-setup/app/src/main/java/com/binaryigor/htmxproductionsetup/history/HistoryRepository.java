package com.binaryigor.htmxproductionsetup.history;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HistoryRepository {
    List<LocalDate> days(UUID userId);
}
