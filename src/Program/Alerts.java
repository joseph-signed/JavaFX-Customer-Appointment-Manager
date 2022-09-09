package Program;

import Models.Appointment;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Alerts {
    public static List<String> Cancelled = Arrays.asList("cancelled", "Cancelled", "CANCELLED", "cancel", "Cancel", "CANCEL");

    /**
     * Displays Error Alert.
     * @param errorMessage
     */
    public static void ShowError(String errorMessage) {
        var Button_Ok = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        var Alert_Error = new Alert(Alert.AlertType.WARNING, errorMessage, Button_Ok);
        Alert_Error.showAndWait();
    }

    /**
     * Display a Confirmation Alert.
     * @param message
     */
    public static void ShowOk(String message) {
        ButtonType clickOkay = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, clickOkay);
        alert.showAndWait();
    }

    /**
     * Displays an Optional Alert.
     * @param message
     * @return
     */
    public static Optional<ButtonType> ShowOptional(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Displays Alert notifying user of upcoming appointments.
     * @param appointment
     */
    public static void UpcomingAppointments(Appointment appointment) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Appointment in the next 15 Minutes!");
        alert.setContentText("Appointment ID: " + appointment.getAppointmentId() + "\n" + "Appointment Date:" + appointment.getAppointmentDateString() + "\n" + "Start Time: " + appointment.getAppointmentStartString()
                + "\n" + "End Time: " + appointment.getAppointmentEndString());
        alert.showAndWait();
    }
}
