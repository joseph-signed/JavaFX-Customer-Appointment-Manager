package Program;

import Models.Appointment;
import Models.Customer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.sql.*;
import java.util.function.Predicate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 * JDBC is the main class that holds information that the whole application is dependent on.
 * JDBC also handles connection the the database and serves as the database access layer.
 */
public class JDBC {
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER"; // LOCAL
    private static final String driver = "com.mysql.cj.jdbc.Driver"; // Driver reference
    private static final String userName = "sqlUser"; // Username
    public static Connection connection = null;  // Connection Interface
    public static String currentUser;
    private static ZoneId userTimeZone = ZoneId.systemDefault();
    public static DateTimeFormatter TimeFormat = DateTimeFormatter.ofPattern("HH:mm");
    public static DateTimeFormatter DateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat AppointmentDateFormat = new SimpleDateFormat("MM-dd-yyyy");
    public static SimpleDateFormat AppointmentTimeFormat = new SimpleDateFormat("hh:mm a z");
    public static Path loginActivityPath = Paths.get("login_activity.txt");
    public static ResourceBundle Language = ResourceBundle.getBundle("Resources/Language", Locale.getDefault());
    public static Integer UserId = 0;

    /**
     * Makes a connection to the Database.
     */
    public static void MakeConnection() {
        try {
            Class.forName(driver);
            String password = "Passw0rd!";
            connection = DriverManager.getConnection(jdbcUrl, userName, password);
            System.out.println("Connection successful!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error:" + e.getMessage());
        }
    }

    /**
     * Gets the connection from the database to use in other classes.
     * @return Connection
     */
    public static Connection GetConnection() {
        return connection;
    }

    public static boolean AttemptLogin(String username, String password) {
        try {
            String checkLogInCredentials = "SELECT * FROM Users WHERE User_Name = '" + username + "' AND Password = '" + password + "'";
            var userCredentials = GetConnection().prepareStatement(checkLogInCredentials);
            var validUserCredentials = userCredentials.executeQuery();

            if (validUserCredentials.next()) {
                if (validUserCredentials.getString("User_Name").equals(username) && validUserCredentials.getString("Password").equals(password)) {
                    currentUser = username;
                    userTimeZone = ZoneId.systemDefault();

                    if (currentUser.equals("test")) {
                        UserId = 1;
                    } else if (currentUser.equals("admin")) {
                        UserId = 2;
                    }

                    return true;
                }
            }
        } catch (SQLException ignored) {}

        return false;
    }

    /**
     * Gets the logged in users time zone.
     * @return UserTimeZone
     */
    public static ZoneId GetUserTimeZone() {
        return userTimeZone;
    }

    /**
     * Gets all User_Id's from Database.
     * @return List of all User_IDs
     */
    public static ObservableList<Integer> GetAllUserId() {
        ObservableList<Integer> userIdList = FXCollections.observableArrayList();

        try {
            var getUserIds = GetConnection().prepareStatement("SELECT DISTINCT User_Id" + " FROM Users;");
            var userIds = getUserIds.executeQuery();

            while (userIds.next()) {
                userIdList.add(userIds.getInt("User_Id"));
            }
        } catch (SQLException ignored) {}

        return userIdList;
    }

    /**
     * Displays alert window of appointments within 15 minutes.
     */
    public static void Notification() {
        var appointmentList = GetAllAppointments();
        var appointmentFilteredList = new FilteredList<>(appointmentList);
        var HasAppointmentIn15Minutes = false;

        Predicate<Appointment> userFilter = a -> a.getAppointmentUserId().equals(UserId);
        appointmentFilteredList.setPredicate(userFilter);

        var now = LocalDateTime.now().atZone(GetUserTimeZone()).withZoneSameInstant(ZoneOffset.UTC);
        var nowPlus15Minutes = now.plusMinutes(15);

        //If there's an appointment in 15 minutes display an alert with appointment details
        for (var appointment : appointmentFilteredList) {
            var appointmentStartTime = appointment.getAppointmentStartTime().toInstant().atZone(GetUserTimeZone());
            if (appointmentStartTime.isAfter(now) && appointmentStartTime.isBefore(nowPlus15Minutes)) {
                HasAppointmentIn15Minutes = true;
                Alerts.UpcomingAppointments(appointment);
            }
        }

        //If there's not an appointment in 15 minutes, display an empty alert
        if (!HasAppointmentIn15Minutes) {
            Alerts.ShowOk("No appointments in 15 minutes");
        }
    }

    /**
     *
     * @param divisionIdString
     * @return SpecificCountry
     */
    public static String GetSpecificCountry(String divisionIdString) {
        var divisionId = Integer.parseInt(divisionIdString);
        var country = "";

        try {
            var getCountryId = GetConnection().prepareStatement("SELECT Country_Id FROM First_Level_Divisions WHERE Division_Id = ?");
            getCountryId.setInt(1, divisionId);

            var getCountryIdResults = getCountryId.executeQuery();

            if (getCountryIdResults.next()) {
                var countryId = getCountryIdResults.getInt("Country_Id");

                var getCountry = GetConnection().prepareStatement("SELECT Country FROM Countries WHERE Country_Id = ?");
                getCountry.setInt(1, countryId);
                var getCountryResults = getCountry.executeQuery();

                if (getCountryResults.next()) {
                    country = getCountryResults.getString("Country");
                }
            }
        } catch(SQLException ignored){}

        return country;
    }

    /**
     * Updates the CustomerList for controllers
     */
    public static void UpdateCustomerList() {
        try {
            var customerList = Customer.GetCustomerList();
            var customerIdList = new ArrayList<>();

            customerList.clear();

            var getCustomerIdFromCustomers = GetConnection().createStatement();
            var customerIds = getCustomerIdFromCustomers.executeQuery("SELECT Customer_Id FROM Customers");

            while (customerIds.next()) {
                customerIdList.add(customerIds.getInt(1));
            }

            for (var customerId : customerIdList) {
                Customer customer = new Customer();
                ResultSet customerResult = getCustomerIdFromCustomers.executeQuery("SELECT Customer_Name, Address, Phone, Postal_code," +
                        " Division_Id FROM Customers WHERE Customer_Id = '" + customerId + "'");

                if (customerResult.next()){
                    var customerName = customerResult.getString(1);
                    var address = customerResult.getString(2);
                    var phone = customerResult.getString(3);
                    var postalCode = customerResult.getString(4);
                    var division = customerResult.getString(5);
                    var country = Program.JDBC.GetSpecificCountry(division);

                    customer.setCustomerId((int)customerId);
                    customer.setCustomerName(customerName);
                    customer.setCustomerAddress(address);
                    customer.setCustomerPostalCode(postalCode);
                    customer.setPhone(phone);
                    customer.setCustomerDivision(division);
                    customer.setCustomerCountry(country);

                    customerList.add(customer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the Customer with the matching Customer_ID.
     * @param divisionId
     * @param customerName
     * @param customerAddress
     * @param customerPostalCode
     * @param customerPhoneNumber
     * @param customerId
     */
    public static void UpdateCustomerById(int divisionId, String customerName, String customerAddress, String customerPostalCode, String customerPhoneNumber, Integer customerId) {
        try {
            var updateCustomer = GetConnection().prepareStatement("UPDATE Customers " + "SET Customer_Name=?, Address=?, Postal_Code=?, Phone=?, Last_Update=?," + " Last_Updated_By=?, Division_Id=? WHERE Customer_Id = ?");
            updateCustomer.setString(1, customerName);
            updateCustomer.setString(2, customerAddress);
            updateCustomer.setString(3, customerPostalCode);
            updateCustomer.setString(4, customerPhoneNumber);
            updateCustomer.setString(5, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormat));
            updateCustomer.setString(6, currentUser);
            updateCustomer.setInt(7, divisionId);
            updateCustomer.setInt(8, customerId);

            updateCustomer.executeUpdate();
        }
        catch (SQLException ignored) {
        }
    }

    /**
     * Removes Customer with the matching Customer_ID.
     * @param customerId
     */
    public static void RemoveCustomerById(Integer customerId) {
        try {
            var deleteCustomerAppointment = GetConnection().prepareStatement("DELETE FROM Appointments " + "WHERE Customer_Id = ?");
            deleteCustomerAppointment.setInt(1, customerId);

            var deleteCustomer = GetConnection().prepareStatement("DELETE FROM Customers " + "WHERE Customer_Id = ?");
            deleteCustomer.setInt(1, customerId);

            deleteCustomerAppointment.executeUpdate();
            deleteCustomer.executeUpdate();
        } catch (SQLException ignored) {}
    }

    /**
     * Add Customer to the Database.
     * @param customerName
     * @param customerAddress
     * @param customerPostalCode
     * @param customerPhoneNumber
     * @param divisionId
     */
    public static void AddCustomer(String customerName, String customerAddress, String customerPostalCode, String customerPhoneNumber, Integer divisionId) {
        try {
            var addCustomerQuery = GetConnection().prepareStatement("INSERT INTO Customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, " + "Last_Update, Last_Updated_By, Division_Id) \n" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);");

            addCustomerQuery.setString(1, customerName);
            addCustomerQuery.setString(2, customerAddress);
            addCustomerQuery.setString(3, customerPostalCode);
            addCustomerQuery.setString(4, customerPhoneNumber);
            addCustomerQuery.setString(5, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormat));
            addCustomerQuery.setString(6, currentUser);
            addCustomerQuery.setString(7, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormat));
            addCustomerQuery.setString(8, currentUser);
            addCustomerQuery.setInt(9, divisionId);

            addCustomerQuery.executeUpdate();

        } catch (SQLException ignored) {
        }
    }

    /**
     * Retrieves Division_ID from the Database with the Division name.
     * @param divisionName
     * @return Division_ID
     */
    public static Integer GetDivisionIdByName(String divisionName) {
        int result = -1;

        try {
            var getDivisionQuery = GetConnection().prepareStatement("SELECT Division, Division_Id FROM " + "First_Level_Divisions WHERE Division = ?");

            getDivisionQuery.setString(1, divisionName);

            var divisionId = getDivisionQuery.executeQuery();

            if (divisionId.next()) {
                result = divisionId.getInt("Division_Id");
            }
        } catch (SQLException ignored) {}

        return result;
    }

    /**
     * Gets all Customer_IDs
     * @return Customer_IDs
     */
    public static ObservableList<Integer> GetAllCustomerIds()  {
        ObservableList<Integer> customerIdList = FXCollections.observableArrayList();

        try {
            var getCustomerIdsQuery = GetConnection().prepareStatement("SELECT DISTINCT Customer_Id" + " FROM customers;");
            var customerIds = getCustomerIdsQuery.executeQuery();

            while (customerIds.next()) {
                customerIdList.add(customerIds.getInt("Customer_Id"));
            }
        } catch (SQLException ignored) {}

        return customerIdList;
    }

    /**
     * Gets all Divisions from a Country
     * @param countryName
     * @return Divisions
     */
    public static ObservableList<String> GetDivisionsByCountry(String countryName) {
        ObservableList<String> divisionList = FXCollections.observableArrayList();

        try {
            PreparedStatement getDivisionsQuery = GetConnection().prepareStatement("SELECT Countries.Country, Countries.Country_Id,  First_Level_Divisions.Division_Id, First_Level_Divisions.Division FROM Countries " +
                    "RIGHT OUTER JOIN First_Level_Divisions ON Countries.Country_Id = First_Level_Divisions.Country_Id WHERE Countries.Country = ?");

            getDivisionsQuery.setString(1, countryName);
            var divisions = getDivisionsQuery.executeQuery();

            while (divisions.next()) {
                divisionList.add(divisions.getString("Division"));
            }
        } catch (SQLException ignored) {}

        return divisionList;
    }

    /**
     * Get all Countries from Database.
     * @return Countries
     */
    public static ObservableList<String> GetAllCountries() {
        ObservableList<String> countryList = FXCollections.observableArrayList();

        try {
            var sqlCommand = GetConnection().prepareStatement("SELECT DISTINCT Country FROM countries");
            var countries = sqlCommand.executeQuery();

            while (countries.next()) {
                countryList.add(countries.getString("Country"));
            }
        } catch (SQLException ignored) {}

        return countryList;
    }

    /**
     * Get all Customers from Database.
     * @return Customers
     */
    public static ObservableList<Customer> GetAllCustomers() {
        ObservableList<Customer> customerList = FXCollections.observableArrayList();

        try {
            var getAllCustomersQuery = GetConnection().prepareStatement(
                    "SELECT Customers.Customer_Id, Customers.Customer_Name, Customers.Address, Customers.Postal_Code, Customers.Phone, Customers.Division_Id, First_Level_Divisions.Division, First_Level_Divisions.Country_Id, " +
                            "Countries.Country FROM Customers INNER JOIN First_Level_Divisions on Customers.Division_Id = First_Level_Divisions.Division_Id INNER JOIN Countries ON First_Level_Divisions.Country_Id = " +
                            "Countries.Country_Id");

            var allCustomers = getAllCustomersQuery.executeQuery();

            while (allCustomers.next()) {
                var customerId = allCustomers.getInt("Customer_Id");
                var customerName = allCustomers.getString("Customer_Name");
                var customerAddress = allCustomers.getString("Address");
                var customerPostalCode = allCustomers.getString("Postal_Code");
                var customerPhoneNum = allCustomers.getString("Phone");
                var customerDivision = allCustomers.getString("Division");
                var customerCountry = allCustomers.getString("Country");

                Customer newCustomer = new Customer(customerId, customerName, customerAddress, customerPostalCode, customerPhoneNum, customerDivision, customerCountry);
                customerList.add(newCustomer);
            }
        } catch (SQLException ignored) {}

        return customerList;
    }


    /**
     * Gets Appointments from Database that are within a time range.
     * @param appointmentStart
     * @param appointmentEnd
     * @return Appointments
     */
    public static ObservableList<Appointment> GetAppointsInDateTimeRange(ZonedDateTime appointmentStart, ZonedDateTime appointmentEnd) {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

        try {
            PreparedStatement getAppointmentsQuery = GetConnection().prepareStatement("SELECT * FROM Appointments LEFT OUTER JOIN Contacts ON Appointments.Contact_Id = Contacts.Contact_Id WHERE Start between ? AND ?");

            var startString = appointmentStart.format(DateTimeFormat);
            var endString = appointmentEnd.format(DateTimeFormat);

            getAppointmentsQuery.setString(1, startString);
            getAppointmentsQuery.setString(2, endString);

            var appointments = getAppointmentsQuery.executeQuery();

            while (appointments.next()) {
                var appointmentId = appointments.getInt("Appointment_Id");
                var appointmentTitle = appointments.getString("Title");
                var appointmentDescription = appointments.getString("Description");
                var appointmentLocation = appointments.getString("Location");
                var appointmentType = appointments.getString("Type");
                var appointmentStartDateTime = appointments.getTimestamp("Start");
                var appointmentEndDateTime = appointments.getTimestamp("End");
                var appointmentCustomerId = appointments.getInt("Customer_Id");
                var appointmentUserId = appointments.getInt("User_Id");
                var appointmentContactId = appointments.getInt("Contact_Id");

                Appointment newAppointment = new Appointment(appointmentId, appointmentTitle, appointmentDescription, appointmentLocation, appointmentType, appointmentStartDateTime, appointmentEndDateTime, appointmentCustomerId,
                        appointmentUserId, appointmentContactId);

                appointmentList.add(newAppointment);
            }
        } catch (SQLException ignored) {}

        return appointmentList;
    }

    /**
     * Get Customer's Appointments
     * @param appointmentDate
     * @param customerId
     * @return Appointments
     */
    public static ObservableList<Appointment> GetAppointmentByCustomerId(LocalDate appointmentDate, Integer customerId) {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

        try {
            PreparedStatement getCustomerAppointmentsQuery = GetConnection().prepareStatement("SELECT * FROM Appointments LEFT OUTER JOIN Contacts ON Appointments.Contact_Id = Contacts.Contact_Id WHERE DATEDIFF(Appointments.Start, ?) = 0 AND Customer_Id = ?;");

            getCustomerAppointmentsQuery.setInt(2, customerId);

            getCustomerAppointmentsQuery.setString(1, appointmentDate.toString());

            var appointments = getCustomerAppointmentsQuery.executeQuery();

            while (appointments.next()) {
                var appointmentId = appointments.getInt("Appointment_Id");
                var appointmentTitle = appointments.getString("Title");
                var appointmentDescription = appointments.getString("Description");
                var appointmentLocation = appointments.getString("Location");
                var appointmentType = appointments.getString("Type");
                var appointmentStart = appointments.getTimestamp("Start");
                var appointmentEnd = appointments.getTimestamp("End");
                var appointmentCustomerId = appointments.getInt("Customer_Id");
                var appointmentUserId = appointments.getInt("User_Id");
                var appointmentContactId = appointments.getInt("Contact_Id");

                Appointment newAppointment = new Appointment(appointmentId, appointmentTitle, appointmentDescription, appointmentLocation, appointmentType, appointmentStart, appointmentEnd,
                         appointmentCustomerId, appointmentUserId, appointmentContactId);

                appointmentList.add(newAppointment);
            }
        } catch (SQLException ignored) {}

        return appointmentList;
    }

    /**
     * Update an Appointment in the Database using the matching Appointment_ID.
     * @param appointmentId
     * @param appointmentTitle
     * @param appointmentDescription
     * @param appointmentLocation
     * @param appointmentType
     * @param appointmentStart
     * @param appointmentEnd
     * @param appointmentLastUpdateBy
     * @param appointmentCustomerId
     * @param appointmentUserId
     * @param appointmentContactId
     */
    public static void UpdateAppointmentById(Integer appointmentId, String appointmentTitle, String appointmentDescription, String appointmentLocation, String appointmentType, ZonedDateTime appointmentStart,
                                             ZonedDateTime appointmentEnd, String appointmentLastUpdateBy, Integer appointmentCustomerId, Integer appointmentUserId, Integer appointmentContactId) {
        try {
            var updateAppointmentQuery = GetConnection().prepareStatement("UPDATE Appointments SET Title=?, Description=?, Location=?, Type=?, Start=?, End=?, Last_Update=?,Last_Updated_By=?, Customer_Id=?, User_Id=?, " +
                    "Contact_Id=? WHERE Appointment_Id = ?");

            var appointmentStartString = appointmentStart.format(DateTimeFormat);
            var appointmentEndString = appointmentEnd.format(DateTimeFormat);

            updateAppointmentQuery.setString(1, appointmentTitle);
            updateAppointmentQuery.setString(2, appointmentDescription);
            updateAppointmentQuery.setString(3, appointmentLocation);
            updateAppointmentQuery.setString(4, appointmentType);
            updateAppointmentQuery.setString(5, appointmentStartString);
            updateAppointmentQuery.setString(6, appointmentEndString);
            updateAppointmentQuery.setString(7, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormat));
            updateAppointmentQuery.setString(8, appointmentLastUpdateBy);
            updateAppointmentQuery.setInt(9, appointmentCustomerId);
            updateAppointmentQuery.setInt(10, appointmentUserId);
            updateAppointmentQuery.setInt(11, appointmentContactId);
            updateAppointmentQuery.setInt(12, appointmentId);

            updateAppointmentQuery.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    /**
     * Add Appointment to the Database.
     * @param appointmentTitle
     * @param appointmentDescription
     * @param appointmentLocation
     * @param appointmentType
     * @param appointmentStart
     * @param appointmentEnd
     * @param appointmentCreatedBy
     * @param appointmentLastUpdateBy
     * @param appointmentCustomerId
     * @param appointmentUserId
     * @param appointmentContactId
     */
    public static void AddAppointment(String appointmentTitle, String appointmentDescription, String appointmentLocation, String appointmentType, ZonedDateTime appointmentStart, ZonedDateTime appointmentEnd,
                                      String appointmentCreatedBy, String appointmentLastUpdateBy, Integer appointmentCustomerId, Integer appointmentUserId, Integer appointmentContactId) {
        try {
            var addAppointmentQuery = GetConnection().prepareStatement("INSERT INTO Appointments (Title, Description, Location, Type, Start, End, Create_date, Created_By, Last_Update, Last_Updated_By, " +
                    "Customer_Id, User_Id, Contact_Id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            var appointmentStartString = appointmentStart.format(DateTimeFormat);
            var appointmentEndString = appointmentEnd.format(DateTimeFormat);

            addAppointmentQuery.setString(1, appointmentTitle);
            addAppointmentQuery.setString(2, appointmentDescription);
            addAppointmentQuery.setString(3, appointmentLocation);
            addAppointmentQuery.setString(4, appointmentType);
            addAppointmentQuery.setString(5, appointmentStartString);
            addAppointmentQuery.setString(6, appointmentEndString);
            addAppointmentQuery.setString(7, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormat));
            addAppointmentQuery.setString(8, appointmentCreatedBy);
            addAppointmentQuery.setString(9, ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormat));
            addAppointmentQuery.setString(10, appointmentLastUpdateBy);
            addAppointmentQuery.setInt(11, appointmentCustomerId);
            addAppointmentQuery.setInt(12, appointmentUserId);
            addAppointmentQuery.setInt(13, appointmentContactId);

            addAppointmentQuery.executeUpdate();
        } catch (SQLException ignored) {}
    }

    /**
     * Remove Appointment from the Database using Appointment_ID.
     * @param appointmentId
     */
    public static void RemoveAppointmentById(Integer appointmentId) {
        try {
            var removeAppointmentQuery = GetConnection().prepareStatement("DELETE FROM Appointments " + "WHERE Appointment_Id = ?");

            removeAppointmentQuery.setInt(1, appointmentId);

            removeAppointmentQuery.executeUpdate();
        } catch (SQLException ignored) {}
}

    /**
     * Get all Appointments from the Database.
     * @return Appointments
     */
    public static ObservableList<Appointment> GetAllAppointments() {
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();

        try {
            var getAllAppointmentsQuery = GetConnection().prepareStatement("SELECT * FROM Appointments LEFT OUTER JOIN Contacts ON Appointments.Contact_Id = Contacts.Contact_Id;");
            var allAppointments = getAllAppointmentsQuery.executeQuery();

            while (allAppointments.next()) {
                var appointmentId = allAppointments.getInt("Appointment_Id");
                var appointmentTitle = allAppointments.getString("Title");
                var appointmentDescription = allAppointments.getString("Description");
                var appointmentLocation = allAppointments.getString("Location");
                var appointmentType = allAppointments.getString("Type");
                var appointmentStartDateTime = allAppointments.getTimestamp("Start");
                var appointmentEndDateTime = allAppointments.getTimestamp("End");
                var appointmentCustomerId = allAppointments.getInt("Customer_Id");
                var appointmentUserId = allAppointments.getInt("User_Id");
                var appointmentContactId = allAppointments.getInt("Contact_Id");

                Appointment newAppointment = new Appointment(
                        appointmentId, appointmentTitle, appointmentDescription, appointmentLocation, appointmentType, appointmentStartDateTime, appointmentEndDateTime, appointmentCustomerId, appointmentUserId, appointmentContactId);

                appointmentList.add(newAppointment);
            }
        } catch (SQLException ignored) {}
        return appointmentList;
    }

    /**
     * Get Contact's Appointments using Contact_ID.
     * @param contactId
     * @return Appointments as strings.
     */
    public static ObservableList<String> GetContactAppointmentsById(String contactId) {
        ObservableList<String> appointmentList = FXCollections.observableArrayList();
        try {
            var getContactAppointmentsQuery = GetConnection().prepareStatement("SELECT * FROM Appointments WHERE Contact_Id = ?");
            getContactAppointmentsQuery.setString(1, contactId);
            var contactAppointments = getContactAppointmentsQuery.executeQuery();
            while ( contactAppointments.next()) {
                var appointmentDetails = FXCollections.observableArrayList();
                appointmentDetails.add("Appointment ID: " + contactAppointments.getString("Appointment_Id"));
                appointmentDetails.add("Appointment Title: " + contactAppointments.getString("Title"));
                appointmentDetails.add("Appointment Type: " + contactAppointments.getString("Type"));
                appointmentDetails.add("Appointment Start Time: " + contactAppointments.getString("Start"));
                appointmentDetails.add("Appointment End Time: " + contactAppointments.getString("End"));
                appointmentDetails.add("Customer ID: " + contactAppointments.getString("Customer_Id"));
                appointmentList.add(String.valueOf(appointmentDetails));
            }
        } catch (SQLException ignored) {}

        return appointmentList;
    }

    /**
     * Get all Contact names from the Database.
     * @return Contact names as Strings
     */
    public static ObservableList<String> GetAllContactNames() {
        ObservableList<String> contactList = FXCollections.observableArrayList();

        try {
            var GetAllContactNamesQuery = GetConnection().prepareStatement("SELECT DISTINCT Contact_Name FROM Contacts;");
            var contactNames = GetAllContactNamesQuery.executeQuery();

            while ( contactNames.next() ) {
                contactList.add(contactNames.getString("Contact_Name"));
            }
        }catch (SQLException ignored) {}
        return contactList;
    }

    /**
     * Get Contact name from the Database using Contact_ID.
     * @param contactId
     * @return
     */
    public static String GetContactNameById(int contactId) {
        String contactName = "";

        try {

            var getContactNameQuery = GetConnection().prepareStatement("SELECT Contact_Name FROM Contacts WHERE Contact_Id = ?");

            getContactNameQuery.setInt(1, contactId);
            var results = getContactNameQuery.executeQuery();
            if (results.next()) {
                contactName = results.getString("Contact_Name");
            }

        }catch (SQLException ignored) {}
        return contactName;
    }

    /**
     * Get Contact_ID from the Database using Contact name.
     * @param contactName
     * @return Contact_ID.
     */
    public static Integer GetContactIdByContactName(String contactName) {
        int id = -1;

        try{
            var getContactIdByName = GetConnection().prepareStatement("SELECT Contact_Id, Contact_Name FROM Contacts WHERE Contact_Name = ?");
            getContactIdByName.setString(1, contactName);

            var customerId = getContactIdByName.executeQuery();

            if (customerId.next()) {
                id = customerId.getInt("Contact_Id");
            }
        }catch (SQLException ignored) {}
            return id;
    }
}

