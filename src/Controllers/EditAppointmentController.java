package Controllers;

import Models.Appointment;

import Program.Alerts;
import Program.Errors;
import Program.JDBC;
import Program.SelectedAppointment;

import java.io.IOException;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditAppointmentController implements Initializable {
    @FXML TextField TextBox_AppointmentTitle, TextBox_AppointmentLocation, TextBox_AppointmentType, TextBox_AppointmentStartTime, TextBox_AppointmentEndTime;
    @FXML TextArea TextArea_AppointmentDescription;
    @FXML ComboBox<String> ComboBox_AppointmentContact;
    @FXML ComboBox<Integer> ComboBox_AppointmentUserId, ComboBox_AppointmentCustomerId;
    @FXML DatePicker DatePicker_AppointmentDate;
    @FXML Button Button_Save, Button_Back;
    @FXML Label Label_UserTimeZone;

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

    ZoneId UserTimeZone = Program.JDBC.GetUserTimeZone();

    LocalDateTime formattedAppointmentEnd_DateTime;
    LocalDateTime formattedAppointmentStart_DateTime;

    ZonedDateTime UTC_ZonedAppointmentEnd_DateTime;
    ZonedDateTime UTC_ZonedAppointmentStart_DateTime;

    ZonedDateTime UserZonedAppointmentStart_DateTime;
    ZonedDateTime UserZonedAppointmentEnd_DateTime;

    ZonedDateTime EST_OpenBusiness_DateTime;
    ZonedDateTime EST_CloseBusiness_DateTime;

    /**
     * Loads the selected appointment's data into the form.
     * @param appointment
     */
    public void InitializeAppointment(Appointment appointment) {
        Label_UserTimeZone.setText(UserTimeZone.toString());

        UTC_ZonedAppointmentStart_DateTime = appointment.getAppointmentStartTime().toInstant().atZone(ZoneOffset.UTC);
        UTC_ZonedAppointmentEnd_DateTime = appointment.getAppointmentEndTime().toInstant().atZone(ZoneOffset.UTC);

        UserZonedAppointmentStart_DateTime = UTC_ZonedAppointmentStart_DateTime.withZoneSameInstant(UserTimeZone);
        UserZonedAppointmentEnd_DateTime = UTC_ZonedAppointmentEnd_DateTime.withZoneSameInstant(UserTimeZone);

        TextBox_AppointmentTitle.setText(appointment.getAppointmentTitle());
        TextArea_AppointmentDescription.setText(appointment.getAppointmentDescription());
        TextBox_AppointmentLocation.setText(appointment.getAppointmentLocation());
        ComboBox_AppointmentContact.setItems(Program.JDBC.GetAllContactNames());
        ComboBox_AppointmentContact.setValue(appointment.getAppointmentContactName());
        TextBox_AppointmentType.setText(appointment.getAppointmentType());
        ComboBox_AppointmentCustomerId.setItems(Program.JDBC.GetAllCustomerIds());
        ComboBox_AppointmentCustomerId.getSelectionModel().select(appointment.getAppointmentCustomerId());
        ComboBox_AppointmentUserId.setItems(Program.JDBC.GetAllUserId());
        ComboBox_AppointmentUserId.getSelectionModel().select(appointment.getAppointmentUserId());
        DatePicker_AppointmentDate.setValue(appointment.getAppointmentStartTime().toLocalDateTime().toLocalDate());
        TextBox_AppointmentStartTime.setText(UserZonedAppointmentStart_DateTime.format(JDBC.TimeFormat));
        TextBox_AppointmentEndTime.setText(UserZonedAppointmentEnd_DateTime.format(JDBC.TimeFormat));
    }

    /**
     * Reads the latest user input from the form and assigns them to variables.
     */
    public void UpdateUserInputValues() {
        appointmentTitle = TextBox_AppointmentTitle.getText();
        appointmentDescription = TextArea_AppointmentDescription.getText();
        appointmentLocation = TextBox_AppointmentLocation.getText();
        appointmentContact = ComboBox_AppointmentContact.getValue();
        appointmentType = TextBox_AppointmentType.getText();
        appointmentCustomer = ComboBox_AppointmentCustomerId.getValue();
        appointmentUser = ComboBox_AppointmentUserId.getValue();
        appointmentDate = DatePicker_AppointmentDate.getValue();
        appointmentStartTime = TextBox_AppointmentStartTime.getText();
        appointmentEndTime = TextBox_AppointmentEndTime.getText();
    }

    /**
     * Validates the form has been completely filled.
     * @return
     */
    public boolean CheckEmptyUserInputs() {
        if (appointmentTitle.isBlank() || appointmentDescription.isBlank()
                || appointmentLocation.isBlank() || appointmentContact == null
                || appointmentType.isBlank() || appointmentCustomer == null
                || appointmentUser == null || appointmentDate == null
                || appointmentStartTime.isBlank() || appointmentEndTime.isBlank()
                || DatePicker_AppointmentDate == null) {

            Alerts.ShowError(Errors.emptyInputErrorMessage);
            return false;
        }

        return true;
    }

    /**
     * Formats the dates and load them into variables.
     * @return
     */
    public boolean FormatAndLoadDateTimes() {
        try {
            formattedAppointmentStart_DateTime = LocalDateTime.of(appointmentDate, LocalTime.parse(appointmentStartTime, JDBC.TimeFormat));
        } catch (DateTimeParseException ignored) {
            Alerts.ShowError(Errors.wrongTimeFormatErrorMessage);
            return false;
        }

        try {
            formattedAppointmentEnd_DateTime = LocalDateTime.of(appointmentDate, LocalTime.parse(appointmentEndTime, JDBC.TimeFormat));
        } catch (DateTimeParseException ignored) {
            Alerts.ShowError(Errors.wrongTimeFormatErrorMessage);
            return false;
        }

        UserZonedAppointmentStart_DateTime = ZonedDateTime.of(formattedAppointmentStart_DateTime, Program.JDBC.GetUserTimeZone());
        UserZonedAppointmentEnd_DateTime = ZonedDateTime.of(formattedAppointmentEnd_DateTime, Program.JDBC.GetUserTimeZone());

        UTC_ZonedAppointmentStart_DateTime = ZonedDateTime.of(formattedAppointmentStart_DateTime, Program.JDBC.GetUserTimeZone()).withZoneSameInstant(ZoneOffset.UTC);
        UTC_ZonedAppointmentEnd_DateTime = ZonedDateTime.of(formattedAppointmentEnd_DateTime, Program.JDBC.GetUserTimeZone()).withZoneSameInstant(ZoneOffset.UTC);

        EST_OpenBusiness_DateTime = ZonedDateTime.of(appointmentDate, LocalTime.of(8,0), ZoneId.of("America/New_York"));
        EST_CloseBusiness_DateTime = ZonedDateTime.of(appointmentDate, LocalTime.of(22, 0), ZoneId.of("America/New_York"));

        return true;
    }

    /**
     * Check to see if the appointment created is within appropriate work hours.
     * @return
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
                if (appointment.getAppointmentId().equals(SelectedAppointment.GetSelectedAppointment().getAppointmentId())) {
                    continue;
                }
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
     * Update an appointment stored in the Database.
     */
    public void UpdateAppointment() {
        var appointmentContactId = Program.JDBC.GetContactIdByContactName(appointmentContact);
        var appointmentId = SelectedAppointment.GetSelectedAppointment().getAppointmentId();

        Program.JDBC.UpdateAppointmentById(appointmentId, appointmentTitle, appointmentDescription, appointmentLocation, appointmentType, UTC_ZonedAppointmentStart_DateTime, UTC_ZonedAppointmentEnd_DateTime, JDBC.currentUser,
                appointmentCustomer, appointmentUser, appointmentContactId );
    }

    /**
     * Handles the action on Back.
     * @param event
     */
    public void PressBack(ActionEvent event)  {
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/AppointmentsView.fxml")));
            Scene scene = new Scene(parent);
            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException ignored) {}
    }

    /**
     * Handles the action on Save.
     * @param event
     */
    public void PressSave(ActionEvent event)  {
        UpdateUserInputValues();
        if (CheckEmptyUserInputs()) {
            if (FormatAndLoadDateTimes()) {
                if (IsValidAppointmentHours()) {
                    UpdateAppointment();
                    Alerts.ShowOk("Appointment updated.");

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InitializeAppointment(SelectedAppointment.GetSelectedAppointment());
    }
}
