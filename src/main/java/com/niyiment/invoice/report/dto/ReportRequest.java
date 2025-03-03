package com.niyiment.invoice.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private String format; // excel, csv, pdf, json
    private ReportData data;
    private Map<String, Object> options; // Optional parameters like title, author, etc.
}
