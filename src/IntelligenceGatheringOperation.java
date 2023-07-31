import java.time.LocalDateTime;

public class IntelligenceGatheringOperation extends Operation {
    private final String cameraType;
    private final String flightRoute;

    public IntelligenceGatheringOperation(TaskInformation taskInformation, String cameraType,
                                          String flightRoute, LocalDateTime start, LocalDateTime end) {
        super(taskInformation, start, end);
        this.cameraType = cameraType;
        this.flightRoute = flightRoute;
    }

    public String getCameraType() {
        return this.cameraType;
    }

    public String getFlightRoute() {
        return this.flightRoute;
    }

    public boolean isOperationReady() {
        if (LocalDateTime.now().isAfter(this.start)) {
            return false;
        }
        int requiredAircraftsCount = taskInformation.getNumOfAircrafts();
        int amountCurrently = assignedAircrafts.size();
        boolean hasRequiredAircraftCount =
                (int)Math.ceil((((double)requiredAircraftsCount) / amountCurrently) * 100) >= 80;
        boolean isInTime = LocalDateTime.now().isAfter(this.start.minusHours(3));
        return hasRequiredAircraftCount && isInTime;
    }

    public boolean cannotAssignAircraftToOperation(Aircraft aircraft) {
        return isAircraftAssigned(aircraft.getId())
                || isOperationReady() || reachedCapacity();
    }
}
