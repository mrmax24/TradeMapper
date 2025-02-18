package app.trademapper.util;

import app.trademapper.model.Trade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class TradeDataLoader {
    private final CsvLoader csvLoader;
    private final JsonLoader jsonLoader;
    private final XmlLoader xmlLoader;

    public List<Trade> loadTradesFromFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name is empty");
        }

        return switch (getFileExtension(fileName)) {
            case "csv" -> csvLoader.loadCsv(file);
            case "json" -> jsonLoader.loadJson(file);
            case "xml" -> xmlLoader.loadXml(file);
            default -> throw new
                    IllegalArgumentException("Unsupported file format: " + fileName);
        };
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return (lastDotIndex == -1) ? ""
                : fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
