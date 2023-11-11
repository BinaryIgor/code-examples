package com.binaryigor.modularmonolith.budget;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class SqlBudgetRepository implements BudgetRepository {

    private final JdbcTemplate jdbcTemplate;

    public SqlBudgetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Budget budget) {
        jdbcTemplate.update("""
                INSERT INTO budget (id, amount, created_at)
                VALUES (?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE SET
                  amount = EXCLUDED.amount,
                  created_at = EXCLUDED.created_at;
                """, budget.id(), budget.amount(), Timestamp.from(budget.createdAt()));
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        var result = jdbcTemplate.query("SELECT * FROM budget WHERE id = ?",
                (r, n) -> new Budget(r.getObject("id", UUID.class),
                        r.getObject("amount", BigDecimal.class),
                        r.getTimestamp("created_at").toInstant()),
                id);

        return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.getFirst());
    }
}
