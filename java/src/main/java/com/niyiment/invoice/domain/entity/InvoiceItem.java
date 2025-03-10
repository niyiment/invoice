package com.niyiment.invoice.domain.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InvoiceItem {
    private String description;
    private int quantity;
    private double price;
    private double amount;
}
