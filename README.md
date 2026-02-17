# Simple Workforce Management Program

A comprehensive desktop application built with **JavaFX** designed to streamline the interaction between Employers and Employees. This system handles task assignment, attendance tracking, payroll calculation, and job management using a lightweight JSON-based data storage system.

**Note: This app does not have a built in server, for commercialized use, you must host your own server that requires tweaking the code. This app should serve as a the front end while the backend must be created by your self.**

##  Features

### For Employers

- **Employee Management:** Add employees to your organization using their unique UUIDs.
- **Job Creation:** Define job roles with specific titles, descriptions, and hourly pay rates.
- **Task Assignment:** Assign tasks to specific employees or create unassigned tasks. Supports creating tasks with start/end times and descriptions.
- **Calendar View:** Visual overview of all assigned tasks via the integrated **CalendarFX** view.
- **Payroll Management:** Track total earnings for employees based on completed tasks and hourly rates. Includes functionality to reset payment periods.
- **Filtering:** Filter employee lists by job titles for easier management.

### For Employees

- **Personal Dashboard:** View total earnings and a schedule overview.
- **Attendance:** Trackable daily attendance.
- **Task Management:** View assigned tasks, descriptions, and time frames visually on a calendar.
- **Task Completion:** Mark tasks as complete to automatically calculate and update earnings.

## Tech Stack

- **Language:** Java (OpenJDK)
- **UI Framework:** JavaFX
- **Data Persistence:** JSON (Jackson Library)
- **UI Libraries:**
    - **CalendarFX:** For the schedule/calendar visualization.
    - **GemsFX:** For advanced UI controls (SelectionBox).
    - **Jackson Databind:** For serializing/deserializing objects to JSON.
## Project Structure

This project follows a standard JavaFX application structure, organizing source code, resources (FXML views, JSON data), and configuration files.

```
src/
├── main/
│   ├── java/
│   │   └── net/
│   │       └── backslashtrash/
│   │           └── project1/
│   │               ├── App.java                # Main entry point for the JavaFX application
│   │               ├── AccountManager.java     # Handles data logic (JSON read/write, business rules)
│   │               ├── Controller.java         # Main controller managing all FXML views and interactions
│   │               └── Account.java            # Model class for user account data
│   │
│   └── resources/
│       └── net/
│           └── backslashtrash/
│               ├── objects/                    # JSON Data Storage
│               │   ├── employee.json           # Stores employee profiles, earnings, and status
│               │   ├── employer.json           # Stores employer accounts and linked employees
│               │   ├── tasks.json              # Stores all assigned tasks and their details
│               │   ├── jobs.json               # Stores job definitions (titles, pay rates)
│               │   ├── attendance.json         # Stores daily sign-in records
│               │   └── lastreset.json          # Stores timestamps for the last payment reset
│               │
│               └── project1/                   # FXML Views (User Interface)
│                   ├── login.fxml              # Login screen
│                   ├── register.fxml           # Registration screen
│                   ├── titleScreen.fxml        # Initial landing screen
│                   ├── employer.fxml           # Main Employer Dashboard
│                   ├── employee.fxml           # Main Employee Dashboard
│                   ├── employeelist.fxml       # List view for managing employees
│                   ├── tasklist.fxml           # List view for managing/assigning tasks
│                   ├── joblist.fxml            # List view for creating/managing jobs
│                   └── employeetasklist.fxml   # Task view specific to the logged-in employee
```

### Key Components

- **`Controller.java`**: A centralized controller that manages navigation and logic for all FXML views. It handles dynamic data loading, table population, and user interactions.
- **`AccountManager.java`**: The "backend" of the application. It performs file I/O operations using Jackson to read/write JSON files, handles authentication, and executes business logic like calculating earnings and assigning tasks.
- **`objects/`**: This directory acts as the database for the application, storing persistent data in JSON format.
## Installation & Setup

1. **Clone the repository**
    
    ```
    git clone https://github.com/BackslashTrash/Management-Project.git
    ```
    
2. **Open in IDE**
    - Open the project in IntelliJ IDEA, Eclipse, or VS Code.
    - Ensure your project SDK is set to **Java 17** or higher.
3. Edit the files you like to fit your own personal need

##  Usage Guide

### 1. Sign up accounts

- Launch the app and select **Sign Up**.
- Create an **Employer** account first to set up the organization.
- Create an **Employee** account to simulate a worker.

### 2. Adding employees

1. Log in as the **Employee**.
2. On the sidebar, click **Copy My UUID**.
3. Log out and log in as the **Employer**.
4. Click the **Add Employee** button (usually represented by a `+` user icon).
5. Paste the UUID to link the employee to your dashboard.

### 3. Workflow

1. **Employer:** Go to "Job List" and create a job role (e.g., "Developer", "$25.00").
2. **Employer:** Go to "Employee List", select the employee, and assign them the Job Role.
3. **Employer:** Go to "Task List" or click "Add Task" to assign work to the employee.
4. **Employee:** Log in, view the task in "My Task List", and click **Complete** when finished.
5. **System:** Earnings are automatically calculated based on the task duration and job pay rate.

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
