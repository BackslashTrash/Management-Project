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
    private static final DateTimeFormatter RESET_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
            // Also remove tasks associated with this employee for this employer
            removeTasksForEmployee(employeeID, employerUsername);
        }
    }

    // Helper method to clean up tasks when an employee is removed
    private static void removeTasksForEmployee(String employeeID, String employerUsername) throws IOException {
        // 1. Remove from tasks.json
        File taskFile = getFile("tasks.json");
        if (taskFile.exists()) {
            List<Map<String, String>> tasks = objectMapper.readValue(taskFile, new TypeReference<>() {});
            boolean tasksChanged = tasks.removeIf(task ->
                    employeeID.equals(task.get("employeeUuid")) &&
                            employerUsername.equals(task.get("employer"))
            );

            if (tasksChanged) {
                objectMapper.writeValue(taskFile, tasks);
            }
        }

        unassignCurrentTask(employeeID);
    }

    // --- Unassign current task manually (for "X" button) ---
    public static void unassignCurrentTask(String employeeUuid) throws IOException {
        // 1. Clear status in employee.json
        File empFile = getFile("employee.json");
        if (empFile.exists()) {
            List<Map<String, Object>> employees = objectMapper.readValue(empFile, new TypeReference<>() {});
            boolean empUpdated = false;
            for (Map<String, Object> emp : employees) {
                if (employeeUuid.equals(emp.get("uuid"))) {
                    emp.put("task", "None");
                    empUpdated = true;
                    break;
                }
            }
            if (empUpdated) objectMapper.writeValue(empFile, employees);
        }

        // 2. Remove active/future tasks from tasks.json for this user
        File taskFile = getFile("tasks.json");
        if (taskFile.exists()) {
            List<Map<String, String>> tasks = objectMapper.readValue(taskFile, new TypeReference<>() {});
            LocalDate today = LocalDate.now();
            boolean tasksChanged = tasks.removeIf(task -> {
                if (!employeeUuid.equals(task.get("employeeUuid"))) return false;
                // Remove if date is today or future (active tasks)
                if (task.containsKey("rawDate")) {
                    try {
                        LocalDate d = LocalDate.parse(task.get("rawDate"), DATE_FORMATTER);
                        return !d.isBefore(today);
                    } catch (Exception e) { return true; }
                }
                return true;
            });
            if (tasksChanged) objectMapper.writeValue(taskFile, tasks);
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
    public static void assignTask(List<String> employeeUuids, String title, String description, LocalDate date, LocalTime start, LocalTime end, String employerUsername) throws IOException {
        String timeString = start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER);
        String dateString = date.format(DATE_FORMATTER);

        // 1. Update Current Task in employee.json
        File empFile = getFile("employee.json");
        if (empFile.exists()) {
            List<Map<String, Object>> employees = objectMapper.readValue(empFile, new TypeReference<>() {});
            boolean empUpdated = false;
            for (Map<String, Object> emp : employees) {
                if (employeeUuids.contains(emp.get("uuid"))) {
                    // Update format to include Title
                    emp.put("task", title + ": " + description + " (" + timeString + ")");
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
            newTask.put("title", title);
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

    // NEW METHOD: Get tasks specific to an employee
    public static List<Map<String, String>> getTasksForEmployee(String employeeUuid) throws IOException {
        File file = getFile("tasks.json");
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        List<Map<String, String>> allTasks = objectMapper.readValue(file, new TypeReference<>() {});
        List<Map<String, String>> employeeTasks = new ArrayList<>();

        for (Map<String, String> task : allTasks) {
            if (employeeUuid.equals(task.get("employeeUuid"))) {
                employeeTasks.add(task);
            }
        }
        return employeeTasks;
    }

    /**
     * Removes tasks from tasks.json AND clears the task status in employee.json
     */
    public static void removeTask(String taskId) throws IOException {
        List<String> ids = new ArrayList<>();
        ids.add(taskId);
        removeTasks(ids);
    }

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
                        // Check if they have other tasks
                        List<Map<String, String>> remaining = getTasksForEmployee((String) emp.get("uuid"));
                        if (remaining.isEmpty()) {
                            emp.put("task", "None");
                        } else {
                            // Optionally update to next task
                            Map<String, String> next = remaining.get(0);
                            String t = next.getOrDefault("title", "Task");
                            String d = next.getOrDefault("description", "");
                            String tm = next.getOrDefault("time", "");
                            emp.put("task", t + ": " + d + " (" + tm + ")");
                        }
                        empUpdated = true;
                    }
                }
                if (empUpdated) objectMapper.writeValue(empFile, employees);
            }
        }
    }

    /**
     * Checks all tasks.
     * If the task's date is strictly before today, it is considered expired and removed.
     */
    public static void checkExpiredTasks() throws IOException {
        File file = getFile("tasks.json");
        if (!file.exists() || file.length() == 0) return;

        List<Map<String, String>> tasks = objectMapper.readValue(file, new TypeReference<>() {});
        List<String> expiredIds = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Map<String, String> task : tasks) {
            if (task.containsKey("rawDate")) {
                try {
                    LocalDate taskDate = LocalDate.parse(task.get("rawDate"), DATE_FORMATTER);

                    // Logic: If task date is before today (e.g. task was yesterday), it expires.
                    if (taskDate.isBefore(today)) {
                        expiredIds.add(task.get("id"));
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing task date: " + e.getMessage());
                }
            }
        }

        if (!expiredIds.isEmpty()) {
            removeTasks(expiredIds);
            System.out.println("Removed " + expiredIds.size() + " expired tasks (past date).");
        }
    }

    // --- Job Management ---

    public static void addJob(String employer, String title, String description, double pay) throws IOException {
        File file = getFile("jobs.json");
        List<Map<String, String>> jobs;
        if (file.exists() && file.length() > 0) {
            jobs = objectMapper.readValue(file, new TypeReference<>() {});
        } else {
            jobs = new ArrayList<>();
        }

        Map<String, String> job = new HashMap<>();
        job.put("id", UUID.randomUUID().toString());
        job.put("employer", employer);
        job.put("title", title);
        job.put("description", description);
        job.put("pay", String.format("%.2f", pay));

        jobs.add(job);
        objectMapper.writeValue(file, jobs);
    }

    public static List<Map<String, String>> getEmployerJobs(String employer) throws IOException {
        File file = getFile("jobs.json");
        if (!file.exists() || file.length() == 0) return new ArrayList<>();

        List<Map<String, String>> allJobs = objectMapper.readValue(file, new TypeReference<>() {});
        List<Map<String, String>> employerJobs = new ArrayList<>();
        for (Map<String, String> j : allJobs) {
            if (employer.equals(j.get("employer"))) {
                employerJobs.add(j);
            }
        }
        return employerJobs;
    }

    public static void removeJobs(List<String> jobIds) throws IOException {
        File file = getFile("jobs.json");
        if (!file.exists()) return;
        List<Map<String, String>> jobs = objectMapper.readValue(file, new TypeReference<>() {});
        jobs.removeIf(j -> jobIds.contains(j.get("id")));
        objectMapper.writeValue(file, jobs);
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

    // --- Payment Calculation Helpers ---

    public static double getHourlyRate(String employeeUuid) throws IOException {
        String jobTitle = getEmployeeJob(employeeUuid);
        if ("Unassigned".equals(jobTitle)) return 0.0;

        String employer = findEmployer(employeeUuid);
        if (employer == null) return 0.0;

        List<Map<String, String>> jobs = getEmployerJobs(employer);
        for (Map<String, String> j : jobs) {
            if (jobTitle.equals(j.get("title"))) {
                try {
                    return Double.parseDouble(j.get("pay"));
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }

    public static void addEarnings(String employeeUuid, double amount) throws IOException {
        File file = getFile("employee.json");
        if (!file.exists()) return;

        List<Map<String, Object>> employees = objectMapper.readValue(file, new TypeReference<>() {});
        boolean updated = false;

        for (Map<String, Object> emp : employees) {
            if (employeeUuid.equals(emp.get("uuid"))) {
                double current = 0.0;
                if (emp.containsKey("totalEarnings")) {
                    current = ((Number) emp.get("totalEarnings")).doubleValue();
                }
                emp.put("totalEarnings", current + amount);
                updated = true;
                break;
            }
        }

        if (updated) {
            objectMapper.writeValue(file, employees);
        }
    }

    public static double getEarnings(String employeeUuid) throws IOException {
        File file = getFile("employee.json");
        if (!file.exists()) return 0.0;

        List<Map<String, Object>> employees = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> emp : employees) {
            if (employeeUuid.equals(emp.get("uuid"))) {
                if (emp.containsKey("totalEarnings")) {
                    return ((Number) emp.get("totalEarnings")).doubleValue();
                }
                return 0.0;
            }
        }
        return 0.0;
    }

    // --- Reset Payment Logic ---
    public static void resetAllEarnings(String employerUsername) throws IOException {
        // 1. Get employees
        ArrayList<String> empIds = getEmployerEmployeeList(employerUsername);

        // 2. Update employees
        File empFile = getFile("employee.json");
        if (empFile.exists()) {
            List<Map<String, Object>> employees = objectMapper.readValue(empFile, new TypeReference<>() {});
            boolean changed = false;
            for (Map<String, Object> emp : employees) {
                if (empIds.contains(emp.get("uuid"))) {
                    emp.put("totalEarnings", 0.0);
                    changed = true;
                }
            }
            if (changed) objectMapper.writeValue(empFile, employees);
        }

        // 3. Update employer reset time
        File employerFile = getFile("employer.json");
        if (employerFile.exists()) {
            List<Map<String, Object>> employers = objectMapper.readValue(employerFile, new TypeReference<>() {});
            for (Map<String, Object> emp : employers) {
                if (employerUsername.equals(emp.get("username"))) {
                    emp.put("lastPaymentReset", LocalDateTime.now().format(RESET_TIME_FORMATTER));
                    objectMapper.writeValue(employerFile, employers);
                    break;
                }
            }
        }
    }

    public static String getLastPaymentReset(String employerUsername) throws IOException {
        File file = getFile("employer.json");
        if (!file.exists()) return "Never";
        List<Map<String, Object>> employers = objectMapper.readValue(file, new TypeReference<>() {});
        for (Map<String, Object> emp : employers) {
            if (employerUsername.equals(emp.get("username"))) {
                return (String) emp.getOrDefault("lastPaymentReset", "Never");
            }
        }
        return "Never";
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