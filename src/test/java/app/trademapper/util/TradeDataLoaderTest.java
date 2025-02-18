package app.trademapper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.trademapper.model.Trade;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class TradeDataLoaderTest {

    @Mock
    private CsvLoader csvLoader;
    @Mock
    private JsonLoader jsonLoader;
    @Mock
    private XmlLoader xmlLoader;
    @Mock
    private MultipartFile mockFile;

    private TradeDataLoader tradeDataLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tradeDataLoader = new TradeDataLoader(csvLoader, jsonLoader, xmlLoader);
    }

    @Test
    void testLoadCsvFile() {
        List<Trade> expectedTrades = Arrays.asList(new Trade(), new Trade());
        when(mockFile.getOriginalFilename()).thenReturn("data.csv");
        when(csvLoader.loadCsv(mockFile)).thenReturn(expectedTrades);

        List<Trade> result = tradeDataLoader.loadTradesFromFile(mockFile);

        assertEquals(expectedTrades, result);
        verify(csvLoader, times(1)).loadCsv(mockFile);
        verify(jsonLoader, times(0)).loadJson(mockFile);
        verify(xmlLoader, times(0)).loadXml(mockFile);
    }

    @Test
    void testLoadJsonFile() {
        List<Trade> expectedTrades = Arrays.asList(new Trade(), new Trade());
        when(mockFile.getOriginalFilename()).thenReturn("data.json");
        when(jsonLoader.loadJson(mockFile)).thenReturn(expectedTrades);

        List<Trade> result = tradeDataLoader.loadTradesFromFile(mockFile);

        assertEquals(expectedTrades, result);
        verify(csvLoader, times(0)).loadCsv(mockFile);
        verify(jsonLoader, times(1)).loadJson(mockFile);
        verify(xmlLoader, times(0)).loadXml(mockFile);
    }

    @Test
    void testLoadXmlFile() {
        List<Trade> expectedTrades = Arrays.asList(new Trade(), new Trade());
        when(mockFile.getOriginalFilename()).thenReturn("data.xml");
        when(xmlLoader.loadXml(mockFile)).thenReturn(expectedTrades);

        List<Trade> result = tradeDataLoader.loadTradesFromFile(mockFile);

        assertEquals(expectedTrades, result);
        verify(csvLoader, times(0)).loadCsv(mockFile);
        verify(jsonLoader, times(0)).loadJson(mockFile);
        verify(xmlLoader, times(1)).loadXml(mockFile);
    }

    @Test
    void testLoadFileWithUnsupportedExtension() {
        when(mockFile.getOriginalFilename()).thenReturn("data.txt");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeDataLoader.loadTradesFromFile(mockFile);
        });

        assertEquals("Unsupported file format: data.txt", exception.getMessage());
    }

    @Test
    void testLoadFileWithNullName() {
        when(mockFile.getOriginalFilename()).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeDataLoader.loadTradesFromFile(mockFile);
        });

        assertEquals("File name is empty", exception.getMessage());
    }
}
