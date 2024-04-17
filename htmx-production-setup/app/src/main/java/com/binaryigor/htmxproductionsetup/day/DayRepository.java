package com.binaryigor.htmxproductionsetup.day;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DayRepository {

    void save(Day day);

    List<LocalDate> daysOfUser(UUID userId);

    Optional<Day> dayOfUser(UUID userId, LocalDate date);
}
