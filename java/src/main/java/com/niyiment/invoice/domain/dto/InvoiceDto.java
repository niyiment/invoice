package com.niyiment.invoice.domain.dto;

import com.niyiment.invoice.domain.enums.InvoiceStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class InvoiceDto {
    private String id;

    @NotBlank(message = "Invoice number is required")
    private String invoiceNumber;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    private String customerEmail;

    private String customerAddress;

    @NotNull(message = "Invoice date is required")
    private LocalDateTime invoiceDate;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @Size(min = 1, message = "At least one item is required")
    private List<InvoiceItemDto> items = new ArrayList<>();

    private double subtotal;

    @Min(value = 0, message = "Tax rate cannot be negative")
    private double taxRate;

    private double taxAmount;
    private double totalAmount;
    private InvoiceStatus status;
    private String notes;

}
