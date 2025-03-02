package com.niyiment.invoice.utility;

import com.niyiment.invoice.report.dto.ReportData;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;


@Component
public class ReportHelper {

    public static String generateFileName(ReportData data, Map<String, Object> options, String extension) {
        if (options != null && options.containsKey("fileName")) {
            String fileName = (String) options.get("fileName");
            if (!fileName.toLowerCase().endsWith("." + extension)) {
                fileName += "." + extension;
            }
            return fileName;
        }

        String baseName = data.getTitle() != null && !data.getTitle().isEmpty()
                ? data.getTitle().replaceAll("[^a-zA-Z0-9]", "_")
                : "report";

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        return baseName + "_" + timestamp + "." + extension;
    }
}
