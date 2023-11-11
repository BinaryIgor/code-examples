package com.binaryigor.modularmonolith.campaign;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class SqlCampaignRepository implements CampaignRepository {

    private final JdbcTemplate jdbcTemplate;

    public SqlCampaignRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Campaign campaign) {
        jdbcTemplate.update("""
                        INSERT INTO campaign (id, name, budget_id, inventory_id, start_date, end_date)
                        VALUES (?, ?, ?, ?, ?, ?)
                        ON CONFLICT (id)
                        DO UPDATE SET
                          name = EXCLUDED.name,
                          budget_id = EXCLUDED.budget_id,
                          inventory_id = EXCLUDED.inventory_id,
                          start_date = EXCLUDED.start_date,
                          end_date = EXCLUDED.end_date
                        """, campaign.id(), campaign.name(), campaign.budgetId(), campaign.inventoryId(),
                toSqlDateOrNull(campaign.startDate()), toSqlDateOrNull(campaign.endDate()));
    }

    private Date toSqlDateOrNull(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    @Override
    public Optional<Campaign> findById(UUID id) {
        var result = jdbcTemplate.query("SELECT * FROM campaign WHERE id = ?",
                (r, n) -> new Campaign(r.getObject("id", UUID.class),
                        r.getString("name"),
                        r.getObject("budget_id", UUID.class),
                        r.getObject("inventory_id", UUID.class),
                        getDateOrNull(r, "start_date"),
                        getDateOrNull(r, "end_date")),
                id);

        return result.isEmpty() ? Optional.empty() : Optional.ofNullable(result.getFirst());
    }

    private LocalDate getDateOrNull(ResultSet r, String column) throws SQLException {
        return Optional.ofNullable(r.getDate(column))
                .map(Date::toLocalDate)
                .orElse(null);
    }
}
