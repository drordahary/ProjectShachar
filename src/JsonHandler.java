import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

public class JsonHandler {
    private JSONArray jsonAircraft;
    private JSONArray jsonOperations;
    private JSONArray jsonDrawerOperations;

    private final String JSON_AIRCRAFT_PATH = "data\\aircraft.json";
    private final String JSON_DRAWER_OPERATION_PATH = "data\\drawerOperation.json";
    private final String JSON_OPERATION_PATH = "data\\operation.json";
    private final String SRC_DIRECTORY = "src\\";

    public void setDataFromAircraftJson(HashSet<Aircraft> aircrafts) {
        String resourceName = JSON_AIRCRAFT_PATH;
        InputStream is = MainSystem.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        JSONTokener tokener = new JSONTokener(is);
        this.jsonAircraft = new JSONArray(tokener);
        for (int i = 0; i < this.jsonAircraft.length(); i++) {
            aircrafts.add(new Aircraft(this.jsonAircraft.getInt(i)));
        }
    }

    public void setDataFromDrawerOperationsJson(List<DrawerOperation> drawerOperations) {
        String resourceName = JSON_DRAWER_OPERATION_PATH;
        InputStream is = MainSystem.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        JSONTokener tokener = new JSONTokener(is);
        this.jsonDrawerOperations = new JSONArray(tokener);
        for (int i = 0; i < this.jsonDrawerOperations.length(); i++) {
            JSONObject currentObject = this.jsonOperations.getJSONObject(i);

            String operationName = currentObject.getString("operationName");
            String taskDescription = currentObject.getString("taskDescription");
            int numOfAircrafts = currentObject.getInt("numOfAircrafts");

            TaskInformation taskInformation = new TaskInformation(operationName,
                    taskDescription, numOfAircrafts);
            if (currentObject.has("cameraType")) {
                String cameraType = currentObject.getString("cameraType");
                String flightRoute = currentObject.getString("flightRoute");
                drawerOperations.add(new IntelligenceDrawerOperation(taskInformation, cameraType, flightRoute));
            } else {
                drawerOperations.add(new DrawerOperation(taskInformation));
            }
        }
    }

    public void setDataFromOperationJson(List<Operation> operations) {
        String resourceName = JSON_OPERATION_PATH;
        InputStream is = MainSystem.class.getResourceAsStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        JSONTokener tokener = new JSONTokener(is);
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
                operations.add(op);
            } else {
                String armamentType = currentObject.getString("armamentType");
                double x = currentObject.getDouble("x");
                double y = currentObject.getDouble("y");
                Point p = new Point(x, y);
                Operation op = new AttackOperation(taskInformation, armamentType, p, start, end);
                op.setAssignedAircrafts(constructedAircraftsSet);
                operations.add(op);
            }
        }
    }

    public void addAircraftToJson(int id, HashSet<Aircraft> aircrafts) {
        this.jsonAircraft.put(id);
        HashSet<Integer> ids = new HashSet<>();
        for (Aircraft aircraft : aircrafts) {
            ids.add(aircraft.getId());
        }
        Utils.writeJsonToFile(SRC_DIRECTORY + JSON_AIRCRAFT_PATH, ids.toString());
    }

    public void addOperationToJson(Operation operation) {
        int idx = Utils.getIndexByOperationName(this.jsonOperations,
                operation.getTaskInformation().getOperationName());
        if (idx != -1) {
            this.jsonOperations.remove(idx);
        }
        this.jsonOperations.put(Utils.operationToJson(operation));
        Utils.writeJsonToFile(SRC_DIRECTORY + JSON_OPERATION_PATH, this.jsonOperations.toString());
    }

    public void addNewOperationToJson(Operation operation) {
        this.jsonOperations.put(Utils.operationToJson(operation));
        Utils.writeJsonToFile(SRC_DIRECTORY + JSON_OPERATION_PATH, this.jsonOperations.toString());
    }

    public void addDrawerOperationToJson(DrawerOperation drawerOperation) {
        if (drawerOperation instanceof IntelligenceDrawerOperation) {
            this.jsonDrawerOperations.put(Utils.intelligenceDrawerToJson((IntelligenceDrawerOperation) drawerOperation));
        } else {
            this.jsonDrawerOperations.put(Utils.drawerOperationToJson(drawerOperation));
        }
        Utils.writeJsonToFile(SRC_DIRECTORY + JSON_DRAWER_OPERATION_PATH,
                this.jsonDrawerOperations.toString());
    }

    public void handleDrawerOperationConversion(DrawerOperation drawerOperation) {
        String opName = drawerOperation.getTaskInformation().getOperationName();
        this.jsonDrawerOperations
                .remove(Utils.getIndexByOperationName(this.jsonDrawerOperations, opName));
        Utils.writeJsonToFile(SRC_DIRECTORY + JSON_DRAWER_OPERATION_PATH,
                this.jsonDrawerOperations.toString());
    }
}
