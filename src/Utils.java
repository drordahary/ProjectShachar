import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;

public class Utils {
    public static void writeJsonToFile(String filename, String jsonData) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));
            bufferedWriter.write(jsonData);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject drawerOperationToJson(DrawerOperation op) {
        JSONObject jsonObject = new JSONObject();
        TaskInformation taskInformation = op.getTaskInformation();
        jsonObject.put("operationName", taskInformation.getOperationName());
        jsonObject.put("taskDescription", taskInformation.getTaskDescription());
        jsonObject.put("numOfAircrafts", taskInformation.getNumOfAircrafts());
        return jsonObject;
    }

    public static JSONObject operationToJson(Operation op) {
        JSONObject jsonObject = new JSONObject();
        TaskInformation taskInformation = op.getTaskInformation();
        jsonObject.put("operationName", taskInformation.getOperationName());
        jsonObject.put("taskDescription", taskInformation.getTaskDescription());
        jsonObject.put("numOfAircrafts", taskInformation.getNumOfAircrafts());
        String startStr = op.getStart().toString();
        startStr = startStr.replace('T', ' ');
        jsonObject.put("start", startStr);
        String endStr = op.getEnd().toString();
        endStr = endStr.replace('T', ' ');
        jsonObject.put("end", endStr);

        HashSet<Integer> assignedAircrafts = op.getAssignedAircrafts();
        JSONArray assignedAircraftsJson = new JSONArray();
        for (Integer id : assignedAircrafts) {
            assignedAircraftsJson.put(id);
        }

        jsonObject.put("assignedAircrafts", assignedAircraftsJson);

        if (op instanceof IntelligenceGatheringOperation) {
            jsonObject.put("cameraType", ((IntelligenceGatheringOperation) op).getCameraType());
            jsonObject.put("flightRoute", ((IntelligenceGatheringOperation) op).getFlightRoute());
        } else {
            jsonObject.put("armamentType", ((AttackOperation) op).getArmamentType());
            jsonObject.put("x", ((AttackOperation) op).getAttackLocation().getX());
            jsonObject.put("y", ((AttackOperation) op).getAttackLocation().getY());
        }
        return jsonObject;
    }

    public static int getIndexByOperationName(JSONArray jsonOperations, String operationName) {
        for (int i = 0; i < jsonOperations.length(); i++) {
            if (((JSONObject) jsonOperations.get(i)).get("operationName").equals(operationName)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isValidDateString(String dateString, String formatPattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            LocalDateTime.parse(dateString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
