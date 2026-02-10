package net.backslashtrash.project1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.stream.Collectors;

public class AccountManager {
    // Configured to not fail on unknown properties
    private static final ObjectMapper objectMapper = JsonMapper
            .builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public AccountManager() {

    }

    public static boolean register(String type, String username, String password) throws IOException {
        ArrayList<Account> accountList = new ArrayList<>();
        File file = new File("src/main/resources/net/backslashtrash/project1/objects/",type.toLowerCase() + ".json");
        if (!file.exists()) {
            file = new File("src/main/resources/net/backslashtrash/objects/",type.toLowerCase() + ".json");
        }

        if (file.exists() && file.length()>0) {
            accountList = objectMapper.readValue(file, new TypeReference<>() {});
        };

        for (Account account : accountList) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                alertCreator(Alert.AlertType.WARNING,"Sign up", "Username already exist!");
                return false;
            }
        }

        String uuid = null;
        if (type.equalsIgnoreCase("employee")){
            byte[] nameBytes = username.getBytes(StandardCharsets.UTF_8);
            final UUID uuid1= UUID.nameUUIDFromBytes(nameBytes);
            uuid = uuid1.toString();
        }

        accountList.add(new Account(uuid,username,password));
        objectMapper.writeValue(file, accountList);
        return true;
    }

    public static void addEmployeeToEmployer(String employerUsername, String employeeID) throws IOException {
        File file = getFile("employer.json");
        if (!file.exists()) return;

        List<Map<String, Object>> employers = objectMapper.readValue(file, new TypeReference<>() {});

        boolean updated = false;
        for (Map<String, Object> emp : employers) {
            if (employerUsername.equals(emp.get("username"))) {
                List<String> employees = (List<String>) emp.get("employees");
                if (employees == null) {
                    employees = new ArrayList<>();
                    emp.put("employees", employees);
                }

                if (!employees.contains(employeeID)) {
                    employees.add(employeeID);
                    updated = true;
                }
                break;
            }
        }

        if (updated) {
            objectMapper.writeValue(file, employers);
        }
    }

    public static void removeEmployeeFromEmployer(String employerUsername, String employeeID) throws IOException {
        File file = getFile("employer.json");
        if (!file.exists()) return;

        List<Map<String, Object>> employers = objectMapper.readValue(file, new TypeReference<>() {});
        boolean updated = false;

        for (Map<String, Object> emp : employers) {
            if (employerUsername.equals(emp.get("username"))) {
                List<String> employees = (List<String>) emp.get("employees");
                if (employees != null) {
                    if (employees.remove(employeeID)) {
                        updated = true;
                    }
                }
                break;
            }
        }

        if (updated) {
            objectMapper.writeValue(file, employers);
        }
    }

    public static ArrayList<String> getEmployerEmployeeList(String employerUsername) throws IOException {
        File file = getFile("employer.json");
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        List<Map<String, Object>> employers = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> emp : employers) {
            if (employerUsername.equals(emp.get("username"))) {
                List<String> list = (List<String>) emp.get("employees");
                return list != null ? new ArrayList<>(list) : new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public static String findEmployer(String employeeUuid) throws IOException {
        File file = getFile("employer.json");
        if (!file.exists() || file.length() == 0) return null;

        List<Map<String, Object>> employers = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> emp : employers) {
            List<String> list = (List<String>) emp.get("employees");
            if (list != null && list.contains(employeeUuid)) {
                return (String) emp.get("username");
            }
        }
        return null;
    }

    public static void markAttendance(String employeeUuid, String employerUsername) throws IOException {
        File file = getFile("attendance.json");
        List<Map<String, String>> attendanceList;

        if (file.exists() && file.length() > 0) {
            attendanceList = objectMapper.readValue(file, new TypeReference<>() {});
        } else {
            attendanceList = new ArrayList<>();
        }

        String today = LocalDate.now().toString();

        for (Map<String, String> entry : attendanceList) {
            if (entry.get("date").equals(today) && entry.get("uuid").equals(employeeUuid)) {
                return;
            }
        }

        Map<String, String> entry = new HashMap<>();
        entry.put("date", today);
        entry.put("uuid", employeeUuid);
        entry.put("employer", employerUsername);

        attendanceList.add(entry);
        objectMapper.writeValue(file, attendanceList);
    }

    public static boolean isSignedToday(String employeeUuid) throws IOException {
        File file = getFile("attendance.json");
        if (!file.exists() || file.length() == 0) return false;

        List<Map<String, String>> attendanceList = objectMapper.readValue(file, new TypeReference<>() {});
        String today = LocalDate.now().toString();

        for (Map<String, String> entry : attendanceList) {
            if (entry.get("date").equals(today) && entry.get("uuid").equals(employeeUuid)) {
                return true;
            }
        }
        return false;
    }

    public static void updateEmployeeJob(String employeeUuid, String newJob) throws IOException {
        File file = getFile("employee.json");
        if (!file.exists()) return;

        List<Map<String, Object>> employees = objectMapper.readValue(file, new TypeReference<>() {});
        boolean updated = false;

        for (Map<String, Object> emp : employees) {
            if (employeeUuid.equals(emp.get("uuid"))) {
                emp.put("job", newJob);
                updated = true;
                break;
            }
        }

        if (updated) {
            objectMapper.writeValue(file, employees);
        }
    }

    public static String getEmployeeJob(String employeeUuid) throws IOException {
        File file = getFile("employee.json");
        if (!file.exists()) return "Unassigned";

        List<Map<String, Object>> employees = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> emp : employees) {
            if (employeeUuid.equals(emp.get("uuid"))) {
                Object job = emp.get("job");
                return job != null ? job.toString() : "Unassigned";
            }
        }
        return "Unassigned";
    }

    // --- Task Management Methods ---

    /**
     * Assigns a task to a list of employees.
     * Updates both tasks.json (log) and employee.json (current status).
     */
    public static void assignTask(List<String> employeeUuids, String description, LocalDate date, LocalTime start, LocalTime end, String employerUsername) throws IOException {
        String timeString = start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER);
        String dateString = date.format(DATE_FORMATTER);

        // 1. Update Current Task in employee.json
        File empFile = getFile("employee.json");
        if (empFile.exists()) {
            List<Map<String, Object>> employees = objectMapper.readValue(empFile, new TypeReference<>() {});
            boolean empUpdated = false;
            for (Map<String, Object> emp : employees) {
                if (employeeUuids.contains(emp.get("uuid"))) {
                    emp.put("task", description + " (" + timeString + ")");
                    empUpdated = true;
                }
            }
            if (empUpdated) objectMapper.writeValue(empFile, employees);
        }

        // 2. Add to tasks.json log with structural data for expiry checking
        File taskFile = getFile("tasks.json");
        List<Map<String, String>> tasks;
        if (taskFile.exists() && taskFile.length() > 0) {
            tasks = objectMapper.readValue(taskFile, new TypeReference<>() {});
        } else {
            tasks = new ArrayList<>();
        }

        for (String uuid : employeeUuids) {
            Map<String, String> newTask = new HashMap<>();
            newTask.put("id", UUID.randomUUID().toString());
            newTask.put("employeeUuid", uuid);
            newTask.put("employer", employerUsername);
            newTask.put("description", description);
            // Store display string
            newTask.put("time", dateString + " " + timeString);
            // Store raw ISO strings for logic
            newTask.put("rawDate", dateString);
            newTask.put("rawStart", start.format(TIME_FORMATTER));
            newTask.put("rawEnd", end.format(TIME_FORMATTER));

            tasks.add(newTask);
        }
        objectMapper.writeValue(taskFile, tasks);
    }

    public static List<Map<String, String>> getAllTasks(String employerUsername) throws IOException {
        File file = getFile("tasks.json");
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        List<Map<String, String>> allTasks = objectMapper.readValue(file, new TypeReference<>() {});
        List<Map<String, String>> filteredTasks = new ArrayList<>();

        for (Map<String, String> task : allTasks) {
            if (employerUsername.equals(task.get("employer"))) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }

    /**
     * Removes tasks from tasks.json AND clears the task status in employee.json
     */
    public static void removeTasks(List<String> taskIds) throws IOException {
        File taskFile = getFile("tasks.json");
        if (!taskFile.exists()) return;

        List<Map<String, String>> tasks = objectMapper.readValue(taskFile, new TypeReference<>() {});
        List<String> affectedEmployeeUuids = new ArrayList<>();

        // Identify employees affected by the removal
        for (Map<String, String> task : tasks) {
            if (taskIds.contains(task.get("id"))) {
                affectedEmployeeUuids.add(task.get("employeeUuid"));
            }
        }

        // Remove from tasks.json
        tasks.removeIf(task -> taskIds.contains(task.get("id")));
        objectMapper.writeValue(taskFile, tasks);

        // Remove from employee.json (Set task to "None")
        if (!affectedEmployeeUuids.isEmpty()) {
            File empFile = getFile("employee.json");
            if (empFile.exists()) {
                List<Map<String, Object>> employees = objectMapper.readValue(empFile, new TypeReference<>() {});
                boolean empUpdated = false;
                for (Map<String, Object> emp : employees) {
                    if (affectedEmployeeUuids.contains(emp.get("uuid"))) {
                        emp.put("task", "None");
                        empUpdated = true;
                    }
                }
                if (empUpdated) objectMapper.writeValue(empFile, employees);
            }
        }
    }

    /**
     * Checks all tasks. If current time > task end time, remove the task automatically.
     * Should be called on application startup or periodically.
     */
    public static void checkExpiredTasks() throws IOException {
        File file = getFile("tasks.json");
        if (!file.exists() || file.length() == 0) return;

        List<Map<String, String>> tasks = objectMapper.readValue(file, new TypeReference<>() {});
        List<String> expiredIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Map<String, String> task : tasks) {
            if (task.containsKey("rawDate") && task.containsKey("rawEnd")) {
                try {
                    LocalDate date = LocalDate.parse(task.get("rawDate"), DATE_FORMATTER);
                    LocalTime endTime = LocalTime.parse(task.get("rawEnd"), TIME_FORMATTER);
                    LocalDateTime taskEndDateTime = LocalDateTime.of(date, endTime);

                    if (now.isAfter(taskEndDateTime)) {
                        expiredIds.add(task.get("id"));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing task date/time: " + e.getMessage());
                }
            }
        }

        if (!expiredIds.isEmpty()) {
            removeTasks(expiredIds);
            System.out.println("Removed " + expiredIds.size() + " expired tasks.");
        }
    }

    public static String getEmployeeTask(String employeeUuid) throws IOException {
        File file = getFile("employee.json");
        if (!file.exists()) return "None";

        List<Map<String, Object>> employees = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> emp : employees) {
            if (employeeUuid.equals(emp.get("uuid"))) {
                Object task = emp.get("task");
                return task != null ? task.toString() : "None";
            }
        }
        return "None";
    }

    // Helper to handle path differences
    private static File getFile(String name) {
        File f = new File("src/main/resources/net/backslashtrash/project1/objects/" + name);
        if (!f.getParentFile().exists()) {
            f = new File("src/main/resources/net/backslashtrash/objects/" + name);
        }
        return f;
    }

    public static void alertCreator(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}