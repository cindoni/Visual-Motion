//SQSPU = Single Queue, Single Processing Unit
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.nio.file.Path;

public class SQSPU {

    boolean connectionSet = false;

    int maxQueueSize = -1;
    boolean priority;
    boolean preemptive;

    int nodesToSpawn;
    double[] chosenSpawnTimes;
    double[] schedulingMetrics;
    int[] priorities;

    Random rand = new Random();


    ProcessingUnit[] inputProcUnits;
    ProcessingUnit[] interiorProcUnits;
    ProcessingUnit[] supportingProcUnits;
    ProcessingUnit[] outputProcUnits;

    ProcessingUnit.Package[] packages;

    QueuingConfiguration qc = new QueuingConfiguration();

    Map<String, Boolean> processingUnitConditions;

    private String folderPath = "";

    private String parameterString = "";

    public SQSPU(){;}

    public void primeSimulation(int nodesToSpawn,
                                double[] schedulingMetrics,
                                double arrivalTimeRange,
                                int configurationType,
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
            connectionSet = true;
        }

        DistributionGeneration distGen = new DistributionGeneration();
        double[] chosenSpawnTimes = distGen.interarrivalTimeDistribution(nodesToSpawn, arrivalTimeRange, nodeProbabilities, nodeDistribution);

        int[] priorities = distGen.priorityDistribution(nodesToSpawn, maxPriorityClasses, priorityProbabilities, priorityDistribution);

        this.nodesToSpawn = nodesToSpawn;
        this.chosenSpawnTimes = chosenSpawnTimes;
        this.schedulingMetrics = schedulingMetrics;
        this.priorities = priorities;

        setProcessingUnitConditions(schedulingMetrics);

        switch (configurationType){
            case 1:
                qc.setTrivialConfiguration(maxQueueSize, schedulingMetrics, processingUnitConditions);
                break;

            case 2:
                qc.setSimpleConfiguration(maxQueueSize, schedulingMetrics, processingUnitConditions);
                break;

            case 3:
                qc.setComplexConfiguration(maxQueueSize, schedulingMetrics, processingUnitConditions);
                break;

            default:
                break;
        }

        inputProcUnits = qc.inputProcUnits;
        interiorProcUnits = qc.interiorProcUnits;
        supportingProcUnits = qc.supportingProcUnits;
        outputProcUnits = qc.outputProcUnits;

        packages = new ProcessingUnit.Package[nodesToSpawn];

        double arrivalTime = 0.0;
        double processingTime = 0.0;

        float min = 0;
        float max = 0;

        ProcessingUnit fillerProcUnit = new ProcessingUnit();

        for (int i = 0; i<nodesToSpawn;i++){
            arrivalTime += chosenSpawnTimes[i];

            switch(priorities[i]){
                case 1:
                    min = 3;
                    max = 9;
                    processingTime = min + rand.nextFloat() * (max-min);

                    break;

                case 2:
                    min = 10;
                    max = 16;
                    processingTime = min + rand.nextFloat() * (max-min);

                    break;

                case 3:
                    min = 17;
                    max = 23;
                    processingTime = min + rand.nextFloat() * (max-min);

                    break;

                default:
                    break;
            }

            packages[i] = new ProcessingUnit.Package(i, arrivalTime, processingTime, priorities[i]);
        }
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

    public enum Configurations{
        Trivial, Simple, Complex
    }

    public void run() throws IOException {

        qc.writeConnections();

        //start sim
        System.out.println("Simulation State: Starting simulation...\n");

        inputProcUnits[0].packageProcessingSetUp(packages);

        System.out.println("Simulation State: Finished simulation.");

        saveSimResults();


        /*
        long simStartTime = System.nanoTime();
        long intervalStartTime = simStartTime;

        int i = 0;

        System.out.println("Simulation State: Enqueuing and processing nodes\n");
        while (i < nodesToSpawn){
            if (System.nanoTime() - intervalStartTime >= (long) chosenSpawnTimes[i] * 1_000_000_000) {
                System.out.printf("Node %d inter-arrival time: %f, ", i+1, chosenSpawnTimes[i]);
                long enqueuedTime = System.nanoTime() - simStartTime;
                System.out.printf("Attempting to enqueue node %d of %d at time %.10f seconds!\n", i + 1, nodesToSpawn, (double) enqueuedTime / 1_000_000_000.0);
                queue.enqueue(i, priorities[i], null, null, false, chosenSpawnTimes[i]);
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
            /
            System.out.printf("queue empty? %b, queue size: %d, is processing? %b\n\n", queue.isEmpty(), queue.getSize(), processingUnit.isProcessing());
            if (previousQueueSize != queue.getSize()){
                System.out.printf("Simulation State: %d nodes remaining in the system\n\n", queue.getSize());
                previousQueueSize = queue.getSize();
            }
            /
        }

        //record end of sim
        long simEndTime = System.nanoTime();
        System.out.flush();
        System.out.printf("Simulation State: Finished after %.10f\n", (double) (simEndTime - simStartTime) / 1_000_000_000.0);
        return;
        */
    }

    public void setMaxQueueSize(int maxQueueSize){
        this.maxQueueSize = maxQueueSize;
    }

    public void setProcessingUnitConditions(double[] schedulingMetrics){
        //FCFS = 0, SJF = 1, HP = 2, RR = 3

        processingUnitConditions = new HashMap<>();
        processingUnitConditions.put("FCFS", false);
        processingUnitConditions.put("SJF", false);
        processingUnitConditions.put("PR", false);
        processingUnitConditions.put("RR", false);

        boolean preemptiveCondition = schedulingMetrics[4] != 0.0;
        processingUnitConditions.put("Preemptive", preemptiveCondition);

        if (schedulingMetrics[0] == 1.0){
            processingUnitConditions.replace("FCFS", true);
        }

        if (schedulingMetrics[1] == 1.0){
            processingUnitConditions.replace("SJF", true);
        }

        if (schedulingMetrics[2] == 1.0){
            processingUnitConditions.replace("PR", true);
        }

        if (schedulingMetrics[3] == 1.0){
            processingUnitConditions.replace("RR", true);
        }
    }

    public int getMaxQueueSize(){
        return maxQueueSize;
    }

    public boolean getPreemptive(double[] schedulingMetrics){
        return schedulingMetrics[4] != 0.0;
    }

    public void setParameterString(String parameterString){
        this.parameterString = parameterString;
    }

    public void saveSimResults() throws IOException {
        int i = 1;

        Path folder = Path.of("results");

        if (!Files.isDirectory(folder)){
            Files.createDirectory((folder));
        }

        String innerFolderName = "";

        while (true){
            innerFolderName = "sim_%d".formatted(i);

            if (!Files.isDirectory(Path.of("results/%s".formatted(innerFolderName)))){
                Files.createDirectory(Path.of("results/%s".formatted(innerFolderName)));
                break;
            }

            i++;
        }

        String folderPath = "results/%s".formatted(innerFolderName);

        Path filePath = Path.of(folderPath+"/parameter_set_up.txt");
        Files.writeString(filePath, parameterString);

        for (ProcessingUnit procUnit: inputProcUnits){
            procUnit.writeToFile(folderPath);
        }

        for (ProcessingUnit procUnit: interiorProcUnits){
            procUnit.writeToFile(folderPath);
        }

        for (ProcessingUnit procUnit: supportingProcUnits){
            procUnit.writeToFile(folderPath);
        }

        for (ProcessingUnit procUnit: outputProcUnits){
            procUnit.writeToFile(folderPath);
        }
    }
}