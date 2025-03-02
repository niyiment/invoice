package com.niyiment.invoice.report.service;

import com.niyiment.invoice.report.dto.ReportRequest;
import com.niyiment.invoice.report.dto.ReportResponse;
import com.niyiment.invoice.report.strategy.*;
import com.niyiment.invoice.utility.report.strategy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportGeneratorService {

    private final Map<String, ReportGenerationStrategy> strategies = new HashMap<>();

    @Autowired
    public ReportGeneratorService(
            ExcelReportGenerationStrategy excelStrategy,
            CsvReportGenerationStrategy csvStrategy,
            PdfReportGenerationStrategy pdfStrategy,
            JsonReportGenerationStrategy jsonStrategy) {
        
        strategies.put("excel", excelStrategy);
        strategies.put("csv", csvStrategy);
        strategies.put("pdf", pdfStrategy);
        strategies.put("json", jsonStrategy);
    }

    /**
     * Generates a report based on the provided request
     *
     * @param request The report request containing data and format specifications
     * @return ReportResponse containing the generated report as byte array and metadata
     * @throws IllegalArgumentException if the format is not supported
     */
    public ReportResponse generateReport(ReportRequest request) {
        String format = request.getFormat().toLowerCase();
        
        ReportGenerationStrategy strategy = strategies.get(format);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported report format: " + format);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileName = strategy.generateReport(request.getData(), request.getOptions(), outputStream);
        
        return ReportResponse.builder()
                .reportBytes(outputStream.toByteArray())
                .fileName(fileName)
                .contentType(strategy.getContentType())
                .build();
    }
}