package com.niyiment.invoice.report.strategy;

import com.niyiment.invoice.report.dto.ReportData;

import java.io.OutputStream;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.niyiment.invoice.utility.ReportHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



@Component
public class JsonReportGenerationStrategy implements ReportGenerationStrategy {

    @Override
    public String generateReport(ReportData data, Map<String, Object> options, OutputStream outputStream) {
        try {
            Map<String, Object> jsonReport = new HashMap<>();

            jsonReport.put("title", data.getTitle());
            jsonReport.put("description", data.getDescription());
            jsonReport.put("generated", LocalDateTime.now().toString());

            List<Map<String, Object>> jsonRows = new ArrayList<>();
            List<String> headers = data.getHeaders();

            for (List<Object> row : data.getRows()) {
                Map<String, Object> jsonRow = new HashMap<>();
                for (int i = 0; i < headers.size() && i < row.size(); i++) {
                    jsonRow.put(headers.get(i), row.get(i));
                }
                jsonRows.add(jsonRow);
            }

            jsonReport.put("data", jsonRows);

            if (options != null && !options.isEmpty()) {
                jsonReport.put("options", options);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            if (options != null && Boolean.TRUE.equals(options.get("prettyPrint"))) {
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            }

            objectMapper.writeValue(outputStream, jsonReport);

            return ReportHelper.generateFileName(data, options, "json");
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate JSON report", e);
        }
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

}

