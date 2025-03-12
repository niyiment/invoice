package com.niyiment.invoice.domain.entity;

import com.niyiment.invoice.domain.enums.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;



class InvoiceTest {
    private Invoice invoice;
    private InvoiceItem invoiceItem1;
    private InvoiceItem invoiceItem2;

    @BeforeEach
    void setup() {
        invoice = new Invoice("INV-2025-001");
        invoiceItem1 = new InvoiceItem("Item 1", 2, 10.0);
        invoiceItem2 = new InvoiceItem("Item 2", 3, 15.0);
    }

    @Test
    void shouldInitializeWithCorrectDefaults() {
        assertEquals("INV-2025-001", invoice.getInvoiceNumber(), "Invoice number should be set");
        assertNull(invoice.getInvoiceDate(), "Invoice date should not be null");
        assertNull(invoice.getDueDate(), "Due date should not be null");
        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus(), "Status should default to DRAFT");
        assertEquals(0, invoice.getItems().size(), "Item list should not be empty");
    }

    @Test
    void shouldcalculateSubtotalCorrectly() {
        invoice.addItem(invoiceItem1);
        invoice.addItem(invoiceItem2);
        double subTotal = invoice.getSubtotal();
        assertEquals(65.0, subTotal, "Subtotal should be calculated correctly");
        assertEquals(65.0, invoice.getSubtotal(), "Subtotal should be updated");
    }

    @Test
    void shouldCalculateTaxAmountCorreclty() {
        invoice.addItem(invoiceItem1);
        invoice.addItem(invoiceItem2);
        invoice.setTaxRate(10.0);
        double taxAmount = invoice.calculateTaxAmount();

        assertEquals(6.5, taxAmount, "Tax amount should be calculated correctly");
        assertEquals(6.5, invoice.getTaxAmount(), "Tax amount should be updated");
    }

    @Test
    void shouldCalculateTotalCorrectly() {
        invoice.addItem(invoiceItem1);
        invoice.addItem(invoiceItem2);
        invoice.setTaxRate(10.0);
        invoice.calculateSubtotal();
        invoice.calculateTaxAmount();
        double total = invoice.calculateTotal();

        assertEquals(71.5, total, "Total should be subtotal + tax amount");
        assertEquals(71.5, invoice.getTotalAmount(), "Total amount should be updated");
    }

    @Test
    void shouldRecalculateAllAmounts() {
        invoice.addItem(invoiceItem1);
        invoice.addItem(invoiceItem2);
        invoice.setTaxRate(10.0);
        invoice.reCalculateAmount();

        assertEquals(65.0, invoice.getSubtotal(), "Subtotal should be calculated");
        assertEquals(6.5, invoice.getTaxAmount(), "Tax amount should be calculated");
        assertEquals(71.5, invoice.getTotalAmount(), "Total amount should be calculated");
    }

    @Test
    void shouldAddItemAndRecalculate() {
        invoice.addItem(invoiceItem1);
        assertEquals(1, invoice.getItems().size(), "Item should be added");
        assertEquals(20.0, invoice.getSubtotal(), "Subtotal should be recalculated");
        assertTrue(invoice.getItems().contains(invoiceItem1), "Items list should contain added item");
    }

    @Test
    void shouldRemoveItemAndRecalculate() {
        invoice.addItem(invoiceItem1);
        invoice.addItem(invoiceItem2);
        invoice.removeItem(invoiceItem1);
        invoice.reCalculateAmount();

        assertEquals(1, invoice.getItems().size(), "Item should be removed");
        assertEquals(45.0, invoice.getSubtotal(), "Subtotal should be recalculated");
        assertFalse(invoice.getItems().contains(invoiceItem1), "Items list should not contain removed item");
        assertTrue(invoice.getItems().contains(invoiceItem2), "Items list should contain remaining item");
    }

    @Test
    void shouldSetItemsAndRecalculate() {
        invoice.setItems(Arrays.asList(invoiceItem1, invoiceItem2));
        invoice.reCalculateAmount();

        assertEquals(2, invoice.getItems().size(), "Items should be set");
        assertEquals(65.0, invoice.getSubtotal(), "Subtotal should be recalculated");
    }

    @Test
    void shouldUpdateAmountsWhenTaxRateChanges() {
        invoice.addItem(invoiceItem1);
        invoice.addItem(invoiceItem2);
        invoice.setTaxRate(10.0);
        double taxAmount = invoice.calculateTaxAmount();
        invoice.setTaxAmount(taxAmount);
        invoice.reCalculateAmount();

        assertEquals(65.0, invoice.getSubtotal(), "Subtotal should not change");
        assertEquals(6.5, invoice.getTaxAmount(), "Tax amount should be recalculated");
        assertEquals(71.5, invoice.getTotalAmount(), "Total amount should be recalculated");
    }

    @Test
    void shouldCompareInvoicesCorrectly() {
        Invoice invoice1 = new Invoice("INV-2025-001");
        Invoice invoice2 = new Invoice("INV-2025-001");
        Invoice invoice3 = new Invoice("INV-2025-002");

        invoice1.setId("1");
        invoice2.setId("1");
        invoice3.setId("2");

        assertEquals(invoice1, invoice2, "Invoices with same ID and number should be equal");
        assertNotEquals(invoice1, invoice3, "Invoices with different ID or number should not be equal");
        assertEquals(invoice1.hashCode(), invoice2.hashCode(), "Equal invoices should have same hash code");
    }

    @Test
    void toStringShouldContainRelevantInfo() {
        invoice.setId("1");
        invoice.setCustomerName("Test Client");
        invoice.addItem(invoiceItem1);
        invoice.setTaxRate(10.0);
        invoice.reCalculateAmount();
        String toString = invoice.toString();

        assertTrue(toString.contains("1"), "toString should contain the ID");
        assertTrue(toString.contains("INV-2025-001"), "toString should contain the invoice number");
        assertTrue(toString.contains("Test Client"), "toString should contain the client name");
        assertTrue(toString.contains("DRAFT"), "toString should contain the status");
        assertTrue(toString.contains("22.0"), "toString should contain the total amount");
    }
}