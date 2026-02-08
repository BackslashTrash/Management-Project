package net.backslashtrash.project1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class App extends Application {

    public static Scene mainMenu;
    private static Account currentUser;

    public static void login(Account account){
        currentUser = account;
    }
    public static Account getCurrentUser() {
        return currentUser;
    }
    public static void logout() {
        currentUser = null;
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource("titleScreen.fxml"));
        mainMenu = new Scene(root, 900, 500);
        stage.setTitle("Management Tool");
        stage.setScene(mainMenu);
        stage.show();
    }
}
