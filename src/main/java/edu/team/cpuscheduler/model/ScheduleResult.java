package edu.team.cpuscheduler.model;

/** Timing information for one process after it has been scheduled. */
public record ScheduleResult(Process process, int startTime, int completionTime) {
    public int waitingTime() {
        return turnaroundTime() - process.burstTime();
    }

    public int turnaroundTime() {
        return completionTime - process.arrivalTime();
    }
}
