package app.trademapper.service;

import app.trademapper.model.Trade;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface FileResponseBuilder {
    Resource buildFile(List<Trade> trades);

    ResponseEntity<Resource> buildFileResponse(ByteArrayResource resource);
}
