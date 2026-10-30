import java.util.ArrayList;

public class ProcessingUnit {
    int identifier;
    boolean priority;
    boolean preemptive;
    volatile boolean processing;
    boolean congested;
    boolean hasProcessedBefore;
    int indexOFChosenQueue;

    SchedulingType schedulingType = null;

    Queue.Node activeNode;
    ArrayList<Queue> inputQueues = new ArrayList<>();
    ArrayList<Queue> outputQueues = new ArrayList<>();

    public ProcessingUnit(int identifier){
        this.identifier = identifier;
    }

    public ProcessingUnit(int identifier, boolean priority, boolean preemptive){
        this.identifier = identifier;
        this.priority = priority;
        this.preemptive = preemptive;
    }

    //input a node from the array of input queues associated with this process
    public void inputNodeFromQueue(){

        /*
        set default last time accessed for each queue if Server
        has not yet processed anything before
        */
        if (!(hasProcessedBefore)){
            long time_last_accessed = System.nanoTime();

            for (Queue queue: inputQueues){
                queue.setTimeLastAccessed(time_last_accessed);
            }

            hasProcessedBefore = true;
        }

        //set default temp node value
        Queue.Node temp = null;

        //set the average waiting time of the input queues attached to ProcessingUnit
        long avg_waiting_time = 0;

        for (Queue queue: inputQueues){
            avg_waiting_time += queue.getTimeLastAccessed();
        }

        avg_waiting_time /= inputQueues.size();

        //default value to invalid index
        int index_of_chosen_queue = -1;

        //decide which queue to pull from next
        int i = 0;
        for (Queue queue: inputQueues){
            if (queue.isEmpty()) continue;

            if (temp == null){ //set default customer value
                temp = queue.peek();
                index_of_chosen_queue = i;
            }

            /*
            if a queue has been waiting too long then give it the right to be pulled from
            right this instant
            */
            if (1.25 * avg_waiting_time < queue.getTimeLastAccessed() && queue.getTimeLastAccessed() <= 1.5 * avg_waiting_time){
                temp = queue.peek();
                index_of_chosen_queue = i;
                break;
            }

            i++;
        }

        /*
        if still invalid index then rely on update from a queue, itself, to notify Server when
        to process again
        */
        if (index_of_chosen_queue == -1){
            System.out.printf("Processing Unit %d State: No node enqueued\n\n", identifier);
            return;
        }

        //else, go ahead and process the chosen customer from the chosen queue
        indexOFChosenQueue = inputQueues.get(index_of_chosen_queue).identifier;
        inputQueues.get(index_of_chosen_queue).dequeue();
        inputQueues.get(index_of_chosen_queue).setTimeLastAccessed(System.nanoTime());

        activeNode = temp;
        System.out.printf("Processing Unit %d State: Ready to process node %d from Queue %d\n\n", identifier, activeNode.identifier+1, indexOFChosenQueue);

        /*
        is designed to have the queue the processing unit chose to force the processing unit
        to call "processActiveNode()"
        */
        return;
    }

    public void processActiveNode(){

        processing = true;

        long startTime = System.nanoTime();

        long processingTime = (long) activeNode.processingTime * 1_000_000_000;

        boolean enforcedRoundRobin = false;

        System.out.printf("Processing Unit %d State: Processing node %d\n\n", identifier, activeNode.identifier+1);

        //simulate node being processed
        while(true){
            long timestamp = System.nanoTime() - startTime;

            if (timestamp >= processingTime){
                System.out.printf("Processing Unit %d State: Finished processing node %d in %.4f seconds!\n\n", identifier, activeNode.identifier+1, (float) timestamp / 1_000_000_000);
                break;
            }

            if (schedulingType == SchedulingType.RR){
                long timeQuantum = (long) inputQueues.get(indexOFChosenQueue).getTimeQuantum() * 1_000_000_000;
                if (timestamp >=  timeQuantum && inputQueues.get(indexOFChosenQueue).getSize() > 0){
                    activeNode.processingTime -= (float) timestamp / 1_000_000_000;
                    System.out.printf("Processing Unit %d State: Enforcing time quantum, returning node %d to Queue %d\n\n", identifier, activeNode.identifier+1, indexOFChosenQueue);
                    inputQueues.get(indexOFChosenQueue).enqueue(-1, -1, null, activeNode, true);
                    activeNode = null;
                    enforcedRoundRobin = true;
                    break;
                }
            }
        }

        //see if connected to output queue
        if (outputQueues.isEmpty() || enforcedRoundRobin){
            activeNode = null;

            System.out.printf("Processing Unit %d State: No output queues\n\n", identifier);

            //determine if nay other customer needs to be processed
            //inputNodeFromQueue();
            try {
                inputNodeFromQueue();
            } catch (Throwable t) {
                System.out.println("Silent crash!");
                t.printStackTrace(); // This will reveal if it's crashing silently
            }
            //System.out.println("Active node value: " + activeNode);

            //determine if ProcessingUnit is still in the phase of processing input nodes from queues
            if (activeNode == null){
                System.out.printf("Processing Unit %d State: Finished processing all previously queued nodes\n\n", identifier);
                processing = false;
            }else{
                System.out.printf("Processing Unit %d State: Processing inputted node\n\n", identifier);
                processActiveNode();
            }
            return;

        }

        //if connected to output queue, see where customer needs to go next
        System.out.printf("Processing Unit %d State: Attempting to send node to output queue\n\n", identifier);
        Queue localOutputQueue = null;

        for (Queue queue: outputQueues){
            if (queue.identifier == activeNode.remainingTraversals.getFirst()){
                localOutputQueue = queue;
                break;
            }
        }

        /*
        enqueue the node to their next queue if said queue is not full
        else, wait until queue is not full
        */

        while (true){
            if (!(localOutputQueue.isFull())){
                localOutputQueue.enqueue(activeNode);
                break;
            }else{
                congested=true;
            }
        }

        //rid of activeNode when output to different queue
        activeNode = null;
        congested = false;

        //determine if any other queue has node that needs to be processed
        inputNodeFromQueue();

        //determine if ProcessingUnit is still in the phase of processing input nodes from queues
        if (activeNode == null){
            System.out.printf("Processing Unit %d State: Finished processing all previously queued nodes\n\n", identifier);
            processing = false;
        }else{
            System.out.printf("Processing Unit %d State: Processing inputted node\n\n", identifier);
            processActiveNode();
        }
        return;
    }

    public boolean isProcessing(){return processing;}

    public void connectInputQueue(Queue queue){
        inputQueues.add(queue);
        queue.addProcessingUnit(this);
        return;
    }

    public int getSizeConnectedInputQueues(){return inputQueues.size();}

    public void connectOutputQueue(Queue queue){
        outputQueues.add(queue);
        return;
    }

    public int getSizeConnectedOutputQueues(){return outputQueues.size();}

    public void setPriority(boolean priority){this.priority = priority;}

    public void setPreemptive(boolean preemptive){this.preemptive = preemptive;}

    public void setSchedulingType(double[] schedulingMetrics){
        this.schedulingType = SchedulingType.values()[(int) schedulingMetrics[0]];
    }

    public enum SchedulingType{
        FCFS, SJF, HP, RR
    }
}