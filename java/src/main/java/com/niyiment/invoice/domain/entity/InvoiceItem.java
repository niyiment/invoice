package com.niyiment.invoice.domain.entity;


import lombok.Getter;

import java.util.Objects;

@Getter
public class InvoiceItem {
    private String description;
    private int quantity;
    private double unitPrice;
    private double amount;

    public InvoiceItem() {

    }

    public InvoiceItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = calculateAmount();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateAmount();
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        calculateAmount();
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double calculateAmount() {
        amount = quantity * unitPrice;
        return amount;
    }


    @Override
    public String toString() {
        return "InvoiceItem{" +
                "description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        InvoiceItem that = (InvoiceItem) object;
        return quantity == that.quantity && Double.compare(unitPrice, that.unitPrice) == 0 && Double.compare(amount, that.amount) == 0 && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, quantity, unitPrice, amount);
    }
}
