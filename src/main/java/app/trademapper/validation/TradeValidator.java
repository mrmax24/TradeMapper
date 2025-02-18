package app.trademapper.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Component;

@Component
public class TradeValidator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
