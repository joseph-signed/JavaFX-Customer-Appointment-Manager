package Controllers;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ReportsController implements Initializable {
    @FXML Button Button_CustomerAppointments, Button_ContactSchedules, Button_CustomReport, Button_Back;
    @FXML TextArea TextArea_Report;

    /**
     * Handles the Back action
     * @param event
     */
    public void PressBack(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/MainView.fxml")));
            Scene scene = new Scene(parent);
            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException ignored) {}
    }

    /**
     * Handles the action for Generation of the Customer Appointment Report
     * REPORT
     */
    public void PressCustomerAppointments() {
        TextArea_Report.clear();
        for (var line : Program.Report.GenerateCustomerAppointmentReport()) {
            TextArea_Report.appendText(line);
        }
    }

    /**
     * Handles the action for Generation of the Customer Country Report
     * REPORT
     */
    public void PressCustomerCountryReport() {
        TextArea_Report.clear();
        for (var line : Program.Report.GenerateCustomerCountryReport()) {
            TextArea_Report.appendText(line);
        }
    }

    /**
     * Handles the action for Generation of the Contact Schedules Report
     * REPORT
     */
    public void PressContactSchedules() {
        TextArea_Report.clear();
        for (var line : Program.Report.GenerateContactScheduleReport()) {
            TextArea_Report.appendText(line);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}
}
