package com.niyiment.invoice.report.strategy;

import com.niyiment.invoice.report.dto.ReportData;

import java.io.OutputStream;
import java.util.Map;

/**
 * Strategy interface for report generation
 */
public interface ReportGenerationStrategy {
    
    /**
     * Generate a report using the provided data and options
     *
     * @param data The report data
     * @param options Additional options for report generation
     * @param outputStream The output stream to write the report to
     * @return The filename of the generated report
     */
    String generateReport(ReportData data, Map<String, Object> options, OutputStream outputStream);
    
    /**
     * Get the content type for the generated report
     *
     * @return The content type as a string
     */
    String getContentType();
}