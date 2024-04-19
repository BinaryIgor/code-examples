package com.binaryigor.htmxproductionsetup.day.domain;

import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DayService {

    private final DayRepository dayRepository;
    private final Clock clock;

    public DayService(DayRepository dayRepository,
                      Clock clock) {
        this.dayRepository = dayRepository;
        this.clock = clock;
    }

    public Optional<Day> currentDayOfUser(UUID userId) {
        return dayRepository.dayOfUser(userId, LocalDate.now(clock));
    }

    public void saveCurrentDay(UUID userId, String note) {
        // TODO validate too long/html note probably
        var day = new Day(userId, LocalDate.now(clock), note);
        dayRepository.save(day);
    }

    public List<LocalDate> daysOfUser(UUID userId) {
        return dayRepository.daysOfUser(userId);
    }

    public Day historicalDayOfUser(UUID userId, LocalDate date) {
        return dayRepository.dayOfUser(userId, date)
                .orElseThrow(() -> new NotFoundException("Day"));
    }
}
