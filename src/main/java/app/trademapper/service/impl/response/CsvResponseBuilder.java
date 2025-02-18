package app.trademapper.service.impl.response;

import app.trademapper.model.Trade;
import app.trademapper.service.FileResponseBuilder;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CsvResponseBuilder implements FileResponseBuilder {

    @Override
    public ByteArrayResource buildFile(List<Trade> trades) {
        StringWriter writer = new StringWriter();
        writer.append("date,productName,currency,price\n");

        for (Trade trade : trades) {
            writer.append(trade.getDate()).append(",")
                  .append(trade.getName()).append(",")
                  .append(trade.getCurrency()).append(",")
                  .append(String.valueOf(trade.getPrice())).append("\n");
        }

        return new ByteArrayResource(writer.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ResponseEntity<Resource> buildFileResponse(ByteArrayResource resource) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=trades.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(resource);
    }
}
