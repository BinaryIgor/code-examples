package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class CourseScheduleTest {

    @ParameterizedTest
    @MethodSource
    void returnsWhetherCoursesCanBeFinished(CourseSchedule.Case c) {
        var actual = CourseSchedule.canFinish(c.courses(), c.prerequisites());
        Assertions.assertEquals(c.canFinish(), actual,
                "Expected canFinish %b for %s prerequisites but got %b"
                        .formatted(c.canFinish(), Arrays.deepToString(c.prerequisites()), actual));

    }

    static List<CourseSchedule.Case> returnsWhetherCoursesCanBeFinished() {
        return CourseSchedule.Case.cases();
    }
}
