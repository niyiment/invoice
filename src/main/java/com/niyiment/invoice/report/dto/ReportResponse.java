package com.niyiment.invoice.report.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private byte[] reportBytes;
    private String fileName;
    private String contentType;
}

