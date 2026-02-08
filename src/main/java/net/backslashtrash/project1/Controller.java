package net.backslashtrash.project1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    public TextField loginUser;
    public PasswordField loginPass;
    public Button loginButton;

    private Scene scene;
    private Stage stage;
    private Parent lastRoot;

    private final String[] resourceList = {
            "titleScreen.fxml", "register.fxml", "login.fxml", "employee.fxml", "employer.fxml"
    };

    public void onSignup(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceList[Files.REGISTER.INDEX])));
    }

    public void onLogin(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceList[Files.LOGIN.INDEX])));
    }

    @FXML
    public void onRegister(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceList[Files.REGISTER.INDEX])));
    }

    @FXML
    public void onConfirmRegister(ActionEvent event) throws IOException {
        if (isAccountValid()){
            if (Register.register(accountTypeSelect.getValue(),enterUser.getText(),enterPass.getText())){
                JOptionPane.showMessageDialog(null, "Account created, please login now", "Account Creation",JOptionPane.INFORMATION_MESSAGE);
                switchScene(event,FXMLLoader.load(App.class.getResource(resourceList[Files.TITLESCREEN.INDEX])));
            }
        }
    }

    @FXML
    public void onHome(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceList[Files.TITLESCREEN.INDEX])));
    }

    /*
    * Switches scene between different FXML files
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
    * */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final String[] accountType = {"Employee", "Employer"};
        accountTypeSelect.getItems().addAll(accountType);
    }

    private boolean isAccountValid() {
        String username =  enterUser.getText();
        String pass = enterPass.getText();
        String confirm = confirmPass.getText();
        warningMessage.setText("");
        if (username.length()<3 || username.length()>15){
            return accountInvalid("Username must be between 3 and 15 characters");
        }
        if (pass.length()<3 || pass.length()>15){
            return accountInvalid("Password must be between 3 and 15 characters");
        }
        if (!pass.equals(confirm)) {
            return accountInvalid("Password mismatch");
        }
        if (accountTypeSelect.getValue() == null){
           return accountInvalid("Please select an account type");
        }
        return true;
    }

    private Parent getLastRoot() {
        return lastRoot;
    }

    @FXML
    public void onConfirmLogin(ActionEvent event) {

    }

    private boolean accountInvalid(String message){
        warningMessage.setText(message);
        return false;
    }

    private void debugPrinting() {
        System.out.println(accountTypeSelect.getValue());
        System.out.println(enterUser.getText());
        System.out.println(enterPass.getText());
    }


}
