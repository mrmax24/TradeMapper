package app.trademapper.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.trademapper.model.Trade;
import app.trademapper.service.TradeService;
import app.trademapper.service.impl.response.FileResponseService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TradeControllerTest {

    @Mock
    private TradeService tradeService;

    @Mock
    private FileResponseService fileResponseService;

    @InjectMocks
    private TradeController tradeController;

    private MultipartFile mockFile;

    @BeforeEach
    public void setUp() {
        mockFile = mock(MultipartFile.class);
    }

    @Test
    public void testEnrichTradesSuccess() throws Exception {
        List<Trade> enrichedTrades = Arrays.asList(new Trade(), new Trade());
        when(tradeService.getEnrichedTrades(mockFile)).thenReturn(enrichedTrades);
        when(mockFile.getOriginalFilename()).thenReturn("trades.csv");

        Resource mockResource = mock(Resource.class);
        when(fileResponseService.buildResponse(
                anyString(), eq(enrichedTrades))).thenReturn(
                        new ResponseEntity<>(mockResource, HttpStatus.OK));

        ResponseEntity<?> response = tradeController.enrichTrades(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(fileResponseService, times(1))
                .buildResponse(anyString(), eq(enrichedTrades));
    }

    @Test
    public void testEnrichTradesEmptyFile() {
        when(mockFile.isEmpty()).thenReturn(true);
        when(mockFile.getOriginalFilename()).thenReturn("");

        ResponseEntity<?> response = tradeController.enrichTrades(mockFile);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File is empty. Please upload a valid file.", response.getBody());
    }

    @Test
    public void testEnrichTradesNoValidTrades() {
        when(tradeService.getEnrichedTrades(mockFile)).thenReturn(List.of());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            tradeController.enrichTrades(mockFile);
        });

        assertEquals("Error processing trade data", exception.getMessage());
    }
}
