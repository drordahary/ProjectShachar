import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static MainSystem mainSystem = new MainSystem();
    public static String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    public static int INTELLIGENCE_GATHERING = 1;
    public static int ATTACK_OPERATION = 2;

    public static String ADD_AIRCRAFT = "1";
    public static String ADD_OPERATION = "2";
    public static String GET_OP_TIME_SPAN = "3";
    public static String CHECK_OP_READY = "4";
    public static String CHANGE_OP_TIME = "5";
    public static String ADD_DRAWER_OP = "6";
    public static String ADD_INTEL_DRAWER_OP = "7";
    public static String CREATE_FROM_DRAWER = "8";
    public static String GET_UNPREPARED_OPS = "9";
    public static String EXIT = "10";

    public static void main(String[] args) {
        showMenu();
        while (handleMenuChooser()) {
            System.out.println();
            showMenu();
        }
    }

    public static void showMenu() {
        System.out.println("Choose one of the following:");
        System.out.println("1. Add aircraft");
        System.out.println("2. Add operation");
        System.out.println("3. Get all operations within a time span");
        System.out.println("4. Check if an operation is ready");
        System.out.println("5. Change operation time");
        System.out.println("6. Add drawer operation");
        System.out.println("7. Add intelligence drawer operation");
        System.out.println("8. Create Intelligence gathering/Attack operation from drawer operation");
        System.out.println("9. Get all unprepared operations in X hours in advance");
        System.out.println("10. Exit");
    }

    public static boolean handleMenuChooser() {
        Scanner sc = new Scanner(System.in);
        String choice = sc.nextLine();
        if (choice.equals(ADD_AIRCRAFT)) {
            handleAddAircraft();
            return true;
        }
        if (choice.equals(ADD_OPERATION)) {
            handleAddOperation();
            return true;
        }
        if (choice.equals(GET_OP_TIME_SPAN)) {
            handleGetOperationsTimeSpan();
            return true;
        }
        if (choice.equals(CHECK_OP_READY)) {
            handleCheckOperationReady();
            return true;
        }
        if (choice.equals(CHANGE_OP_TIME)) {
            handleChangeOperationTime();
            return true;
        }
        if (choice.equals(ADD_DRAWER_OP)) {
            handleAddDrawerOperation();
            return true;
        }
        if (choice.equals(ADD_INTEL_DRAWER_OP)) {
            handleAddIntelligenceDrawerOperation();
            return true;
        }
        if (choice.equals(CREATE_FROM_DRAWER)) {
            handleCreateFromDrawerOperation();
            return true;
        }
        if (choice.equals(GET_UNPREPARED_OPS)) {
            handleGetUnpreparedOperations();
            return true;
        }
        return !choice.equals(EXIT);
    }

    public static void handleAddAircraft() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter aircraft ID: ");
        String input = sc.nextLine();
        try {
            int id = Integer.parseInt(input);
            if (!mainSystem.addAircraft(id)) {
                System.out.println("Aircraft ID already exists");
            }
        } catch (NumberFormatException e) {
            System.out.println("Not a number");
        }
    }

    public static TaskInformation getTaskInformation() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter operation name: ");
        String operationName = sc.nextLine();
        if (mainSystem.operationNameExists(operationName)) {
            System.out.println("Operation name already exists");
            return null;
        }

        System.out.print("Enter task description: ");
        String taskDescription = sc.nextLine();

        System.out.print("Enter number of required aircrafts: ");
        String input = sc.nextLine();
        int count;
        try {
            count = Integer.parseInt(input);
            if (count <= 0) {
                System.out.println("Invalid number of required aircrafts");
                return null;
            }
        } catch (NumberFormatException e) {
            System.out.println("Not a number");
            return null;
        }
        return new TaskInformation(operationName, taskDescription, count);
    }

    public static LocalDateTime[] getDates(boolean isForOperation) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter starting date (yyyy-MM-dd HH:mm format): ");
        String startString = sc.nextLine();
        if (!Utils.isValidDateString(startString, DATE_FORMAT)) {
            System.out.println("Invalid date");
            return null;
        }
        System.out.print("Enter ending date: ");
        String endString = sc.nextLine();
        if (!Utils.isValidDateString(endString, DATE_FORMAT)) {
            System.out.println("Invalid date");
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime start = LocalDateTime.parse(startString, formatter);
        LocalDateTime end = LocalDateTime.parse(endString, formatter);

        if (start.isAfter(end) || start.equals(end)) {
            System.out.println("Invalid end date - start should be before end");
            return null;
        }

        if (isForOperation && start.isBefore(LocalDateTime.now())) {
            System.out.println("Invalid date - can only be in future");
        }

        return new LocalDateTime[] {start, end};
    }

    public static void handleAddOperation() {
        TaskInformation TI = getTaskInformation();
        if (TI == null) {
            return;
        }

        LocalDateTime[] dates = getDates(true);
        if (dates == null) {
            return;
        }

        int type = getOperationType();
        if (type == Integer.MIN_VALUE) {
            return;
        }

        if (type == INTELLIGENCE_GATHERING) {
            handleIntelligenceGatheringChoice(dates[0], dates[1], TI);
        } else {
            handleAttackChoice(dates[0], dates[1], TI);
        }
    }

    public static int getOperationType() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter operation type (1 - Intelligence gathering, 2 - Attack): ");
        String input = sc.nextLine();
        int type;
        try {
            type = Integer.parseInt(input);
            if (type != INTELLIGENCE_GATHERING && type != ATTACK_OPERATION) {
                System.out.println("Invalid input");
                return Integer.MIN_VALUE;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            return Integer.MIN_VALUE;
        }
        return type;
    }

    public static void handleIntelligenceGatheringChoice(LocalDateTime start, LocalDateTime end,
                                                         TaskInformation taskInformation) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter camera type: ");
        String cameraType = sc.nextLine();
        System.out.print("Enter flight route: ");
        String flightRoute = sc.nextLine();

        Operation op = new IntelligenceGatheringOperation(taskInformation, cameraType, flightRoute,
                start, end);
        if (!mainSystem.addOperation(op)) {
            System.out.println("Cannot create operation");
        }
    }

    public static void handleAttackChoice(LocalDateTime start, LocalDateTime end, TaskInformation TI) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter armament type: ");
        String armamentType = sc.nextLine();

        System.out.println("Enter attack location:");
        System.out.print("Enter x: ");
        String input = sc.nextLine();
        double x;
        try {
            x = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            return;
        }

        System.out.print("Enter y: ");
        input = sc.nextLine();
        double y;
        try {
            y = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
            return;
        }

        Point attackLocation = new Point(x, y);
        Operation op = new AttackOperation(TI, armamentType, attackLocation, start, end);
        if (!mainSystem.addOperation(op)) {
            System.out.println("Cannot create operation");
        }
    }

    public static void handleGetOperationsTimeSpan() {
        LocalDateTime[] dates = getDates(false);
        if (dates == null) {
            return;
        }

        System.out.println("Operations:");
        List<Operation> operations = mainSystem.getAllOperationsWithinTime(dates[0], dates[1]);
        for (Operation op : operations) {
            System.out.println(op.getTaskInformation().getOperationName());
        }
    }

    public static void handleCheckOperationReady() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter operation name: ");
        String operationName = sc.nextLine();
        Operation op = mainSystem.getOperationByName(operationName);
        if (op == null) {
            System.out.println("Operation does not exists");
        } else {
            if (mainSystem.isOperationReady(op)) {
                System.out.println("Operation is ready");
            } else {
                System.out.println("Operation is not ready");
            }
        }
    }

    public static void handleChangeOperationTime() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter operation name: ");
        String operationName = sc.nextLine();
        Operation op = mainSystem.getOperationByName(operationName);
        if (op == null) {
            System.out.println("Operation does not exists");
            return;
        }

        LocalDateTime[] dates = getDates(true);
        if (dates == null) {
            return;
        }
        mainSystem.changeOperationTime(op, dates[0], dates[1]);
    }

    public static void handleAddIntelligenceDrawerOperation() {
        Scanner sc = new Scanner(System.in);
        TaskInformation TI = getTaskInformation();
        if (TI == null) {
            return;
        }
        System.out.print("Enter camera type: ");
        String cameraType = sc.nextLine();
        System.out.print("Enter flight route: ");
        String flightRoute = sc.nextLine();

        mainSystem.addIntelligenceDrawerOperation(TI, cameraType, flightRoute);
    }

    public static void handleAddDrawerOperation() {
        TaskInformation TI = getTaskInformation();
        if (TI == null) {
            return;
        }
        mainSystem.addDrawerOperation(TI.getOperationName(), TI.getTaskDescription(),
                TI.getNumOfAircrafts());
    }

    public static void handleCreateFromDrawerOperation() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter operation name: ");
        String operationName = sc.nextLine();
        if (!mainSystem.drawerOperationNameExists(operationName)) {
            System.out.println("Drawer operation name does not exists");
            return;
        }

        LocalDateTime[] dates = getDates(true);
        if (dates == null) {
            return;
        }

        if (mainSystem.isIntelligenceDrawerOperation(operationName)) {
            DrawerOperation intelligenceDrawerOperation = mainSystem.getDrawerOperationByName(operationName);

            if (!mainSystem.createIntelligenceGatheringOperation(operationName, dates[0], dates[1],
                    ((IntelligenceDrawerOperation)intelligenceDrawerOperation).getCameraType(),
                    ((IntelligenceDrawerOperation)intelligenceDrawerOperation).getFlightRoute())) {
                System.out.println("Cannot create operation");
            }
            return;
        }

        int type = getOperationType();
        if (type == Integer.MIN_VALUE) {
            return;
        }

        if (type == INTELLIGENCE_GATHERING) {
            System.out.print("Enter camera type: ");
            String cameraType = sc.nextLine();
            System.out.print("Enter flight route: ");
            String flightRoute = sc.nextLine();

            if (!mainSystem.createIntelligenceGatheringOperation(operationName, dates[0], dates[1],
                    cameraType, flightRoute)) {
                System.out.println("Cannot create operation");
            }
        } else {
            System.out.print("Enter armament type: ");
            String armamentType = sc.nextLine();

            System.out.println("Enter attack location:");
            System.out.print("Enter x: ");
            String input = sc.nextLine();
            double x;
            try {
                x = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                return;
            }

            System.out.print("Enter y: ");
            input = sc.nextLine();
            double y;
            try {
                y = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                return;
            }

            Point attackLocation = new Point(x, y);

            if (!mainSystem.createAttackOperation(operationName, dates[0], dates[1], armamentType, attackLocation)) {
                System.out.println("Cannot create operation");
            }
        }
    }

    public static void handleGetUnpreparedOperations() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter X hours in advance: ");
        String input = sc.nextLine();
        int xHours;
        try {
            xHours = Integer.parseInt(input);
            if (xHours <= 0) {
                System.out.println("Invalid input");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Not a number");
            return;
        }

        mainSystem.getAllUnpreparedOperations(xHours);
    }
}
