package com.binaryigor.htmxproductionsetup;

import com.binaryigor.htmxproductionsetup.day.domain.Day;
import com.binaryigor.htmxproductionsetup.day.domain.DayRepository;
import com.binaryigor.htmxproductionsetup.shared.Language;
import com.binaryigor.htmxproductionsetup.user.domain.PasswordHasher;
import com.binaryigor.htmxproductionsetup.user.domain.User;
import com.binaryigor.htmxproductionsetup.user.domain.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class DemoDataInitializer {

    private static final UUID USER1_ID = UUID.fromString("bdcdc153-3865-42b2-9f7d-4c380aa13c80");
    private static final UUID USER2_ID = UUID.fromString("41954772-edd5-4150-be94-da0196a4c66b");
    private static final Logger logger = LoggerFactory.getLogger(DemoDataInitializer.class);
    private final UserRepository userRepository;
    private final DayRepository dayRepository;
    private final PasswordHasher passwordHasher;

    public DemoDataInitializer(UserRepository userRepository,
                               DayRepository dayRepository,
                               PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.dayRepository = dayRepository;
        this.passwordHasher = passwordHasher;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing db with demo data...");

        var user1 = new User(USER1_ID, "igor@gmail.com", "Igor",
                passwordHasher.hash("ComplexPassword12"), Language.EN);
        var user2 = new User(USER2_ID, "other@gmail.com", "Other",
                passwordHasher.hash("ComplexOtherPassword12"), Language.EN);

        userRepository.save(user1);
        userRepository.save(user2);

        var day = LocalDate.now();

        var user1Days = List.of(
                new Day(user1.id(), day.minusDays(1), "Some note 1"),
                new Day(user1.id(), day.minusDays(2), "Some note 2"),
                new Day(user1.id(), day.minusDays(3), "Some note 3"),
                new Day(user1.id(), day.minusDays(7), "Some note 7"));
        var user2Days = List.of(
                new Day(user2.id(), day.minusDays(1), "Another note 1"),
                new Day(user2.id(), day.minusDays(2), "Another note 2"));

        user1Days.forEach(dayRepository::save);
        user2Days.forEach(dayRepository::save);

        logger.info("Db initialized!");
    }
}
