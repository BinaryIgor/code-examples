package com.binaryigor.dsa;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

public class TasksSchedulerTest {

    @ParameterizedTest
    @MethodSource
    void returnsLeastIntervalForSetOfTasks(TaskScheduler.Case c) {
        var actual = TaskScheduler.leastInterval(c.tasks(), c.n());
        Assertions.assertEquals(c.intervals(), actual,
                "Expected to get %d intervals for %s tasks with %d delay but got %d"
                        .formatted(c.intervals(), Arrays.toString(c.tasks()), c.n(), actual));
    }

    static List<TaskScheduler.Case> returnsLeastIntervalForSetOfTasks() {
        return TaskScheduler.Case.cases();
    }
}
