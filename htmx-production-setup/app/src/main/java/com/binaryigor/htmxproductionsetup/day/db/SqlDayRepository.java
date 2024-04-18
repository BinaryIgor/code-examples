package com.binaryigor.htmxproductionsetup.day.db;

import com.binaryigor.htmxproductionsetup.day.Day;
import com.binaryigor.htmxproductionsetup.day.DayRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SqlDayRepository implements DayRepository {

    private static final String DAY_TABLE = "day.day";
    private final JdbcClient jdbcClient;

    public SqlDayRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(Day day) {
        jdbcClient.sql("""
                        INSERT INTO %s (user_id, date, note)
                        VALUES (:user_id, :date, :note)
                        ON CONFLICT (user_id, date)
                        DO UPDATE
                        SET note = EXCLUDED.note
                        """.formatted(DAY_TABLE))
                .param("user_id", day.userId())
                .param("date", day.date())
                .param("note", day.note())
                .update();
    }

    @Override
    public List<LocalDate> daysOfUser(UUID userId) {
        return jdbcClient.sql("SELECT date FROM %s WHERE user_id = ? ORDER BY date DESC".formatted(DAY_TABLE))
                .param(userId)
                .query(LocalDate.class)
                .list();
    }

    @Override
    public Optional<Day> dayOfUser(UUID userId, LocalDate date) {
        return jdbcClient.sql("SELECT * FROM %s WHERE user_id = ?  AND date = ?".formatted(DAY_TABLE))
                .param(userId)
                .param(date)
                .query(Day.class)
                .optional();
    }
}
