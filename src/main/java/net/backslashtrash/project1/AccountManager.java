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
import java.util.*;

public class AccountManager {
    private static final ObjectMapper objectMapper = JsonMapper
            .builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
            .build();


    public AccountManager() {

    }

    public static boolean register(String type, String username, String password) throws IOException {
        ArrayList<Account> accountList = new ArrayList<>();
        File file = new File("src/main/resources/net/backslashtrash/objects/",type.toLowerCase() + ".json");

        if (file.length()>0) {
            accountList = objectMapper.readValue(file, new TypeReference<>() {});
        };

        for (Account account : accountList) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                alertCreator(Alert.AlertType.WARNING,"Sign up", "Username already exist!");
                return false;
            }
        }

        String uuid = null;
        if (type.equalsIgnoreCase("employee")){     //Only employees need a UUID
            byte[] nameBytes = username.getBytes(StandardCharsets.UTF_8);
            final UUID uuid1= UUID.nameUUIDFromBytes(nameBytes);
            uuid = uuid1.toString();
        }

        accountList.add(new Account(uuid,username,password));
        objectMapper.writeValue(file, accountList);
        return true;
    }



    /**
     * Adds an employee UUID to the employer's list of employees in employer.json.
     * We read as a Map to safely modify the structure without needing the Account class to have the field yet.
     */
    public static void addEmployeeToEmployer(String employerUsername, String employeeID) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/employer.json");

        if (!file.exists()) return;

        // Read as list of maps to manipulate JSON structure directly
        List<Map<String, Object>> employers = objectMapper.readValue(file, new TypeReference<>() {});

        boolean updated = false;
        for (Map<String, Object> emp : employers) {
            if (employerUsername.equals(emp.get("username"))) {
                List<String> employees = (List<String>) emp.get("employees");
                if (employees == null) {
                    employees = new ArrayList<>();
                    emp.put("employees", employees);
                }

                // Avoid duplicates
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


    /**
     * Removes an employee UUID from the employer's list in employer.json.
     */
    public static void removeEmployeeFromEmployer(String employerUsername, String employeeID) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/employer.json");
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
    /**
     * Retrieves the list of employee UUIDs associated with a specific employer
     */
    public static ArrayList<String> getEmployerEmployeeList(String employerUsername) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/employer.json");
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
    /**
     * Finds the employer username for a given employee UUID.
     * This scans employer.json to find which employer list contains the UUID.
     */
    public static String findEmployer(String employeeUuid) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/employer.json");
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
    /**
     * Marks an employee as signed in for the current day in attendance.json
     */
    public static void markAttendance(String employeeUuid, String employerUsername) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/attendance.json");
        List<Map<String, String>> attendanceList;

        if (file.exists() && file.length() > 0) {
            attendanceList = objectMapper.readValue(file, new TypeReference<>() {});
        } else {
            attendanceList = new ArrayList<>();
        }

        String today = LocalDate.now().toString();

        // Check if already signed in to prevent duplicates
        for (Map<String, String> entry : attendanceList) {
            if (entry.get("date").equals(today) && entry.get("uuid").equals(employeeUuid)) {
                return; // Already signed in
            }
        }

        Map<String, String> entry = new HashMap<>();
        entry.put("date", today);
        entry.put("uuid", employeeUuid);
        entry.put("employer", employerUsername);

        attendanceList.add(entry);
        objectMapper.writeValue(file, attendanceList);
    }
    /**
     * Checks if the employee has signed in today.
     */
    public static boolean isSignedToday(String employeeUuid) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/attendance.json");
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
    /**
     * Updates the job title for a specific employee in employee.json
     */
    public static void updateEmployeeJob(String employeeUuid, String newJob) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/employee.json");
        if (!file.exists()) return;

        // Read as list of maps to allow adding fields dynamically
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

    /**
     * Gets the job title for a specific employee from employee.json
     */
    public static String getEmployeeJob(String employeeUuid) throws IOException {
        File file = new File("src/main/resources/net/backslashtrash/objects/employee.json");
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
    public static void alertCreator(Alert.AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }


}
