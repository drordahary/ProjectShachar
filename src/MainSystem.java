import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainSystem {
    private final HashSet<Integer> aircrafts;
    private final List<Operation> operations;
    private final List<DrawerOperation> drawerOperations;
    private JSONArray jsonAircraft;
    private JSONArray jsonOperations;
    private JSONArray jsonDrawerOperations;

    public MainSystem() {
        this.aircrafts = new HashSet<>();
        this.operations = new ArrayList<>();
        this.drawerOperations = new ArrayList<>();
        setDataFromJson();
    }

    public void setDataFromJson() {
        // Aircrafts
        String resourceName = "data\\aircraft.json";
        InputStream is = MainSystem.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        JSONTokener tokener = new JSONTokener(is);
        this.jsonAircraft = new JSONArray(tokener);
        for (int i = 0; i < this.jsonAircraft.length(); i++) {
            this.aircrafts.add(this.jsonAircraft.getInt(i));
        }

        // Drawer operations
        resourceName = "data\\drawerOperation.json";
        is = MainSystem.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        tokener = new JSONTokener(is);
        this.jsonDrawerOperations = new JSONArray(tokener);
        for (int i = 0; i < this.jsonDrawerOperations.length(); i++) {
            JSONObject currentObject = this.jsonOperations.getJSONObject(i);

            String operationName = currentObject.getString("operationName");
            String taskDescription = currentObject.getString("taskDescription");
            int numOfAircrafts = currentObject.getInt("numOfAircrafts");

            TaskInformation taskInformation = new TaskInformation(operationName,
                    taskDescription, numOfAircrafts);
            this.drawerOperations.add(new DrawerOperation(taskInformation));
        }

        // Operations
        resourceName = "data\\operation.json";
        is = MainSystem.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        tokener = new JSONTokener(is);
        this.jsonOperations = new JSONArray(tokener);
        for (int i = 0; i < this.jsonOperations.length(); i++) {
            JSONObject currentObject = this.jsonOperations.getJSONObject(i);
            String operationName = currentObject.getString("operationName");
            String taskDescription = currentObject.getString("taskDescription");
            int numOfAircrafts = currentObject.getInt("numOfAircrafts");
            TaskInformation taskInformation = new TaskInformation(operationName,
                    taskDescription, numOfAircrafts);

            // Date in yyyy-MM-dd HH:mm
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime start = LocalDateTime.parse(currentObject.getString("start"), formatter);
            LocalDateTime end = LocalDateTime.parse(currentObject.getString("end"), formatter);

            JSONArray jsonAssignedAircrafts = currentObject.getJSONArray("assignedAircrafts");
            HashSet<Integer> constructedAircraftsSet = new HashSet<>();
            for (int j = 0; j < jsonAssignedAircrafts.length(); j++) {
                constructedAircraftsSet.add(jsonAssignedAircrafts.getInt(j));
            }

            // Intelligence gathering operation
            if (currentObject.has("cameraType")) {
                String cameraType = currentObject.getString("cameraType");
                String flightRoute = currentObject.getString("flightRoute");
                Operation op = new IntelligenceGatheringOperation(taskInformation, cameraType,
                        flightRoute, start, end);
                op.setAssignedAircrafts(constructedAircraftsSet);
                this.operations.add(op);
            } else {
                String armamentType = currentObject.getString("armamentType");
                double x = currentObject.getDouble("x");
                double y = currentObject.getDouble("y");
                Point p = new Point(x, y);
                Operation op = new AttackOperation(taskInformation, armamentType, p, start, end);
                op.setAssignedAircrafts(constructedAircraftsSet);
                this.operations.add(op);
            }
        }
    }

    public boolean addAircraft(int id) {
        if (this.aircrafts.contains(id)) {
            return false;
        }
        this.aircrafts.add(id);
        this.jsonAircraft.put(id);
        Utils.writeJsonToFile("src\\data\\aircraft.json", this.aircrafts.toString());
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

    public void assignAircraftToOperation(int id, Operation operation) {
        if (!this.operations.contains(operation) || operation.isAircraftAssigned(id)
        || operation.isOperationReady() || operation.reachedCapacity()) {
            return;
        }

        for (Operation op : this.operations) {
            if (op.isAircraftAssigned(id) && !operation.canAssignWithTime(op)) {
                return;
            }
        }
        operation.assignAircraft(id);
        int idx = Utils.getIndexByOperationName(this.jsonOperations,
                operation.getTaskInformation().getOperationName());
        if (idx != -1) {
            this.jsonOperations.remove(idx);
        }
        this.jsonOperations.put(Utils.operationToJson(operation));
        Utils.writeJsonToFile("src\\data\\operation.json", this.jsonOperations.toString());
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
        this.jsonOperations.put(Utils.operationToJson(operation));
        Utils.writeJsonToFile("src\\data\\operation.json", this.jsonOperations.toString());
    }

    public void getAllUnpreparedOperations(int xHours) {
        for (Operation op : this.operations) {
            if (op.isOperationInXTime(xHours) && !op.isOperationReady()) {
                System.out.println(op.getTaskInformation().getOperationName());
            }
        }
    }

    public boolean addDrawerOperation(String operationName, String taskDescription, int numOfAircrafts) {
        if (operationNameExists(operationName)) {
            return false;
        }
        TaskInformation taskInformation = new TaskInformation(operationName, taskDescription, numOfAircrafts);
        DrawerOperation drawerOperation = new DrawerOperation(taskInformation);
        this.drawerOperations.add(drawerOperation);
        this.jsonDrawerOperations.put(Utils.drawerOperationToJson(drawerOperation));
        Utils.writeJsonToFile("src\\data\\drawerOperation.json", this.jsonDrawerOperations.toString());
        return true;
    }

    public boolean createAttackOperation(String opName, LocalDateTime start, LocalDateTime end,
                                      String armamentType, Point attackLocation) {
        AttackOperation attackOperation = null;
        for (DrawerOperation op : this.drawerOperations) {
            if (op.getTaskInformation().getOperationName().equals(opName)) {
                attackOperation = op.createAttackOperation(start, end, armamentType, attackLocation);
                this.drawerOperations.remove(op);
                this.jsonDrawerOperations
                        .remove(Utils.getIndexByOperationName(this.jsonDrawerOperations, opName));
                Utils.writeJsonToFile("src\\data\\drawerOperation.json",
                        this.jsonDrawerOperations.toString());
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
