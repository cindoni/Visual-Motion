package edu.team.cpuscheduler.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/*Non-preemptive priority scheduling simulator.
The process with the highest priority process gets the CPU first.
If two processes have equal priority, FCFS is use.
A lower priority number indicates a higher priority
*/

public class PriorityScheduling {

    static class Process {
        int pid; // Process ID
        int arrivalTime; // Time when the process arrives
        int burstTime; // Total CPU time needed to complete the process
        int priority; // Priority Level (Lower the number = higher priority)

        // Variables to calculate
        int startTime; // Time when  the process first starts running
        int completionTime; // Time when the process finishes execution

        int turnaroundTime; // Total time from arrival to completion (completionTime - arrivalTime)
        int waitingTime; // Total time spent waiting for the CPU (turnaroundTime - burstTime);

        /* Constructor to initialize a new process */
        public Process(int pid, int arrivalTime, int burstTime, int priority) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
        }
    }

    /* GanttEntry class tracks the start and end times of
    each process's execution, which helps build the Gantt chart. */
    static class GanttEntry {
        int pid; // The process id
        int startTime; // Time when the process starts executing
        int endTime; // Time when the process ends execution


        public GanttEntry(int pid, int startTime, int endTime) {
            this.pid = pid;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public static List<GanttEntry> schedule(Process[] processes) {

        int n = processes.length; // Total number of processes
        boolean[] executionCompleted = new boolean[n]; // Tracks which processes have finished execution
        int completedCount = 0; // Keeps count of processes that have finished execution
        int currentTime = 0; // Current time in the scheduling 

        List<GanttEntry> gantt = new ArrayList<>(); // Stores each process's in the Gant chart entry


        while (completedCount < n) {
            int index = -1; // Means no process has been selected yet
            int bestPriority = Integer.MAX_VALUE;
            int bestArrival = Integer.MAX_VALUE;


            //Finds the highest-priority
            for (int i = 0; i < n; i++) {
                if (!executionCompleted[i] && processes[i].arrivalTime <= currentTime) {
                    boolean isBetter = (processes[i].priority < bestPriority) ||
                            (processes[i].priority == bestPriority && processes[i].arrivalTime < bestArrival);

                    if (isBetter) {
                        bestPriority = processes[i].priority;
                        bestArrival = processes[i].arrivalTime;
                        index = i; //Stores the index of the highest-priority found so far
                    }
                }
            }
            //Handles the CPU idle time
            if (index == -1) {
                //No process has arrived yet.
                gantt.add(new GanttEntry(0, currentTime, currentTime + 1));
                currentTime++;
            } else {
                Process p = processes[index];
                p.startTime = currentTime;
                p.completionTime = currentTime + p.burstTime;

                gantt.add(new GanttEntry(p.pid, currentTime, p.completionTime));
                currentTime = p.completionTime;
                executionCompleted[index] = true;
                completedCount++;
            }
        }
        return gantt;
    }

   // In non-preemptive the response time is mathematically identical to waiting time
   //Calculates the waiting time for each process
    public static void findWaitingTime(Process[] processes) {
        for (Process p : processes) {
            p.waitingTime = p.turnaroundTime - p.burstTime;
        }
    }


    //Calculates the turnaround time for each process
    public static void findTurnaroundTime(Process[] processes) {
        for (Process p : processes) {
        p.turnaroundTime = p.completionTime - p.arrivalTime;
        }
    }

    //Calculates the average time for each process
    public static void findAverageTime(Process[] processes){
        double totalWT = 0;
        double totalTAT = 0;

        for (Process p: processes){
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        int n = processes.length;;
        System.out.printf("\nAverage Waiting Time   : %.2f%n", totalWT/n);
        System.out.printf("Average Turnaround Time: %.2f%n", totalTAT/n);
    }

    public static void printGanttChart(List<GanttEntry> gantt){
        System.out.println("\n=== Gantt Chart ===");
        StringBuilder top = new StringBuilder();
        StringBuilder middle = new StringBuilder();
        StringBuilder bottom = new StringBuilder();

        for (GanttEntry e: gantt){
            String label = (e.pid == 0) ? " IDLE " : " P" + e.pid + " "; //-1?
            middle.append("|").append(label);
            top.append("+").append("-".repeat(label.length()));

            String timeStr = String.valueOf(e.startTime);
            bottom.append("|").append(timeStr);
            int padding = label.length() - timeStr.length();
            if(padding > 0 ) bottom.append(" ".repeat(padding));
        }
        top.append("+");
        bottom.append("|").append(gantt.get(gantt.size()-1).endTime);

        System.out.println(top);
        System.out.println(middle.append("|"));
        System.out.println(top);
        System.out.println(bottom);
    }
    public static void printProcessTable(Process[] processes){

    System.out.println("\n=== Process Details ===");
    System.out.printf("%-5s %-10s %-10s %-10s %-10s %-10s %-10s %-10s%n",
                "PID", "Arrival", "Burst", "Priority", "Start", "Complete", "TAT", "WT");
    System.out.println("-".repeat(75));
    for (Process p : processes){
            System.out.printf("%-5d %-10d %-10d %-10d %-10d %-10d %-10d %-10d%n",
                    p.pid, p.arrivalTime, p.burstTime, p.priority,
                    p.startTime, p.completionTime, p.turnaroundTime, p.waitingTime);
        }
    }

    //Run the simulation
    public static void runSimulation(Process[] processes){

        List<GanttEntry> gantt = schedule(processes);
        findTurnaroundTime(processes);
        findWaitingTime(processes);

        printGanttChart(gantt);
        printProcessTable(processes);
        findAverageTime(processes);
    }




    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in); //Creates scanner object
        int option;//Declare variable to hold menu option

        //Main loop - repeats until user selects exit program(option 2)
        do{

            System.out.println("WAREHOUSE PACKAGING - PRIORITY SCHEDULER");
            System.out.println("=========================================");
            System.out.println("Priority Mapping");
            System.out.println("1=Overnight/Urgent");
            System.out.println("2=Express");
            System.out.println("3=Standard");
            System.out.println("=========================================");
            System.out.println();
            System.out.println("--------MAIN MENU--------");
            System.out.println("1 - Enter Package Data");
            System.out.println("2 - Exit Program");
            System.out.print("Enter option number (1-2): "); //Prompts user for menu option

            option = sc.nextInt();//Read user input and stores it in 'option'

            //Handles user option
            switch (option){
                case 1:
                    System.out.print("\nEnter the number of packages: ");//Prompt user to enter an int
                    int num_processes = sc.nextInt();//Read integer input and stores it in 'num_processes'
                    Process[] processes = new Process[num_processes];//Creates an array to hold 'num_processes'
                    System.out.println();//Single blank line

                    //loop will prompt user to enter the data for each package and stores it in the array
                    for (int i = 0; i < num_processes; i++) {
                        System.out.println("Enter the data for Package #" + (i + 1) + ":");

                        System.out.print("Enter the Process ID (PID): ");
                        int pid = sc.nextInt();

                        System.out.print("Enter the Arrival Time: ");
                        int arrival_time = sc.nextInt();

                        System.out.print("Enter the Packaging Burst Time: ");
                        int burst_time = sc.nextInt();

                        System.out.print("Enter Priority (1=Overnight/Urgent, 2=Express, 3=Standard): ");
                        int priority = sc.nextInt();

                        processes[i] = new Process(pid, arrival_time, burst_time, priority);
                        System.out.println();
                    }

                    System.out.println("Running Priority Scheduling...\n"); //Prints message to the console
                    runSimulation(processes);//Calls the run simulation method 

                    break;
                case 2:
                    //Exits the program
                    System.out.println("\nExiting program.");//Prints a message indicating the program is exiting
                    break;
                default:
                    //Display if invalid input is enter
                    System.out.println("\nInvalid Option, Please try again.");
            }
            System.out.println();//Prints empty line for spacing in the console

        }while (option != 2);

    }
}
