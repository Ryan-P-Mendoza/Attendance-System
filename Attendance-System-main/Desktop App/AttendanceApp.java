import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AttendanceApp extends Application {
    private static final Charset CSV_CHARSET = StandardCharsets.UTF_16LE;
    private TableView<Student> tableView = new TableView<>();
    private Label totalStudentsLabel = new Label("Total Students: 0");
    private Label presentStudentsLabel = new Label("Present Students: 0");
    private Label absentStudentsLabel = new Label("Absent Students: 0");
    private Label attendanceRateLabel = new Label("Attendance Rate: 0%");
    private Set<String> allStudents = new HashSet<>();
    private Set<String> presentStudents = new HashSet<>();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private boolean rosterLoaded = false;
    private boolean attendanceLoaded = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("UTD Attendance System");
        setupTableView();
        Scene scene = new Scene(setupRootPane(), 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private BorderPane setupRootPane() {
        BorderPane root = new BorderPane();
        root.setTop(setupMenuBar());
        root.setCenter(tableView);
        root.setBottom(setupAnalyticsPanel());
        return root;
    }

    private MenuBar setupMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(
            createMenuItem("Load Student Roster...", true),
            createMenuItem("Load Attendance...", false)
        );
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private MenuItem createMenuItem(String title, boolean isRoster) {
        MenuItem menuItem = new MenuItem(title);
        menuItem.setOnAction(e -> loadCSV(isRoster));
        return menuItem;
    }

    private HBox setupAnalyticsPanel() {
        HBox analyticsPanel = new HBox(10);
        analyticsPanel.getChildren().addAll(totalStudentsLabel, presentStudentsLabel, absentStudentsLabel, attendanceRateLabel);
        return analyticsPanel;
    }

    private void loadCSV(boolean isRoster) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            loadCsvData(file, isRoster);
        }
    }

    private void loadCsvData(File file, boolean isRoster) {
        try {
            List<Student> data = Files.lines(file.toPath(), CSV_CHARSET)
                .skip(1)
                .map(this::parseStudent)
                .collect(Collectors.toList());

            if (isRoster) {
                processRoster(data);
            } else {
                processAttendance(data);
            }

            if (rosterLoaded && attendanceLoaded) {
                updateTableWithAttendanceInfo();
                updateAnalytics();
            }
        } catch (Exception e) {
            showErrorDialog("Failed to load the file: " + e.getMessage());
        }
    }

    private Student parseStudent(String line) {
        String[] parts = line.replace("\"", "").split(",");
        return new Student(parts[1], parts[0], parts[3], "Absent");
    }

    private void processRoster(List<Student> data) {
        allStudents.clear();
        studentList.clear();
        studentList.addAll(data);
        allStudents.addAll(data.stream().map(Student::getStudentId).collect(Collectors.toSet()));
        rosterLoaded = true;
    }

    private void processAttendance(List<Student> data) {
        presentStudents.clear();
        presentStudents.addAll(data.stream().map(Student::getStudentId).collect(Collectors.toSet()));
        attendanceLoaded = true;
    }

    private void setupTableView() {
        TableColumn<Student, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        TableColumn<Student, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        TableColumn<Student, String> studentIdCol = new TableColumn<>("Student ID");
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        TableColumn<Student, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableView.getColumns().addAll(firstNameCol, lastNameCol, studentIdCol, statusCol);
        tableView.setItems(studentList);
        tableView.setRowFactory(tv -> new TableRow<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if ("Present".equals(item.getStatus())) {
                    setStyle("-fx-background-color: lightgreen;");
                } else {
                    setStyle("-fx-background-color: salmon;");
                }
            }
        });
    }

    private void updateTableWithAttendanceInfo() {
        studentList.forEach(student -> {
            if (presentStudents.contains(student.getStudentId())) {
                student.setStatus("Present");
            }
        });
        refreshTableView();
    }

    private void refreshTableView() {
        tableView.getColumns().get(0).setVisible(false);
        tableView.getColumns().get(0).setVisible(true);
    }

    private void updateAnalytics() {
        int totalStudents = allStudents.size();
        int presentCount = (int) presentStudents.stream().filter(allStudents::contains).count();
        int absentCount = totalStudents - presentCount; // Calculate absent count
        double attendanceRate = totalStudents > 0 ? 100.0 * presentCount / totalStudents : 0;
        totalStudentsLabel.setText("Total Students: " + totalStudents);
        presentStudentsLabel.setText("Present Students: " + presentCount);
        absentStudentsLabel.setText("Absent Students: " + absentCount);
        attendanceRateLabel.setText(String.format("Attendance Rate: %.2f%%", attendanceRate));
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public class Student {
        private SimpleStringProperty firstName;
        private SimpleStringProperty lastName;
        private SimpleStringProperty studentId;
        private SimpleStringProperty status; // "Present" or "Absent"

        public Student(String firstName, String lastName, String studentId, String status) {
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.studentId = new SimpleStringProperty(studentId);
            this.status = new SimpleStringProperty(status);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public String getLastName() {
            return lastName.get();
        }

        public String getStudentId() {
            return studentId.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}