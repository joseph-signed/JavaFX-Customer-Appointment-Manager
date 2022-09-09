package Program;

import javafx.scene.control.ComboBox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;

public class Lambdas {

    /**
     * Lambda function used to update a checkbox on the OnValueChanged action.
     *
     * This function helped my code by providing me with the unique feature of dynamically updating my division name list based on the country name selected item.
     *
     * Location:
     *      C:\Users\LabUser\Documents\GitHub\C195\C195Final\src\Program\Lambdas.java
     *
     * @param ComboBox_CountryName
     * @param ComboBox_DivisionName
     */
    public static void SetDivisionComboBox_OnValueChanged(ComboBox<String> ComboBox_CountryName, ComboBox<String> ComboBox_DivisionName) {
        ComboBox_CountryName.valueProperty().addListener((o, oldCountryName, newCountryName) ->
        {
            var divisionForCountry = Program.JDBC.GetDivisionsByCountry(ComboBox_CountryName.getValue());
            if (newCountryName == null || newCountryName.isBlank()) {
                ComboBox_DivisionName.setDisable(true);
                ComboBox_DivisionName.setItems(divisionForCountry);
            }
            else {
                ComboBox_DivisionName.setItems(divisionForCountry);
                ComboBox_DivisionName.setDisable(false);
            }
        });
    }

    /**
     * Lambda function used to record log in attempts.
     *
     * Using a lambda function helped my code by increasing the readability of my code.
     *
     * Location:
     *      C:\Users\LabUser\Documents\GitHub\C195\C195Final\src\Program\Lambdas.java
     *
     * @param loginAttempt
     */
    public static void Logger(boolean loginAttempt) {
        Consumer<Boolean> checkCredentials = (attempt) -> {
            String message;
            if (attempt) {
                message = "Successful log in attempt: " + Date.from(Instant.from(Instant.now().atZone(JDBC.GetUserTimeZone()))) + ".\n";
            } else {
                message = "Unsuccessful log in attempt: " + Date.from(Instant.from(Instant.now().atZone(JDBC.GetUserTimeZone()))) + ".\n";
            }
            try {
                Files.write(
                        JDBC.loginActivityPath,
                        Collections.singletonList(message),
                        StandardCharsets.UTF_8,
                        Files.exists(JDBC.loginActivityPath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE
                );
            } catch (IOException ignored) {}

        };
        checkCredentials.accept(loginAttempt);
    }
}
