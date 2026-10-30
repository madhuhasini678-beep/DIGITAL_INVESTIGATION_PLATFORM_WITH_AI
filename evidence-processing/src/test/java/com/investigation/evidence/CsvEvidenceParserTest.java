package com.investigation.evidence;

import com.investigation.evidence.model.ParsedEvidenceResult;
import com.investigation.evidence.parser.CsvEvidenceParser;
import com.investigation.evidence.util.CryptoUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class CsvEvidenceParserTest {

    private final CsvEvidenceParser parser = new CsvEvidenceParser();

    @Test
    public void testCsvParsingAndTimestampDetection() throws IOException {
        String csv = "Id,Date,Entity,Amount,Note\n" +
                "1,1911-08-21 07:15:00,Peruggia,0,\"Salon Carré, Louvre\"\n" +
                "2,1911-08-21 07:30:00,Louvre Guard,0,\"Door unlocked\"\n";

        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        String hash = CryptoUtil.computeSha256(bytes);
        assertNotNull(hash);
        assertEquals(64, hash.length());

        ParsedEvidenceResult result = parser.parse(new ByteArrayInputStream(bytes));
        assertEquals(5, result.getHeaders().size());
        assertEquals("Date", result.getDetectedTimestampColumn());
        assertEquals(2, result.getRowCount());
        assertNotNull(result.getRows().get(0).getParsedTimestamp());
        assertEquals("Salon Carré, Louvre", result.getRows().get(0).getValue("Note"));
    }
}
