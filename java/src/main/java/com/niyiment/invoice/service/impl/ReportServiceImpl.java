package com.niyiment.invoice.service.impl;

import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.dto.InvoiceItemDto;
import com.niyiment.invoice.domain.enums.ExportFormat;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import com.niyiment.invoice.exception.BadRequestException;
import com.niyiment.invoice.exception.ReportException;
import com.niyiment.invoice.service.InvoiceService;
import com.niyiment.invoice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final InvoiceService invoiceService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CURRENT_DAY = "Current";
    private static final String ONE_THIRTY_DAYS = "1-30 days";
    private static final String THIRTY_SIXTY_DAYS = "31-60 days";
    private static final String SIXTY_NINETY_DAYS = "61-90 days";
    private static final String ABOVE_NINETY_DAYS = "90+ days";
    private static final String INVALID_FORMAT = "Invalid export format: ";
    private static final String NUMBER_FORMAT = "$%.2f";
    private static final String REPORT_DATE = "Report Date: ";
    private static final String CATEGORY = "Category";
    private static final String VALUE = "Value";

    @Override
    public void exportInvoice(String invoiceId, ExportFormat format, OutputStream outputStream) {
        InvoiceDto invoiceDto = invoiceService.getInvoiceById(invoiceId);
        List<InvoiceDto> invoices = Collections.singletonList(invoiceDto);

        switch(format) {
            case CSV:
                exportInvoicesToCSV(invoices, outputStream);
                break;
            case EXCEL:
                exportInvoicesToExcel(invoices, outputStream);
                break;
            case PDF:
                exportInvoicesToPDF(invoices, outputStream);
                break;
            default:
                throw new BadRequestException("");

        }
    }

    @Override
    public void exportInvoices(List<String> invoiceIds, ExportFormat format, OutputStream outputStream) {
        List<InvoiceDto> invoices = invoiceIds.stream()
                .map(invoiceService::getInvoiceById)
                .toList();

        switch(format) {
            case CSV:
                exportInvoicesToCSV(invoices, outputStream);
                break;
            case EXCEL:
                exportInvoicesToExcel(invoices, outputStream);
                break;
            case PDF:
                exportInvoicesToPDF(invoices, outputStream);
                break;
            default:
                throw new BadRequestException(INVALID_FORMAT + format);
        }
    }

    @Override
    public void exportInvoicesWithCriteria(String clientName, InvoiceStatus status, LocalDateTime startDate,
                                           LocalDateTime endDate, ExportFormat format, OutputStream outputStream) {
        List<InvoiceDto> invoices = invoiceService.advancedSearch(clientName,status, startDate, endDate,
                null, null);
        switch(format) {
            case CSV:
                exportInvoicesToCSV(invoices, outputStream);
                break;
            case EXCEL:
                exportInvoicesToExcel(invoices, outputStream);
                break;
            case PDF:
                exportInvoicesToPDF(invoices, outputStream);
                break;
            default:
                throw new BadRequestException(INVALID_FORMAT + format);
        }
    }

    @Override
    public Map<String, Double> generateRevenueReportByCustomer(LocalDateTime startDate, LocalDateTime endDate) {
        List<InvoiceDto> invoices = invoiceService.advancedSearch(null, InvoiceStatus.PAID, startDate,
                endDate,null,null);

        return invoices.stream()
                .collect(Collectors.groupingBy(
                        InvoiceDto::getCustomerName,
                        Collectors.summingDouble(InvoiceDto::getTotalAmount)
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> generateRevenueReportByMonth(int year) {
        LocalDateTime startDate = LocalDateTime.of(year, 1,1,0,0);
        LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        List<InvoiceDto> invoices = invoiceService.advancedSearch(null, InvoiceStatus.PAID,
                startDate, endDate, null, null);

        Map<String, Double> revenueByMonth = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            revenueByMonth.put(month.toString(), 0.0);
        }

        invoices.forEach(invoice -> {
            String month = invoice.getInvoiceDate().getMonth().toString();
            revenueByMonth.put(month, revenueByMonth.get(month) + invoice.getTotalAmount());
        });


        return revenueByMonth;
    }

    @Override
    public Map<InvoiceStatus, Long> generateInvoicesByStatusReport() {
        List<InvoiceDto> allInvoices = invoiceService.advancedSearch(null,null,null,null,null,null);

        return allInvoices.stream()
                .collect(Collectors.groupingBy(InvoiceDto::getStatus, Collectors.counting()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> generateAgingReport() {
        LocalDateTime now = LocalDateTime.now();


        List<InvoiceDto> invoices = invoiceService.advancedSearch(null,null,
                        null,null, null, null).stream()
                .filter(invoice -> invoice.getStatus() != InvoiceStatus.PAID && invoice.getStatus() != InvoiceStatus.CANCELLED)
                .toList();

        Map<String, Double> agingReport = new LinkedHashMap<>();
        agingReport.put(CURRENT_DAY, 0.0);
        agingReport.put(ONE_THIRTY_DAYS, 0.0);
        agingReport.put(THIRTY_SIXTY_DAYS, 0.0);
        agingReport.put(SIXTY_NINETY_DAYS, 0.0);
        agingReport.put(ABOVE_NINETY_DAYS, 0.0);

        invoices.forEach(invoice -> {
            long daysPastDue = ChronoUnit.DAYS.between(invoice.getDueDate(), now);

            if (daysPastDue <= 0) {
                agingReport.put(CURRENT_DAY, agingReport.get(CURRENT_DAY) + invoice.getTotalAmount());
            } else if (daysPastDue <= 30) {
                agingReport.put(ONE_THIRTY_DAYS, agingReport.get(ONE_THIRTY_DAYS) + invoice.getTotalAmount());
            } else if (daysPastDue <= 60) {
                agingReport.put(THIRTY_SIXTY_DAYS, agingReport.get(THIRTY_SIXTY_DAYS) + invoice.getTotalAmount());
            } else if (daysPastDue <= 90) {
                agingReport.put(SIXTY_NINETY_DAYS, agingReport.get(SIXTY_NINETY_DAYS) + invoice.getTotalAmount());
            } else {
                agingReport.put(ABOVE_NINETY_DAYS, agingReport.get(ABOVE_NINETY_DAYS) + invoice.getTotalAmount());
            }
        });

        return agingReport;
    }

    @Override
    public void exportReport(Map<?, ?> reportData, String reportTitle, ExportFormat format, OutputStream outputStream) {
        switch(format) {
            case CSV:
                exportReportToCSV(reportData, reportTitle, outputStream);
                break;
            case EXCEL:
                exportReportToExcel(reportData, reportTitle, outputStream);
                break;
            case PDF:
                exportReportToPDF(reportData, reportTitle, outputStream);
                break;
            default:
                throw new BadRequestException(INVALID_FORMAT + format);
        }
    }


    private void exportInvoicesToCSV(List<InvoiceDto> invoices, OutputStream outputStream) {
        try(OutputStreamWriter writer = new OutputStreamWriter(outputStream);
         CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("Invoice Number", "Customer Name", "Status", "Invoice Date",
                    "Due Date", "Subtotal", "Tax Rate", "Tax Amount", "Total Amount");
            for (InvoiceDto invoice : invoices) {
                csvPrinter.printRecord(invoice.getInvoiceNumber(), invoice.getCustomerName(),
                        invoice.getStatus().name(), formatDate(invoice.getInvoiceDate()),
                        formatDate(invoice.getDueDate()), invoice.getSubtotal(),
                        invoice.getTaxRate(), invoice.getTaxAmount(), invoice.getTotalAmount());
            }
            csvPrinter.flush();
        } catch (IOException exception) {
            throw new ReportException("Error exporting invoices to CSV", exception);
        }
    }

    private void exportInvoicesToExcel(List<InvoiceDto> invoices, OutputStream outputStream) {
        try(Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoices");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Invoice Number", "Customer Name", "Status", "Invoice Date",
                    "Due Date", "Subtotal", "Tax Rate", "Tax Amount", "Total Amount"};
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellStyle(headerStyle);
                headerCell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (InvoiceDto invoice : invoices) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(invoice.getInvoiceNumber());
                row.createCell(1).setCellValue(invoice.getCustomerName());
                row.createCell(2).setCellValue(invoice.getStatus().name());
                row.createCell(3).setCellValue(formatDate(invoice.getInvoiceDate()));
                row.createCell(4).setCellValue(formatDate(invoice.getDueDate()));
                row.createCell(5).setCellValue(invoice.getSubtotal());
                row.createCell(6).setCellValue(invoice.getTaxRate());
                row.createCell(7).setCellValue(invoice.getTaxAmount());
                row.createCell(8).setCellValue(invoice.getTotalAmount());
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new ReportException("Error exporting invoices to Excel", e);
        }
    }

    private void exportInvoicesToPDF(List<InvoiceDto> invoices, OutputStream outputStream) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font bodyFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    12);

            for (InvoiceDto invoice : invoices) {
                Paragraph title = new Paragraph("Invoices: " + invoice.getInvoiceNumber(), titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(Chunk.NEWLINE);

                document.add(new Paragraph("Invoice Date: " + formatDate(invoice.getInvoiceDate()), bodyFont));
                document.add(new Paragraph("Due Date: " + formatDate(invoice.getDueDate()), bodyFont));
                document.add(new Paragraph("Status: " + invoice.getStatus(), bodyFont));
                document.add(new Paragraph("Customer Name: " + invoice.getCustomerName(), bodyFont));
                document.add(new Paragraph("Email: " + invoice.getCustomerEmail(), bodyFont));
                document.add(new Paragraph("Address: " + invoice.getCustomerAddress(), bodyFont));
                document.add(Chunk.NEWLINE);

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[] {4, 2, 2, 2});

                Stream.of("Description", "Quantity", "Unit Price", "Amount")
                        .forEach(columnTitle -> {
                            PdfPCell headerCell = new PdfPCell();
                            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                            headerCell.setBorderWidth(2);
                            headerCell.setPhrase(new Phrase(columnTitle, headerFont));
                            table.addCell(headerCell);
                        });

                for(InvoiceItemDto item : invoice.getItems()) {
                    table.addCell(new Phrase(item.getDescription(), bodyFont));
                    table.addCell(new Phrase(String.valueOf(item.getQuantity()), bodyFont));
                    table.addCell(new Phrase(String.format(NUMBER_FORMAT, item.getUnitPrice()), bodyFont));
                    table.addCell(new Phrase(String.format(NUMBER_FORMAT, item.getAmount()), bodyFont));
                }

                document.add(table);
                document.add(Chunk.NEWLINE);

                Paragraph subtotal = new Paragraph("Subtotal: $" + String.format("%.2f", invoice.getSubtotal()), bodyFont);
                subtotal.setAlignment(Element.ALIGN_RIGHT);
                document.add(subtotal);

                Paragraph tax = new Paragraph(
                        "Tax (" + String.format("%.1f", invoice.getTaxRate()) + "%): $" +
                                String.format("%.2f", invoice.getTaxAmount()), bodyFont);
                tax.setAlignment(Element.ALIGN_RIGHT);
                document.add(tax);

                Paragraph total = new Paragraph("Total: $" + String.format("%.2f", invoice.getTotalAmount()), headerFont);
                total.setAlignment(Element.ALIGN_RIGHT);
                document.add(total);

                if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("Notes:", headerFont));
                    document.add(new Paragraph(invoice.getNotes(), bodyFont));
                }

                if (invoices.indexOf(invoice) < invoices.size() - 1) {
                    document.newPage();
                }
            }

            document.close();

        } catch (DocumentException exception) {
            throw new ReportException("Error exporting invoices to PDF", exception);
        }
    }

    private void exportReportToCSV(Map<?, ?> reportData, String reportTitle, OutputStream outputStream) {
        try (
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            csvPrinter.printRecord(CATEGORY, VALUE);
            csvPrinter.printComment(reportTitle);
            csvPrinter.printComment(REPORT_DATE + formatDate(LocalDateTime.now()));

            for (Map.Entry<?, ?> entry : reportData.entrySet()) {
                csvPrinter.printRecord(entry.getKey().toString(), entry.getValue().toString());
            }

            csvPrinter.flush();
        } catch (IOException e) {
            throw new ReportException("Error exporting report to CSV", e);
        }
    }

    private void exportReportToExcel(Map<?, ?> reportData, String reportTitle, OutputStream outputStream) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Report");

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue(REPORT_DATE + formatDate(LocalDateTime.now()));

            Row headerRow = sheet.createRow(3);
            String[] headers = {CATEGORY, VALUE};

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 4;
            for (Map.Entry<?, ?> entry : reportData.entrySet()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(entry.getKey().toString());

                Cell valueCell = row.createCell(1);
                if (entry.getValue() instanceof Double) {
                    valueCell.setCellValue((Double) entry.getValue());

                    CellStyle currencyStyle = workbook.createCellStyle();
                    DataFormat format = workbook.createDataFormat();
                    currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));
                    valueCell.setCellStyle(currencyStyle);
                } else if (entry.getValue() instanceof Number) {
                    valueCell.setCellValue(((Number) entry.getValue()).doubleValue());
                } else {
                    valueCell.setCellValue(entry.getValue().toString());
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        } catch (IOException e) {
            throw new ReportException("Error exporting report to Excel", e);
        }
    }

    private void exportReportToPDF(Map<?, ?> reportData, String reportTitle, OutputStream outputStream) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    18,com.itextpdf.text. Font.BOLD);
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    12, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA,
                    10, com.itextpdf.text.Font.NORMAL);

            Paragraph title = new Paragraph(reportTitle, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Paragraph reportDate = new Paragraph(REPORT_DATE + formatDate(LocalDateTime.now()), normalFont);
            reportDate.setAlignment(Element.ALIGN_RIGHT);
            document.add(reportDate);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 2});

            Stream.of(CATEGORY, VALUE)
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(2);
                        header.setPhrase(new Phrase(columnTitle, headerFont));
                        table.addCell(header);
                    });

            reportData.forEach((key, value) -> {
                table.addCell(new Phrase(key.toString(), normalFont));

                // Format value based on type
                String formattedValue;
                if (value instanceof Double) {
                    formattedValue = String.format(NUMBER_FORMAT, value);
                } else if (value instanceof Number) {
                    formattedValue = value.toString();
                } else {
                    formattedValue = value.toString();
                }

                table.addCell(new Phrase(formattedValue, normalFont));
            });

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new ReportException("Error exporting report to PDF", e);
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
    }
}
