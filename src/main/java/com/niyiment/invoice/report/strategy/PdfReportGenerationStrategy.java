package com.niyiment.invoice.report.strategy;

import com.niyiment.invoice.report.dto.ReportData;

import java.io.OutputStream;
import java.util.Map;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.niyiment.invoice.utility.ReportHelper;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
public class PdfReportGenerationStrategy implements ReportGenerationStrategy {

    @Override
    public String generateReport(ReportData data, Map<String, Object> options, OutputStream outputStream) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 60, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.addTitle(data.getTitle() != null ? data.getTitle() : "Report");
            document.addCreationDate();

            if (options != null) {
                if (options.containsKey("author")) {
                    document.addAuthor((String) options.get("author"));
                }
                if (options.containsKey("subject")) {
                    document.addSubject((String) options.get("subject"));
                }
                if (options.containsKey("keywords")) {
                    document.addKeywords((String) options.get("keywords"));
                }
            }

            if (data.getTitle() != null && !data.getTitle().isEmpty()) {
                Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
                Paragraph title = new Paragraph(data.getTitle(), titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(10);
                document.add(title);
            }

            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                Font descFont = new Font(Font.HELVETICA, 12);
                Paragraph desc = new Paragraph(data.getDescription(), descFont);
                desc.setSpacingAfter(15);
                document.add(desc);
            }

            List<String> headers = data.getHeaders();
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(220, 220, 220));
                cell.setPadding(5);
                table.addCell(cell);
            }

            Font dataFont = new Font(Font.HELVETICA, 10);
            for (List<Object> row : data.getRows()) {
                for (Object value : row) {
                    String cellValue = value != null ? value.toString() : "";
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, dataFont));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            document.add(table);

            Paragraph footer = new Paragraph("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    new Font(Font.HELVETICA, 8));
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();

            return ReportHelper.generateFileName(data, options, "pdf");

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

}

