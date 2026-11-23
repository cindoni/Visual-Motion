import java.util.Scanner;

/*

It's looking like each processing station will have its own Gantt Chart


For example, via a simple model
    An input station / processing unit will have a Gantt Chart for its array of incoming processes/packages
    These processes will be input as FCFS
        If while a package is being processed that the array after said package has varying
        priority levels then those packages will be rearranged according to priority for as long as those
        packages arrive within the completion time of the package currently being processed

    If these packages where completed within time quantum, then they are output to the next main processing unit
    Else, the packages are output to the secondary processing unit which connects to a processing unit that would
    allow the packages to continue through the system
        if a

    arrival time = stall time + previous completion time
    stall time = completion time of a unit xi - completion time of a unit xi+1
    if negative, then there is stall time. Add it to the stall time of the processing units xi+1 on backward

    Steps for integration concerning starting processing units
    1) process your packages
    2) What other packages arrive within the completion time of the first package being processed / the basis package
    3) What packages actually fit within our queue size? (if queue size = 3 and 5 packages arrived then the queue would entail x1 - x3)
    4) Use the completion time of the last package in our queue and the first package right outside the queue to find the stall time for the receiving processing unit
        If the stall time is negative, add the magnitude of the stall time to the current processsing unit
    5) With the current set of accepted packages (the one being "processed" and the ones in the queue), produce a Gantt Chart and show the stall time given the input packages
    6) Send this array of packages from the Gantt Chart on to the next processing unit
    7) The completion times of each package will be subtracted from by the time quantum.
        If the result is a nonzero positive number then the package will be placed in a separate array
        that will be offhanded to the current processing unit's supporting / secondary processing unit
        (if the current processing unit is not already supporting)
        Else, the packages will be passed onto the next major processing unit

    8) The completion time of the last package in the queue will be looked at to see how many packages arrived within its completion
    9) The package immediately after the last package in the previous queue will become the basis package
    10) Repeat steps 2 through 9 as needed

    Steps for integration concerning non-supporting interior processing units
    1) If you have only one input processing unit then accept its processes and process it yourself and either pass it to the next
        processing unit or a supporting processing unit if need be
       Else:
            a) sort elements from all processing units according to completion time
            b) Complete packages according to holding size (queue size + processing space)
            c) Find the stall time between the last element in the queue and the first element that was unable to be processed
               and add it to current processing unit and backpropagate it
            d) Repeat steps b to c as needed

    Everything needs to be processed in order from the last machine in the pipeline to the first

    When there is stall time at a processing unit then backtrace.
    If while backtracking there is a processing unit that has multiple outputs, then follow each branch to the leaf node
    and begin the backtracing process again at said leaf processing unit
    If a proocessing unit which points in multiple directions is meet with many different stalling times, choose the least stall time
    to apply to said processing unit and continue backpropagating
    Backpropagate until it is no longer possible

    Note to self: backpropagation would happen without needed to purposely to specific algorithm
                  due to how everything is set up in "ProcessingUnit2.java"
 */

public class Main {

    public static void main(String[] args) {

        //PriorityScheduling ps = new PriorityScheduling();
        //ps.method1();

        Scanner sc = new Scanner(System.in);

        SQSPU sqspu = new SQSPU();

        String menu1 = """
                       Menu 1
                       1. Set max queue size
                       2. Set nodes to spawn
                       3. Set arrival time range
                       4. Set node arrival distribution type
                       5. Set scheduling type
                       6. Set priority assignment distribution type
                       7. Set preemption
                       8. Set queueing configuration
                       9. View simulation parameters
                       10. Prime simulation
                       11. Run simulation
                       12. Exit
                       """;

        boolean sentinel = false;

        int nodesToSpawn = -1;
        double arrivalTimeRange = -1;
        double[] schedulingMetrics = new double[5];
        schedulingMetrics[4] = 0;
        int priorityClasses = 3;
        int preemptionSet = -1;
        int configurationType = 0;

        int indexOfNodeDistributionChoice = -1;
        int indexOfProbabilityDistributionChoice = -1;

        int sizeProbabilityArray = 4;
        double[] nodeDistributionProbabilities = new double[sizeProbabilityArray];
        double[] priorityDistributionProbabilities = new double[sizeProbabilityArray];

        for (int i = 0; i < sizeProbabilityArray; i++){
            nodeDistributionProbabilities[i]=-1;
            priorityDistributionProbabilities[i]=-1;
        }

        while (!(sentinel)){

            System.out.println(menu1);
            System.out.print("User input: ");
            String choice = sc.nextLine();
            System.out.println();

            switch (choice){
                case "1": //set max queue size

                    while (true) {

                        try {
                            System.out.print("Enter the maximum queue size: ");
                            String input = sc.nextLine();
                            System.out.println();
                            int maxQueueSize = Integer.parseInt(input);
                            if (maxQueueSize < 1){
                                throw new NumberFormatException();
                            }
                            sqspu.setMaxQueueSize(maxQueueSize);
                            break;

                        }catch (NumberFormatException e){
                            System.out.println("Please enter an integer value equal to or greater than 1!");
                        }
                    }
                    break;

                case "2": //set node spawning metrics

                    while (true) {

                        try {
                            System.out.print("Enter the # of nodes to spawn: ");
                            String input = sc.nextLine();
                            System.out.println();
                            nodesToSpawn = Integer.parseInt(input);
                            break;

                        }catch (NumberFormatException e){
                            System.out.println("Please enter an integer value!");
                        }
                    }
                    break;

                case "3": //set node arrival time range

                    while (true) {

                        try {
                            System.out.print("Enter an arrival time range greater than 0: ");
                            String input = sc.nextLine();
                            System.out.println();
                            arrivalTimeRange = Double.parseDouble(input);

                            if (arrivalTimeRange <= 0){
                                throw new NumberFormatException();
                            }
                            break;

                        }catch (NumberFormatException e){
                            System.out.println("Please enter a numeric value greater than 0!");
                        }
                    }
                    break;

                case "4": //set node arrival distribution type
                    indexOfNodeDistributionChoice = distributionChoices("node");

                    //shift the index choice down by 1 at account for indexing from the menu
                    if (indexOfNodeDistributionChoice != -1) indexOfNodeDistributionChoice--;

                    if (indexOfNodeDistributionChoice > 1 && indexOfNodeDistributionChoice != 5) {
                        nodeDistributionProbabilities = setProbabilities(indexOfNodeDistributionChoice);
                    }
                    break;

                case "5": //set scheduling type
                    schedulingMetrics = chooseSchedulingType();
                    break;


                case "6": //set priority assignment distribution type
                    indexOfProbabilityDistributionChoice = distributionChoices("priority");

                    if (indexOfProbabilityDistributionChoice == -1 && priorityClasses == 0){
                        indexOfProbabilityDistributionChoice = 0;
                    }

                    //set probability distribution type for inputs that require user-set probabilities
                    if (indexOfProbabilityDistributionChoice > 1 && indexOfProbabilityDistributionChoice != 5) {
                        priorityDistributionProbabilities = setProbabilities(indexOfProbabilityDistributionChoice);
                    }
                    break;

                case "7": //set preemption
                    while (true) {

                        try {
                            System.out.print("Is simulation preemptive? (Y) or (N): ");
                            String input = sc.nextLine();
                            System.out.println();

                            if (input.equals("Y") || input.equals("y")){
                                sqspu.setPreemptive(true);

                                boolean success = false;

                                while (true) {

                                    try {
                                        System.out.print("Enter a value for the time quantum: ");
                                        String input2 = sc.nextLine();
                                        System.out.println();
                                        schedulingMetrics[4] = Double.parseDouble(input2);

                                        if (schedulingMetrics[4] <= 0){
                                            throw new NumberFormatException();
                                        }

                                        success = true;

                                        break;

                                    }catch (NumberFormatException e){
                                        System.out.println("Please enter a numeric value greater than 0!");
                                    }
                                }

                                if (success) break;

                            }else if (input.equals("N") || input.equals("n")){
                                sqspu.setPreemptive(false);

                                schedulingMetrics[4] = (schedulingMetrics[3] == 0) ? 0 : schedulingMetrics[4];
                            }else{
                                throw new Exception();
                            }
                            break;

                        }catch (Exception e){
                            System.out.println("Please enter the letters Y or N!");
                        }
                    }
                    preemptionSet = 0;
                    break;

                case "8":

                    String subMenu1 = """
                          Choose a configuration
                          1. Trivial (only one processing unit)
                          2. Simple (one input, one interior, one output processing units)
                          """;

                    while (true) {

                        try {
                            System.out.printf(subMenu1);
                            System.out.print("Enter configuration #: ");
                            String input = sc.nextLine();
                            System.out.println();
                            configurationType = Integer.parseInt(input);

                            if (configurationType < 1 || configurationType > 2){
                                throw new NumberFormatException();
                            }
                            break;

                        }catch (NumberFormatException e){
                            System.out.println("Please choose from the given options!");
                        }
                    }

                    break;

                case "9": //view simulation parameter choices

                    System.out.println("Parameter Choices");
                    System.out.println("---------------------------------------------------------------------------------------------------");
                    System.out.printf("Max queue size: %d\n", sqspu.getMaxQueueSize());
                    System.out.println("Preemptive scheduling: " + ((preemptionSet == -1) ? null: sqspu.getPreemptive()));

                    String schedulingTypeString = "";

                    for (int i = 0; i < 4; i++) {
                        if (schedulingMetrics[i] != 0) {
                            if (!schedulingTypeString.isEmpty()) {
                                schedulingTypeString += ", ";
                            }
                            schedulingTypeString += SQSPU.SchedulingType.values()[i];
                        }
                    }

                    System.out.printf("Scheduling type(s): %s\n", schedulingTypeString);


                    if (schedulingMetrics[4]!=-1){
                        System.out.println("Time Quantum: " + schedulingMetrics[4]);
                    }


                    System.out.println("Configuration Type: " + ((configurationType == 0) ? "NONE" : SQSPU.Configurations.values()[configurationType-1].toString()));

                    //view node #
                    //# nodes spawning within #hrs, minutes, seconds
                    System.out.printf("\n%d nodes spawning within %.10f time\n", nodesToSpawn, arrivalTimeRange);
                    //Node Inter-arrival Distribution: #
                    System.out.println("Node Inter-arrival Distribution: " + ((indexOfNodeDistributionChoice != -1) ? SQSPU.NodeDistribution.values()[indexOfNodeDistributionChoice] : null));
                    //mu1 sigma1 mu2 sigma2
                    double[] npd = nodeDistributionProbabilities;
                    System.out.println("mu1: " + ((npd[0] != -1) ? npd[0] : null) + " sigma1: " + ((npd[1] != -1) ? npd[1] : null) + " mu2: " + ((npd[2] != -1) ? npd[2] : null) + " sigma2: " + ((npd[3] != -1) ? npd[3] : null));

                    //Priority Class:
                    System.out.printf("\nPriority Classes: %d\n", priorityClasses);
                    //Priority Assignment Distribution: #
                    System.out.println("Priority Assignment Distribution: " + ((indexOfProbabilityDistributionChoice != -1) ? SQSPU.PriorityDistribution.values()[indexOfProbabilityDistributionChoice] : null));
                    //mu1 sigma1 mu2 sigma2
                    double[] pdp = priorityDistributionProbabilities;
                    System.out.println("mu1: " + ((pdp[0] != -1) ? pdp[0] : null) + " sigma1: " + ((pdp[1] != -1) ? pdp[1] : null) + " mu2: " + ((pdp[2] != -1) ? pdp[2] : null) + " sigma2: " + ((pdp[3] != -1) ? pdp[3] : null));
                    System.out.println("---------------------------------------------------------------------------------------------------");
                    break;

                case "10": //prime simulation
                    int approval = checkRequirements(sqspu,
                                                     nodesToSpawn,
                                                     schedulingMetrics,
                                                     arrivalTimeRange,
                                                     priorityClasses,
                                                     preemptionSet,
                                                     configurationType,
                                                     indexOfNodeDistributionChoice,
                                                     indexOfProbabilityDistributionChoice);

                    if (approval == -1) continue;

                    sqspu.primeSimulation(nodesToSpawn,
                                          schedulingMetrics,
                                          arrivalTimeRange,
                                          configurationType,
                                          nodeDistributionProbabilities,
                                          priorityDistributionProbabilities,
                                          SQSPU.NodeDistribution.values()[indexOfNodeDistributionChoice],
                                          priorityClasses,
                                          SQSPU.PriorityDistribution.values()[indexOfProbabilityDistributionChoice]);
                    System.out.println("Parameters enforced onto simulation!");
                    break;

                case "11": //run simulation

                    approval = checkRequirements(sqspu,
                                                 nodesToSpawn,
                                                 schedulingMetrics,
                                                 arrivalTimeRange,
                                                 priorityClasses,
                                                 preemptionSet,
                                                 configurationType,
                                                 indexOfNodeDistributionChoice,
                                                 indexOfProbabilityDistributionChoice);

                    if (approval == -1) continue;

                    sqspu.run();
                    break;

                case "12": //exit program
                    System.out.println("Exiting program...");
                    sentinel = true;
                    break;

                default:
                    System.out.println("Please choose one of the options!");
                    break;
            }
            System.out.println();
        }
    }




    /*
            If FCFS
                then no priority and no SJF
                but can do Round-Robin
            check for FCFS and (no) Round-Robin

            If Priority
                then no FCFS and no SJF
                but can do Round-Robin
            check for Priority and (no) Round-Robin

            If SJF
                then no FCFS and no priority
                but can do Round-Robin
            check for SJF and (no) Round-Robin

            If Preemption but no Round-Robin
                then no FCFS
                but can do SJF or Priority
            check for Preemption and either (SJF or Priority)

            If Preemption and Round-Robin
                then ...
                but can do FCFS or SJF or Priority
            check for Preemption, Round-Robin and either (FCFS or SJF or Priority)
         */
    static double[] chooseSchedulingType(){

        Scanner sc = new Scanner(System.in);

        String subMenu1 = """
                          Choose a scheduling type
                          Note - Choosing an active scheduling type deactivates it
                          1. First Come First Serve (FCFS) - Choosing FCFS disables SJF and HP
                          2. Shortest Job First (SJF) - Choosing SJF disables FCFS and HP
                          3. Highest Priority (HP) - Choosing HP disables FCFS and SJF
                          4. Round Robin (RR)
                          5. Go Back
                          """;

        int schedulingType = 0;

        double[] schedulingMetrics = new double[5];

        while (true) {

            try {
                System.out.println(subMenu1);
                System.out.print("Enter a scheduling type: ");
                String input = sc.nextLine();
                System.out.println();
                schedulingType = Integer.parseInt(input);

                if (schedulingType == 5){
                    return schedulingMetrics;
                }

                if (schedulingType < 1 || schedulingType > 4){
                    throw new NumberFormatException();
                }
                schedulingType--; //decrement to match zero-indexing


                switch(schedulingType){
                    case 0:
                        if (schedulingMetrics[schedulingType] == 0) {
                            schedulingMetrics[schedulingType] = 1;
                            schedulingMetrics[1] = 0;
                            schedulingMetrics[2] = 0;

                        }else{
                            schedulingMetrics[schedulingType] = 0;
                        }
                        break;

                    case 1:
                        if (schedulingMetrics[schedulingType] == 0) {
                            schedulingMetrics[schedulingType] = 1;
                            schedulingMetrics[0] = 0;
                            schedulingMetrics[2] = 0;

                        }else{
                            schedulingMetrics[schedulingType] = 0;
                        }
                        break;

                    case 2:
                        if (schedulingMetrics[schedulingType] == 0) {
                            schedulingMetrics[schedulingType] = 1;
                            schedulingMetrics[0] = 0;
                            schedulingMetrics[1] = 0;

                        }else{
                            schedulingMetrics[schedulingType] = 0;
                        }
                        break;

                    case 3:
                        schedulingMetrics[schedulingType] = (schedulingMetrics[schedulingType] == 0) ? 1 : 0;
                        break;
                }

                String schedulingTypeString = "";

                for (int i = 0; i < 4; i++) {
                    if (schedulingMetrics[i] != 0) {
                        if (!schedulingTypeString.isEmpty()) {
                            schedulingTypeString += ", ";
                        }
                        schedulingTypeString += SQSPU.SchedulingType.values()[i];
                    }
                }

                System.out.printf("Active Scheduling type(s): %s\n", schedulingTypeString);

                break;

            }catch (NumberFormatException e){
                System.out.println("Please enter an integer value!");
            }
        }

        double timeQuantum = -1;

        if (schedulingType==3){

            while (true) {

                try {
                    System.out.print("Enter a value for the time quantum: ");
                    String input = sc.nextLine();
                    System.out.println();
                    schedulingMetrics[4] = Double.parseDouble(input);

                    if (schedulingMetrics[4] <= 0){
                        throw new NumberFormatException();
                    }
                    break;

                }catch (NumberFormatException e){
                    System.out.println("Please enter a numeric value greater than 0!");
                }
            }
        }
        return schedulingMetrics;
    }

    static int distributionChoices(String type){
        Scanner sc = new Scanner(System.in);

        int distributionChoice = -1;

        String subMenu1 = """
                          Choose a distribution for %s
                          1. Uniform
                          2. Normal
                          3. Bimodal
                          4. Geometric
                          5. Custom
                          6. Go Back
                          """;

        String distributionType = (type.equals("node")) ? "the arrival of nodes" : "assigning probability";

        while (true) {

            try {
                System.out.printf(subMenu1, distributionType);
                System.out.print("Enter distribution #: ");
                String input = sc.nextLine();
                System.out.println();
                distributionChoice = Integer.parseInt(input);

                if (distributionChoice == 6) return -1;

                if (distributionChoice < 1 || distributionChoice > 5){
                    throw new NumberFormatException();
                }
                break;

            }catch (NumberFormatException e){
                System.out.println("Please enter an integer value between 1 and 5!");
            }
        }
        return distributionChoice;
    }

    static double[] setProbabilities(int distributionChoice){
        Scanner sc = new Scanner(System.in);

        double[] probabilityDistributions = new double[4];

        switch(distributionChoice){
            case 2: //normal distribution

                while (true) {

                    try {
                        System.out.print("Enter the value for mu: ");
                        String input = sc.nextLine();
                        System.out.println();
                        probabilityDistributions[0] = Double.parseDouble(input);
                        break;

                    }catch (NumberFormatException e){
                        System.out.println("Please enter a double value!");
                    }
                }

                while (true) {

                    try {
                        System.out.print("Enter a value for sigma that is greater than 0: ");
                        String input = sc.nextLine();
                        System.out.println();
                        probabilityDistributions[1] = Double.parseDouble(input);

                        if (probabilityDistributions[1] <= 0) throw new NumberFormatException();

                        break;

                    }catch (NumberFormatException e){
                        System.out.println("Please enter a double value!");
                    }
                }
                break;

            case 3: //bimodal distribution

                while (true) {

                    try {
                        System.out.print("Enter the value for mu1: ");
                        String input = sc.nextLine();
                        System.out.println();
                        probabilityDistributions[0] = Double.parseDouble(input);
                        break;

                    }catch (NumberFormatException e){
                        System.out.println("Please enter a double value!");
                    }
                }

                while (true) {

                    try {
                        System.out.print("Enter a value for sigma1 that is greater than 0: ");
                        String input = sc.nextLine();
                        System.out.println();
                        probabilityDistributions[1] = Double.parseDouble(input);

                        if (probabilityDistributions[1] <= 0) throw new NumberFormatException();

                        break;

                    }catch (NumberFormatException e){
                        System.out.println("Please enter a double value!");
                    }
                }

                while (true) {

                    try {
                        System.out.print("Enter the value for mu2: ");
                        String input = sc.nextLine();
                        System.out.println();
                        probabilityDistributions[2] = Double.parseDouble(input);
                        break;

                    }catch (NumberFormatException e){
                        System.out.println("Please enter a double value!");
                    }
                }

                while (true) {

                    try {
                        System.out.print("Enter a value for sigma2 that is greater than 0: ");
                        String input = sc.nextLine();
                        System.out.println();
                        probabilityDistributions[3] = Double.parseDouble(input);

                        if (probabilityDistributions[3] <= 0) throw new NumberFormatException();

                        break;

                    }catch (NumberFormatException e){
                        System.out.println("Please enter a double value!");
                    }
                }
                break;

            case 4: //geometric distribution

                while (true) {

                    try {
                        System.out.print("Enter a value for probability p: ");
                        String input = sc.nextLine();
                        System.out.println();
                        probabilityDistributions[0] = Double.parseDouble(input);
                        break;

                    }catch (NumberFormatException e){
                        System.out.println("Please enter a double value!");
                    }
                }
                break;

            default:
                break;
        }

        return probabilityDistributions;
    }

    static int checkRequirements(SQSPU sqspu,
                                 int nodesToSpawn,
                                 double[] schedulingMetrics,
                                 double arrivalTimeRange,
                                 int priorityClasses,
                                 int preemptionSet,
                                 int configurationType,
                                 int indexOfNodeDistributionChoice,
                                 int indexOfProbabilityDistributionChoice){
        String errMessage = "";

        if (sqspu.getMaxQueueSize() == -1){
            errMessage+="Enter max queue size greater than 0!\n";
        }

        if (nodesToSpawn == -1){
            errMessage+="Enter an amount of nodes to spawn greater than 0!\n";
        }

        boolean scheduleAssigned = false;

        for (int i = 0; i < 4; i++){
            if (schedulingMetrics[i] != 0){
                scheduleAssigned = true;
                break;
            }
        }

        if (!scheduleAssigned){
            errMessage+="Choose a scheduling type!\n";
        }

        if (arrivalTimeRange == -1.0){
            errMessage+="Enter an arrival time range greater than 0!\n";
        }

        if (indexOfNodeDistributionChoice == -1){
            errMessage+="Choose a node arrival distribution type!\n";
        }

        /*
        if (priorityClasses == -1){
            errMessage+="Enter an amount of priority classes equal to or greater than 0!\n";
        }

         */

        if (indexOfProbabilityDistributionChoice == -1 && priorityClasses != 0){
            errMessage+="Chose a priority assignment distribution type!\n";
        }

        if (preemptionSet == -1){
            errMessage+="Decide if the simulation is preemptive!\n";
        }

        if (configurationType < 1 || configurationType > 2){
            errMessage+="Choose a configuration !\n";
        }

        if (!(errMessage.isEmpty())){
            String separator = "---------------------------------------------------------------------------------------------------\n";
            errMessage = "Invalid Parameter Choices! Please be sure to...\n" + separator + errMessage + separator;
            System.out.println(errMessage);
            return -1;
        }

        return 0;
    }
}

/*

Main: 533
DistributionGeneration: 349
ProcessingUnit: 213
Queue: 191
SQSPU: 134
 */
