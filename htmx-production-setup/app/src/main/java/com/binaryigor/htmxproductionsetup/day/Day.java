package com.binaryigor.htmxproductionsetup.day;

import java.time.LocalDate;
import java.util.UUID;

public record Day(UUID userId, LocalDate date, String description) {
}
