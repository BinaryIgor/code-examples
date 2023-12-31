package com.binaryigor.modularmonolithsimple.budget;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SqlBudgetRepository implements BudgetRepository {

    private static final String BUDGET_TABLE = "budget.budget";

    private final JdbcTemplate jdbcTemplate;

    public SqlBudgetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Budget budget) {
        jdbcTemplate.update("""
                INSERT INTO %s (id, amount, created_at)
                VALUES (?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE SET
                  amount = EXCLUDED.amount,
                  created_at = EXCLUDED.created_at;
                """.formatted(BUDGET_TABLE), budget.id(), budget.amount(), Timestamp.from(budget.createdAt()));
    }

    @Override
    public Optional<Budget> findById(UUID id) {
        var result = jdbcTemplate.query("SELECT * FROM %s WHERE id = ?".formatted(BUDGET_TABLE),
                (r, n) -> new Budget(r.getObject("id", UUID.class),
                        r.getObject("amount", BigDecimal.class),
                        r.getTimestamp("created_at").toInstant()),
                id);

        return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.getFirst());
    }
}
