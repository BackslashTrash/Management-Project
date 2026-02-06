package net.backslashtrash.project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    public ChoiceBox<String> accountTypeSelect = new ChoiceBox<>();
    public TextField enterUser;
    public PasswordField enterPass;
    public PasswordField confirmPass;
    public Text warningMessage;

    private Scene scene;
    private Stage stage;
    private Parent lastRoot;
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
    public void onConfirmRegister(ActionEvent event) throws IOException {
        if (isAccountValid()){
            Register.register(accountTypeSelect.getValue(),enterUser.getText(),enterPass.getText());
            switchScene(event,getResource(Files.TITLESCREEN.INDEX));
            JOptionPane.showMessageDialog(null, "Account created, please login now", "Account Creation",JOptionPane.INFORMATION_MESSAGE);

        }
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
        lastRoot = root;
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

    private boolean isAccountValid() {
        String username =  enterUser.getText();
        String pass = enterPass.getText();
        String confirm = confirmPass.getText();
        warningMessage.setText("");
        boolean isValid = true;
        if (username.length()<3 || username.length()>15){
            warningMessage.setText("Username must be between 3 and 15 characters");
            isValid =  false;
        }
        if (pass.length()<3 || pass.length()>15){
            warningMessage.setText("Password must be between 3 and 15 characters");
            isValid =  false;
        }
        if (!pass.equals(confirm)) {
            warningMessage.setText("Password mismatch");
            isValid =  false;
        }
        if (accountTypeSelect.getValue() == null){
            warningMessage.setText("Please select an account type");
            isValid = false;
        }

        return isValid;
    }

    private Parent getLastRoot() {
        return lastRoot;
    }
    private Parent getResource(int index) throws IOException{
        Parent[] resourceList = {
                FXMLLoader.load(App.class.getResource("titleScreen.fxml")),
                FXMLLoader.load(App.class.getResource("employee.fxml")),
                FXMLLoader.load(App.class.getResource("employer.fxml")),
                FXMLLoader.load(App.class.getResource("login.fxml")),
                FXMLLoader.load(App.class.getResource("register.xml"))
        };
        return resourceList[index];
    }
}
