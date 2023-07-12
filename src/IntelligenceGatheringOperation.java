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
}
