import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainSystem {
    private final HashSet<Aircraft> aircrafts;
    private final List<DrawerOperation> drawerOperations;
    private final List<Operation> operations;
    private final JsonHandler jsonHandler;

    public MainSystem() {
        this.aircrafts = new HashSet<>();
        this.operations = new ArrayList<>();
        this.drawerOperations = new ArrayList<>();
        this.jsonHandler = new JsonHandler();
        setDataFromJson();

        for (Aircraft aircraft : this.aircrafts) {
            tryAddAircraftToOperations(aircraft);
        }
    }

    public void setDataFromJson() {
        this.jsonHandler.setDataFromAircraftJson(this.aircrafts);
        this.jsonHandler.setDataFromDrawerOperationsJson(this.drawerOperations);
        this.jsonHandler.setDataFromOperationJson(this.operations);
    }

    public boolean hasAircraftId(int id) {
        for (Aircraft aircraft : this.aircrafts) {
            if (aircraft.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public boolean addAircraft(int id) {
        if (hasAircraftId(id)) {
            return false;
        }

        Aircraft aircraft = new Aircraft(id);
        this.aircrafts.add(aircraft);
        this.jsonHandler.addAircraftToJson(id, this.aircrafts);
        tryAddAircraftToOperations(aircraft);
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

    public void assignAircraftToOperation(Aircraft aircraft, Operation operation) {
        if (operation.cannotAssignAircraftToOperation(aircraft)
                || aircraft.operationAssignmentOverlaps(operation)) {
            return;
        }

        operation.assignAircraft(aircraft.getId());
        aircraft.addOperationToList(operation);
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
            if (start.isBefore(op.getStart()) && end.isAfter(op.getEnd())) {
                allOperations.add(op);

            } else if (start.isAfter(op.getStart()) && start.isBefore(op.getEnd())) {
                allOperations.add(op);

            } else if (end.isBefore(op.getEnd()) && end.isAfter(op.getStart())) {
                allOperations.add(op);

            } else if (start.isAfter(op.getStart()) && end.isBefore(op.getEnd())) {
                allOperations.add(op);

            } else if (start.equals(op.getStart()) || end.equals(op.getEnd())) {
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
        for (Aircraft aircraft : this.aircrafts) {
            aircraft.clearOperations();
            tryAddAircraftToOperations(aircraft);
        }
    }

    public void tryAddAircraftToOperations(Aircraft aircraft) {
        for (Operation operation : this.operations) {
            assignAircraftToOperation(aircraft, operation);
        }
    }

    public void assignAllPossibleAircrafts(Operation operation) {
        int assign = 1;
        for (Aircraft aircraft : this.aircrafts) {
            for (Operation op : this.operations) {
                if (operation.getTaskInformation().getOperationName()
                        .equals(op.getTaskInformation().getOperationName())) {
                    continue;
                }

                if (op.isAircraftAssigned(aircraft.getId()) && !operation.canAssignWithTime(op)) {
                    assign = 0;
                    break;
                }
            }
            if (assign == 1 && !operation.reachedCapacity() && !aircraft.operationAssignmentOverlaps(operation)) {
                operation.assignAircraft(aircraft.getId());
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

    public DrawerOperation getDrawerOperationByName(String opName) {
        for (DrawerOperation op : this.drawerOperations) {
            if (op.getTaskInformation().getOperationName().equals(opName)) {
                return op;
            }
        }
        return null;
    }

    public boolean isIntelligenceDrawerOperation(String opName) {
        DrawerOperation op = getDrawerOperationByName(opName);
        if (op == null) {
            return false;
        }
        return op instanceof IntelligenceDrawerOperation;
    }

    public void addIntelligenceDrawerOperation(TaskInformation TI, String cameraType, String flightRoute) {
        if (operationNameExists(TI.getOperationName())) {
            return;
        }
        DrawerOperation drawerOperation = new IntelligenceDrawerOperation(TI, cameraType, flightRoute);
        this.drawerOperations.add(drawerOperation);
        this.jsonHandler.addDrawerOperationToJson(drawerOperation);
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
