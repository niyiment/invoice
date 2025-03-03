package com.niyiment.invoice.report.strategy;

import com.niyiment.invoice.report.dto.ReportData;

import java.io.OutputStream;
import java.util.Map;

import com.niyiment.invoice.utility.ReportHelper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;




@Component
public class CsvReportGenerationStrategy implements ReportGenerationStrategy {

    @Override
    public String generateReport(ReportData data, Map<String, Object> options, OutputStream outputStream) {
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            String[] headers = data.getHeaders().toArray(new String[0]);
            CSVFormat format = getCSVFormat(options, headers);

            if (options != null && Boolean.TRUE.equals(options.get("includeMetadata"))) {
                if (data.getTitle() != null) {
                    writer.write("# Title: " + data.getTitle() + "\n");
                }
                if (data.getDescription() != null) {
                    writer.write("# Description: " + data.getDescription() + "\n");
                }
                writer.write("# Generated: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
            }

            try (CSVPrinter csvPrinter = new CSVPrinter(writer, format)) {
                for (var row : data.getRows()) {
                    csvPrinter.printRecord(row);
                }
            }

           return ReportHelper.generateFileName(data, options, "csv");

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    private CSVFormat getCSVFormat(Map<String, Object> options, String[] headers) {
        CSVFormat baseFormat = CSVFormat.DEFAULT;

        if (options != null) {
            if (options.containsKey("delimiter")) {
                char delimiter = ((String) options.get("delimiter")).charAt(0);
                baseFormat = baseFormat.builder().setDelimiter(delimiter).get();
            }

            if (options.containsKey("quoteChar")) {
                char quoteChar = ((String) options.get("quoteChar")).charAt(0);
                baseFormat = baseFormat.builder().setQuote(quoteChar).get();
            }
        }

        return baseFormat.builder().setHeader(headers).get();
    }

}