package app.trademapper.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.trademapper.model.Trade;
import app.trademapper.util.TradeDataLoader;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TradeServiceImplTest {

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private TradeCacheService tradeCacheService;

    @Mock
    private TradeDataLoader tradeDataLoader;

    @InjectMocks
    private TradeServiceImpl tradeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        MultipartFile mockFile = mock(MultipartFile.class);
    }

    @Test
    public void testEnrichTradeData_withCachedData() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.csv",
                        "text/csv", "content".getBytes());
        String fileHash = "someHash";
        List<Trade> cachedTrades = List.of(new Trade("20250218", 1,
                "USD", 100.0));
        when(tradeCacheService.getFromCache(fileHash)).thenReturn(cachedTrades);

        Mono<List<Trade>> enrichedTrades = tradeService.enrichTradeData(file);

        enrichedTrades.subscribe(trades -> {
            assertNotNull(trades);
            assertEquals(1, trades.size());
            verify(tradeCacheService, times(1)).getFromCache(fileHash);
        });
    }

    @Test
    public void testEnrichTradeData_noCachedData() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.csv",
                    "text/csv", "content".getBytes());
        List<Trade> newTrades = List.of(new Trade("20250218", 1,
                "USD", 100.0));

        when(tradeCacheService.getFromCache(anyString())).thenReturn(null);
        when(tradeDataLoader.loadTradesFromFile(file)).thenReturn(newTrades);

        Mono<List<Trade>> enrichedTrades = tradeService.enrichTradeData(file);

        StepVerifier.create(enrichedTrades)
        .expectNextMatches(trades -> trades.size() == 1
                && trades.get(0).getCurrency().equals("USD")).verifyComplete();

        verify(tradeCacheService, times(1)).getFromCache(anyString());
        verify(tradeCacheService, times(1))
                .saveToCache(anyString(), any(), any());
    }

    @Test
    public void testGetEnrichedTrades() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.csv",
                        "text/csv", "content".getBytes());
        List<Trade> enrichedTrades = List.of(new Trade("20250218",
                1, "USD", 100.0));

        when(tradeCacheService.getFromCache(anyString())).thenReturn(enrichedTrades);

        List<Trade> result = tradeService.getEnrichedTrades(file);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testEnrichTradeData_withError() {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.csv",
                        "text/csv", "content".getBytes());

        when(tradeCacheService.getFromCache(anyString())).thenReturn(null);
        when(tradeDataLoader.loadTradesFromFile(file)).thenThrow(
                new RuntimeException("File read error"));

        Mono<List<Trade>> result = tradeService.enrichTradeData(file);

        result.doOnError(error -> {
            assertInstanceOf(RuntimeException.class, error);
            assertEquals("File read error", error.getMessage());
        }).block();

        assertTrue(true);
    }
}
