import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;

public class IntelligenceGatheringOperation extends Operation {
    private String cameraType;
    private String flightRoute;

    public IntelligenceGatheringOperation(TaskInformation taskInformation, String cameraType,
                                          String flightRoute, LocalDateTime start, LocalDateTime end) {
        super(taskInformation, start, end);
        this.cameraType = cameraType;
        this.flightRoute = flightRoute;
    }
}
