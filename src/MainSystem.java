import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainSystem {
    private HashSet<Integer> aircrafts;
    private List<Operation> operations;
    private JSONArray jsonAircraft;
    private JSONArray jsonOperations;

    public MainSystem() {
        this.aircrafts = new HashSet<>();
        this.operations = new ArrayList<>();
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
        Utils.writeJsonToFile("data\\aircraft.json", this.aircrafts.toString());
        tryAddAircraftToOperations(id);
        return true;
    }

    public boolean addOperation(Operation operation) {
        // Checking if the operation already exists (Check by operation name)
        for (Operation op : this.operations) {
            if (op.getTaskInformation().getOperationName()
                    .equals(operation.getTaskInformation().getOperationName())) {
                return false;
            }
        }
        assignAllPossibleAircrafts(operation);
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
        int idx = Utils.getIndexByOperationName(this.jsonOperations,
                operation.getTaskInformation().getOperationName());
        if (idx != -1) {
            this.jsonOperations.remove(idx);
        }
        this.jsonOperations.put(Utils.operationToJson(operation));
        Utils.writeJsonToFile("data\\operation.json", this.jsonOperations.toString());
    }

    public Operation getOperationByName(String operationName) {
        for (Operation op : this.operations) {
            if (op.getTaskInformation().getOperationName().equals(operationName)) {
                return op;
            }
        }
        return null;
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
            tryAddAircraftToOperations(aircraftId);
        }
    }

    public void tryAddAircraftToOperations(int id) {
        for (Operation operation : this.operations) {
            assignAircraftToOperation(id, operation);
        }
    }

    public void assignAllPossibleAircrafts(Operation operation) {
        for (Integer aircraftId : this.aircrafts) {
            for (Operation op : this.operations) {
                if (op.isAircraftAssigned(aircraftId) && !operation.canAssignWithTime(op)) {
                    break;
                }
            }
            operation.assignAircraft(aircraftId);
        }
        this.jsonOperations.put(Utils.operationToJson(operation));
        Utils.writeJsonToFile("data\\operation.json", this.jsonOperations.toString());
    }
}
