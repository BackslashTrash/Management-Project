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
import javafx.collections.transformation.FilteredList; // Added
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
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.Map;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    // --- Navigation History ---
    private static final Stack<Integer> history = new Stack<>();
    private static int currentViewIndex = 0;

    // --- Table Data Models ---
    // Master list containing all loaded employees
    private final ObservableList<EmployeeTableItem> masterData = FXCollections.observableArrayList();
    // Filtered list wrapper that the table actually displays
    private final FilteredList<EmployeeTableItem> filteredData = new FilteredList<>(masterData, p -> true);

    @FXML
    public ChoiceBox<String> accountTypeSelect = new ChoiceBox<>();
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
    @FXML public TableColumn<EmployeeTableItem, String> colTask;
    @FXML public ComboBox<String> filterJobSelect;

    // --- Task List Screen Fields ---
    @FXML public TableView<TaskTableItem> taskTable;
    @FXML public TableColumn<TaskTableItem, CheckBox> colTaskSelect;
    @FXML public TableColumn<TaskTableItem, String> colTaskDesc;
    @FXML public TableColumn<TaskTableItem, String> colTaskTime;
    @FXML public TableColumn<TaskTableItem, String> colTaskAssignee;

    // --- Job List Screen Fields ---
    @FXML public TableView<JobTableItem> jobTable;
    @FXML public TableColumn<JobTableItem, CheckBox> colJobSelect;
    @FXML public TableColumn<JobTableItem, String> colJobTitle;
    @FXML public TableColumn<JobTableItem, String> colJobPay;
    @FXML public TableColumn<JobTableItem, String> colJobDesc;

    private CheckBox selectAllCheckBox;
    private CheckBox selectAllTasksCheckBox;
    private CheckBox selectAllJobsCheckBox;

    private Scene scene;
    private Stage stage;
    private Parent lastRoot;

    // Added jobList.fxml at index 7
    private final String[] resourceListFXML = {
            "titleScreen.fxml", "register.fxml", "login.fxml", "employee.fxml", "employer.fxml", "employeeList.fxml", "taskList.fxml", "jobList.fxml"
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
                currentViewIndex = 4;
                switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[4])));
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

        // Validation
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
                // Refresh employee list so the new job appears in dropdowns immediately
                loadEmployeeListData();
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

    // --- ADD TASK DIALOG (Robust Date/Time) ---
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

        grid.add(new Label("Description:"), 0, 0);
        grid.add(taskDesc, 1, 0, 3, 1);

        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1, 3, 1);

        grid.add(new Label("Start Time:"), 0, 2);
        HBox startBox = new HBox(5, startHour, new Label(":"), startMin);
        startBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(startBox, 1, 2, 3, 1);

        grid.add(new Label("End Time:"), 0, 3);
        HBox endBox = new HBox(5, endHour, new Label(":"), endMin);
        endBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(endBox, 1, 3, 3, 1);

        dialog.getDialogPane().setContent(grid);

        final Button assignButton = (Button) dialog.getDialogPane().lookupButton(assignButtonType);
        assignButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String desc = taskDesc.getText();
            LocalDate date = datePicker.getValue();

            if (desc.isEmpty() || date == null) {
                AccountManager.alertCreator(Alert.AlertType.WARNING, "Invalid Input", "Please enter description and date.");
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
                AccountManager.assignTask(selectedUuids, data.desc, data.date, data.start, data.end, App.getCurrentUser().getUsername());
                loadEmployeeListData();
                AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Success", "Task assigned successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class TaskData {
        String desc; LocalDate date; LocalTime start; LocalTime end;
        TaskData(String d, LocalDate dt, LocalTime s, LocalTime e) {
            desc = d; date = dt; start = s; end = e;
        }
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
                masterData.remove(item); // Remove from masterData, FilteredList updates auto
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
                idsToRemove.add(item.getId());
            }
        }

        if (toRemove.isEmpty()) {
            AccountManager.alertCreator(Alert.AlertType.WARNING, "Remove", "No tasks selected.");
            return;
        }

        try {
            AccountManager.removeTasks(idsToRemove);
            allItems.removeAll(toRemove);
            updateSelectAllTasksState();
            AccountManager.alertCreator(Alert.AlertType.INFORMATION, "Remove", "Deleted " + toRemove.size() + " tasks.");
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
            allItems.removeAll(toRemove);
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

        // --- Initialize Employee List Table with Select All ---
        if (employeeTable != null) {

            // Create the Header Checkbox
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
            colTask.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTask()));

            // Bind table to FilteredList
            employeeTable.setItems(filteredData);

            // Add Listener to Filter Dropdown
            if (filterJobSelect != null) {
                filterJobSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    filteredData.setPredicate(employee -> {
                        // If no filter or "All Jobs", show everything
                        if (newValue == null || "All Jobs".equals(newValue)) {
                            return true;
                        }
                        // Compare dropdown value with employee's job
                        String empJob = employee.getJobBox().getValue();
                        return newValue.equals(empJob);
                    });
                });
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
            colTaskDesc.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
            colTaskTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTime()));
            colTaskAssignee.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAssignee()));

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

            loadJobListData();
        }

        if (signInButton != null && App.getCurrentUser() != null && signInText != null) {
            try {
                if (AccountManager.isSignedToday(App.getCurrentUser().getUuid())) {
                    signInText.setText("You have signed \n in today");
                    signInButton.setVisible(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                } else {
                    chooseEmployee.getItems().add(uuid);
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
            // Fetch available jobs for the dropdown
            List<String> availableJobs = AccountManager.getEmployerJobs(App.getCurrentUser().getUsername())
                    .stream().map(j -> j.get("title"))
                    .collect(Collectors.toList());
            availableJobs.add(0, "Unassigned"); // Default option

            // Update Filter Dropdown items (preserve selection if possible)
            if (filterJobSelect != null) {
                String currentFilter = filterJobSelect.getValue();
                filterJobSelect.getItems().clear();
                filterJobSelect.getItems().add("All Jobs");
                filterJobSelect.getItems().addAll(availableJobs);
                filterJobSelect.getItems().remove("Unassigned"); // Typically don't filter by 'Unassigned' via dynamic list, but ok to leave or add explicitly

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

                EmployeeTableItem item = new EmployeeTableItem(uuid, name, job, status, task, availableJobs);
                item.getSelectBox().selectedProperty().addListener((obs, oldVal, newVal) -> {
                    updateSelectAllState();
                });

                masterData.add(item);
            }
            // No need to employeeTable.setItems() here, it's bound to filteredData which wraps masterData

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTaskListData() {
        if (App.getCurrentUser() == null) return;

        ObservableList<TaskTableItem> tableData = FXCollections.observableArrayList();
        try {
            List<Map<String, String>> tasks = AccountManager.getAllTasks(App.getCurrentUser().getUsername());

            for (Map<String, String> t : tasks) {
                Account emp = findAccount(resourceListJSON[1], t.get("employeeUuid"));
                String assignee = (emp != null) ? emp.getUsername() : "Unknown";

                TaskTableItem item = new TaskTableItem(
                        t.get("id"),
                        t.get("description"),
                        t.get("time"),
                        assignee
                );
                item.getSelectBox().selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectAllTasksState());
                tableData.add(item);
            }
            taskTable.setItems(tableData);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadJobListData() {
        if (App.getCurrentUser() == null) return;

        ObservableList<JobTableItem> tableData = FXCollections.observableArrayList();
        try {
            List<Map<String, String>> jobs = AccountManager.getEmployerJobs(App.getCurrentUser().getUsername());

            for (Map<String, String> j : jobs) {
                JobTableItem item = new JobTableItem(
                        j.get("id"),
                        j.get("title"),
                        j.get("pay"),
                        j.get("description")
                );
                item.getSelectBox().selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectAllJobsState());
                tableData.add(item);
            }
            jobTable.setItems(tableData);

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
        private final CheckBox selectBox;
        private final ComboBox<String> jobBox;

        // Updated constructor to accept dynamic job list
        public EmployeeTableItem(String uuid, String name, String currentJob, String status, String task, List<String> availableJobs) {
            this.uuid = uuid;
            this.name = name;
            this.status = status;
            this.task = task;

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
        public CheckBox getSelectBox() { return selectBox; }
        public ComboBox<String> getJobBox() { return jobBox; }
    }

    public static class TaskTableItem {
        private final String id;
        private final String description;
        private final String time;
        private final String assignee;
        private final CheckBox selectBox;

        public TaskTableItem(String id, String description, String time, String assignee) {
            this.id = id;
            this.description = description;
            this.time = time;
            this.assignee = assignee;
            this.selectBox = new CheckBox();
            this.selectBox.setAlignment(Pos.CENTER);
            this.selectBox.setCursor(Cursor.HAND);
        }
        public String getId() { return id; }
        public String getDescription() { return description; }
        public String getTime() { return time; }
        public String getAssignee() { return assignee; }
        public CheckBox getSelectBox() { return selectBox; }
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

            signInText.setText("You have signed \n in today");
            signInButton.setVisible(false);

        } catch (IOException e) {
            e.printStackTrace();
            AccountManager.alertCreator(Alert.AlertType.ERROR, "Error", "Could not save attendance.");
        }
    }

    private void loginAccount(Account account,ActionEvent event, int index) throws IOException {
        App.lock(account);
        navigate(event, index); // Use navigate instead of switchScene
    }

    private boolean isSignIn(Account account) {
        Account current =  App.getCurrentUser();
        return false;
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
        history.clear(); // Clear history on logout
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