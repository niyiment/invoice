package com.niyiment.invoice.domain.dto;

import com.niyiment.invoice.domain.entity.InvoiceItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvoiceItemDto {
    @NotBlank(message = "Item description is required")
    private String description;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Min(value = 0, message = "Unit price cannot be negative")
    private double unitPrice;

    private double amount;

    public static InvoiceItemDto fromEntity(InvoiceItem item) {
        if (item == null) {
            return null;
        }

        InvoiceItemDto dto = new InvoiceItemDto();
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setAmount(item.getAmount());

        return dto;
    }

    public InvoiceItem toEntity() {
        return new InvoiceItem(description, quantity, unitPrice);
    }

}
