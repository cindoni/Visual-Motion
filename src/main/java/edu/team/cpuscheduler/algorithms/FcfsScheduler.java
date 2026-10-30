package edu.team.cpuscheduler.algorithms;

import edu.team.cpuscheduler.model.Process;
import edu.team.cpuscheduler.model.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** First-Come, First-Served scheduling for a single CPU. */
public final class FcfsScheduler {
    /** Returns results in execution order without changing the input list. */
    public List<ScheduleResult> schedule(List<Process> processes) {
        List<Process> ordered = new ArrayList<>(processes);
        // List.sort is stable: equal arrival times retain their input order.
        ordered.sort(Comparator.comparingInt(Process::arrivalTime));

        List<ScheduleResult> results = new ArrayList<>();
        int currentTime = 0;
        for (Process process : ordered) {
            // Advance past idle time if the next process has not arrived.
            int startTime = Math.max(currentTime, process.arrivalTime());
            int completionTime = Math.addExact(startTime, process.burstTime());
            results.add(new ScheduleResult(process, startTime, completionTime));
            currentTime = completionTime;
        }
        return results;
    }
}
