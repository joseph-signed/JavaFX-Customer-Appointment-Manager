package Models;

import Program.JDBC;
import java.sql.Timestamp;

public class Appointment {
    private final Integer AppointmentId;
    private final String AppointmentTitle;
    private final String AppointmentDescription;
    private final String AppointmentLocation;
    private final String AppointmentType;
    private final String AppointmentContactName;
    private final Timestamp AppointmentStartTime;
    private final Timestamp AppointmentEndTime;
    private final Integer AppointmentCustomerId;
    private final Integer AppointmentUserId;
    private final String AppointmentDateString;
    private final String AppointmentStartString;
    private final String AppointmentEndString;

    public Appointment(Integer appointmentId, String appointmentTitle, String appointmentDescription, String appointmentLocation, String appointmentType, Timestamp appointmentStartTime,
                       Timestamp appointmentEndTime, Integer appointmentCustomerId, Integer appointmentUserId, Integer appointmentContactId) {
        AppointmentId = appointmentId;
        AppointmentTitle = appointmentTitle;
        AppointmentDescription = appointmentDescription;
        AppointmentLocation = appointmentLocation;
        AppointmentType = appointmentType;
        AppointmentStartTime = appointmentStartTime;
        AppointmentEndTime = appointmentEndTime;
        AppointmentCustomerId = appointmentCustomerId;
        AppointmentUserId = appointmentUserId;
        AppointmentContactName = JDBC.GetContactNameById(appointmentContactId);
        AppointmentDateString = JDBC.AppointmentDateFormat.format(AppointmentStartTime);
        AppointmentStartString = JDBC.AppointmentTimeFormat.format(AppointmentStartTime);
        AppointmentEndString = JDBC.AppointmentTimeFormat.format(AppointmentEndTime);
    }

    public Integer getAppointmentId() {
        return AppointmentId;
    }
    public String getAppointmentTitle() {
        return AppointmentTitle;
    }
    public String getAppointmentType() {
        return AppointmentType;
    }
    public String getAppointmentDescription() {
        return AppointmentDescription;
    }
    public String getAppointmentLocation() {
        return AppointmentLocation;
    }
    public Timestamp getAppointmentStartTime() {
        return AppointmentStartTime;
    }
    public Timestamp getAppointmentEndTime() {
        return AppointmentEndTime;
    }
    public Integer getAppointmentCustomerId() {
        return AppointmentCustomerId;
    }
    public Integer getAppointmentUserId() {
        return AppointmentUserId;
    }
    public String getAppointmentDateString() {
        return AppointmentDateString;
    }
    public String getAppointmentStartString() {
        return AppointmentStartString;
    }
    public String getAppointmentEndString() {
        return AppointmentEndString;
    }
    public String getAppointmentContactName() {
        return AppointmentContactName;
    }
}
