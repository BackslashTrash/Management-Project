package net.backslashtrash.project1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    public Label signInText;
    public Button signInButton;

    private Scene scene;
    private Stage stage;
    private Parent lastRoot;

    private final String[] resourceListFXML = {
            "titleScreen.fxml", "register.fxml", "login.fxml", "employee.fxml", "employer.fxml"
    };
    private final String[] resourceListJSON = {
            "attendance","employee","employer"
    };


    public void onSignup(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[Files.REGISTER.INDEX])));
    }

    public void onLogin(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[Files.LOGIN.INDEX])));
    }

    @FXML
    public void onConfirmLogin(ActionEvent event) throws IOException {
        String user = loginUser.getText();
        String pass = loginPass.getText();
        warningMessage.setText("");
        Account account = findAccount(resourceListJSON[1],user,pass );
        if (account != null){
            loginAccount(account,event,3);
            return;
        }

        account = findAccount(resourceListJSON[2],user,pass );
        if (account != null){
            loginAccount(account,event,4);
            return;
        }
        warningMessage.setText("Username or password incorrect");
    }

    @FXML
    public void onConfirmRegister(ActionEvent event) throws IOException {
        if (isAccountValid()){
            if (AccountManager.register(accountTypeSelect.getValue(),enterUser.getText(),enterPass.getText())){
                AccountManager.alertCreator(Alert.AlertType.INFORMATION,"Sign up","Account created, please login now" );
                switchScene(event,FXMLLoader.load(App.class.getResource(resourceListFXML[Files.TITLESCREEN.INDEX])));
            }
        }
    }

    @FXML
    public void onHome(ActionEvent event) throws IOException {
        switchScene(event, FXMLLoader.load(App.class.getResource(resourceListFXML[Files.TITLESCREEN.INDEX])));
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



    private boolean accountInvalid(String message){
        warningMessage.setText(message);
        return false;
    }

    private void debugPrinting() {
        System.out.println(accountTypeSelect.getValue());
        System.out.println(enterUser.getText());
        System.out.println(enterPass.getText());
    }

    private Account findAccount(String filename, String user, String pass) throws IOException {
        File file =  new File("src/main/resources/net/backslashtrash/objects/",filename + ".json");
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Account> accountArrayList = mapper.readValue(file, new TypeReference<>() {});
        if (file.length() == 0) {
            return null;
        }
        for (Account account : accountArrayList){
            if (account.getUsername().equals(user) && account.getPassword().equals(pass)){
                return account;
            }
        }
        return null;
    }

    @FXML
    public void onSignInDaily(ActionEvent event) {
        signInText.setText("You have signed \n in today");
        signInButton.setVisible(false);
    }

    private void loginAccount(Account account,ActionEvent event, int index) throws IOException {
        App.lock(account);
        switchScene(event,FXMLLoader.load(App.class.getResource(resourceListFXML[index])));
    }

}
