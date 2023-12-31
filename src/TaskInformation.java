public class TaskInformation {
    private final String operationName;
    private final String taskDescription;
    private final int numOfAircrafts;

    public TaskInformation(String operationName, String taskDescription, int numOfAircrafts) {
        this.operationName = operationName;
        this.taskDescription = taskDescription;
        this.numOfAircrafts = numOfAircrafts;
    }

    public int getNumOfAircrafts() {
        return this.numOfAircrafts;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public String getTaskDescription() {
        return this.taskDescription;
    }
}
