package com.niyiment.invoice.model.dto.request;

import com.niyiment.invoice.model.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    private String invoiceNumber;
    
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
    
    @NotNull(message = "Status is required")
    private InvoiceStatus status;
    
    @NotEmpty(message = "At least one item is required")
    private List<InvoiceItemRequest> items;
    
    private String notes;
}
