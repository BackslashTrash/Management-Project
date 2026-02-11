module net.backslashtrash.project1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.calendarfx.view;
    requires com.dlsc.gemsfx;
    requires javafx.graphics;


    opens net.backslashtrash.project1 to javafx.fxml;
    exports net.backslashtrash.project1;
}