import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;

public class AttackOperation extends Operation {
    private String armamentType;
    private Point attackLocation; // (x, y) location

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