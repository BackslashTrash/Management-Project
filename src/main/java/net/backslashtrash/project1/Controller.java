package net.backslashtrash.project1;

import com.dlsc.gemsfx.SelectionBox;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.calendarfx.view.CalendarView;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    // --- Navigation History ---
    private static final Stack<Integer> history = new Stack<>();
    private static int currentViewIndex = 0;

    // --- Table Data Models ---
    private final ObservableList<EmployeeTableItem> masterData = FXCollections.observableArrayList();
    private final FilteredList<EmployeeTableItem> filteredData = new FilteredList<>(masterData, p -> true);

    private final ObservableList<TaskTableItem> masterTaskData = FXCollections.observableArrayList();
    private final FilteredList<TaskTableItem> filteredTaskData = new FilteredList<>(masterTaskData, p -> true);

    private final ObservableList<JobTableItem> masterJobData = FXCollections.observableArrayList();
    private final FilteredList<JobTableItem> filteredJobData = new FilteredList<>(masterJobData, p -> true);

    // NEW: Employee Personal Task List
    private final ObservableList<EmployeeTaskTableItem> employeeTaskData = FXCollections.observableArrayList();

    @FXML public ChoiceBox<String> accountTypeSelect = new ChoiceBox<>();
    @FXML public StackPane calendarContainer;
    @FXML public Label earningsLabel;

    private final Calendar workCalendar = new Calendar("Work Tasks");
    private final Map<String, String> nameToUuidMap = new HashMap<>();

    // Auth & Sidebar Fields
    public TextField enterUser;
    public PasswordField enterPass;
    public PasswordField confirmPass;
    public Text warningMessage;
    public TextField loginUser;
    public PasswordField loginPass;
    public Button loginButton;
    public Label signInText;
    public Button signInButton;
    public VBox rootVBox;
    public Button signUpButton;
    public Button confirmLogin;
    public Button homeButton;
    public SelectionBox<String> chooseEmployee;

    // --- Employee List Screen Fields ---
    @FXML public TableView<EmployeeTableItem> employeeTable;
    @FXML public TableColumn<EmployeeTableItem, CheckBox> colSelect;
    @FXML public TableColumn<EmployeeTableItem, String> colName;
    @FXML public TableColumn<EmployeeTableItem, ComboBox<String>> colJob;
    @FXML public TableColumn<EmployeeTableItem, String> colStatus;
    @FXML public TableColumn<EmployeeTableItem, Node> colTask;
    @FXML public TableColumn<EmployeeTableItem, String> colEarnings;
    @FXML public ComboBox<String> filterJobSelect;
    @FXML public TextField searchEmployeeField;
    @FXML public Label lastResetLabel;

    // --- Task List Screen Fields ---
    @FXML public TableView<TaskTableItem> taskTable;
    @FXML public TableColumn<TaskTableItem, CheckBox> colTaskSelect;
    @FXML public TableColumn<TaskTableItem, String> colTaskTitle;
    @FXML public TableColumn<TaskTableItem, String> colTaskDesc;
    @FXML public TableColumn<TaskTableItem, String> colTaskTime;
    @FXML public TableColumn<TaskTableItem, HBox> colTaskAssignee;
    @FXML public TextField searchTaskField;

    // --- Job List Screen Fields ---
    @FXML public TableView<JobTableItem> jobTable;
    @FXML public TableColumn<JobTableItem, CheckBox> colJobSelect;
    @FXML public TableColumn<JobTableItem, String> colJobTitle;
    @FXML public TableColumn<JobTableItem, String> colJobPay;
    @FXML public TableColumn<JobTableItem, String> colJobDesc;
    @FXML public TextField searchJobField;

    // --- Employee Personal Task List Fields ---
    @FXML public TableView<EmployeeTaskTableItem> employeeTaskTable;
    @FXML public TableColumn<EmployeeTaskTableItem, String> colEmpTaskTitle;
    @FXML public TableColumn<EmployeeTaskTableItem, String> colEmpTaskDesc;
    @FXML public TableColumn<EmployeeTaskTableItem, String> colEmpTaskTime;
    @FXML public TableColumn<EmployeeTaskTableItem, Button> colEmpTaskAction;

    private CheckBox selectAllCheckBox;
    private CheckBox selectAllTasksCheckBox;
    private CheckBox selectAllJobsCheckBox;

    private Scene scene;
    private Stage stage;
    private Parent lastRoot;

    // 0: Title, 1: Register, 2: Login, 3: Employee DB, 4: Employer DB,
    // 5: Emp List, 6: Task List, 7: Job List, 8: Employee Task List
    private final String[] resourceListFXML = {
            "titleScreen.fxml", "register.fxml", "login.fxml", "employee.fxml", "employer.fxml",
            "employeeList.fxml", "taskList.fxml", "jobList.fxml", "employeeTaskList.fxml"
    };
    private final String[] resourceListJSON = {
            "attendance","employee","employer"
    };

    private static final String ADD_USER =
            "M21.9,37c0-2.7,0.9-5.8,2.3-8.2c1.7-3,3.6-4.2,5.1-6.4c2.5-3.7,3-9,1.4-13c-1.6-4.1-5.4-6.5-9.8-6.4s-8,2.8-9.4,6.9c-1.6,4.5-0.9,9.9,2.7,13.3c1.5,1.4,2.9,3.6,2.1,5.7c-0.7,2-3.1,2.9-4.8,3.7c-3.9,1.7-8.6,4.1-9.4,8.7C1.3,45.1,3.9,49,8,49h17c0.8,0,1.3-1,0.8-1.6C23.3,44.5,21.9,40.8,21.9,37z" +
                    "M37.9,25c-6.6,0-12,5.4-12,12s5.4,12,12,12s12-5.4,12-12S44.5,25,37.9,25z M44,38c0,0.6-0.5,1-1.1,1H40v3 c0,0.6-0.5,1-1.1,1h-2c-0.6,0-0.9-0.4-0.9-1v-3h-3.1c-0.6,0-0.9-0.4-0.9-1v-2c0-0.6,0.3-1,0.9-1H36v-3c0-0.6,0.3-1,0.9-1h2 c0.6,0,1.1,0.4,1.1,1v3h2.9c0.6,0,1.1,0.4,1.1,1V38z";

    private static final String LOGIN =
            "M155.81,0v173.889h33.417V33.417h235.592l-74.87,50.656c-8.469,5.727-13.535,15.289-13.535,25.503v286.24 H189.227V282.079H155.81v147.154h180.604v70.93c0,4.382,2.423,8.404,6.29,10.451c3.867,2.056,8.558,1.811,12.189-0.644 l119.318-80.736V0H155.81z" +
                    "M228.657,290.4c0,1.844,1.068,3.524,2.75,4.3c1.664,0.775,3.638,0.514,5.042-0.685l78.044-66.035 l-78.044-66.034c-1.404-1.2-3.378-1.46-5.042-0.686c-1.681,0.775-2.75,2.456-2.75,4.3v33.392H37.79v58.064h190.868V290.4z";

    @FXML public void onShowEmployeeTasks(ActionEvent event) throws IOException { navigate(event, 8); }

    @FXML
    public void onHelp(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/BackslashTrash/Management-Project"));
        } catch (Exception e) {
            AccountManager.alertCreator(Alert.AlertType.ERROR, "Error", "Could not open browser.");
        }
    }

    @FXML
    public void onResetPayments(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Payments");
        alert.setHeaderText("Reset all employee earnings to $0.00?");
        alert.setContentText("This action cannot be undone. It marks the start of a new pay period.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                AccountManager.resetAllEarnings(App.getCurrentUser().getUsername());
                loadEmployeeListData(); // Refresh table
                if (lastResetLabel != null) {
                    lastResetLabel.setText("Last Reset: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
                AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Success", "All payments have been reset.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // --- NAVIGATION LOGIC ---
    private void navigate(ActionEvent event, int targetIndex) throws IOException {
        if (targetIndex == currentViewIndex) return;
        history.push(currentViewIndex);
        currentViewIndex = targetIndex;
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[targetIndex])));
    }

    @FXML
    public void onBack(ActionEvent event) throws IOException {
        if (!history.isEmpty()) {
            int previousIndex = history.pop();
            currentViewIndex = previousIndex;
            switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[previousIndex])));
        } else {
            if (App.getCurrentUser() != null) {
                if (currentViewIndex == 8 || currentViewIndex == 3) {
                    currentViewIndex = 3;
                    switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[3])));
                } else if (currentViewIndex >= 4) {
                    currentViewIndex = 4;
                    switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[4])));
                } else {
                    currentViewIndex = 0;
                    switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[0])));
                }
            } else {
                currentViewIndex = 0;
                switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[0])));
            }
        }
    }

    // ------------------------

    public void onSignup(ActionEvent event) throws IOException {
        navigate(event, Files.REGISTER.INDEX);
    }

    public void onLogin(ActionEvent event) throws IOException {
        navigate(event, Files.LOGIN.INDEX);
    }

    // --- CALENDAR LOGIC ---
    private void refreshCalendar(String uuid) {
        System.out.println("Refreshing calendar for UUID: " + uuid);
        workCalendar.clear();
        try {
            List<Map<String, String>> tasks = AccountManager.getTasksForEmployee(uuid);

            for (Map<String, String> t : tasks) {
                String title = t.getOrDefault("title", "Task");
                if (title == null || title.trim().isEmpty()) title = "Task";

                LocalDate date = null;
                LocalTime start = null;
                LocalTime end = null;

                if (t.containsKey("rawDate") && t.containsKey("rawStart") && t.containsKey("rawEnd")) {
                    try {
                        date = LocalDate.parse(t.get("rawDate"));
                        start = LocalTime.parse(t.get("rawStart"));
                        end = LocalTime.parse(t.get("rawEnd"));
                    } catch (Exception e) { System.out.println("Error parsing raw fields: " + e.getMessage()); }
                }
                else if (t.containsKey("time")) {
                    try {
                        String timeStr = t.get("time");
                        String[] parts = timeStr.split(" ");
                        if (parts.length >= 4) {
                            date = LocalDate.parse(parts[0]);
                            start = LocalTime.parse(parts[1]);
                            end = LocalTime.parse(parts[3]);
                        }
                    } catch (Exception e) { System.out.println("Error parsing fallback time string: " + e.getMessage()); }
                }

                if (date != null && start != null && end != null) {
                    Entry<String> entry = new Entry<>(title);
                    entry.setInterval(date, start, date, end);
                    workCalendar.addEntry(entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onConfirmLogin(ActionEvent event) throws IOException {
        String user = loginUser.getText();
        String pass = loginPass.getText();
        warningMessage.setText("");
        Account account = findAccount(resourceListJSON[1],user,pass );
        if (account != null){
            loginAccount(account,event,3);
            return;
        }

        account = findAccount(resourceListJSON[2],user,pass );
        if (account != null){
            loginAccount(account,event,4);
            return;
        }
        warningMessage.setText("Username or password incorrect");
    }

    @FXML
    public void onConfirmRegister(ActionEvent event) throws IOException {
        if (isAccountValid()){
            if (AccountManager.register(accountTypeSelect.getValue(),enterUser.getText(),enterPass.getText())){
                AccountManager.alertCreator(Alert.AlertType.INFORMATION,"Sign up","Account created, please login now" );
                navigate(event, Files.TITLESCREEN.INDEX);
            }
        }
    }

    @FXML
    public void onHome(ActionEvent event) throws IOException {
        history.clear();
        currentViewIndex = Files.TITLESCREEN.INDEX;
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[Files.TITLESCREEN.INDEX])));
    }

    @FXML
    public void onOpenEmployeeList(ActionEvent event) throws IOException {
        navigate(event, 5); // employeelist.fxml
    }

    @FXML
    public void onBackToDashboard(ActionEvent event) throws IOException {
        onBack(event);
    }

    @FXML
    public void onBackToEmployeeList(ActionEvent event) throws IOException {
        onBack(event);
    }
    @FXML
    public void onViewEmployees(ActionEvent event) throws IOException {
        navigate(event,Files.EMPLOYEELIST.INDEX);
    }

    @FXML
    public void onShowTasks(ActionEvent event) throws IOException {
        navigate(event, 6); // tasklist.fxml
    }

    @FXML
    public void onShowJobs(ActionEvent event) throws IOException {
        navigate(event, 7); // jobList.fxml
    }

    // --- ADD JOB DIALOG ---
    @FXML
    public void onAddJob(ActionEvent event) {
        Dialog<JobData> dialog = new Dialog<>();
        dialog.setTitle("Create Job");
        dialog.setHeaderText("Create a new job profile.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Job Title (Max 50 chars)");

        TextField payField = new TextField();
        payField.setPromptText("Pay per Hour (e.g. 15.50)");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Job Description (Max 200 chars)");
        descArea.setPrefHeight(80);
        descArea.setWrapText(true);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Pay ($/hr):"), 0, 1);
        grid.add(payField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        final Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String title = titleField.getText().trim();
            String desc = descArea.getText().trim();
            String payStr = payField.getText().trim();

            if (title.isEmpty() || desc.isEmpty() || payStr.isEmpty()) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "All fields are required.");
                ae.consume(); return;
            }
            if (title.length() > 50) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "Title must be under 50 characters.");
                ae.consume(); return;
            }
            if (desc.length() > 200) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "Description must be under 200 characters.");
                ae.consume(); return;
            }
            try {
                double pay = Double.parseDouble(payStr);
                if (pay <= 0) {
                    AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "Pay must be a positive, non-zero number.");
                    ae.consume();
                }
            } catch (NumberFormatException e) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "Pay must be a valid number.");
                ae.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new JobData(titleField.getText(), descArea.getText(), Double.parseDouble(payField.getText()));
            }
            return null;
        });

        Optional<JobData> result = dialog.showAndWait();
        result.ifPresent(data -> {
            try {
                AccountManager.addJob(App.getCurrentUser().getUsername(), data.title, data.desc, data.pay);
                loadEmployeeListData();
                loadJobListData();
                AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Success", "Job created successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class JobData {
        String title; String desc; double pay;
        JobData(String t, String d, double p) { title = t; desc = d; pay = p; }
    }

    // --- ADD TASK DIALOG ---
    @FXML
    public void onAddTask(ActionEvent event) {
        if (employeeTable == null) return;
        List<String> selectedUuids = new ArrayList<>();
        for (EmployeeTableItem item : employeeTable.getItems()) {
            if (item.getSelectBox().isSelected()) {
                selectedUuids.add(item.getUuid());
            }
        }

        if (selectedUuids.isEmpty()) {
            AccountManager.alertCreator(Alert.AlertType.WARNING, "Add Task", "No employees selected.");
            return;
        }

        Dialog<TaskData> dialog = new Dialog<>();
        dialog.setTitle("Assign Task");
        dialog.setHeaderText("Assign a task to " + selectedUuids.size() + " employee(s).");

        ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField taskTitle = new TextField();
        taskTitle.setPromptText("Task Title (Max 50 chars)");

        TextField taskDesc = new TextField();
        taskDesc.setPromptText("Task Description");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        Spinner<Integer> startHour = new Spinner<>(0, 23, 9);
        Spinner<Integer> startMin = new Spinner<>(0, 59, 0);
        startHour.setEditable(true); startMin.setEditable(true);
        startHour.setPrefWidth(60); startMin.setPrefWidth(60);

        Spinner<Integer> endHour = new Spinner<>(0, 23, 17);
        Spinner<Integer> endMin = new Spinner<>(0, 59, 0);
        endHour.setEditable(true); endMin.setEditable(true);
        endHour.setPrefWidth(60); endMin.setPrefWidth(60);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(taskTitle, 1, 0, 3, 1);

        grid.add(new Label("Description:"), 0, 1);
        grid.add(taskDesc, 1, 1, 3, 1);

        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2, 3, 1);

        grid.add(new Label("Start Time:"), 0, 3);

        HBox startBox = new HBox(5, startHour, new Label(":"), startMin);
        startBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(startBox, 1, 3, 3, 1);

        grid.add(new Label("End Time:"), 0, 4);
        HBox endBox = new HBox(5, endHour, new Label(":"), endMin);
        endBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(endBox, 1, 4, 3, 1);

        dialog.getDialogPane().setContent(grid);

        final Button assignButton = (Button) dialog.getDialogPane().lookupButton(assignButtonType);
        assignButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String title = taskTitle.getText();
            String desc = taskDesc.getText();
            LocalDate date = datePicker.getValue();

            if (title.isEmpty() || desc.isEmpty() || date == null) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "All fields are required.");
                ae.consume(); return;
            }
            if (title.length() > 50) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "Title must be under 50 characters.");
                ae.consume(); return;
            }

            LocalTime start = LocalTime.of(startHour.getValue(), startMin.getValue());
            LocalTime end = LocalTime.of(endHour.getValue(), endMin.getValue());
            if (LocalDateTime.of(date, start).isBefore(LocalDateTime.now())) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Time", "Task start time cannot be in the past.");
                ae.consume(); return;
            }
            if (!end.isAfter(start)) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Time", "End time must be after start time.");
                ae.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                return new TaskData(
                        taskTitle.getText(),
                        taskDesc.getText(),
                        datePicker.getValue(),
                        LocalTime.of(startHour.getValue(), startMin.getValue()),
                        LocalTime.of(endHour.getValue(), endMin.getValue())
                );
            }
            return null;
        });

        Optional<TaskData> result = dialog.showAndWait();
        result.ifPresent(data -> {
            try {
                AccountManager.assignTask(selectedUuids, data.title, data.desc, data.date, data.start, data.end, App.getCurrentUser().getUsername());
                loadEmployeeListData();
                loadTaskListData();
                AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Success", "Task assigned successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class TaskData {
        String title; String desc; LocalDate date; LocalTime start; LocalTime end;
        TaskData(String t, String d, LocalDate dt, LocalTime s, LocalTime e) {
            title = t; desc = d; date = dt; start = s; end = e;
        }
    }

    // --- DIALOG FOR ADDING MORE PEOPLE TO AN EXISTING TASK ---
    public void openAddAssigneeDialog(TaskTableItem taskItem, List<String> currentAssigneeUuids, Map<String, String> rawTaskData) {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Add Assignees");
        dialog.setHeaderText("Add more employees to task: \n" + taskItem.getTitle());

        ButtonType assignButtonType = new ButtonType("Add Selected", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 20, 10, 10));

        CheckBox selectAll = new CheckBox("Select All New Employees");
        selectAll.setStyle("-fx-font-weight: bold;");
        vbox.getChildren().add(selectAll);
        vbox.getChildren().add(new Separator());

        List<CheckBox> employeeChecks = new ArrayList<>();

        try {
            ArrayList<String> allEmployees = AccountManager.getEmployerEmployeeList(App.getCurrentUser().getUsername());
            for (String uuid : allEmployees) {
                Account empAccount = findAccount(resourceListJSON[1], uuid);
                String name = (empAccount != null) ? empAccount.getUsername() : "Unknown (" + uuid + ")";

                CheckBox cb = new CheckBox(name);
                cb.setUserData(uuid);

                // If the employee is already assigned, disable and check the box visually
                if (currentAssigneeUuids.contains(uuid)) {
                    cb.setSelected(true);
                    cb.setDisable(true);
                    cb.setText(name + " (Already Assigned)");
                    cb.setStyle("-fx-text-fill: gray;");
                } else {
                    employeeChecks.add(cb);
                }
                vbox.getChildren().add(cb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        selectAll.setOnAction(e -> {
            boolean selected = selectAll.isSelected();
            for (CheckBox cb : employeeChecks) {
                if (!cb.isDisabled()) {
                    cb.setSelected(selected);
                }
            }
        });

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(250);
        scroll.setStyle("-fx-background-color: transparent;");
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignButtonType) {
                List<String> newSelectedUuids = new ArrayList<>();
                for (CheckBox cb : employeeChecks) {
                    if (cb.isSelected() && !cb.isDisabled()) {
                        newSelectedUuids.add((String) cb.getUserData());
                    }
                }
                return newSelectedUuids;
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(newUuids -> {
            if (!newUuids.isEmpty()) {
                // Determine Original Task Details (Fallback to current time if parsing fails)
                LocalDate date = LocalDate.now();
                LocalTime start = LocalTime.of(9, 0);
                LocalTime end = LocalTime.of(17, 0);

                try {
                    if (rawTaskData.containsKey("date")) {
                        date = LocalDate.parse(rawTaskData.get("date"));
                    } else if (rawTaskData.containsKey("rawDate")) {
                        date = LocalDate.parse(rawTaskData.get("rawDate"));
                    }
                    if (rawTaskData.containsKey("start")) {
                        start = LocalTime.parse(rawTaskData.get("start"));
                    } else if (rawTaskData.containsKey("rawStart")) {
                        start = LocalTime.parse(rawTaskData.get("rawStart"));
                    }
                    if (rawTaskData.containsKey("end")) {
                        end = LocalTime.parse(rawTaskData.get("end"));
                    } else if (rawTaskData.containsKey("rawEnd")) {
                        end = LocalTime.parse(rawTaskData.get("rawEnd"));
                    }
                } catch (Exception e) {
                    System.out.println("Could not parse exact date/time from task data. Using defaults.");
                }

                try {
                    AccountManager.assignTask(newUuids, taskItem.getTitle(), taskItem.getDescription(), date, start, end, App.getCurrentUser().getUsername());
                    loadTaskListData();
                    AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Success", "Added " + newUuids.size() + " new employee(s) to the task!");
                } catch (IOException e) {
                    e.printStackTrace();
                    AccountManager.alertCreator(Alert.AlertType.ERROR, "Error", "Failed to assign task.");
                }
            }
        });
    }

    @FXML
    public void onRemoveSelected(ActionEvent event) {
        if (employeeTable == null || App.getCurrentUser() == null) return;

        ObservableList<EmployeeTableItem> allItems = employeeTable.getItems();
        ArrayList<EmployeeTableItem> toRemove = new ArrayList<>();

        for (EmployeeTableItem item : allItems) {
            if (item.getSelectBox().isSelected()) {
                toRemove.add(item);
            }
        }

        if (toRemove.isEmpty()) {
            AccountManager.alertCreator(Alert.AlertType.WARNING, "Remove", "No employees selected.");
            return;
        }

        String employer = App.getCurrentUser().getUsername();
        try {
            for (EmployeeTableItem item : toRemove) {
                AccountManager.removeEmployeeFromEmployer(employer, item.getUuid());
                masterData.remove(item);
            }
            updateSelectAllState();
            AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Remove", "Removed " + toRemove.size() + " employees.");
        } catch (IOException e) {
            e.printStackTrace();
            AccountManager.alertCreator(Alert.AlertType.ERROR, "Error", "Failed to update database.");
        }
    }

    @FXML
    public void onRemoveSelectedTasks(ActionEvent event) {
        if (taskTable == null || App.getCurrentUser() == null) return;

        ObservableList<TaskTableItem> allItems = taskTable.getItems();
        ArrayList<TaskTableItem> toRemove = new ArrayList<>();
        List<String> idsToRemove = new ArrayList<>();

        for (TaskTableItem item : allItems) {
            if (item.getSelectBox().isSelected()) {
                toRemove.add(item);
                idsToRemove.addAll(item.getIds());
            }
        }

        if (toRemove.isEmpty()) {
            AccountManager.alertCreator(Alert.AlertType.WARNING, "Remove", "No tasks selected.");
            return;
        }

        try {
            AccountManager.removeTasks(idsToRemove);
            masterTaskData.removeAll(toRemove);
            updateSelectAllTasksState();
            AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Remove", "Deleted selected tasks.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onRemoveSelectedJobs(ActionEvent event) {
        if (jobTable == null || App.getCurrentUser() == null) return;

        ObservableList<JobTableItem> allItems = jobTable.getItems();
        ArrayList<JobTableItem> toRemove = new ArrayList<>();
        List<String> idsToRemove = new ArrayList<>();

        for (JobTableItem item : allItems) {
            if (item.getSelectBox().isSelected()) {
                toRemove.add(item);
                idsToRemove.add(item.getId());
            }
        }

        if (toRemove.isEmpty()) {
            AccountManager.alertCreator(Alert.AlertType.WARNING, "Remove", "No jobs selected.");
            return;
        }

        try {
            AccountManager.removeJobs(idsToRemove);
            masterJobData.removeAll(toRemove);
            updateSelectAllJobsState();
            AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Remove", "Deleted " + toRemove.size() + " jobs.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchScene(ActionEvent event, Parent root) {
        lastRoot = root;
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root,stage.getWidth()-16,stage.getHeight()-39);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            AccountManager.checkExpiredTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String[] accountType = {"Employee", "Employer"};
        accountTypeSelect.getItems().addAll(accountType);
        setBackgroundColor(rootVBox, "#E7E7E7","#CFCFCF","#B6B6B6");
        makeButtonStyle(signUpButton, Color.web("#4A93FF"),170,90,0.32, ADD_USER,true);
        makeButtonStyle(loginButton,Color.web("#2EC27E"),170,90,0.32, LOGIN,true);
        makeButtonStyle(confirmLogin,Color.web("#2EC27E"),60,30,0,"",false);

        if (chooseEmployee != null && App.getCurrentUser() != null) {
            loadEmployerEmployees();
        }

        // --- Setup CalendarFX ---
        if (calendarContainer != null) {
            CalendarView calendarView = new CalendarView();
            calendarView.setShowDeveloperConsole(false);
            calendarView.setShowAddCalendarButton(false);

            // Set our custom calendar source
            workCalendar.setStyle(Calendar.Style.STYLE1); // Set a color style
            CalendarSource myCalendarSource = new CalendarSource("My Calendars");
            myCalendarSource.getCalendars().add(workCalendar);
            calendarView.getCalendarSources().setAll(myCalendarSource);

            calendarContainer.getChildren().add(calendarView);

            // If logged in as Employee, load tasks immediately
            if (signInButton != null && App.getCurrentUser() != null) { // Employee Dashboard has signInButton
                refreshCalendar(App.getCurrentUser().getUuid());
            }

            // If logged in as Employer, setup listener for selection
            if (chooseEmployee != null) {
                // If using JavaFX ComboBox or similar API
                chooseEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && nameToUuidMap.containsKey(newVal)) {
                        refreshCalendar(nameToUuidMap.get(newVal));
                    }
                });
            }
        }

        // --- Initialize Employee List Table ---
        if (employeeTable != null) {
            selectAllCheckBox = new CheckBox();
            selectAllCheckBox.setCursor(Cursor.HAND);
            selectAllCheckBox.setOnAction(e -> {
                boolean isSelected = selectAllCheckBox.isSelected();
                for (EmployeeTableItem item : employeeTable.getItems()) {
                    item.getSelectBox().setSelected(isSelected);
                }
            });
            colSelect.setGraphic(selectAllCheckBox);
            colSelect.setText("");

            colSelect.setCellValueFactory(new PropertyValueFactory<>("selectBox"));
            colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
            colJob.setCellValueFactory(new PropertyValueFactory<>("jobBox"));
            colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
            colTask.setCellValueFactory(new PropertyValueFactory<>("taskBox")); // CHANGED to custom HBox
            colEarnings.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEarnings()));

            employeeTable.setItems(filteredData);

            // Search field listener
            if (searchEmployeeField != null) {
                searchEmployeeField.textProperty().addListener((observable, oldValue, newValue) -> updateEmployeeFilter());
            }

            // Filter dropdown listener
            if (filterJobSelect != null) {
                filterJobSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateEmployeeFilter());
            }

            loadEmployeeListData();
        }

        // --- Initialize Task List Table ---
        if (taskTable != null) {
            selectAllTasksCheckBox = new CheckBox();
            selectAllTasksCheckBox.setCursor(Cursor.HAND);
            selectAllTasksCheckBox.setOnAction(e -> {
                boolean isSelected = selectAllTasksCheckBox.isSelected();
                for (TaskTableItem item : taskTable.getItems()) {
                    item.getSelectBox().setSelected(isSelected);
                }
            });

            colTaskSelect.setGraphic(selectAllTasksCheckBox);
            colTaskSelect.setText("");

            colTaskSelect.setCellValueFactory(new PropertyValueFactory<>("selectBox"));
            colTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle())); // NEW
            colTaskDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
            colTaskTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
            colTaskAssignee.setCellValueFactory(new PropertyValueFactory<>("assigneeBox"));

            taskTable.setItems(filteredTaskData);

            // Search field listener - SEARCH BY TITLE
            if (searchTaskField != null) {
                searchTaskField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredTaskData.setPredicate(task -> {
                        if (newValue == null || newValue.trim().isEmpty()) return true;
                        return task.getTitle().toLowerCase().contains(newValue.toLowerCase()); // Changed to getTitle()
                    });
                });
            }

            loadTaskListData();
        }

        // --- Initialize Job List Table ---
        if (jobTable != null) {
            selectAllJobsCheckBox = new CheckBox();
            selectAllJobsCheckBox.setCursor(Cursor.HAND);
            selectAllJobsCheckBox.setOnAction(e -> {
                boolean isSelected = selectAllJobsCheckBox.isSelected();
                for (JobTableItem item : jobTable.getItems()) {
                    item.getSelectBox().setSelected(isSelected);
                }
            });

            colJobSelect.setGraphic(selectAllJobsCheckBox);
            colJobSelect.setText("");

            colJobSelect.setCellValueFactory(new PropertyValueFactory<>("selectBox"));
            colJobTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
            colJobPay.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPay()));
            colJobDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDesc()));

            jobTable.setItems(filteredJobData);

            // Search field listener
            if (searchJobField != null) {
                searchJobField.textProperty().addListener((observable, oldValue, newValue) -> {
                    filteredJobData.setPredicate(job -> {
                        if (newValue == null || newValue.trim().isEmpty()) return true;
                        return job.getTitle().toLowerCase().contains(newValue.toLowerCase());
                    });
                });
            }

            loadJobListData();
        }

        // --- Initialize Employee Personal Task Table ---
        if (employeeTaskTable != null) {
            colEmpTaskTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
            colEmpTaskDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
            colEmpTaskTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
            colEmpTaskAction.setCellValueFactory(new PropertyValueFactory<>("actionButton"));
            employeeTaskTable.setItems(employeeTaskData);
            loadEmployeeTaskListData();
        }

        if (signInButton != null && App.getCurrentUser() != null && signInText != null) {
            try {
                if (AccountManager.isSignedToday(App.getCurrentUser().getUuid())) {
                    signInText.setVisible(true);
                    signInText.setText("You have signed in today");
                    signInButton.setVisible(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateEmployeeFilter() {
        String searchString = searchEmployeeField != null ? searchEmployeeField.getText() : null;
        String jobFilter = filterJobSelect != null ? filterJobSelect.getValue() : null;

        filteredData.setPredicate(employee -> {
            boolean matchesSearch = true;
            if (searchString != null && !searchString.trim().isEmpty()) {
                matchesSearch = employee.getName().toLowerCase().contains(searchString.toLowerCase());
            }

            boolean matchesJob = true;
            if (jobFilter != null && !"All Jobs".equals(jobFilter)) {
                matchesJob = jobFilter.equals(employee.getJobBox().getValue());
            }

            return matchesSearch && matchesJob;
        });
    }

    private void updateSelectAllState() {
        if (selectAllCheckBox == null || employeeTable == null) return;
        boolean allSelected = !employeeTable.getItems().isEmpty();
        for (EmployeeTableItem item : employeeTable.getItems()) {
            if (!item.getSelectBox().isSelected()) {
                allSelected = false;
                break;
            }
        }
        selectAllCheckBox.setSelected(allSelected);
    }

    private void updateSelectAllTasksState() {
        if (selectAllTasksCheckBox == null || taskTable == null) return;
        boolean allSelected = !taskTable.getItems().isEmpty();
        for (TaskTableItem item : taskTable.getItems()) {
            if (!item.getSelectBox().isSelected()) {
                allSelected = false;
                break;
            }
        }
        selectAllTasksCheckBox.setSelected(allSelected);
    }

    private void updateSelectAllJobsState() {
        if (selectAllJobsCheckBox == null || jobTable == null) return;
        boolean allSelected = !jobTable.getItems().isEmpty();
        for (JobTableItem item : jobTable.getItems()) {
            if (!item.getSelectBox().isSelected()) {
                allSelected = false;
                break;
            }
        }
        selectAllJobsCheckBox.setSelected(allSelected);
    }

    private void loadEmployerEmployees() {
        try {
            chooseEmployee.getItems().clear();
            ArrayList<String> employeeIds = AccountManager.getEmployerEmployeeList(App.getCurrentUser().getUsername());

            for (String uuid : employeeIds) {
                Account emp = findAccount(resourceListJSON[1], uuid);
                if (emp != null) {
                    chooseEmployee.getItems().add(emp.getUsername());
                    nameToUuidMap.put(emp.getUsername(), uuid);
                } else {
                    chooseEmployee.getItems().add(uuid);
                    nameToUuidMap.put(uuid, uuid);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEmployeeListData() {
        if (App.getCurrentUser() == null) return;
        masterData.clear();

        try {
            List<String> availableJobs = AccountManager.getEmployerJobs(App.getCurrentUser().getUsername())
                    .stream().map(j -> j.get("title"))
                    .collect(Collectors.toList());
            availableJobs.add(0, "Unassigned");

            if (filterJobSelect != null) {
                String currentFilter = filterJobSelect.getValue();
                filterJobSelect.getItems().clear();
                filterJobSelect.getItems().add("All Jobs");
                filterJobSelect.getItems().addAll(availableJobs);
                filterJobSelect.getItems().remove("Unassigned");

                if (currentFilter != null && filterJobSelect.getItems().contains(currentFilter)) {
                    filterJobSelect.setValue(currentFilter);
                } else {
                    filterJobSelect.setValue("All Jobs");
                }
            }

            ArrayList<String> employeeIds = AccountManager.getEmployerEmployeeList(App.getCurrentUser().getUsername());
            for (String uuid : employeeIds) {
                Account empAccount = findAccount(resourceListJSON[1], uuid);
                String name = (empAccount != null) ? empAccount.getUsername() : "Unknown (" + uuid + ")";

                boolean isSigned = AccountManager.isSignedToday(uuid);
                String status = isSigned ? "Signed In" : "Absent";
                String job = AccountManager.getEmployeeJob(uuid);
                String task = AccountManager.getEmployeeTask(uuid);
                double earn = AccountManager.getEarnings(uuid);
                String earningsStr = String.format("$%.2f", earn);

                EmployeeTableItem item = new EmployeeTableItem(uuid, name, job, status, task, earningsStr, availableJobs);
                item.getSelectBox().selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectAllState());
                masterData.add(item);
            }

            // Alphabetically sort the employees by Name
            masterData.sort(Comparator.comparing(EmployeeTableItem::getName, String.CASE_INSENSITIVE_ORDER));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTaskListData() {
        if (App.getCurrentUser() == null) return;
        masterTaskData.clear();

        try {
            List<Map<String, String>> tasks = AccountManager.getAllTasks(App.getCurrentUser().getUsername());

            // 1. Group Tasks by exactly matching TITLE, description and time.
            Map<String, List<Map<String, String>>> groupedTasks = new HashMap<>();
            for (Map<String, String> t : tasks) {
                String title = t.getOrDefault("title", "");
                String key = title + "|||" + t.get("description") + "|||" + t.get("time");
                groupedTasks.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
            }

            // 2. Process each group into one unified Table Item
            for (Map.Entry<String, List<Map<String, String>>> entry : groupedTasks.entrySet()) {
                List<Map<String, String>> group = entry.getValue();

                List<String> ids = new ArrayList<>();
                List<String> assigneeUuids = new ArrayList<>();
                List<String> assigneeNames = new ArrayList<>();

                for (Map<String, String> t : group) {
                    ids.add(t.get("id"));

                    String empUuid = t.get("employeeUuid");
                    assigneeUuids.add(empUuid);

                    Account emp = findAccount(resourceListJSON[1], empUuid);
                    assigneeNames.add((emp != null) ? emp.getUsername() : "Unknown");
                }

                Map<String, String> rawData = group.get(0);
                String title = rawData.getOrDefault("title", "");
                String desc = rawData.get("description");
                String time = rawData.get("time");
                String namesStr = String.join(", ", assigneeNames);

                TaskTableItem item = new TaskTableItem(ids, title, desc, time, namesStr, assigneeUuids, rawData, this);

                item.getSelectBox().selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectAllTasksState());
                masterTaskData.add(item);
            }

            // Alphabetically sort the tasks by Title
            masterTaskData.sort(Comparator.comparing(TaskTableItem::getTitle, String.CASE_INSENSITIVE_ORDER));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadJobListData() {
        if (App.getCurrentUser() == null) return;
        masterJobData.clear();

        try {
            List<Map<String, String>> jobs = AccountManager.getEmployerJobs(App.getCurrentUser().getUsername());

            for (Map<String, String> j : jobs) {
                JobTableItem item = new JobTableItem(
                        j.get("id"), j.get("title"), j.get("pay"), j.get("description")
                );
                item.getSelectBox().selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectAllJobsState());
                masterJobData.add(item);
            }

            // Alphabetically sort the jobs by Title
            masterJobData.sort(Comparator.comparing(JobTableItem::getTitle, String.CASE_INSENSITIVE_ORDER));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Inner Classes ---

    public static class EmployeeTableItem {
        private final String uuid;
        private final String name;
        private final String status;
        private final String task;
        private final String earnings;
        private final CheckBox selectBox;
        private final ComboBox<String> jobBox;

        public EmployeeTableItem(String uuid, String name, String currentJob, String status, String task, String earnings, List<String> availableJobs) {
            this.uuid = uuid;
            this.name = name;
            this.status = status;
            this.task = task;
            this.earnings = earnings;

            this.selectBox = new CheckBox();
            this.selectBox.setAlignment(Pos.CENTER);
            this.selectBox.setCursor(Cursor.HAND);

            this.jobBox = new ComboBox<>();
            this.jobBox.getItems().addAll(availableJobs);
            this.jobBox.setValue(currentJob);
            this.jobBox.setMaxWidth(Double.MAX_VALUE);
            this.jobBox.setStyle("-fx-background-color: white; -fx-border-color: #CED4DA; -fx-border-radius: 4;");

            this.jobBox.setOnAction(e -> {
                try {
                    AccountManager.updateEmployeeJob(uuid, this.jobBox.getValue());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
        public String getUuid() { return uuid; }
        public String getName() { return name; }
        public String getStatus() { return status; }
        public String getTask() { return task; }
        public String getEarnings() { return earnings; }
        public CheckBox getSelectBox() { return selectBox; }
        public ComboBox<String> getJobBox() { return jobBox; }
    }

    public static class EmployeeTaskTableItem {
        private final String id, title, description, time;
        private final Map<String, String> rawData;
        private final Button actionButton;

        public EmployeeTaskTableItem(String id, String title, String description, String time, Map<String, String> rawData, Controller controller) {
            this.id = id; this.title = title; this.description = description; this.time = time; this.rawData = rawData;
            this.actionButton = new Button("Complete");

            // Check time constraint
            boolean isLocked = false;
            if (rawData.containsKey("rawDate") && rawData.containsKey("rawEnd")) {
                try {
                    LocalDate date = LocalDate.parse(rawData.get("rawDate"));
                    LocalTime end = LocalTime.parse(rawData.get("rawEnd"));
                    LocalDateTime taskEndTime = LocalDateTime.of(date, end);
                    if (LocalDateTime.now().isBefore(taskEndTime)) {
                        isLocked = true;
                    }
                } catch (Exception e) {}
            }

            if (isLocked) {
                this.actionButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;");
                this.actionButton.setTooltip(new Tooltip("Available after scheduled time"));
            } else {
                this.actionButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            }

            this.actionButton.setOnAction(e -> controller.onCompleteTask(this));
        }
        public String getId() { return id; } public String getTitle() { return title; } public String getDescription() { return description; }
        public String getTime() { return time; } public Button getActionButton() { return actionButton; }
        public Map<String, String> getRawData() { return rawData; }
    }

    public void onCompleteTask(EmployeeTaskTableItem item) {
        // 1. Time Validation
        Map<String, String> rawData = item.getRawData();
        if (rawData.containsKey("rawDate") && rawData.containsKey("rawEnd")) {
            try {
                LocalDate date = LocalDate.parse(rawData.get("rawDate"));
                LocalTime end = LocalTime.parse(rawData.get("rawEnd"));
                LocalDateTime taskEndTime = LocalDateTime.of(date, end);

                if (LocalDateTime.now().isBefore(taskEndTime)) {
                    AccountManager.alertCreator(Alert.AlertType.WARNING, "Task In Progress",
                            "You cannot complete this task yet.\nPlease wait until the scheduled end time: " + end);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. Confirmation & Completion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Complete Task");
        alert.setHeaderText("Mark task as complete?");
        alert.setContentText("This will remove the task and calculate payment.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String uuid = App.getCurrentUser().getUuid();
                double rate = AccountManager.getHourlyRate(uuid);
                double hours = 0.0;

                if (rawData.containsKey("rawStart") && rawData.containsKey("rawEnd")) {
                    LocalTime s = LocalTime.parse(rawData.get("rawStart"));
                    LocalTime e = LocalTime.parse(rawData.get("rawEnd"));
                    hours = java.time.Duration.between(s, e).toMinutes() / 60.0;
                }
                double pay = hours * rate;

                AccountManager.addEarnings(uuid, pay);
                AccountManager.removeTask(item.getId());

                loadEmployeeTaskListData();
                refreshCalendar(uuid);

                if (earningsLabel != null) {
                    double total = AccountManager.getEarnings(uuid);
                    earningsLabel.setText(String.format("Total Earnings: $%.2f", total));
                }
                AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Complete", String.format("Task completed! Payment added: $%.2f", pay));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadEmployeeTaskListData() {
        if (App.getCurrentUser() == null) return;
        employeeTaskData.clear();
        try {
            List<Map<String, String>> tasks = AccountManager.getTasksForEmployee(App.getCurrentUser().getUuid());
            for (Map<String, String> t : tasks) {
                String title = t.getOrDefault("title", "Task");
                String desc = t.getOrDefault("description", "");
                String time = t.getOrDefault("time", "");
                String id = t.get("id");
                employeeTaskData.add(new EmployeeTaskTableItem(id, title, desc, time, t, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TaskTableItem {
        private final List<String> ids;
        private final String title; // NEW
        private final String description;
        private final String time;
        private final CheckBox selectBox;

        private final HBox assigneeBox;

        public TaskTableItem(List<String> ids, String title, String description, String time, String assigneesText,
                             List<String> currentAssigneeUuids, Map<String, String> rawTaskData, Controller controller) {
            this.ids = ids;
            this.title = title;
            this.description = description;
            this.time = time;
            this.selectBox = new CheckBox();
            this.selectBox.setAlignment(Pos.CENTER);
            this.selectBox.setCursor(Cursor.HAND);

            Label label = new Label(assigneesText);
            label.setWrapText(true);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button addButton = new Button("+");
            addButton.setStyle("-fx-background-color: #4A93FF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-min-width: 26px; -fx-min-height: 26px; -fx-padding: 0;");
            addButton.setCursor(Cursor.HAND);
            addButton.setTooltip(new Tooltip("Add more employees to this task"));

            addButton.setOnAction(e -> controller.openAddAssigneeDialog(this, currentAssigneeUuids, rawTaskData));

            this.assigneeBox = new HBox(10, label, spacer, addButton);
            this.assigneeBox.setAlignment(Pos.CENTER_LEFT);
        }

        public List<String> getIds() { return ids; }
        public String getTitle() { return title; } // NEW
        public String getDescription() { return description; }
        public String getTime() { return time; }
        public CheckBox getSelectBox() { return selectBox; }
        public HBox getAssigneeBox() { return assigneeBox; }
    }

    public static class JobTableItem {
        private final String id;
        private final String title;
        private final String pay;
        private final String desc;
        private final CheckBox selectBox;

        public JobTableItem(String id, String title, String pay, String desc) {
            this.id = id;
            this.title = title;
            this.pay = pay;
            this.desc = desc;
            this.selectBox = new CheckBox();
            this.selectBox.setAlignment(Pos.CENTER);
            this.selectBox.setCursor(Cursor.HAND);
        }
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getPay() { return pay; }
        public String getDesc() { return desc; }
        public CheckBox getSelectBox() { return selectBox; }
    }

    private boolean isAccountValid() {
        String username =  enterUser.getText();
        String pass = enterPass.getText();
        String confirm = confirmPass.getText();
        warningMessage.setText("");
        if (username.length()<3 || username.length()>15){
            return accountInvalid("Username must be between 3 and 15 characters");
        }
        if (pass.length()<3 || pass.length()>15){
            return accountInvalid("Password must be between 3 and 15 characters");
        }
        if (!pass.equals(confirm)) {
            return accountInvalid("Password mismatch");
        }
        if (accountTypeSelect.getValue() == null){
            return accountInvalid("Please select an account type");
        }
        return true;
    }

    private Parent getLastRoot() {
        return lastRoot;
    }

    private boolean accountInvalid(String message){
        warningMessage.setText(message);
        return false;
    }

    private void debugPrinting() {
        System.out.println(accountTypeSelect.getValue());
        System.out.println(enterUser.getText());
        System.out.println(enterPass.getText());
    }

    private Account findAccount(String filename, String user, String pass) throws IOException {
        File file =  new File("src/main/resources/net/backslashtrash/project1/objects/",filename + ".json");
        if (!file.exists()) {
            file = new File("src/main/resources/net/backslashtrash/objects/",filename + ".json");
        }
        ArrayList<Account> accountArrayList = objectMapper.readValue(file, new TypeReference<>() {});
        if (file.length() == 0) {
            return null;
        }
        for (Account account : accountArrayList){
            if (account.getUsername().equals(user) && account.getPassword().equals(pass)){
                return account;
            }
        }
        return null;
    }

    private Account findAccount(String filename, String UUID) throws IOException{
        File file =  new File("src/main/resources/net/backslashtrash/project1/objects/",filename + ".json");
        if (!file.exists()) {
            file = new File("src/main/resources/net/backslashtrash/objects/",filename + ".json");
        }
        ArrayList<Account> accountArrayList = objectMapper.readValue(file, new TypeReference<>() {});
        if (file.length() == 0) {
            return null;
        }
        for (Account account : accountArrayList){
            if (account.getUuid().equals(UUID)){
                return account;
            }
        }
        return null;
    }

    @FXML
    public void onSignInDaily(ActionEvent event) {
        Account account =  App.getCurrentUser();
        if (account == null) return;

        try {
            String employer = AccountManager.findEmployer(account.getUuid());
            if (employer == null) employer = "Unknown";
            AccountManager.markAttendance(account.getUuid(), employer);

            signInText.setText("You have signed in today");
            signInButton.setVisible(false);

        } catch (IOException e) {
            e.printStackTrace();
            AccountManager.alertCreator(Alert.AlertType.ERROR, "Error", "Could not save attendance.");
        }
    }

    private void loginAccount(Account account,ActionEvent event, int index) throws IOException {
        App.lock(account);
        navigate(event, index);
    }

    @FXML
    public void onAddEmployee(ActionEvent event) throws IOException {
        TextInputDialog addEmployee = new TextInputDialog();
        addEmployee.setTitle("Add employee");
        addEmployee.setHeaderText("Enter employee's UUID below");
        addEmployee.setContentText("Employee UUID:");
        Optional<String> result =  addEmployee.showAndWait();

        if (result.isPresent()) {
            String uuid = result.get().trim();
            Account employeeAcc = findAccount(resourceListJSON[1], uuid);

            if (employeeAcc == null){
                AccountManager.alertCreator(Alert.AlertType.WARNING,"Add employee","Account not found!");
            } else {
                Account currentUser = App.getCurrentUser();
                if (currentUser != null) {
                    AccountManager.addEmployeeToEmployer(currentUser.getUsername(), uuid);
                    AccountManager.alertCreator(Alert.AlertType.INFORMATION,"Add employee","Employee " + employeeAcc.getUsername() + " added!");
                    loadEmployerEmployees();
                } else {
                    AccountManager.alertCreator(Alert.AlertType.ERROR, "Error", "No employer logged in.");
                }
            }
        }
    }

    public void onLogout(ActionEvent event) throws IOException {
        history.clear();
        currentViewIndex = Files.TITLESCREEN.INDEX;
        switchScene(event,FXMLLoader.load(App.class.getResource(resourceListFXML[Files.TITLESCREEN.INDEX])));
        App.unlock();
    }

    private void makeButtonStyle(Button button, Color base, double prefWidth,double prefHeight, double glyphsize,String svgPath, boolean enableStrip) {
        if (button == null) return;
        applyTileStyle(button, base, svgPath,prefWidth,prefHeight,glyphsize,enableStrip);
    }

    private void applyTileStyle(Button button, Color accent, String svgPath, double prefWidth, double prefHeight, double glyphsize, boolean enableStrip) {
        double radius = 0;
        CornerRadii radii = new CornerRadii(radius);

        Color normal = accent;
        Color hover = accent.deriveColor(0, 0.85, 1.10, 1.0);
        Color pressed = accent.deriveColor(0, 1.0, 0.82, 1.0);

        DropShadow shadow = new DropShadow(14, 0, 6, Color.rgb(0, 0, 0, 0.18));

        SVGPath glyph = new SVGPath();
        glyph.setContent(svgPath);
        glyph.setFill(Color.rgb(255, 255, 255, 0.92));
        double targetGlyphSize = Math.min(prefWidth, prefHeight) * glyphsize;
        Bounds b = glyph.getLayoutBounds();
        double max = Math.max(b.getWidth(), b.getHeight());
        if (max > 0.0001) {
            double s = targetGlyphSize / max;
            glyph.setScaleX(s);
            glyph.setScaleY(s);
        }

        Group glyphContainer = new Group(glyph);
        Text label = new Text(button.getText());
        label.setFill(Color.WHITE);

        VBox graphic = new VBox(6, glyphContainer, label);
        graphic.setMouseTransparent(true);
        graphic.setFillWidth(false);
        graphic.setAlignment(Pos.CENTER);

        Region strip = new Region();
        strip.setMouseTransparent(true);
        strip.setPrefHeight(4);
        strip.setMinHeight(4);
        strip.setMaxHeight(4);
        if (enableStrip) {
            strip.setBackground(new Background(new BackgroundFill(
                    Color.rgb(255, 255, 255, 0.18), CornerRadii.EMPTY, Insets.EMPTY
            )));
        }
        StackPane content = new StackPane(graphic);
        content.setMouseTransparent(true);

        VBox wrapper = new VBox(content, strip);
        wrapper.setMouseTransparent(true);
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(content, Priority.ALWAYS);

        button.setText(null);
        button.setGraphic(wrapper);

        button.setBackground(new Background(new BackgroundFill(normal, radii, Insets.EMPTY)));
        button.setBorder(Border.EMPTY);
        button.setEffect(shadow);
        button.setPickOnBounds(false);

        button.setMinHeight(prefHeight);
        button.setPrefHeight(prefHeight);
        button.setMinWidth(prefWidth);
        button.setPrefWidth(prefWidth);

        final ObjectProperty<Color> fill = new SimpleObjectProperty<>(normal);
        fill.addListener((obs, o, c) ->
                button.setBackground(new Background(new BackgroundFill(c, radii, Insets.EMPTY)))
        );

        Consumer<Color> animateTo = target -> {
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.millis(120),
                            new KeyValue(fill, target, Interpolator.EASE_BOTH))
            );
            tl.play();
        };

        button.setOnMouseEntered(e -> {
            if (!button.isPressed()) animateTo.accept(hover);
            button.setScaleX(1.01);
            button.setScaleY(1.01);
        });

        button.setOnMouseExited(e -> {
            if (!button.isPressed()) animateTo.accept(normal);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        button.setOnMousePressed(e -> {
            animateTo.accept(pressed);
            button.setScaleX(0.98);
            button.setScaleY(0.98);
        });

        button.setOnMouseReleased(e -> {
            button.setScaleX(button.isHover() ? 1.01 : 1.0);
            button.setScaleY(button.isHover() ? 1.01 : 1.0);
            animateTo.accept(button.isHover() ? hover : normal);
        });
    }

    private void setBackgroundColor(Pane box, String col1, String col2, String col3) {
        if (box!=null){
            box.setBackground(new Background(new BackgroundFill(
                    new LinearGradient(0,0,0,1,true, CycleMethod.NO_CYCLE,
                            new Stop(0.0, Color.web(col1)),
                            new Stop(0.5, Color.web(col2)),
                            new Stop(1.0, Color.web(col3))),
                    CornerRadii.EMPTY, Insets.EMPTY)));
            box.setPadding(new Insets(20));
        }
    }
}