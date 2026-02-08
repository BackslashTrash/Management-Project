module net.backslashtrash.project1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;


    opens net.backslashtrash.project1 to javafx.fxml;
    exports net.backslashtrash.project1;
}