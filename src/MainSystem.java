import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainSystem {
    private final HashSet<Integer> aircrafts;
    private final List<DrawerOperation> drawerOperations;
    private final List<Operation> operations;
    private final JsonHandler jsonHandler;

    public MainSystem() {
        this.aircrafts = new HashSet<>();
        this.operations = new ArrayList<>();
        this.drawerOperations = new ArrayList<>();
        this.jsonHandler = new JsonHandler();
        setDataFromJson();
    }

    public void setDataFromJson() {
        this.jsonHandler.setDataFromAircraftJson(this.aircrafts);
        this.jsonHandler.setDataFromDrawerOperationsJson(this.drawerOperations);
        this.jsonHandler.setDataFromOperationJson(this.operations);
    }

    public boolean addAircraft(int id) {
        if (this.aircrafts.contains(id)) {
            return false;
        }

        this.aircrafts.add(id);
        this.jsonHandler.addAircraftToJson(id, this.aircrafts);
        tryAddAircraftToOperations(id);
        return true;
    }

    public boolean addOperation(Operation operation) {
        // Checking if the operation already exists (Check by operation name)
        if (operationNameExists(operation.getTaskInformation().getOperationName())) {
            return false;
        }

        this.operations.add(operation);
        assignAllPossibleAircrafts(operation);
        return true;
    }

    public boolean cannotAssignAircraftToOperation(int id, Operation operation) {
        return !this.operations.contains(operation) || operation.isAircraftAssigned(id)
                || operation.isOperationReady() || operation.reachedCapacity();
    }

    public boolean operationAssignmentOverlaps(int id, Operation operation) {
        for (Operation op : this.operations) {
            if (op.isAircraftAssigned(id) && !operation.canAssignWithTime(op)) {
                return true;
            }
        }
        return false;
    }

    public void assignAircraftToOperation(int id, Operation operation) {
        if (cannotAssignAircraftToOperation(id, operation) || operationAssignmentOverlaps(id, operation)) {
            return;
        }

        operation.assignAircraft(id);
        this.jsonHandler.addOperationToJson(operation);
    }

    public Operation getOperationByName(String operationName) {
        for (Operation op : this.operations) {
            if (op.getTaskInformation().getOperationName().equals(operationName)) {
                return op;
            }
        }
        return null;
    }

    public boolean operationNameExists(String operationName) {
        for (Operation op : this.operations) {
            if (op.getTaskInformation().getOperationName()
                    .equals(operationName)) {
                return true;
            }
        }
        for (DrawerOperation op : this.drawerOperations) {
            if (op.getTaskInformation().getOperationName()
                    .equals(operationName)) {
                return true;
            }
        }
        return false;
    }

    public List<Operation> getAllOperationsWithinTime(LocalDateTime start, LocalDateTime end) {
        List<Operation> allOperations = new ArrayList<>();
        for (Operation op : this.operations) {
            if (((op.getStart().isAfter(start) || op.getStart().equals(start))
                    && (op.getEnd().isBefore(end) || op.getEnd().equals(end)))
            || ((op.getStart().isBefore(start) || op.getStart().equals(start))
                    && (op.getEnd().isAfter(end) || op.getEnd().equals(end)))) {
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
            tryAddAircraftToOperations(aircraftId);
        }
    }

    public void tryAddAircraftToOperations(int id) {
        for (Operation operation : this.operations) {
            assignAircraftToOperation(id, operation);
        }
    }

    public void assignAllPossibleAircrafts(Operation operation) {
        int assign = 1;
        for (Integer aircraftId : this.aircrafts) {
            for (Operation op : this.operations) {
                if (operation.getTaskInformation().getOperationName()
                        .equals(op.getTaskInformation().getOperationName())) {
                    continue;
                }

                if (op.isAircraftAssigned(aircraftId) && !operation.canAssignWithTime(op)) {
                    assign = 0;
                    break;
                }
            }
            if (assign == 1) {
                operation.assignAircraft(aircraftId);
            }
            assign = 1;
        }
        this.jsonHandler.addNewOperationToJson(operation);
    }

    public void getAllUnpreparedOperations(int xHours) {
        for (Operation op : this.operations) {
            if (op.isOperationInXTime(xHours) && !op.isOperationReady()) {
                System.out.println(op.getTaskInformation().getOperationName());
            }
        }
    }

    public void addDrawerOperation(String operationName, String taskDescription, int numOfAircrafts) {
        if (operationNameExists(operationName)) {
            return;
        }
        TaskInformation taskInformation = new TaskInformation(operationName, taskDescription, numOfAircrafts);
        DrawerOperation drawerOperation = new DrawerOperation(taskInformation);
        this.drawerOperations.add(drawerOperation);
        this.jsonHandler.addDrawerOperationToJson(drawerOperation);
    }

    public boolean createAttackOperation(String opName, LocalDateTime start, LocalDateTime end,
                                      String armamentType, Point attackLocation) {
        AttackOperation attackOperation = null;
        for (DrawerOperation op : this.drawerOperations) {
            if (op.getTaskInformation().getOperationName().equals(opName)) {
                attackOperation = op.createAttackOperation(start, end, armamentType, attackLocation);
                this.drawerOperations.remove(op);
                this.jsonHandler.handleDrawerOperationConversion(op);
                break;
            }
        }
        if (attackOperation == null) {
            return false;
        }

        return addOperation(attackOperation);
    }

    public boolean createIntelligenceGatheringOperation(String opName, LocalDateTime start, LocalDateTime end,
                                         String cameraType, String flightRoute) {
        IntelligenceGatheringOperation intelligenceGatheringOperation = null;
        for (DrawerOperation op : this.drawerOperations) {
            if (op.getTaskInformation().getOperationName().equals(opName)) {
                intelligenceGatheringOperation = op.createIntelligenceGatheringOperation(start, end,
                        cameraType, flightRoute);
                this.drawerOperations.remove(op);
                this.jsonHandler.handleDrawerOperationConversion(op);
                break;
            }
        }
        if (intelligenceGatheringOperation == null) {
            return false;
        }
        return addOperation(intelligenceGatheringOperation);
    }

    public boolean drawerOperationNameExists(String operationName) {
        for (DrawerOperation op : this.drawerOperations) {
            if (op.getTaskInformation().getOperationName().equals(operationName)) {
                return true;
            }
        }
        return false;
    }
}
