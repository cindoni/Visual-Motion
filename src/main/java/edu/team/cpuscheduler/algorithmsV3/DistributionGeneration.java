import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class DistributionGeneration {
    Random rng = new Random();
    Scanner sc = new Scanner(System.in);

    public double[] interarrivalTimeDistribution(int nodesToSpawn, double arrivalTimeRange, double[] nodeProbabilities, SQSPU.NodeDistribution nodeDistribution){
        double[] chosenSpawnTimes = new double[nodesToSpawn];
        double tempSum0 = 0;
        double tempSum1 = 0;
        double tempSum2 = 0;

        switch (nodeDistribution){
            case UNIFORM:
                Arrays.fill(chosenSpawnTimes, (double) arrivalTimeRange / nodesToSpawn);
                break;

            case NORMAL:
                double mu = (nodesToSpawn - 1) * nodeProbabilities[0]; //center of the distribution (0 to 100)
                double sigma = nodesToSpawn * nodeProbabilities[1]; //how wide the bell curve stretches

                //generate X nodes clustered in a bell curve shape over time
                int[] x = new int[nodesToSpawn];

                for (int i = 0; i < nodesToSpawn; i++){
                    x[i]=i;
                }

                for (int i = 0; i < nodesToSpawn; i++){
                    double value = (x[i]-mu)/sigma;
                    chosenSpawnTimes[i]=Math.exp(-0.5 * value * value);
                    tempSum0+=chosenSpawnTimes[i];
                }

                for (int i = 0; i < nodesToSpawn; i++){
                    chosenSpawnTimes[i] *= arrivalTimeRange/tempSum0;
                }
                break;

            case BIMODAL:
                double mu1 = (nodesToSpawn - 1) * nodeProbabilities[0]; //center of the distribution (0 to 100)
                double sigma1 = nodesToSpawn * nodeProbabilities[1]; //how wide the bell curve stretches

                double mu2 = (nodesToSpawn - 1) * nodeProbabilities[2]; //center of the distribution (0 to 100)
                double sigma2 = nodesToSpawn * nodeProbabilities[3]; //how wide the bell curve stretches

                //generate X nodes clustered in a bell curve shape over time
                x = new int[nodesToSpawn];

                for (int i = 0; i < nodesToSpawn; i++){
                    x[i]=i;
                }

                double[] peak1 = new double[nodesToSpawn];
                double[] peak2 = new double[nodesToSpawn];

                tempSum1 = 0;
                for (int i = 0; i < nodesToSpawn; i++){
                    double value = (x[i]-mu1)/sigma1;
                    peak1[i]=Math.exp(-0.5 * value * value);
                    tempSum1+=peak1[i];
                }

                for (int i = 0; i < nodesToSpawn; i++){
                    peak1[i] *= ((double) arrivalTimeRange /2)/tempSum1;
                }

                tempSum2 = 0;
                for (int i = 0; i < nodesToSpawn; i++){
                    double value = (x[i]-mu2)/sigma2;
                    peak2[i]=Math.exp(-0.5 * value * value);
                    tempSum2+=peak2[i];
                }

                for (int i = 0; i < nodesToSpawn; i++){
                    peak2[i] *= ((double) arrivalTimeRange /2)/tempSum2;
                }

                for(int i = 0; i < nodesToSpawn; i++){
                    chosenSpawnTimes[i]=peak1[i]+peak2[i];
                }
                break;

            case GEOMETRIC:
                tempSum0 = 0;
                for (int i = 0; i < nodesToSpawn; i++){
                    chosenSpawnTimes[i] = nodeProbabilities[0] * Math.pow((1-nodeProbabilities[0]), (i-1));
                    tempSum0+=chosenSpawnTimes[i];
                }

                for(int i = 0; i < nodesToSpawn; i++){
                    chosenSpawnTimes[i]=chosenSpawnTimes[i]*(arrivalTimeRange/tempSum0);
                }
                break;

            case CUSTOM:

                double remainingInterarrivalTime = arrivalTimeRange;

                for (int i = 0; i < nodesToSpawn; i++){

                    if (remainingInterarrivalTime > 1e-4){
                        while (true){

                            try{
                                System.out.printf("Input an inter-arrival time between 0 and %f for node %d:",remainingInterarrivalTime, i+1);
                                double interarrivalTime = sc.nextDouble();
                                System.out.println("\n");

                                if (interarrivalTime > remainingInterarrivalTime){
                                    throw new InputMismatchException();
                                }

                                remainingInterarrivalTime -= interarrivalTime;
                                chosenSpawnTimes[i]=interarrivalTime;
                                break;

                            }catch (InputMismatchException e){
                                System.out.printf("Please choose a decimal value between 0 and %f!\n", remainingInterarrivalTime);
                            }

                        }
                    }else{
                        System.out.printf("Nodes %d to %d will have their inter-arrival times automatically set due to how little the remaining inter-arrival time is left to set!", i+1, nodesToSpawn);
                        //divide the remaining inter-arrival time evenly between the remaining nodes
                        for (int j = i; j < nodesToSpawn; j++){
                            chosenSpawnTimes[j]=remainingInterarrivalTime/(nodesToSpawn-i);
                        }
                    }
                }
                break;

            default:
                break;
        }

        return chosenSpawnTimes;
    }

    public int[] priorityDistribution(int nodesToSpawn, int maxPriorityClasses, double[] priorityProbabilities, SQSPU.PriorityDistribution priorityDistribution){
        int[] priorities = new int[nodesToSpawn];

        switch (priorityDistribution){
            case NONE:
                Arrays.fill(priorities, -1);
                break;

            case UNIFORM:
                for (int i = 0; i < nodesToSpawn; i++){
                    priorities[i] = rng.nextInt(maxPriorityClasses)+1;
                }
                break;

            case NORMAL:
                double mu = (maxPriorityClasses - 1) * priorityProbabilities[0]; //center of the distribution (0 to 100)
                double sigma = maxPriorityClasses * priorityProbabilities[1]; //how wide the bell curve stretches

                //generate X nodes clustered in a bell curve shape over time
                int[] x = new int[maxPriorityClasses];

                for (int i = 0; i < maxPriorityClasses; i++){
                    x[i]=i;
                }

                double tempSum0 = 0;
                double[] weights = new double[maxPriorityClasses];
                for (int i = 0; i < maxPriorityClasses; i++){
                    double value = (x[i]-mu)/sigma;
                    weights[i]=Math.exp(-0.5 * value * value);
                    tempSum0+=weights[i];
                }

                for (int i = 0; i < maxPriorityClasses; i++){
                    weights[i] /= tempSum0;
                }

                for (int i = 0; i < nodesToSpawn; i++) {
                    double r = rng.nextDouble(); // Random roll between 0.0 and 1.0
                    double cumulativeProbability = 0.0;
                    int selectedPriority = 1; // Default fallback

                    // The engine: check which part of the Bell Curve bucket the random roll 'r' lands in
                    for (int j = 0; j < maxPriorityClasses; j++) {
                        cumulativeProbability += weights[j];
                        if (r <= cumulativeProbability) {
                            selectedPriority = j + 1; // Map index 0 to priority class 1, index 1 to 2, etc.
                            break;
                        }
                    }

                    priorities[i] = selectedPriority;
                }
                break;

            case BIMODAL:
                double mu1 = (maxPriorityClasses - 1) * priorityProbabilities[0]; //center of the distribution (0 to 100)
                double sigma1 = maxPriorityClasses * priorityProbabilities[1]; //how wide the bell curve stretches

                double mu2 = (maxPriorityClasses - 1) * priorityProbabilities[2]; //center of the distribution (0 to 100)
                double sigma2 = maxPriorityClasses * priorityProbabilities[3]; //how wide the bell curve stretches

                //generate X nodes clustered in a bell curve shape over time
                x = new int[maxPriorityClasses];

                for (int i = 0; i < maxPriorityClasses; i++){
                    x[i]=i;
                }

                tempSum0 = 0;

                double[] peak1 = new double[maxPriorityClasses];
                double[] peak2 = new double[maxPriorityClasses];

                for (int i = 0; i < maxPriorityClasses; i++){
                    double value = (x[i]-mu1)/sigma1;
                    peak1[i]=Math.exp(-0.5 * value * value);
                    tempSum0+=peak1[i];
                }

                for (int i = 0; i < maxPriorityClasses; i++){
                    double value = (x[i]-mu2)/sigma2;
                    peak2[i]=Math.exp(-0.5 * value * value);
                    tempSum0+=peak2[i];
                }

                weights = new double[maxPriorityClasses];
                for (int i = 0; i < maxPriorityClasses; i++){
                    weights[i] = peak1[i] + peak2[i];
                }

                for (int i = 0; i < maxPriorityClasses; i++){
                    weights[i] /= tempSum0;
                }

                for (int i = 0; i < nodesToSpawn; i++) {
                    double r = rng.nextDouble(); // Random roll between 0.0 and 1.0
                    double cumulativeProbability = 0.0;
                    int selectedPriority = 1; // Default fallback

                    // The engine: check which part of the Bell Curve bucket the random roll 'r' lands in
                    for (int j = 0; j < maxPriorityClasses; j++) {
                        cumulativeProbability += weights[j];
                        if (r <= cumulativeProbability) {
                            selectedPriority = j + 1; // Map index 0 to priority class 1, index 1 to 2, etc.
                            break;
                        }
                    }

                    priorities[i] = selectedPriority;
                }
                break;

            case GEOMETRIC:
                weights = new double[maxPriorityClasses];
                tempSum0 = 0;

                for (int i = 0; i < maxPriorityClasses; i++){
                    weights[i] = priorityProbabilities[0] * Math.pow((1-priorityProbabilities[0]), (i));
                    tempSum0 += weights[i];
                }

                for (int i = 0; i < maxPriorityClasses; i++){
                    weights[i] /= tempSum0;
                }

                for (int i = 0; i < nodesToSpawn; i++) {
                    double r = rng.nextDouble(); // Random roll between 0.0 and 1.0
                    double cumulativeProbability = 0.0;
                    int selectedPriority = 1; // Default fallback

                    // The engine: check which part of the Bell Curve bucket the random roll 'r' lands in
                    for (int j = 0; j < maxPriorityClasses; j++) {
                        cumulativeProbability += weights[j];
                        if (r <= cumulativeProbability) {
                            selectedPriority = j + 1; // Map index 0 to priority class 1, index 1 to 2, etc.
                            break;
                        }
                    }

                    priorities[i] = selectedPriority;
                }
                break;

            case CUSTOM:
                double remainingProbability = 1.0;
                double weightsToSet = -1;
                weights = new double[maxPriorityClasses];

                for (int i = 0; i < maxPriorityClasses; i++){

                    if (remainingProbability > 1e-4){
                        while (true){

                            try{
                                System.out.printf("Input an probability value between 0 and %f for priority class %d of %d:",remainingProbability, i+1, maxPriorityClasses);
                                double probability = sc.nextDouble();
                                System.out.println("\n");

                                if (probability > remainingProbability){
                                    throw new InputMismatchException();
                                }

                                remainingProbability -= probability;
                                weights[i]=probability;
                                break;

                            }catch (InputMismatchException e){
                                System.out.printf("Please choose a decimal value between 0 and %f!\n", remainingProbability);
                            }

                        }
                    }else{
                        System.out.printf("Nodes %d to %d will have their probability value automatically set due to having a small amount of remaining probability!", i+1, nodesToSpawn);
                        //divide the remaining probability evenly between the remaining nodes
                        for (int j = i; j < nodesToSpawn; j++){
                            weights[j]=remainingProbability/(nodesToSpawn-i);
                        }
                    }
                }

                for (int i = 0; i < nodesToSpawn; i++) {
                    double r = rng.nextDouble(); // Random roll between 0.0 and 1.0
                    double cumulativeProbability = 0.0;
                    int selectedPriority = 1; // Default fallback

                    // The engine: check which part of the Bell Curve bucket the random roll 'r' lands in
                    for (int j = 0; j < maxPriorityClasses; j++) {
                        cumulativeProbability += weights[j];
                        if (r <= cumulativeProbability) {
                            selectedPriority = j + 1; // Map index 0 to priority class 1, index 1 to 2, etc.
                            break;
                        }
                    }

                    priorities[i] = selectedPriority;
                }

                break;

            default:
                break;
        }

        return priorities;
    }
}