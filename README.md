# CPU Scheduling Simulator

A Java project that demonstrates common operating-system CPU scheduling algorithms and compares their results. The simulator will produce a Gantt chart and calculate waiting and turnaround times for each process.

## Team

| Member | Responsibility |
| --- | --- |
| Sidney | First-Come, First-Served (FCFS) |
| Jeremy | Shortest Job First (SJF) |
| Sarai | Priority Scheduling |
| Jonathan | Round Robin |

## Requirements

- Implement FCFS, SJF, Round Robin, and Priority Scheduling.
- Display a Gantt chart for each schedule.
- Calculate process waiting time and turnaround time.
- Compare algorithm performance.
- Include one real-world problem solved using operating-system scheduling concepts.
- Test edge cases and validate calculations.
- Complete a final project report and prepare the final submission.

## Roadmap and due dates

| Milestone | Target date |
| --- | --- |
| Complete individual scheduling algorithms | Wednesday, October 28, 2026 |
| Integrate algorithms; add Gantt chart and metrics | After algorithm completion |
| Add performance comparison and real-world example | Before final submission |
| Validate edge cases; complete report and submission | November 30, 2026 |
| Final project due | November 30, 2026 |

The October 28 milestone covers the four assigned algorithm implementations. The remaining work is tracked as follow-up project issues.

## Requested GitHub issue breakdown

The Phase 1 parent issue is **Phase 1: Implement CPU Scheduling Algorithms**. Its four algorithm sub-issues are assigned and due Wednesday, October 28, 2026:

| Issue | Owner | Due date |
| --- | --- | --- |
| Implement FCFS in Java | Sidney | October 28, 2026 |
| Implement SJF in Java | Jeremy | October 28, 2026 |
| Implement Priority Scheduling in Java | Sarai | October 28, 2026 |
| Implement Round Robin in Java | Jonathan | October 28, 2026 |

Follow-up issues:

- Integrate all scheduling algorithms
- Implement Gantt chart output
- Calculate waiting time
- Calculate turnaround time
- Compare algorithm performance
- Implement real-world scheduling example
- Test edge cases and validate calculations
- Complete final project report
- Prepare final submission

The follow-up work should be completed by the final project deadline, November 30, 2026.

## Project structure

```text
src/
├── main/java/edu/team/cpuscheduler/
│   ├── algorithms/  # Scheduling algorithm implementations
│   ├── model/       # Process and schedule data types
│   └── output/      # Gantt chart and metrics output
└── test/java/edu/team/cpuscheduler/
```

## Run

Prerequisites: JDK 17 or newer and Maven 3.8 or newer.

Build and run the current project scaffold from the repository root:

```sh
mvn package
java -cp target/classes edu.team.cpuscheduler.Main
```

The launcher is only a project placeholder; the scheduling algorithms have not been implemented yet.

## GitHub project work

Use the feature branches listed below and follow the review workflow in [CONTRIBUTING.md](CONTRIBUTING.md).

- `feature/fcfs`
- `feature/sjf`
- `feature/priority`
- `feature/round-robin`
- `feature/gantt-chart`
- `feature/metrics`
- `test/integration`

Create the issue breakdown above in the repository issue tracker. The repository currently has an existing issue, **Individual Algorithm Implementation**, that covers the algorithm assignments but is not the requested Phase 1 parent issue.