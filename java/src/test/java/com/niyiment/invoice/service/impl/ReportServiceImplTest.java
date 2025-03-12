package com.niyiment.invoice.service.impl;

import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.enums.ExportFormat;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import com.niyiment.invoice.domain.repository.InvoiceRepository;
import com.niyiment.invoice.service.InvoiceService;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class )
class ReportServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private InvoiceDto mockInvoice1;
    private InvoiceDto mockInvoice2;
    private List<InvoiceDto> mockInvoices;

    @BeforeEach
    void setUp() {
        mockInvoice1 = new InvoiceDto();
        mockInvoice1.setId("1");
        mockInvoice1.setCustomerName("Customer 1");
        mockInvoice1.setInvoiceDate(LocalDateTime.now().minusDays(30));
        mockInvoice1.setDueDate(LocalDateTime.now().plusDays(30));
        mockInvoice1.setStatus(InvoiceStatus.PAID);
        mockInvoice1.setTotalAmount(100.0);

        mockInvoice2 = new InvoiceDto();
        mockInvoice2.setId("2");
        mockInvoice2.setCustomerName("Customer 2");
        mockInvoice2.setInvoiceDate(LocalDateTime.now().minusDays(30));
        mockInvoice2.setDueDate(LocalDateTime.now().plusDays(20));
        mockInvoice2.setStatus(InvoiceStatus.PAID);
        mockInvoice2.setTotalAmount(150.0);
    }

    @Test
    void shouldGenerateRevenueReportByCustomer() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();

        mockInvoices = List.of(mockInvoice1, mockInvoice2);

        when(invoiceService.advancedSearch(null, InvoiceStatus.PAID, startDate, endDate, null, null))
                .thenReturn(mockInvoices);

        // When
        Map<String, Double> report = reportService.generateRevenueReportByCustomer(startDate, endDate);

        // Then
        assertEquals(2, report.size());
        assertEquals(100.0, report.get("Customer 1"));
        assertEquals(150.0, report.get("Customer 2"));
    }

    @Test
    void shouldGenerateAgingReport() {
        InvoiceDto currentInvoice = new InvoiceDto("1", "Customer A", InvoiceStatus.SENT, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusDays(10));
        InvoiceDto overdueInvoice1 = new InvoiceDto("2", "Customer B", InvoiceStatus.OVERDUE, 300.0,
                LocalDateTime.now().minusDays(40), LocalDateTime.now().minusDays(30));
        InvoiceDto overdueInvoice2 =  new InvoiceDto("3", "Customer C", InvoiceStatus.OVERDUE, 500.0,
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(60));
        InvoiceDto overdueInvoice3 =  new InvoiceDto("4", "Customer D", InvoiceStatus.OVERDUE, 400.0,
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(100));
        InvoiceDto paidInvoice =  new InvoiceDto("5", "Customer E", InvoiceStatus.PAID, 200.0,
                LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(5));

        mockInvoices = List.of(currentInvoice, overdueInvoice1, overdueInvoice2, overdueInvoice3, paidInvoice);
        when(invoiceService.advancedSearch(any(), any(), any(), any(), any(), any())).thenReturn(mockInvoices);

        Map<String, Double> report = reportService.generateAgingReport();

        assertEquals(5, report.size());
        assertEquals(100.0, report.get("Current"));
        assertEquals(300.0, report.get("1-30 days"));
        assertEquals(500.0, report.get("31-60 days"));
        assertEquals(0.0, report.get("61-90 days"));
        assertEquals(400.0, report.get("90+ days"));

    }

    @Test
    void shouldGenerateInvoiceByStatusReport() {
        InvoiceDto currentInvoice = new InvoiceDto("1", "Customer A", InvoiceStatus.SENT, 100.0,
                LocalDateTime.now(), LocalDateTime.now().plusDays(10));
        InvoiceDto currentInvoice1 = new InvoiceDto("2", "Customer B", InvoiceStatus.SENT, 300.0,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30));
        InvoiceDto overdueInvoice2 =  new InvoiceDto("3", "Customer C", InvoiceStatus.OVERDUE, 500.0,
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(100));
        InvoiceDto paidInvoice3 =  new InvoiceDto("4", "Customer E", InvoiceStatus.PAID, 200.0,
                LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(5));

        mockInvoices = List.of(currentInvoice, currentInvoice1, overdueInvoice2, paidInvoice3);

        when(invoiceService.advancedSearch(any(), any(), any(), any(), any(), any())).thenReturn(mockInvoices);
        Map<InvoiceStatus, Long> allInvoices = reportService.generateInvoicesByStatusReport();

        assertEquals(3, allInvoices.size());
        assertEquals(2, allInvoices.get(InvoiceStatus.SENT));
        assertNull(allInvoices.get(InvoiceStatus.DRAFT));
        assertEquals(1, allInvoices.get(InvoiceStatus.OVERDUE));
        assertEquals(1, allInvoices.get(InvoiceStatus.PAID));
    }

    @Test
    void shouldExportReport(){
        Map<String, Double> reportData = Map.of(
             "Category1", 200.0,
                 "Category2", 500.0,
                 "Category3", 300.0
        );
        String reportTitle = "Test Report";

        for (ExportFormat format : ExportFormat.values()) {
            OutputStream outputStream = new ByteArrayOutputStream();
            reportService.exportReport(reportData, reportTitle, format, outputStream);
            assertFalse(outputStream.toString().isEmpty(), "Export to " +
                    format + " should produce output");
        }

    }

    @Test
    void shouldExportInvoice() {
        mockInvoices = List.of(mockInvoice1, mockInvoice2);
        when(invoiceService.getInvoiceById("1")).thenReturn(mockInvoice1);
        for (ExportFormat format : ExportFormat.values()) {
            OutputStream stream = new ByteArrayOutputStream();
            reportService.exportInvoice("1", format, stream);

            assertFalse(stream.toString().isEmpty(), "Export to " +
                    format + " should produce output");
        }
    }

}