package com.niyiment.invoice.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportData {
    private List<String> headers;
    private List<List<Object>> rows;
    private String title;
    private String description;
}
