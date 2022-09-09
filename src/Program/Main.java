package Program;

import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application{
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Customer/Appointment Manager");
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/LoginView.fxml")));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Create connection to the Database and start the application.
     * @param args
     */
    public static void main(String[] args) {
        Program.JDBC.MakeConnection();
        launch(args);
    }
}
