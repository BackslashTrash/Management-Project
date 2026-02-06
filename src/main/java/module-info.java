module net.backslashtrash.project1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires tools.jackson.core;


    opens net.backslashtrash.project1 to javafx.fxml;
    exports net.backslashtrash.project1;
}