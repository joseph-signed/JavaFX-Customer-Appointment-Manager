package Controllers;

import Program.Errors;
import Program.Alerts;

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
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddCustomerController implements Initializable {
    @FXML ComboBox<String> ComboBox_CustomerCountry, ComboBox_CustomerDivision;
    @FXML TextField TextBox_Customer, TextBox_CustomerId, TextBox_CustomerAddress, TextBox_CustomerPostalCode, TextBox_CustomerPhoneNumber;
    @FXML Button Button_Save, Button_Back;

    String customerCountry;
    String customerDivision;
    String customer;
    String customerAddress;
    String customerPostalCode;
    String customerPhoneNumber;

    /**
     * Reads user input values
     */
    public void UpdateUserInputValues() {
        customerCountry = ComboBox_CustomerCountry.getValue();
        customerDivision = ComboBox_CustomerDivision.getValue();
        customer = TextBox_Customer.getText();
        customerAddress = TextBox_CustomerAddress.getText();
        customerPostalCode = TextBox_CustomerPostalCode.getText();
        customerPhoneNumber = TextBox_CustomerPhoneNumber.getText();
    }

    /**
     * Checks for empty user inputs on the form
     *
     */
    public boolean CheckEmptyUserInputs() {
        if ((customerCountry == null) || customerDivision == null || customer.isBlank() || customerAddress.isBlank() || customerPostalCode.isBlank() || customerPhoneNumber.isBlank()) {
            Alerts.ShowError(Errors.emptyInputErrorMessage);
            return false;
        }

        return true;
    }

    /**
     * Add a customer using the JDBC functions
     */
    public void AddCustomer() {
        Program.JDBC.AddCustomer(customer, customerAddress, customerPostalCode, customerPhoneNumber, Program.JDBC.GetDivisionIdByName(customerDivision));
    }

    /**
     * Actions on Save
     * @param event
     */
    public void PressSave(ActionEvent event)  {
        UpdateUserInputValues();
        if (CheckEmptyUserInputs()) {
            AddCustomer();
            Alerts.ShowOk("Customer added.");

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
     * Actions on Back
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
        ComboBox_CustomerCountry.setItems(Program.JDBC.GetAllCountries());
        Program.Lambdas.SetDivisionComboBox_OnValueChanged(ComboBox_CustomerCountry, ComboBox_CustomerDivision);
    }
}
