package net.backslashtrash.project1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;


public class App extends Application {

    public static Scene mainMenu;
    private static Account currentUser;

    public static void lock(Account account){
        currentUser = account;
    }
    public static Account getCurrentUser() {
        return currentUser;
    }
    public static void unlock() {
        currentUser = null;
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource("titleScreen.fxml"));
        mainMenu = new Scene(root, 900, 600);
        stage.setTitle("Management Tool");
        stage.setScene(mainMenu);
        try {
            Image icon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("icon.png")));
            stage.getIcons().add(icon);
        } catch (NullPointerException e) {
            System.out.println("Icon image not found. Please check the filename and path.");
        }
        stage.setMaximized(true);
        stage.show();
    }
}