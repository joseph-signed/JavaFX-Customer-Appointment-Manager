package Controllers;

import Models.Appointment;

import Program.*;
import Program.AppointmentFilters;

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AppointmentsController implements Initializable {
    @FXML ToggleGroup FilterOptionGroup;
    @FXML RadioButton RadioButton_Week, RadioButton_All, RadioButton_Month;
    @FXML Button Button_EditAppointment, Button_DeleteAppointment, Button_AddAppointment, Button_Back;
    @FXML TableView<Appointment> Table_Appointment;
    @FXML TableColumn<Appointment, String> Column_AppointmentDescription, Column_AppointmentTitle, Column_AppointmentLocation, Column_AppointmentContact, Column_AppointmentType;
    @FXML TableColumn<Appointment, ZonedDateTime> Column_AppointmentStartTime, Column_AppointmentEndTime;
    @FXML TableColumn<Appointment, Integer> Column_AppointmentId, Column_AppointmentCustomerId, Column_AppointmentUserId;

    /**
     * Initialize table cell properties.
     */
    public void SetTableCellValueFactories() {
        Column_AppointmentId.setCellValueFactory(new PropertyValueFactory<>("AppointmentId"));
        Column_AppointmentTitle.setCellValueFactory(new PropertyValueFactory<>("AppointmentTitle"));
        Column_AppointmentDescription.setCellValueFactory(new PropertyValueFactory<>("AppointmentDescription"));
        Column_AppointmentLocation.setCellValueFactory(new PropertyValueFactory<>("AppointmentLocation"));
        Column_AppointmentType.setCellValueFactory(new PropertyValueFactory<>("AppointmentType"));
        Column_AppointmentContact.setCellValueFactory(new PropertyValueFactory<>("AppointmentContactName"));
        Column_AppointmentStartTime.setCellValueFactory(new PropertyValueFactory<>("AppointmentStartTime"));
        Column_AppointmentEndTime.setCellValueFactory(new PropertyValueFactory<>("AppointmentEndTime"));
        Column_AppointmentCustomerId.setCellValueFactory(new PropertyValueFactory<>("AppointmentCustomerId"));
        Column_AppointmentUserId.setCellValueFactory(new PropertyValueFactory<>("AppointmentUserId"));
    }

    /**
     * Populate the Appointment table.
     * @param appointments
     */
    public void SetAppointmentsTable(ObservableList<Appointment> appointments) {
        Table_Appointment.setItems(appointments);
    }

    /**
     * Checks to see if an appointment is cancelled.
     * @param appointments
     */
    public void CancelledAppointments(ObservableList<Appointment> appointments) {
        for (var appointment : appointments) {
            if (Alerts.Cancelled.contains(appointment.getAppointmentType())) {
                Alerts.ShowOk("Appointment " + appointment.getAppointmentId() + " is canceled.");
            }
        }
    }

    /**
     * Filters the Appointment table to show all appointments
     */
    public void ApplyAllFilter() {
        AppointmentFilters.AppointmentList = JDBC.GetAllAppointments();
        SetAppointmentsTable(AppointmentFilters.AppointmentList);
    }

    /**
     * Filters the Appointment table to show appointments this week.
     */
    public void ApplyWeekFilter() {
        AppointmentFilters.FilterStartDateTime = AppointmentFilters.getZonedDateTime_TimeNow();
        AppointmentFilters.FilterEndDateTime = AppointmentFilters.getZonedDateTime_TimeInOneWeek();

        AppointmentFilters.AppointmentList = JDBC.GetAppointsInDateTimeRange(AppointmentFilters.FilterStartDateTime, AppointmentFilters.FilterEndDateTime);
        SetAppointmentsTable(AppointmentFilters.AppointmentList);
    }

    /**
     * Filters the Appointment table to show appointments this month.
     */
    public void ApplyMonthFilter() {
        AppointmentFilters.FilterStartDateTime = AppointmentFilters.getZonedDateTime_TimeNow();
        AppointmentFilters.FilterEndDateTime = AppointmentFilters.getZonedDateTime_TimeInOneMonth();

        AppointmentFilters.AppointmentList = JDBC.GetAppointsInDateTimeRange(AppointmentFilters.FilterStartDateTime, AppointmentFilters.FilterEndDateTime);
        SetAppointmentsTable(AppointmentFilters.AppointmentList);
    }

    /**
     * Handles the Add Appointment press
     * @param event
     */
    public void PressAddAppointment(ActionEvent event){
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/AddAppointmentView.fxml")));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ignored) {}
    }

    /**
     * Handles the Edit Appointment press
     * @param event
     */
    public void PressEditAppointment(ActionEvent event) {
        SelectedAppointment.SetSelectedAppointment(Table_Appointment.getSelectionModel().getSelectedItem());
        if (SelectedAppointment.GetSelectedAppointment() == null){
            Alerts.ShowError(Errors.nullTableSelectionErrorMessage);
            return;
        }

        try{
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/EditAppointmentView.fxml")));
            Scene scene = new Scene(parent);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ignored) {}
    }

    /**
     * Handles the Delete Appointment press
     *
     */
    public void PressDeleteAppointment() {
        Appointment selectedAppointment = Table_Appointment.getSelectionModel().getSelectedItem();
        if (selectedAppointment == null) {
            Alerts.ShowError(Errors.nullTableSelectionErrorMessage);
        } else {
            var buttonOption = Alerts.ShowOptional("Remove selected appointment?");
            if (buttonOption.isPresent()) {
                if (buttonOption.get() == ButtonType.OK) {
                    JDBC.RemoveAppointmentById(selectedAppointment.getAppointmentId());
                    ApplyAllFilter();
                    Alerts.ShowOk("Removed selected appointment.");
                }
            }
        }

    }

    /**
     * Handles action on Back
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SetTableCellValueFactories();
        AppointmentFilters.AppointmentList = JDBC.GetAllAppointments();
        SetAppointmentsTable(AppointmentFilters.AppointmentList);
        CancelledAppointments(AppointmentFilters.AppointmentList);
    }
}

