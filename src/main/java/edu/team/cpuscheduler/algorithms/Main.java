import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        SQSPU sqspu = new SQSPU();

        String menu1 = """
                       Menu 1
                       1. Set max queue size
                       2. Set nodes to spawn
                       3. Set arrival time range
                       4. Set node arrival distribution type
                       5. Set scheduling type
                       6. Set priority
                       7. Set priority assignment distribution type
                       8. Set preemption
                       9. View simulation parameters
                       10. Prime simulation
                       11. Run simulation
                       12. Exit
                       """;

        boolean sentinel = false;

        int nodesToSpawn = -1;
        double arrivalTimeRange = -1;
        double[] schedulingMetrics = new double[2];
        schedulingMetrics[0]=-1;
        int priorityClasses = -1;
        int preemptionSet = -1;

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

                case "6": //set priority metrics

                    while (true) {

                        try {
                            System.out.print("Enter the # of priority classes: ");
                            String input = sc.nextLine();
                            System.out.println();
                            priorityClasses = Integer.parseInt(input);
                            break;

                        }catch (NumberFormatException e){
                            System.out.println("Please enter an integer value!");
                        }
                    }

                    //if no priority classes then set prob distribs choice to NONE
                    if (priorityClasses == 0) indexOfProbabilityDistributionChoice=0;
                    break;

                case "7": //set priority assignment distribution type
                    indexOfProbabilityDistributionChoice = distributionChoices("priority");

                    if (indexOfProbabilityDistributionChoice == -1 && priorityClasses == 0){
                        indexOfProbabilityDistributionChoice = 0;
                    }

                    //set probability distribution type for inputs that require user-set probabilities
                    if (indexOfProbabilityDistributionChoice > 1 && indexOfProbabilityDistributionChoice != 5) {
                        priorityDistributionProbabilities = setProbabilities(indexOfProbabilityDistributionChoice);
                    }
                    break;

                case "8": //set preemption
                    while (true) {

                        try {
                            System.out.print("Is simulation preemptive? (Y) or (N): ");
                            String input = sc.nextLine();
                            System.out.println();

                            if (input.equals("Y") || input.equals("y")){
                                sqspu.setPreemptive(true);
                            }else if (input.equals("N") || input.equals("n")){
                                sqspu.setPreemptive(false);
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

                case "9": //view simulation parameter choices

                    System.out.println("Parameter Choices");
                    System.out.println("---------------------------------------------------------------------------------------------------");
                    System.out.printf("Max queue size: %d\n", sqspu.getMaxQueueSize());
                    System.out.println("Preemptive scheduling: " + ((preemptionSet == -1) ? null: sqspu.getPreemptive()));
                    System.out.println("Scheduling type: " + ((schedulingMetrics[0] != -1) ? SQSPU.SchedulingType.values()[(int) schedulingMetrics[0]-1] : null));

                    if (schedulingMetrics[0]==4){
                        System.out.println("Time Quantum: " + schedulingMetrics[1]);
                    }

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
                                                     indexOfNodeDistributionChoice,
                                                     indexOfProbabilityDistributionChoice);

                    if (approval == -1) continue;

                    sqspu.primeSimulation(nodesToSpawn,
                                          schedulingMetrics,
                                          arrivalTimeRange,
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

    static double[] chooseSchedulingType(){
        Scanner sc = new Scanner(System.in);

        String subMenu1 = """
                          Choose a scheduling type
                          1. First Come First Serve
                          2. Shortest Job First
                          3. Highest Priority
                          4. Round Robin
                          """;

        int schedulingType = 0;

        while (true) {

            try {
                System.out.println(subMenu1);
                System.out.print("Enter a scheduling type: ");
                String input = sc.nextLine();
                System.out.println();
                schedulingType = Integer.parseInt(input);

                if (schedulingType < 1 || schedulingType > 4){
                    throw new NumberFormatException();
                }
                schedulingType--; //decrement to match zero-indexing
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
                    timeQuantum = Double.parseDouble(input);

                    if (timeQuantum <= 0){
                        throw new NumberFormatException();
                    }
                    break;

                }catch (NumberFormatException e){
                    System.out.println("Please enter a numeric value greater than 0!");
                }
            }
        }
        return new double[] {schedulingType ,timeQuantum};
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
                                 int indexOfNodeDistributionChoice,
                                 int indexOfProbabilityDistributionChoice){
        String errMessage = "";

        if (sqspu.getMaxQueueSize() == -1){
            errMessage+="Enter max queue size greater than 0!\n";
        }

        if (nodesToSpawn == -1){
            errMessage+="Enter an amount of nodes to spawn greater than 0!\n";
        }

        if (schedulingMetrics[0]==-1){
            errMessage+="Choose a scheduling type!\n";
        }

        if (arrivalTimeRange == -1.0){
            errMessage+="Enter an arrival time range greater than 0!\n";
        }

        if (indexOfNodeDistributionChoice == -1){
            errMessage+="Choose a node arrival distribution type!\n";
        }

        if (priorityClasses == -1){
            errMessage+="Enter an amount of priority classes equal to or greater than 0!\n";
        }

        if (indexOfProbabilityDistributionChoice == -1 && priorityClasses != 0){
            errMessage+="Chose a priority assignment distribution type!\n";
        }

        if (preemptionSet == -1){
            errMessage+="Decide if the simulation is preemptive!\n";
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