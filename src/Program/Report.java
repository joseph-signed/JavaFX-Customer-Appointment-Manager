package Program;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.SQLException;
import java.time.Month;
import static Program.JDBC.GetConnection;

public class Report {
    public static ObservableList<String> CustomerAppointmentReport = FXCollections.observableArrayList();
    public static ObservableList<String> CustomerCountryReport = FXCollections.observableArrayList();
    public static ObservableList<String> ContactScheduleReport = FXCollections.observableArrayList();

    /**
     * Generates a list of strings that represent a report to display Customer appointments by Type and Month.
     * @return
     */
    public static ObservableList<String> GenerateCustomerAppointmentReport() {
        CustomerAppointmentReport.clear();
        CustomerAppointmentReport.add("Report for the # of Appointments by Type and Month.\n----------------------------------------------------------\n");

        try {
            var typeSqlCommand = GetConnection().prepareStatement("SELECT Type, COUNT(Type) FROM Appointments GROUP BY Type");
            var monthSqlCommand = GetConnection().prepareStatement("SELECT EXTRACT(MONTH FROM Start) AS MonthOfDate, COUNT(MONTH(Start)) FROM Appointments GROUP BY MonthOfDate");

            var typeResults = typeSqlCommand.executeQuery();
            var monthResults = monthSqlCommand.executeQuery();

            CustomerAppointmentReport.add("\nAppointments by Type\n----------------------------------------------------------\n");
            while (typeResults.next()) {
                var type = typeResults.getString("Type");
                var total = typeResults.getString("COUNT(Type)");

                var reportLine = "-Total of \"" + type + "\" appointments: " + total + "\n";

                CustomerAppointmentReport.add(reportLine);
            }

            CustomerAppointmentReport.add("\nAppointments by Month\n----------------------------------------------------------\n");
            while (monthResults.next()) {
                var month = Month.of(Integer.parseInt(monthResults.getString("MonthOfDate"))).name();
                var total = monthResults.getString("COUNT(MONTH(Start))");

                var reportLine = "-Total # of appointments in " + month + ": " + total + "\n";
                CustomerAppointmentReport.add(reportLine);
            }
        } catch (SQLException ignored) {}

        return CustomerAppointmentReport;
    }

    /**
     * Generates a list of strings that represent a report to display where current Customers are from, and display their Country.
     * CUSTOM REPORT
     */
    public static ObservableList<String> GenerateCustomerCountryReport() {
        CustomerCountryReport.clear();
        CustomerCountryReport.add("Report for the different Countries our Customers are from.\n--------------------------------------------------------------------\n");

        try {
            var divisionSqlCommand = GetConnection().prepareStatement("SELECT Division_Id FROM Customers GROUP BY Division_Id");

            var divisionResults = divisionSqlCommand.executeQuery();

            CustomerCountryReport.add("\nCustomer Countries\n--------------------------------------------------------------------\n");
            while (divisionResults.next()) {
                var divisionId = divisionResults.getString("Division_Id");
                var country = Program.JDBC.GetSpecificCountry(divisionId);

                var reportLine = "-A customer is from " + country + "\n";

                CustomerCountryReport.add(reportLine);
            }
        } catch (SQLException ignored) {}

        return CustomerCountryReport;
    }

    /**
     * Generates a list of strings that represent a report to display Contact appointments and the appointment's details.
     * @return
     */
    public static ObservableList<String> GenerateContactScheduleReport() {
        ContactScheduleReport.clear();
        ContactScheduleReport.add("Report for the Details of company Contact appointments\n----------------------------------------------------------\n----------------------------------------------------------\n");

        var allContacts = Program.JDBC.GetAllContactNames();
        for (var contact : allContacts) {
            var id = Program.JDBC.GetContactIdByContactName(contact);
            var contactAppointments = Program.JDBC.GetContactAppointmentsById(id.toString());
            ContactScheduleReport.add("Name: " + contact + "\nContact ID: " + id + "\n");
            if (contactAppointments.isEmpty()) {
                ContactScheduleReport.add("NO APPOINTMENTS\n");
                ContactScheduleReport.add("\n----------------------------------------------------------\n");
            }

            for (var appointment : contactAppointments) {
                ContactScheduleReport.add(appointment);
                ContactScheduleReport.add("\n----------------------------------------------------------\n");
            }
        }

        return ContactScheduleReport;
    }
}
