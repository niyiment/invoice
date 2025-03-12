package com.niyiment.invoice.controller;

import com.niyiment.invoice.domain.enums.ExportFormat;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import com.niyiment.invoice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;


/**
 * REST controller for reporting and exporting functionality.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Reporting and Export API")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    
    
    @GetMapping("/invoice/{id}/export")
    @Operation(summary = "Export a single invoice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export successful"),
        @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<StreamingResponseBody> exportInvoice(
            @Parameter(description = "Invoice ID", required = true)
            @PathVariable String id,
            @Parameter(description = "Export format (PDF, CSV, EXCEL)", required = true)
            @RequestParam ExportFormat format) {
        
        StreamingResponseBody responseBody = outputStream -> {
            reportService.exportInvoice(id, format, outputStream);
        };
        
        return ResponseEntity.ok()
                .headers(getExportHeaders("invoice_" + id, format))
                .contentType(getMediaType(format))
                .body(responseBody);
    }
    
    @PostMapping("/invoices/export")
    @Operation(summary = "Export multiple invoices")
    @ApiResponse(responseCode = "200", description = "Export successful")
    public ResponseEntity<StreamingResponseBody> exportInvoices(
            @Parameter(description = "List of invoice IDs", required = true)
            @RequestBody List<String> invoiceIds,
            @Parameter(description = "Export format (PDF, CSV, EXCEL)", required = true)
            @RequestParam ExportFormat format) {
        
        StreamingResponseBody responseBody = outputStream -> {
            reportService.exportInvoices(invoiceIds, format, outputStream);
        };
        
        return ResponseEntity.ok()
                .headers(getExportHeaders("invoices", format))
                .contentType(getMediaType(format))
                .body(responseBody);
    }
    
    @GetMapping("/invoices/export-by-criteria")
    @Operation(summary = "Export invoices based on search criteria")
    @ApiResponse(responseCode = "200", description = "Export successful")
    public ResponseEntity<StreamingResponseBody> exportInvoicesByCriteria(
            @Parameter(description = "Customer name filter")
            @RequestParam(required = false) String customerName,
            @Parameter(description = "Status filter")
            @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "Export format (PDF, CSV, EXCEL)", required = true)
            @RequestParam ExportFormat format) {
        
        // Convert LocalDate to LocalDateTime (start of day for start date, end of day for end date)
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        
        StreamingResponseBody responseBody = outputStream -> {
            reportService.exportInvoicesWithCriteria(customerName, status, startDateTime, endDateTime, format, outputStream);
        };
        
        return ResponseEntity.ok()
                .headers(getExportHeaders("invoices_report", format))
                .contentType(getMediaType(format))
                .body(responseBody);
    }
    
    @GetMapping("/revenue/by-customer")
    @Operation(summary = "Generate revenue report by customer")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<Map<String, Double>> getRevenueReportByCustomer(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        // Convert LocalDate to LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        Map<String, Double> report = reportService.generateRevenueReportByCustomer(startDateTime, endDateTime);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/revenue/by-customer/export")
    @Operation(summary = "Export revenue report by customer")
    @ApiResponse(responseCode = "200", description = "Report exported successfully")
    public ResponseEntity<StreamingResponseBody> exportRevenueReportByCustomer(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "Export format (PDF, CSV, EXCEL)", required = true)
            @RequestParam ExportFormat format) {
        
        // Convert LocalDate to LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        StreamingResponseBody responseBody = outputStream -> {
            Map<String, Double> report = reportService.generateRevenueReportByCustomer(startDateTime, endDateTime);
            String title = "Revenue Report by Customer (" + startDate + " to " + endDate + ")";
            reportService.exportReport(report, title, format, outputStream);
        };
        
        return ResponseEntity.ok()
                .headers(getExportHeaders("revenue_by_customer", format))
                .contentType(getMediaType(format))
                .body(responseBody);
    }
    
    @GetMapping("/revenue/by-month")
    @Operation(summary = "Generate revenue report by month")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<Map<String, Double>> getRevenueReportByMonth(
            @Parameter(description = "Year", required = true)
            @RequestParam int year) {
        
        Map<String, Double> report = reportService.generateRevenueReportByMonth(year);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/revenue/by-month/export")
    @Operation(summary = "Export revenue report by month")
    @ApiResponse(responseCode = "200", description = "Report exported successfully")
    public ResponseEntity<StreamingResponseBody> exportRevenueReportByMonth(
            @Parameter(description = "Year", required = true)
            @RequestParam int year,
            @Parameter(description = "Export format (PDF, CSV, EXCEL)", required = true)
            @RequestParam ExportFormat format) {
        
        StreamingResponseBody responseBody = outputStream -> {
            Map<String, Double> report = reportService.generateRevenueReportByMonth(year);
            String title = "Revenue Report by Month (" + year + ")";
            reportService.exportReport(report, title, format, outputStream);
        };
        
        return ResponseEntity.ok()
                .headers(getExportHeaders("revenue_by_month_" + year, format))
                .contentType(getMediaType(format))
                .body(responseBody);
    }
    
    @GetMapping("/aging")
    @Operation(summary = "Generate aging report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<Map<String, Double>> getAgingReport() {
        Map<String, Double> report = reportService.generateAgingReport();
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/aging/export")
    @Operation(summary = "Export aging report")
    @ApiResponse(responseCode = "200", description = "Report exported successfully")
    public ResponseEntity<StreamingResponseBody> exportAgingReport(
            @Parameter(description = "Export format (PDF, CSV, EXCEL)", required = true)
            @RequestParam ExportFormat format) {

        StreamingResponseBody responseBody = outputStream -> {
            Map<String, Double> report = reportService.generateAgingReport();
            String title = "Accounts Receivable Aging Report";
            reportService.exportReport(report, title, format, outputStream);
        };

        return ResponseEntity.ok()
                .headers(getExportHeaders("aging_report", format))
                .contentType(getMediaType(format))
                .body(responseBody);
    }
    
    @GetMapping("/status")
    @Operation(summary = "Generate invoices by status report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<Map<InvoiceStatus, Long>> getInvoicesByStatusReport() {
        Map<InvoiceStatus, Long> report = reportService.generateInvoicesByStatusReport();
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/status/export")
    @Operation(summary = "Export invoices by status report")
    @ApiResponse(responseCode = "200", description = "Report exported successfully")
    public ResponseEntity<StreamingResponseBody> exportInvoicesByStatusReport(
            @Parameter(description = "Export format (PDF, CSV, EXCEL)", required = true)
            @RequestParam ExportFormat format) {
        
        StreamingResponseBody responseBody = outputStream -> {
            Map<InvoiceStatus, Long> report = reportService.generateInvoicesByStatusReport();
            String title = "Invoices by Status Report";
            reportService.exportReport(report, title, format, outputStream);
        };
        
        return ResponseEntity.ok()
                .headers(getExportHeaders("invoices_by_status", format))
                .contentType(getMediaType(format))
                .body(responseBody);
    }
    
    /**
     * Helper method to get HTTP headers for export files.
     *
     * @param baseFilename The base filename without extension
     * @param format The export format
     * @return HTTP headers for the response
     */
    private HttpHeaders getExportHeaders(String baseFilename, ExportFormat format) {
        HttpHeaders headers = new HttpHeaders();
        
        String filename = baseFilename + getFileExtension(format);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        
        return headers;
    }
    
    /**
     * Helper method to get the MediaType for a given export format.
     *
     * @param format The export format
     * @return The corresponding MediaType
     */
    private MediaType getMediaType(ExportFormat format) {
        switch (format) {
            case PDF:
                return MediaType.APPLICATION_PDF;
            case CSV:
                return new MediaType("text", "csv");
            case EXCEL:
                return new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
    
    /**
     * Helper method to get the file extension for a given export format.
     *
     * @param format The export format
     * @return The corresponding file extension
     */
    private String getFileExtension(ExportFormat format) {
        switch (format) {
            case PDF:
                return ".pdf";
            case CSV:
                return ".csv";
            case EXCEL:
                return ".xlsx";
            default:
                return "";
        }
    }
}