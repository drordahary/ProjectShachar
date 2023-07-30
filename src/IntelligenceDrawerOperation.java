public class IntelligenceDrawerOperation extends DrawerOperation {
    private final String cameraType;
    private final String flightRoute;
    public IntelligenceDrawerOperation(TaskInformation TI, String cameraType, String flightRoute) {
        super(TI);
        this.cameraType = cameraType;
        this.flightRoute = flightRoute;
    }

    public String getCameraType() {
        return cameraType;
    }

    public String getFlightRoute() {
        return flightRoute;
    }
}
