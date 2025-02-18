package app.trademapper.controller;

import app.trademapper.model.Trade;
import app.trademapper.service.TradeService;
import app.trademapper.service.impl.response.FileResponseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;
    private final FileResponseService fileResponseService;

    @PostMapping(value = "/enrich",
            produces = {"text/csv", "application/json", "application/xml"})
    public ResponseEntity<?> enrichTrades(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return emptyFileResponse();
        }
        try {
            List<Trade> enrichedTrades = tradeService.getEnrichedTrades(file);
            String fileType = getFileType(file);
            return buildResponse(fileType, enrichedTrades);
        } catch (Exception e) {
            throw new RuntimeException("Error processing trade data", e);
        }
    }

    private String getFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name is empty");
        }
        return fileName.substring(
                fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private ResponseEntity<Resource> buildResponse(String fileType,
                                                   List<Trade> enrichedTrades) {
        return fileResponseService.buildResponse(fileType, enrichedTrades);
    }

    private ResponseEntity<String> emptyFileResponse() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("File is empty. Please upload a valid file.");
    }
}
