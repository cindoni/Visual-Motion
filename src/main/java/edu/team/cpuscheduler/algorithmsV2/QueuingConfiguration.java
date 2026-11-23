import java.util.Map;
import java.util.HashMap;

public class QueuingConfiguration {

    ProcessingUnit2[] inputProcUnits;
    ProcessingUnit2[] interiorProcUnits;
    ProcessingUnit2[] supportingProcUnits;
    ProcessingUnit2[] outputProcUnits;

    public void setTrivialConfiguration(int maxQueueSize, double[] schedulingMetrics, Map<String, Boolean> processingUnitConditions){
        inputProcUnits = new ProcessingUnit2[1];
        interiorProcUnits = new ProcessingUnit2[0];
        supportingProcUnits = new ProcessingUnit2[0];
        outputProcUnits = new ProcessingUnit2[0];

        //ProcessingUnit2(processing unit id, processing unit type, maxQueueSize, timeQuantum, processing unit conditions)

        for (int i = 0; i < inputProcUnits.length; i++){
            inputProcUnits[i] = new ProcessingUnit2(i, 0, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < interiorProcUnits.length; i++){
            interiorProcUnits[i] = new ProcessingUnit2(i, 1, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < supportingProcUnits.length; i++){
            supportingProcUnits[i] = new ProcessingUnit2(i, 2, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < outputProcUnits.length; i++){
            outputProcUnits[i] = new ProcessingUnit2(i, 3, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }
    }

    public void setSimpleConfiguration(int maxQueueSize, double[] schedulingMetrics, Map<String, Boolean> processingUnitConditions){
        inputProcUnits = new ProcessingUnit2[1];
        interiorProcUnits = new ProcessingUnit2[1];
        supportingProcUnits = new ProcessingUnit2[0];
        outputProcUnits = new ProcessingUnit2[1];

        //ProcessingUnit2(processing unit id, processing unit type, maxQueueSize, timeQuantum, processing unit conditions)

        for (int i = 0; i < inputProcUnits.length; i++){
            inputProcUnits[i] = new ProcessingUnit2(i, 0, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < interiorProcUnits.length; i++){
            interiorProcUnits[i] = new ProcessingUnit2(i, 1, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < supportingProcUnits.length; i++){
            supportingProcUnits[i] = new ProcessingUnit2(i, 2, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < outputProcUnits.length; i++){
            outputProcUnits[i] = new ProcessingUnit2(i, 3, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        inputProcUnits[0].setNextMainProcessingUnit(interiorProcUnits[0]);

        interiorProcUnits[0].setNextMainProcessingUnit(outputProcUnits[0]);
    }

    public void setComplexConfiguration(int maxQueueSize, double[] schedulingMetrics, Map<String, Boolean> processingUnitConditions){
        inputProcUnits = new ProcessingUnit2[2];
        interiorProcUnits = new ProcessingUnit2[7];
        supportingProcUnits = new ProcessingUnit2[6];
        outputProcUnits = new ProcessingUnit2[3];

        //ProcessingUnit2(processing unit id, processing unit type, maxQueueSize, timeQuantum, processing unit conditions)

        for (int i = 0; i < inputProcUnits.length; i++){
            inputProcUnits[i] = new ProcessingUnit2(i, 0, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < interiorProcUnits.length; i++){
            interiorProcUnits[i] = new ProcessingUnit2(i, 1, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < supportingProcUnits.length; i++){
            supportingProcUnits[i] = new ProcessingUnit2(i, 2, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        for (int i = 0; i < outputProcUnits.length; i++){
            outputProcUnits[i] = new ProcessingUnit2(i, 3, maxQueueSize, schedulingMetrics[4], processingUnitConditions);
        }

        inputProcUnits[0].setNextMainProcessingUnit(interiorProcUnits[0]);
        inputProcUnits[0].setSupportingProcessingUnit(supportingProcUnits[0]);
        supportingProcUnits[0].setNextMainProcessingUnit(interiorProcUnits[0]);

        inputProcUnits[1].setNextMainProcessingUnit(interiorProcUnits[0]);
        inputProcUnits[1].setSupportingProcessingUnit(supportingProcUnits[1]);
        supportingProcUnits[1].setNextMainProcessingUnit(interiorProcUnits[0]);

        interiorProcUnits[0].setNextMainProcessingUnit(interiorProcUnits[1]);
        interiorProcUnits[0].setNextMainProcessingUnit(interiorProcUnits[2]);

        interiorProcUnits[1].setNextMainProcessingUnit(interiorProcUnits[3]);
        interiorProcUnits[1].setSupportingProcessingUnit(supportingProcUnits[2]);
        supportingProcUnits[2].setNextMainProcessingUnit(interiorProcUnits[3]);

        interiorProcUnits[2].setNextMainProcessingUnit(interiorProcUnits[4]);
        interiorProcUnits[2].setSupportingProcessingUnit(supportingProcUnits[3]);
        supportingProcUnits[3].setNextMainProcessingUnit(interiorProcUnits[4]);

        interiorProcUnits[3].setNextMainProcessingUnit(interiorProcUnits[5]);
        interiorProcUnits[3].setSupportingProcessingUnit(supportingProcUnits[4]);
        supportingProcUnits[4].setNextMainProcessingUnit(interiorProcUnits[5]);

        interiorProcUnits[4].setNextMainProcessingUnit(interiorProcUnits[6]);
        interiorProcUnits[4].setSupportingProcessingUnit(supportingProcUnits[5]);
        supportingProcUnits[5].setNextMainProcessingUnit(interiorProcUnits[6]);

        interiorProcUnits[5].setNextMainProcessingUnit(outputProcUnits[0]);
        interiorProcUnits[5].setNextMainProcessingUnit(outputProcUnits[1]);
        interiorProcUnits[6].setNextMainProcessingUnit(outputProcUnits[1]);
        interiorProcUnits[6].setNextMainProcessingUnit(outputProcUnits[2]);
    }
}
