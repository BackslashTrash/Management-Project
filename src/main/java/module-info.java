module net.backslashtrash.project1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires tools.jackson.core;
    requires com.fasterxml.jackson.annotation;


    opens net.backslashtrash.project1 to javafx.fxml;
    exports net.backslashtrash.project1;
}