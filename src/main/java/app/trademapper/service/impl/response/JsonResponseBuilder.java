package app.trademapper.service.impl.response;

import app.trademapper.model.Trade;
import app.trademapper.service.FileResponseBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class JsonResponseBuilder implements FileResponseBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ByteArrayResource buildFile(List<Trade> trades) {
        try {
            String jsonString = objectMapper.writeValueAsString(trades);
            return new ByteArrayResource(jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error creating JSON response", e);
        }
    }

    @Override
    public ResponseEntity<Resource> buildFileResponse(ByteArrayResource resource) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=trades.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(resource);
    }
}
