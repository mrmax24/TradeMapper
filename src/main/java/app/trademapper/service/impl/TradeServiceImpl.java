package app.trademapper.service.impl;

import app.trademapper.model.Trade;
import app.trademapper.service.TradeService;
import app.trademapper.util.FileHashUtil;
import app.trademapper.util.TradeDataLoader;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {
    private final ProductCacheService productCache;
    private final TradeCacheService tradeCache;
    private final TradeDataLoader tradeDataLoader;

    @Override
    public Mono<List<Trade>> enrichTradeData(MultipartFile file) {
        String fileHash = FileHashUtil.calculateFileHash(file);

        List<Trade> cachedResult = tradeCache.getFromCache(fileHash);
        if (cachedResult != null && !cachedResult.isEmpty()) {
            return Mono.just(cachedResult);
        }

        return Mono.fromCallable(() -> tradeDataLoader.loadTradesFromFile(file))
                   .flatMap(this::enrichTradesAsync)
                   .doOnSuccess(enrichedTrades -> {
                       tradeCache.saveToCache(fileHash, enrichedTrades, Duration.ofMinutes(5));
                   })
                   .onErrorReturn(List.of());
    }

    @Override
    public List<Trade> getEnrichedTrades(MultipartFile file) {
        List<Trade> trades = enrichTradeData(file).block();
        if (trades == null || trades.isEmpty()) {
            throw new IllegalArgumentException("No valid trades found in file");
        }
        return trades;
    }

    private Mono<List<Trade>> enrichTradesAsync(List<Trade> trades) {
        List<Mono<Trade>> enrichedTrades = trades.stream()
                                                 .map(this::enrichTradeAsync)
                                                 .collect(Collectors.toList());
        return Mono.zip(enrichedTrades, result ->
                result.length == trades.size() ? trades : List.of());
    }

    Mono<Trade> enrichTradeAsync(Trade trade) {
        return Mono.fromCallable(() -> {
            enrichTrade(trade);
            return trade;
        }).subscribeOn(Schedulers.parallel());
    }

    void enrichTrade(Trade trade) {
        String productName = productCache.getProductName(trade.getProductId());
        trade.setName(productName);
    }
}
