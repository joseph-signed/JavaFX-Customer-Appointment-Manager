package Controllers;

import Models.Customer;

import Program.Alerts;
import Program.Errors;
import Program.SelectedCustomer;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.*;
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

public class MainController implements Initializable {
    @FXML Button Button_Appointments, Button_Reports, Button_AddCustomer, Button_EditCustomer, Button_DeleteCustomer;
    @FXML TableView<Customer> Table_Customer;
    @FXML TableColumn<Customer,String> Column_CustomerName, Column_CustomerAddress, Column_CustomerPostalCode, Column_CustomerPhoneNumber, Column_CustomerDivisionName, Column_CustomerCountryName;

    /**
     * Handles the action for Appointments button.
     * @param event
     */
    public void PressAppointments(ActionEvent event){
        try {
            Parent Parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/AppointmentsView.fxml")));
            Scene Scene = new Scene(Parent);
            Stage Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage.setScene(Scene);
            Stage.show();
        }
        catch (IOException ignored) {}
    }

    /**
     * Handles the action for Reports button.
     * @param event
     */
    public void PressReports(ActionEvent event){
        try {
            Parent Parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/ReportsView.fxml")));
            Scene Scene = new Scene(Parent);
            Stage Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage.setScene(Scene);
            Stage.show();
        }
        catch (IOException ignored) {}
    }

    /**
     * Handles the action for Add Customer button.
     * @param event
     */
    public void PressAddCustomer(ActionEvent event){
        try {
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/AddCustomerView.fxml")));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException ignored) {}
    }

    public void PressEditCustomer(ActionEvent event) {
        SelectedCustomer.SetSelectedCustomer(Table_Customer.getSelectionModel().getSelectedItem());
        if (SelectedCustomer.GetSelectedCustomer() == null) {
            Alerts.ShowError(Errors.nullTableSelectionErrorMessage);
            return;
        }

        try{
            Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/EditCustomerView.fxml")));
            Scene scene = new Scene(parent);
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ignored) {}
    }

    /**
     * Handles the action for Delete Customer button.
     *
     */
    public void PressDeleteCustomer() {
        SelectedCustomer.SetSelectedCustomer(Table_Customer.getSelectionModel().getSelectedItem());
        if (SelectedCustomer.GetSelectedCustomer() == null) {
            Alerts.ShowError(Errors.nullTableSelectionErrorMessage);
            return;
        }

        var buttonOption = Alerts.ShowOptional("Remove " + SelectedCustomer.GetSelectedCustomer().getCustomerName() + "?");
        if (buttonOption.isPresent()) {
            if (buttonOption.get() == ButtonType.OK) {
                Program.JDBC.RemoveCustomerById(SelectedCustomer.GetSelectedCustomer().getCustomerId());
                SetCustomersTable();
                Alerts.ShowOk("Removed " + SelectedCustomer.GetSelectedCustomer().getCustomerName());
            }
        }

    }

    /**
     * Initialize table cell properties.
     */
    public void SetTableCellValueFactories() {
        Column_CustomerName.setCellValueFactory(new PropertyValueFactory<>("CustomerName"));
        Column_CustomerAddress.setCellValueFactory(new PropertyValueFactory<>("CustomerAddress"));
        Column_CustomerPostalCode.setCellValueFactory(new PropertyValueFactory<>("CustomerPostalCode"));
        Column_CustomerPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("CustomerPhoneNumber"));
        Column_CustomerCountryName.setCellValueFactory(new PropertyValueFactory<>("CustomerCountry"));
        Column_CustomerDivisionName.setCellValueFactory(new PropertyValueFactory<>("CustomerDivision"));
    }

    /**
     * Populate the Customer Table.
     */
    public void SetCustomersTable() {
        Program.JDBC.UpdateCustomerList();
        Table_Customer.setItems(Program.JDBC.GetAllCustomers());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SetTableCellValueFactories();
        SetCustomersTable();
        Program.JDBC.Notification();
    }
}