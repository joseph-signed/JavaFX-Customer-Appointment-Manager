package Controllers;

import Models.Appointment;

import Program.JDBC;
import Program.Errors;
import Program.Alerts;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddAppointmentController implements Initializable {
    @FXML TextField TextBox_Title, TextBox_Type, TextBox_Location, TextBox_Start, TextBox_End;
    @FXML TextArea TextArea_Description;
    @FXML ComboBox<String> ComboBoxString_Contact;
    @FXML ComboBox<Integer> ComboBoxInt_Customer, ComboBoxInt_User;
    @FXML Label Label_TimeZone;
    @FXML DatePicker DatePicker_Appointment;
    @FXML Button Button_Save, Button_Back;

    String appointmentTitle;
    String appointmentDescription;
    String appointmentLocation;
    String appointmentContact;
    String appointmentType;
    String appointmentStartTime;
    String appointmentEndTime;
    Integer appointmentCustomer;
    Integer appointmentUser;
    LocalDate appointmentDate;

    LocalDateTime formattedAppointmentEnd_DateTime;
    LocalDateTime formattedAppointmentStart_DateTime;

    ZonedDateTime UTC_ZonedAppointmentEnd_DateTime;
    ZonedDateTime UTC_ZonedAppointmentStart_DateTime;

    ZonedDateTime UserZonedAppointmentStart_DateTime;
    ZonedDateTime UserZonedAppointmentEnd_DateTime;

    ZonedDateTime EST_OpenBusiness_DateTime;
    ZonedDateTime EST_CloseBusiness_DateTime;

    /**
     * Reads user input values
     */
    public void UpdateUserInputValues() {
        appointmentTitle = TextBox_Title.getText();
        appointmentDescription = TextArea_Description.getText();
        appointmentLocation = TextBox_Location.getText();
        appointmentContact = ComboBoxString_Contact.getValue();
        appointmentType = TextBox_Type.getText();
        appointmentCustomer = ComboBoxInt_Customer.getValue();
        appointmentUser = ComboBoxInt_User.getValue();
        appointmentDate = DatePicker_Appointment.getValue();
        appointmentStartTime = TextBox_Start.getText();
        appointmentEndTime = TextBox_End.getText();
    }

    /**
     *Converts, format and loads Date/Time objects
     *
     */
    public boolean FormatAndLoadDateTimes() {
        boolean result = true;
        try {
            formattedAppointmentStart_DateTime = LocalDateTime.of(appointmentDate, LocalTime.parse(appointmentStartTime, JDBC.TimeFormat));
        } catch (DateTimeParseException ignored) {
            Alerts.ShowError(Errors.wrongTimeFormatErrorMessage);
            result = false;
        }
        if (result) {
            try {
                formattedAppointmentEnd_DateTime = LocalDateTime.of(appointmentDate, LocalTime.parse(appointmentEndTime, JDBC.TimeFormat));
            } catch (DateTimeParseException ignored) {
                Alerts.ShowError(Errors.wrongTimeFormatErrorMessage);
                result = false;
            }
            if (result) {
                UserZonedAppointmentStart_DateTime = ZonedDateTime.of(formattedAppointmentStart_DateTime, JDBC.GetUserTimeZone());
                UserZonedAppointmentEnd_DateTime = ZonedDateTime.of(formattedAppointmentEnd_DateTime, JDBC.GetUserTimeZone());

                UTC_ZonedAppointmentStart_DateTime = ZonedDateTime.of(formattedAppointmentStart_DateTime, JDBC.GetUserTimeZone()).withZoneSameInstant(ZoneOffset.UTC);
                UTC_ZonedAppointmentEnd_DateTime = ZonedDateTime.of(formattedAppointmentEnd_DateTime, JDBC.GetUserTimeZone()).withZoneSameInstant(ZoneOffset.UTC);

                EST_OpenBusiness_DateTime = ZonedDateTime.of(appointmentDate, LocalTime.of(8, 0), ZoneId.of("America/New_York"));
                EST_CloseBusiness_DateTime = ZonedDateTime.of(appointmentDate, LocalTime.of(22, 0), ZoneId.of("America/New_York"));
            }


        }

        return result;
    }

    /**
     * Checks for empty user inputs on the form
     *
     */
    public boolean CheckEmptyUserInputs() {
        if (appointmentTitle.isBlank() || appointmentDescription.isBlank()
                || appointmentLocation.isBlank() || appointmentContact == null
                || appointmentType.isBlank() || appointmentCustomer == null
                || appointmentUser == null || appointmentDate == null
                || appointmentStartTime.isBlank() || appointmentEndTime.isBlank()
                || DatePicker_Appointment == null) {

            Alerts.ShowError(Errors.emptyInputErrorMessage);
            return false;
        }

        return true;
    }

    /**
     * Validation for Appointment Hours
     *
     */
    public boolean IsValidAppointmentHours() {
        if (UserZonedAppointmentStart_DateTime.isBefore(EST_OpenBusiness_DateTime) || UserZonedAppointmentStart_DateTime.isAfter(EST_CloseBusiness_DateTime) || UserZonedAppointmentEnd_DateTime.isAfter(EST_CloseBusiness_DateTime)
                || UserZonedAppointmentEnd_DateTime.isBefore(EST_OpenBusiness_DateTime) || UserZonedAppointmentStart_DateTime.isBefore(ZonedDateTime.now())
        ) {
            Alerts.ShowError(Errors.timeOutOfBoundsErrorMessage);
            return false;
        }

        var sameDayCustomerAppointments = JDBC.GetAppointmentByCustomerId(appointmentDate, appointmentCustomer);
        if (!sameDayCustomerAppointments.isEmpty()) {
            for (Appointment appointment : sameDayCustomerAppointments) {
                var startTime = appointment.getAppointmentStartTime().toLocalDateTime();
                var endTime = appointment.getAppointmentEndTime().toLocalDateTime();

                if (
                       startTime.isEqual(formattedAppointmentStart_DateTime)
                    || endTime.isEqual(formattedAppointmentEnd_DateTime)
                    || startTime.isBefore(formattedAppointmentStart_DateTime) && endTime.isAfter(formattedAppointmentEnd_DateTime)
                    || startTime.isAfter(formattedAppointmentStart_DateTime) && startTime.isBefore(formattedAppointmentEnd_DateTime)
                    || endTime.isAfter(formattedAppointmentStart_DateTime) && endTime.isBefore(formattedAppointmentEnd_DateTime)) {

                    Alerts.ShowError(Errors.timeAlreadyScheduledErrorMessage);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Add an appointment using the JDBC functions
     */
    public void AddAppointment() {
        var appointmentContactId = Program.JDBC.GetContactIdByContactName(appointmentContact);
        Program.JDBC.AddAppointment(appointmentTitle, appointmentDescription, appointmentLocation, appointmentType, UTC_ZonedAppointmentStart_DateTime, UTC_ZonedAppointmentEnd_DateTime, JDBC.currentUser, JDBC.currentUser,
                appointmentCustomer, appointmentUser, appointmentContactId);
    }

    /**
     * Actions on Save
     * @param event
     */
    public void PressSave(ActionEvent event) {
        UpdateUserInputValues();
        if (CheckEmptyUserInputs()) {
            if (FormatAndLoadDateTimes()) {
                if (IsValidAppointmentHours()) {
                    AddAppointment();
                    Alerts.ShowOk("Appointment added.");

                    try {
                        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/AppointmentsView.fxml")));
                        Scene scene = new Scene(parent);
                        Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
                        window.setScene(scene);
                        window.show();
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    /**
     * Actions on Back
     * @param event
     */
    public void PressBack(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/AppointmentsView.fxml")));
            Scene scene = new Scene(parent);
            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException ignored) {}
    }

    public void initialize(URL location, ResourceBundle resources) {
        Label_TimeZone.setText("Time Zone: " + Program.JDBC.GetUserTimeZone());
        ComboBoxInt_Customer.setItems(Program.JDBC.GetAllCustomerIds());
        ComboBoxInt_User.setItems(Program.JDBC.GetAllUserId());
        ComboBoxString_Contact.setItems(Program.JDBC.GetAllContactNames());
    }
}
