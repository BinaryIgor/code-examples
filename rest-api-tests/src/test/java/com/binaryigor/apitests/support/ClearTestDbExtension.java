package com.binaryigor.apitests.support;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class ClearTestDbExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        var jdbcTemplate = SpringExtension.getApplicationContext(context).getBean(JdbcTemplate.class);
        jdbcTemplate.execute("TRUNCATE client");
    }
}
