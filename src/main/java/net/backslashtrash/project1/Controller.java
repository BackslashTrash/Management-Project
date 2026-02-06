package net.backslashtrash.project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    public ChoiceBox<String> accountTypeSelect = new ChoiceBox<>();

    private Scene scene;
    private Stage stage;
    private Parent root;
    private final String[] accountType = {"Employee", "Employer"};

    @FXML
    public void employerLogin(ActionEvent event){

    }

    @FXML
    public void employeeLogin(ActionEvent event) {

    }

    @FXML
    public void onRegister(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource("register.fxml")));
    }

    @FXML
    public void onConfirmRegister(ActionEvent event) {

    }

    @FXML
    public void onHome(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource("titleScreen.fxml")));
    }

    /*
    * Switches scene between different FXML files
    *
    *
    * */
    private void switchScene(ActionEvent event, Parent root) {
        stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root,stage.getWidth()-16,stage.getHeight()-39); //I don't know why subtract 16 or subtract 39, but it worked
        //System.out.println(stage.getWidth() + " " + stage.getHeight());
        stage.setScene(scene);
        stage.show();
    }

    /*
    * Loads on startup
    *
    * */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountTypeSelect.getItems().addAll(accountType);
    }

    private String getAccountSelected(){
        return accountTypeSelect.getValue();
    }


}
