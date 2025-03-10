package com.niyiment.invoice.domain.enums;

public enum InvoiceStatus {
    DRAFT, SENT, PAID, OVERDUE, CANCELLED;

    public boolean isFinalState() {
        return this == PAID || this == CANCELLED;
    }

    /**
     * Checks if the invoice status can be changed to the newStatus
     * @param newStatus
     * @return
     */
    public boolean canTransitionTo(InvoiceStatus newStatus) {
        if (this == newStatus) {
            return true;
        }
        if (this.isFinalState()) {
            return false;
        }

        return switch (this) {
            case DRAFT -> true;
            case SENT -> newStatus == PAID || newStatus == CANCELLED || newStatus == OVERDUE;
            case OVERDUE -> newStatus == PAID || newStatus == CANCELLED;
            default -> false;
        };
    }
}
