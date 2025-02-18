package app.trademapper.util;

import app.trademapper.model.Trade;
import app.trademapper.validation.TradeValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonLoader {
    private final TradeValidator tradeValidator;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public List<Trade> loadJson(MultipartFile file) {
        try {
            String jsonContent = readFileContent(file);
            List<Trade> trades = parseJsonToTrades(jsonContent);
            return validateTrades(trades);
        } catch (IOException e) {
            throw new RuntimeException("Error reading the JSON file", e);
        }
    }

    private String readFileContent(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private List<Trade> parseJsonToTrades(String jsonContent) throws IOException {
        return jsonMapper.readValue(jsonContent,
                jsonMapper.getTypeFactory().constructCollectionType(List.class, Trade.class));
    }

    private List<Trade> validateTrades(List<Trade> trades) {
        return trades.stream()
                .peek(this::logValidationErrors)
                .filter(this::isValidTrade)
                .limit(1000000)
                .collect(Collectors.toList());
    }

    private void logValidationErrors(Trade trade) {
        if (trade == null) {
            log.error("Trade is null");
        } else if (trade.getDate() == null) {
            log.error("Trade date is null for trade: {}", trade);
        } else if (!tradeValidator.isValidDate(trade.getDate())) {
            log.error("Invalid trade date: {} for trade: {}", trade.getDate(), trade);
        }
    }

    private boolean isValidTrade(Trade trade) {
        return trade != null && trade.getDate() != null
                && tradeValidator.isValidDate(trade.getDate());
    }
}
