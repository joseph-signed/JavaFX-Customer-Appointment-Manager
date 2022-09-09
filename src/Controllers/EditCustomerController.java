package Controllers;

import Models.Customer;

import Program.Alerts;
import Program.Errors;
import Program.SelectedCustomer;

import java.io.IOException;
import java.lang.*;
import java.lang.Integer;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditCustomerController implements Initializable {
    @FXML ComboBox<String> ComboBox_CustomerCountry, ComboBox_CustomerDivision;
    @FXML TextField TextBox_CustomerId, TextBox_CustomerName, TextBox_CustomerAddress, TextBox_CustomerPostalCode, TextBox_CustomerPhoneNumber;
    @FXML Button Button_Save, Button_Back;

    String customerCountry;
    String customerDivision;
    String customerId;
    String customerName;
    String customerAddress;
    String customerPostalCode;
    String customerPhoneNumber;

    /**
     * Loads the selected Customer into the form.
     * @param selectedCustomer
     */
    public void InitializeCustomer(Customer selectedCustomer) {
        ComboBox_CustomerCountry.setItems(Program.JDBC.GetAllCountries());
        ComboBox_CustomerCountry.getSelectionModel().select(selectedCustomer.getCustomerCountry());
        ComboBox_CustomerDivision.setItems(Program.JDBC.GetDivisionsByCountry(selectedCustomer.getCustomerCountry()));
        ComboBox_CustomerDivision.setValue(selectedCustomer.getCustomerDivision());
        TextBox_CustomerId.setText(selectedCustomer.getCustomerId().toString());
        TextBox_CustomerName.setText(selectedCustomer.getCustomerName());
        TextBox_CustomerAddress.setText(selectedCustomer.getCustomerAddress());
        TextBox_CustomerPostalCode.setText(selectedCustomer.getCustomerPostalCode());
        TextBox_CustomerPhoneNumber.setText(selectedCustomer.getCustomerPhoneNumber());
    }

    /**
     * Reads the current user input from the form fields.
     */
    public void UpdateUserInputValues() {
        customerCountry = ComboBox_CustomerCountry.getValue();
        customerDivision = ComboBox_CustomerDivision.getValue();
        customerName = TextBox_CustomerName.getText();
        customerId = TextBox_CustomerId.getText();
        customerAddress = TextBox_CustomerAddress.getText();
        customerPostalCode = TextBox_CustomerPostalCode.getText();
        customerPhoneNumber = TextBox_CustomerPhoneNumber.getText();
    }

    /**
     * Validates the form for no empty user input fields.
     * @return
     */
    public boolean CheckEmptyUserInputs() {
        if (customerCountry == null || customerDivision == null || customerId.isBlank() || customerAddress.isBlank() || customerPostalCode.isBlank() || customerPhoneNumber.isBlank()) {
            Alerts.ShowError(Errors.emptyInputErrorMessage);
            return false;
        }

        return true;
    }

    /**
     * Updates a Customer in the Database.
     */
    public void UpdateCustomer() {
        String divisionId = String.valueOf(Program.JDBC.GetDivisionIdByName(customerDivision));
        Program.JDBC.UpdateCustomerById(Integer.parseInt(divisionId), customerName, customerAddress, customerPostalCode, customerPhoneNumber, Integer.parseInt(customerId));
    }

    /**
     * Handles action on Save.
     * @param event
     */
    public void PressSave(ActionEvent event)  {
        UpdateUserInputValues();
        if (CheckEmptyUserInputs()) {
            UpdateCustomer();
            Alerts.ShowOk("Customer updated.");

            try {
                Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Views/MainView.fxml")));
                Scene scene = new Scene(parent);
                Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Handles action on Back.
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
    public void initialize(URL url, ResourceBundle resourceBundle) {
        InitializeCustomer(SelectedCustomer.GetSelectedCustomer());
        Program.Lambdas.SetDivisionComboBox_OnValueChanged(ComboBox_CustomerCountry, ComboBox_CustomerDivision);
    }
}
