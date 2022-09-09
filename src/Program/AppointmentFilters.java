package Program;

import Models.Appointment;
import javafx.collections.ObservableList;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class AppointmentFilters {
    public static ObservableList<Appointment> AppointmentList;

    public static ZonedDateTime FilterStartDateTime;

    public static ZonedDateTime FilterEndDateTime;

    /**
     * Gets the current datetime.
     * @return
     */
    public static ZonedDateTime getZonedDateTime_TimeNow() { return ZonedDateTime.now(Program.JDBC.GetUserTimeZone()).withZoneSameInstant(ZoneOffset.UTC); }

    /**
     * Gets the datetime 1 week from now.
     * @return
     */
    public static ZonedDateTime getZonedDateTime_TimeInOneWeek() { return getZonedDateTime_TimeNow().plusWeeks(1).withZoneSameInstant(ZoneOffset.UTC); }

    /**
     * Gets the datetime 1 month from now.
     * @return
     */
    public static ZonedDateTime getZonedDateTime_TimeInOneMonth() { return getZonedDateTime_TimeNow().plusMonths(1).withZoneSameInstant(ZoneOffset.UTC); }

}
