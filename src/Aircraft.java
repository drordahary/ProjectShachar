import java.util.ArrayList;
import java.util.List;

public class Aircraft {
    private final int id;
    private final List<Operation> assignedOperations;

    public Aircraft(int id) {
        this.id = id;
        this.assignedOperations = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    public boolean isAssignedToOperation(String operationName) {
        for (Operation op : assignedOperations) {
            if (op.getTaskInformation().getOperationName().equals(operationName)) {
                return true;
            }
        }
        return false;
    }

    public void clearOperations() {
        this.assignedOperations.clear();
    }

    public void addOperationToList(Operation operation) {
        this.assignedOperations.add(operation);
    }

    public boolean operationAssignmentOverlaps(Operation operation) {
        for (Operation op : this.assignedOperations) {
            if (!op.canAssignWithTime(operation)) {
                return true;
            }
        }
        return false;
    }
}
