package app.trademapper.service.impl.response;

import app.trademapper.model.Trade;
import app.trademapper.service.FileResponseBuilder;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FileResponseService {

    private final Map<String, FileResponseBuilder> fileResponseBuilders;

    @Autowired
    public FileResponseService(CsvResponseBuilder csvResponseBuilder,
                               JsonResponseBuilder jsonResponseBuilder,
                               XmlResponseBuilder xmlResponseBuilder) {
        fileResponseBuilders = Map.of(
                "csv", csvResponseBuilder,
                "json", jsonResponseBuilder,
                "xml", xmlResponseBuilder
        );
    }

    public ResponseEntity<Resource> buildResponse(String fileType, List<Trade> enrichedTrades) {
        FileResponseBuilder fileResponseBuilder = fileResponseBuilders.get(fileType);
        if (fileResponseBuilder == null) {
            throw new IllegalArgumentException("Unsupported file format: " + fileType);
        }

        Resource resource = fileResponseBuilder.buildFile(enrichedTrades);
        if (!(resource instanceof ByteArrayResource byteArrayResource)) {
            throw new IllegalArgumentException(
                    "File response builder did not return ByteArrayResource.");
        }
        return fileResponseBuilder.buildFileResponse(byteArrayResource);
    }
}
