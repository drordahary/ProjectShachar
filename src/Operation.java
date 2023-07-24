import java.time.LocalDateTime;
import java.util.HashSet;

public class Operation {
    protected final TaskInformation taskInformation;
    protected HashSet<Integer> assignedAircrafts;
    protected LocalDateTime start;
    protected LocalDateTime end;

    public Operation(TaskInformation taskInformation, LocalDateTime start, LocalDateTime end) {
        this.taskInformation = taskInformation;
        this.assignedAircrafts = new HashSet<>();
        this.start = start;
        this.end = end;
    }

    public void setAssignedAircrafts(HashSet<Integer> assignedAircrafts) {
        this.assignedAircrafts = assignedAircrafts;
    }

    public HashSet<Integer> getAssignedAircrafts() {
        return this.assignedAircrafts;
    }

    public LocalDateTime getStart() {
        return this.start;
    }

    public LocalDateTime getEnd() {
        return this.end;
    }

    public TaskInformation getTaskInformation() {
        return this.taskInformation;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void removeAllAssignedAircrafts() {
        this.assignedAircrafts.clear();
    }

    public boolean isAircraftAssigned(int id) {
        return assignedAircrafts.contains(id);
    }

    public void assignAircraft(int id) {
        this.assignedAircrafts.add(id);
    }

    public boolean canAssignWithTime(Operation other) {
        if ((this.start.isAfter(other.start) && this.start.isBefore(other.end))
        || (other.start.isAfter(this.start) && other.start.isBefore(this.end))) {
            return false;
        }
        if ((this.end.isBefore(other.end) && this.end.isAfter(other.start))
        || (other.end.isBefore(this.end) && other.end.isAfter(this.start))) {
            return false;
        }
        return this.start.isAfter(other.end.minusHours(-1)) ||
                this.end.isBefore(other.start.minusHours(1));
    }

    public boolean isOperationReady() {
        return false;
    }

    public boolean isOperationInXTime(int x) {
        return LocalDateTime.now().isAfter(this.start.minusHours(x))
                && LocalDateTime.now().isBefore(this.start);
    }

    public boolean reachedCapacity() {
        return this.getTaskInformation().getNumOfAircrafts() == this.assignedAircrafts.size();
    }
}
