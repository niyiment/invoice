package com.niyiment.invoice.report.strategy;

import com.niyiment.invoice.report.dto.ReportData;

import java.io.OutputStream;
import java.util.Map;

import com.niyiment.invoice.utility.ReportHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExcelReportGenerationStrategy implements ReportGenerationStrategy {

    @Override
    public String generateReport(ReportData data, Map<String, Object> options, OutputStream outputStream) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(getSheetName(data, options));
            CellStyle headerStyle = createHeaderStyle(workbook);
            int rowNum = 0;
            if (data.getTitle() != null && !data.getTitle().isEmpty()) {
                Row titleRow = sheet.createRow(rowNum++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(data.getTitle());
                CellStyle titleStyle = workbook.createCellStyle();
                Font titleFont = workbook.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);
            }

            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                Row descRow = sheet.createRow(rowNum++);
                Cell descCell = descRow.createCell(0);
                descCell.setCellValue(data.getDescription());
                rowNum++;
            }

            Row headerRow = sheet.createRow(rowNum++);
            List<String> headers = data.getHeaders();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            List<List<Object>> rows = data.getRows();
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    setCellValue(cell, rowData.get(i));
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

            return ReportHelper.generateFileName(data, options, "xlsx");
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        return headerStyle;
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private String getSheetName(ReportData data, Map<String, Object> options) {
        if (options != null && options.containsKey("sheetName")) {
            return (String) options.get("sheetName");
        } else if (data.getTitle() != null && !data.getTitle().isEmpty()) {
            return data.getTitle().length() > 20 ? data.getTitle().substring(0, 20) : data.getTitle();
        } else {
            return "Report";
        }
    }
}