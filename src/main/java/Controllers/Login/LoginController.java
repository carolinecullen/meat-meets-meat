package Controllers.Login;

import Controllers.Util.QueryConstructor;
import Controllers.Util.SwitchScene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField loginField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label loginStatus;


    @FXML
    public void initialize(){
        loginButton.setOnAction( event -> {
            System.out.println(loginField.getText());
            try {
                if (verifyUser(loginField.getText(), passwordField.getText())) {
                    switchToLandingScene();
                } else {
                    loginStatus.setText("Invalid Login!");
                }
            } catch (SQLException | ClassNotFoundException e){
                e.printStackTrace();
            }
        });
        registerButton.setOnAction( event -> {
            ((Node)(event.getSource())).getScene().getWindow().hide();
            switchToRegisterScene();
        });
    }

    private void switchToRegisterScene() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("FXML/Register.fxml"));
        SwitchScene.switchScene(loader, "Register");
    }

    private void switchToLandingScene() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("FXML/infoDisplay.fxml"));
        SwitchScene.switchScene(loader, "Welcome to Meats Meets Meat!");
    }

    private boolean verifyUser(String username, String passsword) throws ClassNotFoundException, SQLException {
        return QueryConstructor.selectFromUsers(username, passsword);
    }





}
