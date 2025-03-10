package com.niyiment.invoice.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceItemTest {

    @Test
    void shouldCalculateAmountCorrectly() {
        InvoiceItem item = new InvoiceItem("Test Item", 5, 10.0);

        assertEquals(50.0, item.getAmount(), "Amount should be quantity * unitPrice");
    }

    @Test
    void shouldReCalculateAmountWhenQuantityyChanges() {
        InvoiceItem item = new InvoiceItem("Test Item", 5, 10.0);
        item.setQuantity(10);

        assertEquals(100.0, item.getAmount(), "Amount should be new quantity * unitPrice");
    }

    @Test
    void shouldRecalculateAmountWhenUnitPriceChanges() {
        InvoiceItem item = new InvoiceItem("Test Item", 5, 10.0);
        item.setUnitPrice(20.0);

        assertEquals(100.0, item.getAmount(), "Amount should be new quantity * new unitPrice");
    }

    @Test
    void shouldEqualTwoItemsWithSameValues() {
        InvoiceItem item1 = new InvoiceItem("Test Item", 5, 10.0);
        InvoiceItem item2 = new InvoiceItem("Test Item", 5, 10.0);
        InvoiceItem item3 = new InvoiceItem("Different Test Item", 5, 10.0);

        assertEquals(item1, item2, "Two items should be considered equal when all fields match");
        assertNotEquals(item1, item3, "Two items should be considered different when description differs");
        assertEquals(item1.hashCode(), item2.hashCode(), "Equal items should have the same hash code");
    }

    @Test
    void toStringShouldContainRelevantInfo() {
        InvoiceItem item = new InvoiceItem("Test Item", 5, 10.0);
        String toString = item.toString();

        assertEquals("InvoiceItem{description='Test Item', quantity=5, unitPrice=10.0, amount=50.0}", item.toString(), "toString should return the item description, quantity, unit price, and amount");
        assertTrue(toString.contains("10.0"), "toString should contain the unit price");
    }

}