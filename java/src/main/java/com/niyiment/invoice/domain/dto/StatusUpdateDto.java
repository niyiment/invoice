package com.niyiment.invoice.domain.dto;

import com.niyiment.invoice.domain.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateDto {
    @NotNull(message = "Status is required")
    private InvoiceStatus status;
}
