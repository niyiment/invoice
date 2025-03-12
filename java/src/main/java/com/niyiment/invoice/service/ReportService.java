package com.niyiment.invoice.service;

import com.niyiment.invoice.domain.enums.ExportFormat;
import com.niyiment.invoice.domain.enums.InvoiceStatus;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReportService {
    void exportInvoice(String invoiceId, ExportFormat format, OutputStream outputStream);
    void exportInvoices(List<String> invoiceIds, ExportFormat format, OutputStream outputStream);
    void exportInvoicesWithCriteria(String clientName, InvoiceStatus status,
                                    LocalDateTime startDate, LocalDateTime endDate,
                                    ExportFormat format, OutputStream outputStream);
    Map<String, Double> generateRevenueReportByCustomer(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Double> generateRevenueReportByMonth(int year);
    Map<InvoiceStatus, Long> generateInvoicesByStatusReport();
    Map<String, Double> generateAgingReport();
    void exportReport(Map<?, ?> reportData, String reportTitle, ExportFormat format, OutputStream outputStream);
}
