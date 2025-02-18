package app.trademapper.service.impl.response;

import app.trademapper.model.Trade;
import app.trademapper.service.FileResponseBuilder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class XmlResponseBuilder implements FileResponseBuilder {
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public ByteArrayResource buildFile(List<Trade> trades) {
        try {
            String xmlString = xmlMapper.writeValueAsString(trades);
            return new ByteArrayResource(xmlString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error creating XML response", e);
        }
    }

    @Override
    public ResponseEntity<Resource> buildFileResponse(ByteArrayResource resource) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trades.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(resource);
    }
}
