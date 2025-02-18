package app.trademapper.util;

import app.trademapper.model.Trade;
import app.trademapper.validation.TradeValidator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvLoader {
    private final TradeValidator tradeValidator;

    List<Trade> loadCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .limit(1000000)
                    .map(this::mapToTrade)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error loading file: " + file.getName(), e);
        }
    }

    private Trade mapToTrade(String line) {
        String[] cols = line.split(",");
        String date = cols[0].trim();

        if (line.trim().isEmpty()) {
            log.error("Empty line found");
            return null;
        }
        if (!tradeValidator.isValidDate(date)) {
            log.error("Invalid date format: {} in row: {}", date, line);
            return null;
        }
        try {
            return new Trade(date, Integer.parseInt(cols[1].trim()),
                    cols[2].trim(), Double.parseDouble(cols[3].trim()));
        } catch (NumberFormatException e) {
            log.error("Error parsing numeric values in row: {}", line, e);
            return null;
        }
    }
}
