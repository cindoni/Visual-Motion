//SQSPU = Single Queue, Single Processing Unit
import java.util.Arrays;

public class SQSPU {

    Queue queue = new Queue(0);
    ProcessingUnit processingUnit = new ProcessingUnit(0);
    boolean connectionSet = false;

    int maxQueueSize = -1;
    boolean priority;
    boolean preemptive;

    int nodesToSpawn;
    double[] chosenSpawnTimes;
    double[] schedulingMetrics;
    int[] priorities;

    public SQSPU(){;}

    public void setConnections(){
        processingUnit.connectInputQueue(queue);
        return;
    }

    public void primeSimulation(int nodesToSpawn,
                                double[] schedulingMetrics,
                                double arrivalTimeRange,
                                double[] nodeProbabilities,
                                double[] priorityProbabilities,
                                NodeDistribution nodeDistribution,
                                int maxPriorityClasses,
                                PriorityDistribution priorityDistribution
    ) {

        if (nodeDistribution == null) {
            throw new IllegalArgumentException("Customer distribution cannot be null. Must be one of: "
                    + Arrays.toString(NodeDistribution.values()));
        }
        if (priorityDistribution == null) {
            throw new IllegalArgumentException("Priority distribution cannot be null. Must be one of: "
                    + Arrays.toString(PriorityDistribution.values()));
        }

        if (!(connectionSet)){
            setConnections();
            connectionSet = true;
        }

        DistributionGeneration distGen = new DistributionGeneration();
        double[] chosenSpawnTimes = distGen.interarrivalTimeDistribution(nodesToSpawn, arrivalTimeRange, nodeProbabilities, nodeDistribution);

        int[] priorities = distGen.priorityDistribution(nodesToSpawn, maxPriorityClasses, priorityProbabilities, priorityDistribution);

        this.nodesToSpawn = nodesToSpawn;
        this.chosenSpawnTimes = chosenSpawnTimes;
        this.schedulingMetrics = schedulingMetrics;
        queue.setSchedulingType(schedulingMetrics);
        this.priorities = priorities;
    }

    public enum NodeDistribution {
        UNIFORM, NORMAL, BIMODAL, GEOMETRIC, CUSTOM
    }

    public enum PriorityDistribution{
        NONE, UNIFORM, NORMAL, BIMODAL, GEOMETRIC, CUSTOM
    }

    public enum SchedulingType{
        FCFS, SJF, HP, RR
    }

    public void run(){

        //start sim
        System.out.println("Simulation State: Starting simulation...\n");
        long simStartTime = System.nanoTime();
        long intervalStartTime = simStartTime;

        int i = 0;

        System.out.println("Simulation State: Enqueuing and processing nodes\n");
        while (i < nodesToSpawn){
            if (System.nanoTime() - intervalStartTime >= (long) chosenSpawnTimes[i] * 1_000_000_000) {
                System.out.printf("Node %d inter-arrival time: %f, ", i+1, chosenSpawnTimes[i]);
                long enqueuedTime = System.nanoTime() - simStartTime;
                System.out.printf("Attempting to enqueue node %d of %d at time %.10f seconds!\n", i + 1, nodesToSpawn, (double) enqueuedTime / 1_000_000_000.0);
                queue.enqueue(i, priorities[i], null, null, false);
                i++;
                intervalStartTime = System.nanoTime();
            }
        }

        //nodes have finished spawning in
        //wait until sim is finished
        //int previousQueueSize = queue.getSize();
        System.out.println("Simulation State: Queuing arrival time ended. Strictly processing nodes");
        while (!(queue.isEmpty()) || processingUnit.isProcessing() || queue.getActiveTaskCount() > 0){
            Thread.onSpinWait();
            /*
            System.out.printf("queue empty? %b, queue size: %d, is processing? %b\n\n", queue.isEmpty(), queue.getSize(), processingUnit.isProcessing());
            if (previousQueueSize != queue.getSize()){
                System.out.printf("Simulation State: %d nodes remaining in the system\n\n", queue.getSize());
                previousQueueSize = queue.getSize();
            }
            */
        }

        //record end of sim
        long simEndTime = System.nanoTime();
        System.out.flush();
        System.out.printf("Simulation State: Finished after %.10f\n", (double) (simEndTime - simStartTime) / 1_000_000_000.0);
        return;
    }

    public void setMaxQueueSize(int maxQueueSize){
        queue.setMaxSize(maxQueueSize);
    }

    public void setPriority(boolean priority){
        queue.setPriority(priority);
        processingUnit.setPriority(priority);
    }

    public void setPreemptive(boolean preemptive){
        queue.setPreemptive(preemptive);
        processingUnit.setPreemptive(preemptive);
    }

    public int getMaxQueueSize(){
        return queue.getMaxSize();
    }

    public boolean getPriority(){
        return queue.getPriority();
    }

    public boolean getPreemptive(){
        return queue.getPreemptive();
    }
}