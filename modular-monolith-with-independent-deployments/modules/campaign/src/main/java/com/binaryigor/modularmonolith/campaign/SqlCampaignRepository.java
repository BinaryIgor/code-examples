package com.binaryigor.modularmonolith.campaign;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SqlCampaignRepository implements CampaignRepository {

    private final JdbcTemplate campaignJdbcTemplate;

    public SqlCampaignRepository(JdbcTemplate campaignJdbcTemplate) {
        this.campaignJdbcTemplate = campaignJdbcTemplate;
    }

    @Override
    public void save(Campaign campaign) {
        campaignJdbcTemplate.update("""
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
                campaign.startDate() == null ? null : Date.valueOf(campaign.startDate()),
                campaign.endDate() == null ? null : Date.valueOf(campaign.endDate()));
    }

    @Override
    public Optional<Campaign> findById(UUID id) {
        var res = campaignJdbcTemplate.query("SELECT * FROM campaign WHERE id = ?",
                (r, n) -> new Campaign(r.getObject("id", UUID.class),
                        r.getString("name"),
                        r.getObject("budget_id", UUID.class),
                        r.getObject("inventory_id", UUID.class),
                        getDateOrNull(r, "start_date"),
                        getDateOrNull(r, "end_date")),
                id);

        return res.isEmpty() ? Optional.empty() : Optional.ofNullable(res.getFirst());
    }

    private LocalDate getDateOrNull(ResultSet r, String column) throws SQLException {
        return Optional.ofNullable(r.getDate(column))
                .map(Date::toLocalDate)
                .orElse(null);
    }
}
