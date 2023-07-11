import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainSystem {
    private HashSet<Integer> aircrafts;
    private List<Operation> operations;

    public MainSystem() {
        this.aircrafts = new HashSet<>();
        this.operations = new ArrayList<>();
    }

    public boolean addAircraft(int id) {
        if (this.aircrafts.contains(id)) {
            return false;
        }
        this.aircrafts.add(id);
        return true;
    }

    public void assignAircraftToOperation(int id, Operation operation) {
        if (!this.operations.contains(operation) || operation.isAircraftAssigned(id)
        || operation.isOperationReady()) {
            return;
        }

        for (Operation op : this.operations) {
            if (op.isAircraftAssigned(id) && !operation.canAssignWithTime(op)) {
                return;
            }
        }
        operation.assignAircraft(id);
    }

    public List<Operation> getAllOperationsWithinTime(LocalDateTime start, LocalDateTime end) {
        List<Operation> allOperations = new ArrayList<>();
        for (Operation op : this.operations) {
            if (op.getStart().isAfter(start) || op.getEnd().isBefore(end)
            || (op.getStart().isBefore(start) && op.getEnd().isAfter(end))) {
                allOperations.add(op);
            }
        }
        return allOperations;
    }

    public boolean isOperationReady(Operation op) {
        return op.isOperationReady();
    }

    public void changeOperationTime(Operation op, LocalDateTime newStart, LocalDateTime newEnd) {
        op.setStart(newStart);
        op.setEnd(newEnd);
        op.removeAllAssignedAircrafts();

        // It's possible that because the time changes, it will open up
        // other operations for all aircraft
        for (Integer aircraftId : this.aircrafts) {
            for (Operation operation : this.operations) {
                assignAircraftToOperation(aircraftId, operation);
            }
        }
    }
}
