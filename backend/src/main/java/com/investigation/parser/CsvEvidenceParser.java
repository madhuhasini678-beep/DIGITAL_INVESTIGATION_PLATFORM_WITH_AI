package com.investigation.parser;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class CsvEvidenceParser implements EvidenceParser<ParsedEvidenceResult> {

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    private static final List<String> TIMESTAMP_COLUMN_CANDIDATES = List.of(
            "timestamp", "datetime", "date_time", "event_time", "time", "date",
            "transaction_date", "incident_date", "logged_at", "created_at"
    );

    @Override
    public ParsedEvidenceResult parse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String headerLine = reader.readLine();
        if (headerLine == null || headerLine.trim().isEmpty()) {
            return new ParsedEvidenceResult(Collections.emptyList(), Collections.emptyList(), null, List.of("Empty CSV file"));
        }

        List<String> headers = parseCsvLine(headerLine);
        String detectedTimestampColumn = detectTimestampColumn(headers);

        List<EvidenceRow> rows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String line;
        int rowNum = 1;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            List<String> values = parseCsvLine(line);
            Map<String, String> rowMap = new LinkedHashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                String val = (i < values.size()) ? values.get(i) : "";
                rowMap.put(header, val);
            }

            Instant parsedTimestamp = null;
            if (detectedTimestampColumn != null) {
                String rawTime = rowMap.get(detectedTimestampColumn);
                parsedTimestamp = tryParseTimestamp(rawTime);
                if (parsedTimestamp == null && rawTime != null && !rawTime.trim().isEmpty()) {
                    if (warnings.size() < 10) {
                        warnings.add("Row " + rowNum + ": could not parse timestamp value '" + rawTime + "'");
                    }
                }
            }

            rows.add(new EvidenceRow(rowNum, rowMap, parsedTimestamp));
            rowNum++;
        }

        return new ParsedEvidenceResult(headers, rows, detectedTimestampColumn, warnings);
    }

    private String detectTimestampColumn(List<String> headers) {
        for (String candidate : TIMESTAMP_COLUMN_CANDIDATES) {
            for (String header : headers) {
                String normalized = header.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
                if (normalized.equals(candidate) || normalized.contains(candidate)) {
                    return header;
                }
            }
        }
        return null;
    }

    public Instant tryParseTimestamp(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String clean = raw.trim();

        // Try instant parse first
        try {
            return Instant.parse(clean);
        } catch (DateTimeParseException ignored) {}

        // Try standard formatters
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                // Try as ZonedDateTime
                try {
                    return ZonedDateTime.parse(clean, formatter).toInstant();
                } catch (DateTimeParseException ignored) {}

                // Try as OffsetDateTime
                try {
                    return OffsetDateTime.parse(clean, formatter).toInstant();
                } catch (DateTimeParseException ignored) {}

                // Try as LocalDateTime (assume UTC)
                try {
                    LocalDateTime ldt = LocalDateTime.parse(clean, formatter);
                    return ldt.toInstant(ZoneOffset.UTC);
                } catch (DateTimeParseException ignored) {}

                // Try as LocalDate (start of day UTC)
                try {
                    LocalDate ld = LocalDate.parse(clean, formatter);
                    return ld.atStartOfDay(ZoneOffset.UTC).toInstant();
                } catch (DateTimeParseException ignored) {}

            } catch (Exception ignored) {}
        }

        return null;
    }

    /**
     * Parses a single CSV line respecting quotes and escaped quotes ("").
     */
    public List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }
}
