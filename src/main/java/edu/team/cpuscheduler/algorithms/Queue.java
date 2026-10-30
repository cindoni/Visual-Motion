import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Queue {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

    //will be used to force any print statements in a thread to execute before returning to the main menu
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    int identifier;
    int maxSize = -1;
    int reservedSpots;
    boolean priority;
    boolean preemptive;
    volatile int size = 0;

    SchedulingType schedulingType = null;
    double timeQuantum = 0;


    Node queue = null;
    ArrayList<ProcessingUnit> processingUnits = new ArrayList<>();
    boolean hasEmptyProcessingUnit;
    long timeLastAccessed;

    public Queue(int identifier){
        this.identifier = identifier;
    }

    public Queue(int identifier, int maxSize, boolean priority, boolean preemptive){
        this.identifier = identifier;
        this.maxSize = maxSize;
        this.priority = priority;
        this.preemptive = preemptive;
    }

    // 1. Version with only node_id (uses defaults for priority and data)
    public void enqueue(int nodeId) {
        enqueue(nodeId, -1, null, null, false);
    }

    public void enqueue(Node node){
        enqueue(-1, -1, null, node, false);
    }

    // 2. Version with node_id and priority (uses default for data)
    public void enqueue(int nodeId, int priority) {
        enqueue(nodeId, priority, null, null, false);
    }

    // 3. Main version that accepts all parameters
    public void enqueue(int nodeId, int priority, Object data, Node node, boolean roundRobin) {
        if (queue == null){
            queue = (node == null) ? new Node(nodeId, priority, data) : node;
            size++;
            System.out.printf("Queue %d State: Successfully enqueued node %d!\n\n", identifier, nodeId+1);
            attemptNodeDelivery();
            return;
        }


        if (size + reservedSpots == maxSize && !roundRobin && reservedSpots == 0){
            System.out.printf("Queue %d State: Size limit reached! Could not enqueue node %d!\n\n", identifier, nodeId+1);
            return;
        }

        Node newNode = (node == null) ? new Node(nodeId, priority, data) : node;

        if (roundRobin){
            reservedSpots--;
        }

        Node temp = queue;

        while (!(temp.next == null)){
            temp = temp.next;
        }

        temp.next = newNode;

        size++;
        System.out.printf("Queue %d State: Successfully enqueued node %d!\n\n", identifier, nodeId+1);
        System.out.printf("Queue %d new size: %d\n\n", identifier, size);
        attemptNodeDelivery();
        return;
    }

    public Node dequeue(){
        if (queue == null) return null;

        Node temp = queue;
        queue = temp.next;
        temp.next = null;
        size--;
        System.out.printf("Queue %d State: Dequeued node %d\n\n", identifier, temp.identifier+1);

        if (this.schedulingType == SchedulingType.RR){
            reservedSpots++;
        }

        return temp;
    }

    public Node peek(){return (queue == null) ? null : queue;}

    public synchronized void attemptNodeDelivery(){

        /*
        go through the list of processing units the queue is associated with
        and see which one is not occupied. Make the chosen processing unit
        input a node from the input queues it is associated with
        */

        System.out.println();
        if (peek() == null) {
            System.out.printf("Queue %d State: No output queues / queue is empty\n\n", identifier);
            return;
        }

        ProcessingUnit activeProcessingUnit = null;

        System.out.printf("Queue %d State: Attempting to dequeue node %d\n\n", identifier, peek().identifier+1);

        Queue.Node temp = null;

        if (!(processingUnits.getFirst().isProcessing())){
            temp = peek();
            processingUnits.getFirst().inputNodeFromQueue();

        }else{
            System.out.printf("Queue %d State: Failed to dequeue node %d\n\n", identifier, peek().identifier+1);
            return;
        }

        //have the processing unit process the node in the thread as to not prevent the enqueuing of incoming nodes
        System.out.printf("Queue %d State: Prompting Processing Unit %d to enqueue node %d from Queue %d\n\n", identifier, processingUnits.getFirst().identifier, temp.identifier+1, identifier);
        ProcessingUnit finalActiveProcessingUnit = processingUnits.getFirst();

        //is a part of forcing the thread to finish executing the print statements before returning to the main menu
        activeTasks.incrementAndGet();
        threadPool.submit(() -> {
            try{
                finalActiveProcessingUnit.processActiveNode();
            } finally{
                activeTasks.decrementAndGet();
            }
        });
        return;
    }

    public void addProcessingUnit(ProcessingUnit procUnit){
        processingUnits.add(procUnit);
        System.out.printf("Queue %d State: Linked to Processing Unit %d\n\n", identifier, procUnit.identifier);
    }

    public void decremetReservedSpots(){this.reservedSpots--;}

    public boolean isFull(){return maxSize == size;}

    public boolean isEmpty(){return size == 0;}

    public long getTimeLastAccessed(){return timeLastAccessed;}

    public void setTimeLastAccessed(long timeLastAccessed){this.timeLastAccessed = timeLastAccessed;}

    public void setMaxSize(int maxSize){this.maxSize = maxSize;}

    public void setPriority(boolean priority){this.priority = priority;}

    public void setPreemptive(boolean preemptive){this.preemptive = preemptive;}

    public void setSchedulingType(double[] schedulingMetrics){
        this.processingUnits.getFirst().setSchedulingType(schedulingMetrics);
        this.schedulingType = SchedulingType.values()[(int) schedulingMetrics[0]];

        if (this.schedulingType == SchedulingType.RR){
            this.timeQuantum = schedulingMetrics[1];
        }
    }

    public int getMaxSize(){return maxSize;}

    public int getSize(){return size;}

    public boolean getPriority(){return priority;}

    public boolean getPreemptive(){return preemptive;}

    public int getActiveTaskCount(){
        return activeTasks.get();
    }

    public double getTimeQuantum(){return timeQuantum;}

    public enum SchedulingType{
        FCFS, SJF, HP, RR
    }

    public static class Node{
        int identifier;
        int priority;
        Object data = null;
        float processingTime;
        ArrayList<Integer> traversalOrder = new ArrayList<>();
        ArrayList<Integer> remainingTraversals = new ArrayList<>();
        Node next = null;

        public Node(int identifier, int priority, Object data){
            this.identifier = identifier;
            this.priority = priority;
            this.data = data;
            Random rand = new Random();
            float min = 0;
            float max = 11;
            processingTime = 10;//min + rand.nextFloat() * (max-min);
        }
    }
}