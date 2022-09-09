package Models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Customer {
    private static final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    public static ObservableList<Customer> GetCustomerList(){
        return customerList;
    }

    private Integer CustomerId;
    private String CustomerName;
    private String CustomerAddress;
    private String CustomerPostalCode;
    private String CustomerPhoneNumber;
    private String CustomerDivision;
    private String CustomerCountry;

    public Integer getCustomerId() {
        return CustomerId;
    }
    public String getCustomerName() {
        return CustomerName;
    }
    public String getCustomerAddress() {
        return CustomerAddress;
    }
    public String getCustomerPostalCode() {
        return CustomerPostalCode;
    }
    public String getCustomerPhoneNumber() {
        return CustomerPhoneNumber;
    }
    public String getCustomerDivision() {
        return CustomerDivision;
    }
    public String getCustomerCountry() {
        return CustomerCountry;
    }

    public void setCustomerId(int customerId){
        CustomerId = customerId;
    }
    public void setCustomerName(String customerName){
        CustomerName = customerName;
    }
    public void setCustomerAddress(String customerAddress){
        CustomerAddress = customerAddress;
    }
    public void setCustomerPostalCode(String customerPostalCode){
        CustomerPostalCode = customerPostalCode;
    }
    public void setCustomerCountry(String customerCountry){
        CustomerCountry = customerCountry;
    }
    public void setPhone(String customerPhoneNumber){
        CustomerPhoneNumber = customerPhoneNumber;
    }
    public void setCustomerDivision(String customerDivision){
        CustomerDivision = customerDivision;
    }

    public Customer() {}
    public Customer(Integer customerId, String customerName, String customerAddress,
                    String customerPostalCode, String customerPhoneNumber,
                    String customerDivision, String customerCountry) {
        CustomerId = customerId;
        CustomerName = customerName;
        CustomerAddress = customerAddress;
        CustomerPostalCode = customerPostalCode;
        CustomerPhoneNumber = customerPhoneNumber;
        CustomerDivision = customerDivision;
        CustomerCountry = customerCountry;
    }
}
