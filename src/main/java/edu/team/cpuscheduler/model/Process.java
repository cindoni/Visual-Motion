package edu.team.cpuscheduler.model;

/** Input for a CPU scheduling algorithm, with times in consistent units. */
public record Process(String id, int arrivalTime, int burstTime) {
    public Process {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Process id must not be blank");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time must not be negative");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be positive");
        }
    }
}
