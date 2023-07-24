import java.time.LocalDateTime;

public class DrawerOperation {
    private TaskInformation taskInformation;

    public DrawerOperation(TaskInformation taskInformation) {
        this.taskInformation = taskInformation;
    }

    public TaskInformation getTaskInformation() {
        return this.taskInformation;
    }

    public AttackOperation createAttackOperation(LocalDateTime start, LocalDateTime end,
                                     String armamentType, Point attackLocation) {
        return new AttackOperation(this.taskInformation, armamentType, attackLocation, start, end);
    }

    public IntelligenceGatheringOperation createIntelligenceGatheringOperation(LocalDateTime start, LocalDateTime end,
                                                                               String cameraType, String flightRoute) {
        return new IntelligenceGatheringOperation(this.taskInformation, cameraType, flightRoute, start, end);
    }
}
