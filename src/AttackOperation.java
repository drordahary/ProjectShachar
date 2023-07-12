import java.time.LocalDateTime;

public class AttackOperation extends Operation {
    private final String armamentType;
    private final Point attackLocation; // (x, y) location

    public AttackOperation(TaskInformation taskInformation, String armamentType,
                           Point attackLocation, LocalDateTime start, LocalDateTime end) {
        super(taskInformation, start, end);
        this.armamentType = armamentType;
        this.attackLocation = attackLocation;
    }

    public String getArmamentType() {
        return this.armamentType;
    }

    public Point getAttackLocation() {
        return this.attackLocation;
    }
}