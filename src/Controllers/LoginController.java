package Controllers;

import static Program.JDBC.AttemptLogin;
import Program.JDBC;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Initializable {
    @FXML Label Label_Appointment, Label_Username, Label_Password, Label_ErrorMessage, Label_UserTimeZone;
    @FXML TextField TextField_Username, TextField_Password;
    @FXML Button Button_LogIn;

    String Username;
    String Password;

    /**
     * Initialize the log in form data.
     */
    private void InitializeData() {
        Label_UserTimeZone.setText(Program.JDBC.GetUserTimeZone().toString());
        Label_Appointment.setText(JDBC.Language.getString("Label_Appointment"));
        Label_Username.setText(JDBC.Language.getString("Label_Username"));
        Label_Password.setText(JDBC.Language.getString("Label_Password"));
        Button_LogIn.setText(JDBC.Language.getString("Button_LogIn"));
    }

    /**
     * Consumes the Username and Passford and checks if the credentials are valid.
     * @param user
     * @param password
     */
    public void ValidateLogInDetails(String user, String password) {
        if (user.equals("") || password.equals("")) {
            Label_ErrorMessage.setText(JDBC.Language.getString("NullCredentials"));
            return;
        }

        Username = user;
        Password = password;
    }

    /**
     * Handles the action for pressing Log In button.
     * @param event
     */
    public void PressLogIn(ActionEvent event) {
        ValidateLogInDetails(TextField_Username.getText(), TextField_Password.getText());
        boolean successfullyLoggedIn;
        if (AttemptLogin(Username, Password)) {
            successfullyLoggedIn = true;
            try {
                Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/MainView.fxml")));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();

            } catch (IOException ignored) {}
        } else {
            successfullyLoggedIn = false;
            Label_ErrorMessage.setText(JDBC.Language.getString("InvalidCredentials"));
        }

        Program.Lambdas.Logger(successfullyLoggedIn);
        TextField_Password.clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        InitializeData();
    }

}