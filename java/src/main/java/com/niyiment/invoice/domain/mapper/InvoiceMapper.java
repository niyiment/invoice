package com.niyiment.invoice.domain.mapper;


import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.dto.InvoiceItemDto;
import com.niyiment.invoice.domain.entity.Invoice;
import com.niyiment.invoice.domain.entity.InvoiceItem;

import java.util.stream.Collectors;

public class InvoiceMapper {
    /**
     * Converts an Invoice entity to an InvoiceDto.
     *
     * @param invoice The Invoice entity
     * @return The corresponding InvoiceDto
     */
    public InvoiceDto toDto(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCustomerName(invoice.getCustomerName());
        dto.setCustomerEmail(invoice.getCustomerEmail());
        dto.setCustomerAddress(invoice.getCustomerAddress());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setItems(invoice.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList()));

        dto.setSubTotal(invoice.getSubTotal());
        dto.setTaxRate(invoice.getTaxRate());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setStatus(invoice.getStatus());
        dto.setNotes(invoice.getNotes());

        return dto;
    }

    /**
     * Converts an InvoiceDto to an Invoice entity.
     *
     * @param dto The InvoiceDto
     * @return The corresponding Invoice entity
     */
    public Invoice toEntity(InvoiceDto dto) {
        if (dto == null) {
            return null;
        }

        Invoice invoice = new Invoice(dto.getInvoiceNumber());

        if (dto.getId() != null && !dto.getId().isEmpty()) {
            invoice.setId(dto.getId());
        }

        invoice.setCustomerName(dto.getCustomerName());
        invoice.setCustomerEmail(dto.getCustomerEmail());
        invoice.setCustomerAddress(dto.getCustomerAddress());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setItems(dto.getItems().stream()
                .map(InvoiceItemDto::toEntity)
                .collect(Collectors.toList()));

        invoice.setTaxRate(dto.getTaxRate());
        invoice.setStatus(dto.getStatus());
        invoice.setNotes(dto.getNotes());
        invoice.reCalculateAmount();

        return invoice;
    }

    /**
     * Updates an existing Invoice entity with data from an InvoiceDto.
     *
     * @param invoice The existing Invoice entity
     * @param dto The InvoiceDto with updated data
     * @return The updated Invoice entity
     */
    public Invoice updateEntityFromDto(Invoice invoice, InvoiceDto dto) {
        if (invoice == null || dto == null) {
            return invoice;
        }

        invoice.setCustomerName(dto.getCustomerName());
        invoice.setCustomerEmail(dto.getCustomerEmail());
        invoice.setCustomerAddress(dto.getCustomerAddress());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setItems(dto.getItems().stream()
                .map(InvoiceItemDto::toEntity)
                .collect(Collectors.toList()));

        invoice.setTaxRate(dto.getTaxRate());

        if (invoice.getStatus().canTransitionTo(dto.getStatus())) {
            invoice.setStatus(dto.getStatus());
        }

        invoice.setNotes(dto.getNotes());
        invoice.reCalculateAmount();

        return invoice;
    }

    /**
     * Converts an InvoiceItem entity to an InvoiceItemDto.
     *
     * @param item The InvoiceItem entity
     * @return The corresponding InvoiceItemDto
     */
    private InvoiceItemDto toItemDto(InvoiceItem item) {
        return InvoiceItemDto.fromEntity(item);
    }
}
