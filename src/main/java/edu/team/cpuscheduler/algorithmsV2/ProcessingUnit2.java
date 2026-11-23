import java.lang.reflect.Array;
//import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;


/*Non-preemptive priority scheduling simulator.
The process with the highest priority process gets the CPU first.
If two processes have equal priority, FCFS is use.
A lower priority number indicates a higher priority
*/

public class ProcessingUnit2 {

    public int puid; //the ID of this processing unit

    private ProcessingType processingType;

    private ProcessingUnit2 nextMainProcessingUnit = null;

    private ProcessingUnit2 supportingProcessingUnit = null;

    private int maxQueueSize;

    private double timeQuantum;

    public double totalStallTime = 0.0;

    private double mostRecentStallTime = 0.0;

    private double mostRecentCompletionTime = 0.0;

    private Map<String, Boolean> processingUnitConditions = new HashMap<>();

    private ArrayList<Package> preemptedPackages = new ArrayList<>();
    private ArrayList<Package> nonPreemptedPackages = new ArrayList<>();

    private enum ProcessingType{
        INPUT, INTERIOR, SUPPORTING, OUTPUT
    }

    public ProcessingUnit2(){}

    public ProcessingUnit2(int puid, int processingType, int maxQueueSize, double timeQuantum, Map<String, Boolean> processingUnitConditions){
        this.puid = puid;
        this.processingType = ProcessingType.values()[processingType];
        this.maxQueueSize = maxQueueSize;
        this.timeQuantum = timeQuantum;
        this.processingUnitConditions = processingUnitConditions;
    }

    public ProcessingType getProcessingType(){ return processingType; }

    public static class Package {
        int pid; // Process ID
        double arrivalTime; // Time when the process arrives
        double burstTime; // Total CPU time needed to complete the process
        double contemporaryBurstTime;
        int priority; // Priority Level (Lower the number = higher priority)

        // Variables to calculate
        double startTime; // Time when  the process first starts running
        double completionTime = 0.0; // Time when the process finishes execution

        double turnaroundTime; // Total time from arrival to completion (completionTime - arrivalTime)
        double waitingTime; // Total time spent waiting for the CPU (turnaroundTime - burstTime);

        /* Constructor to initialize a new process */
        public Package(int pid, double arrivalTime, double burstTime, int priority) {
            this.pid = pid;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            contemporaryBurstTime = burstTime;
            this.priority = priority;
        }

        public void resetBurstTime(){
            contemporaryBurstTime = burstTime;
        }
    }

    /* GanttEntry class tracks the start and end times of
    each process's execution, which helps build the Gantt chart. */
    public static class GanttEntry {
        int pid; // The process id
        double startTime; // Time when the process starts executing
        double endTime; // Time when the process ends execution


        public GanttEntry(int pid, double startTime, double endTime) {
            this.pid = pid;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public List<GanttEntry> schedule(Package[] packages) {

        int n = packages.length; // Total number of packages
        boolean[] executionCompleted = new boolean[n]; // Tracks which packages have finished execution
        int completedCount = 0; // Keeps count of packages that have finished execution
        double currentTime = 0; // Current time in the scheduling

        List<GanttEntry> gantt = new ArrayList<>(); // Stores each process's in the Gant chart entry


        while (completedCount < n) {
            int index = -1; // Means no process has been selected yet
            double bestPriority = Double.MAX_VALUE;
            double bestArrival = Double.MAX_VALUE;

            //Finds the highest-priority
            for (int i = 0; i < n; i++) {
                if (!executionCompleted[i] && packages[i].arrivalTime <= currentTime) {
                    boolean isBetter = (packages[i].priority < bestPriority) ||
                            (packages[i].priority == bestPriority && packages[i].arrivalTime < bestArrival);

                    if (isBetter) {
                        bestPriority = packages[i].priority;
                        bestArrival = packages[i].arrivalTime;
                        index = i; //Stores the index of the highest-priority found so far
                    }
                }
            }
            //Handles the CPU idle time
            if (index == -1) {
                //No process has arrived yet.
                gantt.add(new GanttEntry(-1, currentTime, currentTime + 1));
                currentTime++;
            } else {
                Package p = packages[index];
                p.startTime = currentTime;
                p.completionTime = currentTime + p.contemporaryBurstTime;

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
    public void findWaitingTime(Package[] packages) {
        for (Package p : packages) {
            p.waitingTime = p.turnaroundTime - p.contemporaryBurstTime;
        }
    }


    //Calculates the turnaround time for each process
    public void findTurnaroundTime(Package[] packages) {
        for (Package p : packages) {
            p.turnaroundTime = p.completionTime - p.arrivalTime;
        }
    }

    //Calculates the average time for each process
    public void findAverageTime(Package[] packages){
        double totalWT = 0;
        double totalTAT = 0;

        for (Package p: packages){
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }

        int n = packages.length;;
        System.out.printf("\nAverage Waiting Time   : %.2f%n", totalWT/n);
        System.out.printf("Average Turnaround Time: %.2f%n", totalTAT/n);
    }

    public void printGanttChart(List<GanttEntry> gantt){
        System.out.println("\n=== Gantt Chart ===");
        StringBuilder top = new StringBuilder();
        StringBuilder middle = new StringBuilder();
        StringBuilder bottom = new StringBuilder();

        for (GanttEntry e: gantt){
            String label = (e.pid == -1) ? " IDLE " : " P" + e.pid + " "; //-1?
            middle.append("|").append(label);
            top.append("+").append("-".repeat(label.length()));

            String timeStr = String.format("%.2f", e.startTime);
            bottom.append("|").append(timeStr);
            int padding = label.length() - timeStr.length();
            if(padding > 0 ) bottom.append(" ".repeat(padding));
        }
        top.append("+");
        bottom.append("|").append(String.format("%.2f", gantt.getLast().endTime));

        System.out.println(top);
        System.out.println(middle.append("|"));
        System.out.println(top);
        System.out.println(bottom);
    }
    public void printPackageTable(Package[] packages){

        System.out.println("\n=== Process Details ===");
        System.out.printf("%-5s %-10s %-10s %-10s %-10s %-10s %-10s %-10s%n",
                "PID", "Arrival", "Burst", "Priority", "Start", "Complete", "TAT", "WT");
        System.out.println("-".repeat(75));
        for (Package p : packages){
            System.out.printf("%-5d %-10.2f %-10.2f %-10d %-10.2f %-10.2f %-10.2f %-10.2f%n",
                    p.pid, p.arrivalTime, p.contemporaryBurstTime, p.priority,
                    p.startTime, p.completionTime, p.turnaroundTime, p.waitingTime);
        }
    }

    public void setNextMainProcessingUnit(ProcessingUnit2 procUnit){
        nextMainProcessingUnit = procUnit;
    }

    public void setSupportingProcessingUnit(ProcessingUnit2 procUnit){
        supportingProcessingUnit = procUnit;
    }


    public Package[] sortPackages(Package[] packages){
        /*
        Package[] sortedPackages = Arrays.copyOf(packages, packages.length);
        Arrays.sort(sortedPackages, Comparator.comparingDouble(p -> p.completionTime));
        return sortedPackages;
         */

        return Arrays.stream(packages)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(p -> p.completionTime))
                .toArray(Package[]::new);
    }



    //Run the simulation
    public void packageProcessingSetUp(Package[] packages){

        List<GanttEntry> rawGantt = schedule(packages);

        //need to sort packages in terms which was completed first

        switch(getProcessingType()){
            case ProcessingType.INPUT:

                int indexLastChecked = 0;

                //the below should be placed in a loop

                while (indexLastChecked < packages.length) {

                    Package[] queue = new Package[Math.min(maxQueueSize, packages.length - indexLastChecked)];
                    for (int i = 0; i < queue.length; i++) {
                        queue[i] = packages[indexLastChecked + i];
                    }

                    indexLastChecked += queue.length;

                    queue = sortPackages(queue);

                    processPackages(queue, rawGantt);
                }

                break;

            //INTERIOR and OUTPUT are separated to allow for specialized functionality in the future
            case ProcessingType.INTERIOR:

                processPackages(packages, rawGantt);

                break;

            case ProcessingType.OUTPUT:

                processPackages(packages, rawGantt);

                break;

            default:
                break;
        }
    }

    /*
    The point of this method is to see how much work this processing unit actually did
    before delegating it to other processing units, if it did so.

    The Gantt chart

     */
    public void processPackages(Package[] packages, List<GanttEntry> rawGantt){

        String procUnitType = "";

        switch (processingType.ordinal()){
            case 0:
                procUnitType = "INPUT";
                break;

            case 1:
                procUnitType = "INTERIOR";
                break;

            case 2:
                procUnitType = "SUPPORTING";
                break;

            case 3:
                procUnitType = "OUTPUT";
                break;

            default:
                break;
        }

        String progressHeader = "%s Processing Unit #%d is processing...' \n".formatted(procUnitType, puid);

        String finishedHeader = "%s Processing Unit #%d has finished processing!\n".formatted(procUnitType, puid);

        double earliestArrivalTime = Double.MAX_VALUE;

        //if preemption is allowed
        if (processingUnitConditions.get("Preemptive") || processingUnitConditions.get("RR")) {

            preemptedPackages.clear();
            nonPreemptedPackages.clear();

            //The raw completion time of each node needs to potentially be adjusted for preemption. The packages
            // that have a burst time over the time quantum will be "ejected" at the time quantum and the difference
            // between its burst time and the time quantum, and the accumulation of this very difference for other packages,
            // will be subtracted from the burst time of the nodes coming after it

            double timeBack = 0.0;

            for (int i = 0; i < packages.length; i++) {

                packages[i].completionTime -= timeBack;

                if (packages[i].contemporaryBurstTime - timeQuantum > 0) {
                    timeBack += packages[i].contemporaryBurstTime - timeQuantum;
                    packages[i].contemporaryBurstTime -= timeQuantum;
                    preemptedPackages.add(packages[i]);

                } else {
                    nonPreemptedPackages.add(packages[i]);
                }
            }
        }

        //if preemption is allowed and no Round-Robin then pass it off to the supporting processing unit
        if (supportingProcessingUnit != null && processingUnitConditions.get("Preemptive") && !processingUnitConditions.get("RR")) {

            int packageSize = 0;

            packageSize = preemptedPackages.size();

            //share what this processing unit did to progress the packages its sending off to the supporting
            // processing unit
            if (packageSize > 0) {

                System.out.println(progressHeader);

                Package[] localPackages = preemptedPackages.toArray(new Package[packageSize]);

                earliestArrivalTime = localPackages[0].arrivalTime;

                findTurnaroundTime(localPackages);
                findWaitingTime(localPackages);

                List<GanttEntry> gantt = new ArrayList<>();

                //the pid for the GanttEntry is the pid of the package that took up said GanttEntry
                for (GanttEntry e: rawGantt){

                    for (Package p: localPackages){
                        if (e.pid == p.pid){
                            gantt.add(new GanttEntry(p.pid, p.arrivalTime, p.completionTime));
                            break;
                        }
                    }
                }

                printGanttChart(gantt); //only show gantt chart of packages relevant to this branch
                printPackageTable(localPackages);
                findAverageTime(localPackages);

                resetPackageBurstTime(localPackages);

                //eventually do the below in threads
                //handoff the packages that got ejected due to Round-Robin

                //arrival time = completion time + stall time of next unit

                if (supportingProcessingUnit != null) {
                    updateArrivalTime(localPackages, supportingProcessingUnit);
                    System.out.printf("Passing aforementioned packages off to Supporting Processing Unit of ID #%d\n", supportingProcessingUnit.puid);
                    supportingProcessingUnit.packageProcessingSetUp(localPackages);
                }

                System.out.println(finishedHeader);
            }


            //share what this processing unit did to progress the packages its sending off to the next main
            // processing unit
            packageSize = nonPreemptedPackages.size();


            if (packageSize > 0) {

                System.out.println(progressHeader);

                Package[] localPackages = nonPreemptedPackages.toArray(new Package[packageSize]);

                earliestArrivalTime = Math.min(earliestArrivalTime, localPackages[0].arrivalTime);

                findTurnaroundTime(localPackages);
                findWaitingTime(localPackages);

                List<GanttEntry> gantt = new ArrayList<>();

                //the pid for the GanttEntry is the pid of the package that took up said GanttEntry
                for (GanttEntry e: rawGantt){

                    for (Package p: localPackages){
                        if (e.pid == p.pid){
                            gantt.add(new GanttEntry(p.pid, p.arrivalTime, p.completionTime));
                            break;
                        }
                    }
                }

                printGanttChart(gantt); //only show gantt chart of packages relevant to this branch
                printPackageTable(localPackages);
                findAverageTime(localPackages);

                resetPackageBurstTime(localPackages);

                nextMainProcessingUnit.packageProcessingSetUp(localPackages);

                //eventually do the below in threads
                //handoff the packages that were fully processed to the next main processing unit
                //arrival time = completion time + stall time of next unit

                if (nextMainProcessingUnit != null) {
                    updateArrivalTime(localPackages, nextMainProcessingUnit);
                    System.out.printf("Passing aforementioned packages off to Main Processing Unit of ID #%d\n", nextMainProcessingUnit.puid);
                    nextMainProcessingUnit.packageProcessingSetUp(localPackages);
                }

                System.out.println(finishedHeader);
            }

            //if processing unit can use Round-Robin...
        }else if (processingUnitConditions.get("RR")){

            Package[] localPackages = nonPreemptedPackages.toArray(new Package[nonPreemptedPackages.size()]);

            List<GanttEntry> gantt = new ArrayList<>();

            //display the packages that were completed during the initial time quantum period
            if (localPackages.length > 0) {

                findTurnaroundTime(localPackages);
                findWaitingTime(localPackages);

                //the pid for the GanttEntry is the pid of the package that took up said GanttEntry
                for (GanttEntry e : rawGantt) {

                    for (Package p : localPackages) {
                        if (e.pid == p.pid) {
                            gantt.add(new GanttEntry(p.pid, p.arrivalTime, p.completionTime));
                            break;
                        }
                    }
                }

                printGanttChart(gantt); //only show gantt chart of packages relevant to this branch
                printPackageTable(localPackages);
                findAverageTime(localPackages);
            }


            //If there are preempted packages then keep processing them until they are finished via
            // Round-Robin strategy
            localPackages = preemptedPackages.toArray(new Package[preemptedPackages.size()]);

            System.out.printf("Size of preempted array: %d\nTime Quantum: %f", localPackages.length, timeQuantum);

            while (localPackages.length > 0){

                gantt = schedule(localPackages);

                preemptedPackages.clear();

                //The raw completion time of each node needs to potentially be adjusted for preemption. The packages
                // that have a burst time over the time quantum will be "ejected" at the time quantum and the difference
                // between its burst time and the time quantum, and the accumulation of this very difference for other packages,
                // will be subtracted from the burst time of the nodes coming after it

                double timeBack = 0.0;

                for (int i = 0; i < localPackages.length; i++) {

                    localPackages[i].completionTime -= timeBack;

                    if (localPackages[i].contemporaryBurstTime - timeQuantum > 0) {
                        timeBack += localPackages[i].contemporaryBurstTime - timeQuantum;
                        localPackages[i].contemporaryBurstTime -= timeQuantum;
                        preemptedPackages.add(localPackages[i]);

                    } else {
                        nonPreemptedPackages.add(localPackages[i]);
                    }
                }

                //If there are still preempted packages, show their progress and repeat the process
                // of Round-Robin
                localPackages = preemptedPackages.toArray(new Package[preemptedPackages.size()]);

                if (localPackages.length == 0) break;

                findTurnaroundTime(localPackages);
                findWaitingTime(localPackages);

                System.out.println("New size of localPackages in RR:" + localPackages.length);

                List<GanttEntry> refinedGantt = new ArrayList<>();

                //the pid for the GanttEntry is the pid of the package that took up said GanttEntry
                for (GanttEntry e: gantt){

                    for (Package p: localPackages){
                        if (e.pid == p.pid){
                            refinedGantt.add(new GanttEntry(p.pid, p.arrivalTime, p.completionTime));
                            break;
                        }
                    }
                }

                printGanttChart(refinedGantt);
                printPackageTable(localPackages);
                findAverageTime(localPackages);
            }

            //show the full, completed Round-Robin Gantt chart report
            packages = nonPreemptedPackages.toArray(new Package[nonPreemptedPackages.size()]);

            findTurnaroundTime(packages);
            findWaitingTime(packages);

            System.out.println("Completed Round-Robin Report:" + packages.length);

            List<GanttEntry> finalRRGantt = new ArrayList<>();

            //the pid for the GanttEntry is the pid of the package that took up said GanttEntry
            for (GanttEntry e: gantt){

                for (Package p: packages){
                    if (e.pid == p.pid){
                        finalRRGantt.add(new GanttEntry(p.pid, p.arrivalTime, p.completionTime));
                        break;
                    }
                }
            }

            printGanttChart(finalRRGantt);
            printPackageTable(packages);
            findAverageTime(packages);

            earliestArrivalTime = packages[0].arrivalTime;

            resetPackageBurstTime(packages);

            if (nextMainProcessingUnit != null) {
                updateArrivalTime(packages, nextMainProcessingUnit);
                System.out.printf("Passing aforementioned packages off to Main Processing Unit of ID #%d\n", nextMainProcessingUnit.puid);
                nextMainProcessingUnit.packageProcessingSetUp(packages);
            }

            System.out.println(finishedHeader);

        }else{

            System.out.println(progressHeader);

            findTurnaroundTime(packages);
            findWaitingTime(packages);

            List<GanttEntry> gantt = new ArrayList<>();

            //the pid for the GanttEntry is the pid of the package that took up said GanttEntry
            for (GanttEntry e: rawGantt){

                for (Package p: packages){
                    if (e.pid == p.pid){
                        gantt.add(new GanttEntry(p.pid, p.arrivalTime, p.completionTime));
                        break;
                    }
                }
            }

            printGanttChart(gantt);
            printPackageTable(packages);
            findAverageTime(packages);

            earliestArrivalTime = packages[0].arrivalTime;

            resetPackageBurstTime(packages);

            if (nextMainProcessingUnit != null) {
                updateArrivalTime(packages, nextMainProcessingUnit);
                System.out.printf("Passing aforementioned packages off to Main Processing Unit of ID #%d\n", nextMainProcessingUnit.puid);
                nextMainProcessingUnit.packageProcessingSetUp(packages);
            }

            System.out.println(finishedHeader);
        }

        totalStallTime += mostRecentCompletionTime + earliestArrivalTime;
        mostRecentStallTime = mostRecentCompletionTime + earliestArrivalTime;
    }

    public void resetPackageBurstTime(Package[] packages){
        for (Package pack: packages){
            pack.resetBurstTime();
        }
    }

    public void updateArrivalTime(Package[] packages, ProcessingUnit2 nextUnit){
        for (Package pack: packages){
            pack.arrivalTime = pack.completionTime + nextUnit.mostRecentStallTime;
        }
    }
}