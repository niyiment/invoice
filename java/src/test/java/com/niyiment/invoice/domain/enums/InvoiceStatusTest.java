package com.niyiment.invoice.domain.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceStatusTest {

    @Test
    void shouldIdentifyFinalStates() {
        assertTrue(InvoiceStatus.PAID.isFinalState(), "PAID should be a final state");
        assertTrue(InvoiceStatus.CANCELLED.isFinalState(), "CANCELLED should be a final state");
        assertFalse(InvoiceStatus.DRAFT.isFinalState(), "DRAFT should not be a final state");
        assertFalse(InvoiceStatus.SENT.isFinalState(), "SENT should not be a final state");
        assertFalse(InvoiceStatus.OVERDUE.isFinalState(), "OVERDUE should not be a final state");
    }

    @Test
    void shouldAllowTransitionToSameStatus() {
        for (InvoiceStatus status : InvoiceStatus.values()) {
            assertTrue(status.canTransitionTo(status),
                    "Any status should be able to transition to itself");
        }
    }

    @Test
    void shouldNotAllowTransitionFromFinalStates() {
        for (InvoiceStatus finalStatus : new InvoiceStatus[]{InvoiceStatus.PAID, InvoiceStatus.CANCELLED}) {
            for (InvoiceStatus otherStatus : InvoiceStatus.values()) {
                if (finalStatus != otherStatus) {
                    assertFalse(finalStatus.canTransitionTo(otherStatus),
                            "Final state " + finalStatus + " should not transition to " + otherStatus);
                }
            }
        }
    }

    @Test
    void draftShouldTransitionToAnyState() {
        for (InvoiceStatus targetStatus : InvoiceStatus.values()) {
            assertTrue(InvoiceStatus.DRAFT.canTransitionTo(targetStatus),
                    "DRAFT should be able to transition to " + targetStatus);
        }
    }

    @Test
    void sentShouldTransitionToAllowedStates() {
        assertTrue(InvoiceStatus.SENT.canTransitionTo(InvoiceStatus.PAID),
                "SENT should be able to transition to PAID");
        assertTrue(InvoiceStatus.SENT.canTransitionTo(InvoiceStatus.OVERDUE),
                "SENT should be able to transition to OVERDUE");
        assertTrue(InvoiceStatus.SENT.canTransitionTo(InvoiceStatus.CANCELLED),
                "SENT should be able to transition to CANCELLED");
        assertFalse(InvoiceStatus.SENT.canTransitionTo(InvoiceStatus.DRAFT),
                "SENT should not be able to transition to DRAFT");
    }

    @Test
    void overdueShouldTransitionToAllowedStates() {
        assertTrue(InvoiceStatus.OVERDUE.canTransitionTo(InvoiceStatus.PAID),
                "OVERDUE should be able to transition to PAID");
        assertTrue(InvoiceStatus.OVERDUE.canTransitionTo(InvoiceStatus.CANCELLED),
                "OVERDUE should be able to transition to CANCELLED");
        assertFalse(InvoiceStatus.OVERDUE.canTransitionTo(InvoiceStatus.DRAFT),
                "OVERDUE should not be able to transition to DRAFT");
        assertFalse(InvoiceStatus.OVERDUE.canTransitionTo(InvoiceStatus.SENT),
                "OVERDUE should not be able to transition to SENT");
    }
}