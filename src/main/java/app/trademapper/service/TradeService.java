package app.trademapper.service;

import app.trademapper.model.Trade;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface TradeService {
    Mono<List<Trade>> enrichTradeData(MultipartFile file);

    List<Trade> getEnrichedTrades(MultipartFile file);
}
