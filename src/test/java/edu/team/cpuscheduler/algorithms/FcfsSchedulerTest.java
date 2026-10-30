package edu.team.cpuscheduler.algorithms;

import edu.team.cpuscheduler.model.Process;
import edu.team.cpuscheduler.model.ScheduleResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FcfsSchedulerTest {
    private final FcfsScheduler scheduler = new FcfsScheduler();

    @Test
    void schedulesByArrivalWithoutPreemptionOrChangingInput() {
        Process p1 = new Process("P1", 0, 5);
        Process p2 = new Process("P2", 1, 3);
        Process p3 = new Process("P3", 2, 1);
        List<Process> input = new ArrayList<>(List.of(p3, p1, p2));

        List<ScheduleResult> results = scheduler.schedule(input);

        assertEquals(List.of(p3, p1, p2), input);
        assertEquals(3, results.size());
        assertTiming(results.get(0), p1, 0, 5, 0, 5);
        assertTiming(results.get(1), p2, 5, 8, 4, 7);
        assertTiming(results.get(2), p3, 8, 9, 6, 7);
    }

    @Test
    void preservesInputOrderForEqualArrivalTimes() {
        Process first = new Process("Z", 0, 4);
        Process second = new Process("A", 0, 1);
        Process third = new Process("M", 0, 2);

        List<ScheduleResult> results = scheduler.schedule(List.of(first, second, third));

        assertEquals(3, results.size());
        assertTiming(results.get(0), first, 0, 4, 0, 4);
        assertTiming(results.get(1), second, 4, 5, 4, 5);
        assertTiming(results.get(2), third, 5, 7, 5, 7);
    }

    @Test
    void accountsForIdleTimeBetweenProcesses() {
        Process first = new Process("P1", 0, 2);
        Process second = new Process("P2", 7, 3);

        List<ScheduleResult> results = scheduler.schedule(List.of(first, second));

        assertEquals(2, results.size());
        assertTiming(results.get(0), first, 0, 2, 0, 2);
        assertTiming(results.get(1), second, 7, 10, 0, 3);
    }

    @Test
    void schedulesSingleProcessAfterInitialIdleTime() {
        Process process = new Process("P1", 4, 3);

        List<ScheduleResult> results = scheduler.schedule(List.of(process));

        assertEquals(1, results.size());
        assertTiming(results.get(0), process, 4, 7, 0, 3);
    }

    @Test
    void handlesEmptyInput() {
        assertTrue(scheduler.schedule(List.of()).isEmpty());
    }

    @Test
    void rejectsInvalidProcessInput() {
        assertThrows(IllegalArgumentException.class, () -> new Process("P1", -1, 2));
        assertThrows(IllegalArgumentException.class, () -> new Process("P1", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Process("P1", 0, -2));
        assertThrows(IllegalArgumentException.class, () -> new Process(" ", 0, 2));
        assertThrows(IllegalArgumentException.class, () -> new Process(null, 0, 2));
    }

    @Test
    void rejectsTimeOverflowInsteadOfReturningNegativeTimes() {
        Process process = new Process("P1", Integer.MAX_VALUE, 1);
        assertThrows(ArithmeticException.class, () -> scheduler.schedule(List.of(process)));
    }

    private static void assertTiming(ScheduleResult result, Process process,
                                     int start, int completion, int waiting, int turnaround) {
        assertEquals(process, result.process());
        assertEquals(start, result.startTime());
        assertEquals(completion, result.completionTime());
        assertEquals(waiting, result.waitingTime());
        assertEquals(turnaround, result.turnaroundTime());
    }
}
