package Program;

import Models.Appointment;

/**
 * Handles the global static SelectedAppointment object for use when passing information between controllers.
 */
public class SelectedAppointment {
    private static Appointment Appointment;

    public static Appointment GetSelectedAppointment() { return Appointment; }
    public static void SetSelectedAppointment(Appointment selectedAppointment) { Appointment = selectedAppointment; }
}
