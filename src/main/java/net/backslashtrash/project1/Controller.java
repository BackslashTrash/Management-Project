package net.backslashtrash.project1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;


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
    public VBox rootVBox;
    public Button signUpButton;
    public Button confirmLogin;
    public Button homeButton;

    private Scene scene;
    private Stage stage;
    private Parent lastRoot;

    private final String[] resourceListFXML = {
            "titleScreen.fxml", "register.fxml", "login.fxml", "employee.fxml", "employer.fxml"
    };
    private final String[] resourceListJSON = {
            "attendance","employee","employer"
    };

    private static final String ADD_USER =
            "M21.9,37c0-2.7,0.9-5.8,2.3-8.2c1.7-3,3.6-4.2,5.1-6.4c2.5-3.7,3-9,1.4-13c-1.6-4.1-5.4-6.5-9.8-6.4s-8,2.8-9.4,6.9c-1.6,4.5-0.9,9.9,2.7,13.3c1.5,1.4,2.9,3.6,2.1,5.7c-0.7,2-3.1,2.9-4.8,3.7c-3.9,1.7-8.6,4.1-9.4,8.7C1.3,45.1,3.9,49,8,49h17c0.8,0,1.3-1,0.8-1.6C23.3,44.5,21.9,40.8,21.9,37z" +
                    "M37.9,25c-6.6,0-12,5.4-12,12s5.4,12,12,12s12-5.4,12-12S44.5,25,37.9,25z M44,38c0,0.6-0.5,1-1.1,1H40v3 c0,0.6-0.5,1-1.1,1h-2c-0.6,0-0.9-0.4-0.9-1v-3h-3.1c-0.6,0-0.9-0.4-0.9-1v-2c0-0.6,0.3-1,0.9-1H36v-3c0-0.6,0.3-1,0.9-1h2 c0.6,0,1.1,0.4,1.1,1v3h2.9c0.6,0,1.1,0.4,1.1,1V38z";

    private static final String LOGIN =
            "M155.81,0v173.889h33.417V33.417h235.592l-74.87,50.656c-8.469,5.727-13.535,15.289-13.535,25.503v286.24 H189.227V282.079H155.81v147.154h180.604v70.93c0,4.382,2.423,8.404,6.29,10.451c3.867,2.056,8.558,1.811,12.189-0.644 l119.318-80.736V0H155.81z" +
                    "M228.657,290.4c0,1.844,1.068,3.524,2.75,4.3c1.664,0.775,3.638,0.514,5.042-0.685l78.044-66.035 l-78.044-66.034c-1.404-1.2-3.378-1.46-5.042-0.686c-1.681,0.775-2.75,2.456-2.75,4.3v33.392H37.79v58.064h190.868V290.4z";

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
        if (rootVBox!=null){
            rootVBox.setBackground(new Background(new BackgroundFill(
                    new LinearGradient(0,0,0,1,true, CycleMethod.NO_CYCLE,
                            new Stop(0.0, Color.web("#E7E7E7")),
                            new Stop(0.5, Color.web("#CFCFCF")),
                            new Stop(1.0, Color.web("#B6B6B6"))),
                    CornerRadii.EMPTY, Insets.EMPTY)));
            rootVBox.setPadding(new Insets(20));
        }
        makeButtonStyle(signUpButton, Color.web("#4A93FF"),170,90,0.32, ADD_USER,true);
        makeButtonStyle(loginButton,Color.web("#2EC27E"),170,90,0.32, LOGIN,true);
        makeButtonStyle(confirmLogin,Color.web("#2EC27E"),60,30,0,"",false);

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
        Account account =  App.getCurrentUser();


        signInText.setText("You have signed \n in today");
        signInButton.setVisible(false);
    }

    private void loginAccount(Account account,ActionEvent event, int index) throws IOException {
        App.lock(account);
        switchScene(event,FXMLLoader.load(App.class.getResource(resourceListFXML[index])));
    }

    private boolean isSignIn(Account account) {
        Account current =  App.getCurrentUser();

        return false;
    }

    @FXML
    public void onAddEmployee(ActionEvent event) {
        TextInputDialog addEmployee = new TextInputDialog();
        addEmployee.setTitle("Add employee");
        addEmployee.setHeaderText("Enter employee's UUID below");
        addEmployee.setContentText("Employee UUID:");
        Optional<String> result =  addEmployee.showAndWait();
    }

    private void makeButtonStyle(Button button, Color base, double prefWidth,double prefHeight, double glyphsize,String svgPath, boolean enableStrip) {
        if (button == null) return;
        applyTileStyle(button, base, svgPath,prefWidth,prefHeight,glyphsize,enableStrip);
    }



    private void applyTileStyle(Button button, Color accent, String svgPath, double prefWidth, double prefHeight, double glyphsize, boolean enableStrip) {
        double radius = 0; // Win 8/8.1 = square tiles
        CornerRadii radii = new CornerRadii(radius);

        Color normal = accent;
        Color hover = accent.deriveColor(0, 0.85, 1.10, 1.0);
        Color pressed = accent.deriveColor(0, 1.0, 0.82, 1.0);

        DropShadow shadow = new DropShadow(14, 0, 6, Color.rgb(0, 0, 0, 0.18));

        SVGPath glyph = new SVGPath();
        glyph.setContent(svgPath);
        glyph.setFill(Color.rgb(255, 255, 255, 0.92));
//        glyph.setScaleX(1.3);
//        glyph.setScaleY(1.3);
        double targetGlyphSize = Math.min(prefWidth, prefHeight) * glyphsize; // tweak: 0.28..0.38
        Bounds b = glyph.getLayoutBounds();
        double max = Math.max(b.getWidth(), b.getHeight());
        if (max > 0.0001) {
            double s = targetGlyphSize / max;
            glyph.setScaleX(s);
            glyph.setScaleY(s);
        }

        Group glyphContainer = new Group(glyph);
        Text label = new Text(button.getText());
        label.setFill(Color.WHITE);

        VBox graphic = new VBox(6, glyphContainer, label);
        graphic.setMouseTransparent(true);
        graphic.setFillWidth(false);
        graphic.setAlignment(Pos.CENTER);

        Region strip = new Region();
        strip.setMouseTransparent(true);
        strip.setPrefHeight(4);
        strip.setMinHeight(4);
        strip.setMaxHeight(4);
        if (enableStrip) {
            strip.setBackground(new Background(new BackgroundFill(
                    Color.rgb(255, 255, 255, 0.18), CornerRadii.EMPTY, Insets.EMPTY
            )));
        }
        StackPane content = new StackPane(graphic);
        content.setMouseTransparent(true);

        VBox wrapper = new VBox(content, strip);
        wrapper.setMouseTransparent(true);
        wrapper.setAlignment(Pos.CENTER);
        VBox.setVgrow(content, Priority.ALWAYS);

        button.setText(null); // we render text ourselves
        button.setGraphic(wrapper);

        button.setBackground(new Background(new BackgroundFill(normal, radii, Insets.EMPTY)));
        button.setBorder(Border.EMPTY);
        button.setEffect(shadow);

        button.setPickOnBounds(false);

        button.setMinHeight(prefHeight);
        button.setPrefHeight(prefHeight);
        button.setMinWidth(prefWidth);
        button.setPrefWidth(prefWidth);

        final ObjectProperty<Color> fill = new SimpleObjectProperty<>(normal);
        fill.addListener((obs, o, c) ->
                button.setBackground(new Background(new BackgroundFill(c, radii, Insets.EMPTY)))
        );

        Consumer<Color> animateTo = target -> {
            Timeline tl = new Timeline(
                    new KeyFrame(Duration.millis(120),
                            new KeyValue(fill, target, Interpolator.EASE_BOTH))
            );
            tl.play();
        };

        button.setOnMouseEntered(e -> {
            if (!button.isPressed()) animateTo.accept(hover);
            button.setScaleX(1.01);
            button.setScaleY(1.01);
        });

        button.setOnMouseExited(e -> {
            if (!button.isPressed()) animateTo.accept(normal);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        button.setOnMousePressed(e -> {
            animateTo.accept(pressed);
            button.setScaleX(0.98);
            button.setScaleY(0.98);
        });

        button.setOnMouseReleased(e -> {
            button.setScaleX(button.isHover() ? 1.01 : 1.0);
            button.setScaleY(button.isHover() ? 1.01 : 1.0);
            animateTo.accept(button.isHover() ? hover : normal);
        });
    }

    public void onLogout(ActionEvent event) throws IOException {
        switchScene(event,FXMLLoader.load(App.class.getResource(resourceListFXML[Files.TITLESCREEN.INDEX])));
        App.unlock();
    }
}
