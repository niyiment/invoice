package com.niyiment.invoice.domain.entity;


import com.niyiment.invoice.domain.enums.InvoiceStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    private String id;

    @Indexed(unique = true)
    private String invoiceNumber;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private List<InvoiceItem> items = new ArrayList<>();
    private double subtotal;
    private double taxRate;
    private double taxAmount;
    private double totalAmount;
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    private String notes;
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Invoice(@NotBlank(message = "Invoice number is required") String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }


    /**
     * Calculatess the invoice subtotal by summing up all items amount
     * @return the calculated subtotal
     */
    public double calculateSubtotal() {
        subtotal = items.stream()
                .mapToDouble(InvoiceItem::getAmount)
                .sum();
        return subtotal;
    }

    /**
     * Calculates the invoice total amount by adding the subtotal and tax amount
     * @return the calculated total amount
     */
    public double calculateTaxAmount() {
        taxAmount = subtotal * (taxRate / 100);
        return taxAmount;
    }

    /**
     * Calculates the invoice total amount by adding the subtotal and tax amount
     * @return the calculated total amount
     */
    public double calculateTotal() {
        totalAmount = subtotal + taxAmount;
        return totalAmount;
    }

    /**
     * Recalculates the invoice subtotal, tax amount, and total amount
     */
    public void reCalculateAmount() {
        calculateSubtotal();
        calculateTaxAmount();
        calculateTotal();
    }

    /**
     * Adds an item to the invoice and recalculates amounts.
     * @param invoiceItem to add
     * @return the invoice instance
     */
    public Invoice addItem(InvoiceItem invoiceItem) {
        items.add(invoiceItem);
        reCalculateAmount();
        return this;
    }

    /**
     * Removes an item from the invoice and recalculates amounts.
     * @param invoiceItem to remove
     * @return the invoice instance
     */
    public Invoice removeItem(InvoiceItem invoiceItem) {
        items.remove(invoiceItem);
        reCalculateAmount();
        return this;
    }


}
