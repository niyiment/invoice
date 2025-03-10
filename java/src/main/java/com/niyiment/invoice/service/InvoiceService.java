package com.niyiment.invoice.service;

import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceService {
    InvoiceDto createInvoice(InvoiceDto invoiceDto);
    InvoiceDto getInvoiceById(String id);
    InvoiceDto getInvoiceByNumber(String invoiceNumber);
    Page<InvoiceDto> getAllInvoices(Pageable pageable);
    InvoiceDto updateInvoice(String id, InvoiceDto invoiceDto);
    InvoiceDto updateInvoiceStatus(String id, InvoiceStatus invoiceStatus);
    void deleteInvoice(String id);
    Page<InvoiceDto> getInvoiceByCustomerEmail(String customerEmail, Pageable pageable);
    Page<InvoiceDto> getInvoicesByDueDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<InvoiceDto> getOverdueInvoices(Pageable pageable);
    Page<InvoiceDto> getInvoicesByTotalAmountRange(double minAmount, Pageable pageable);
    List<InvoiceDto> advancedSearch(String customerName, InvoiceStatus invoiceStatus,
                                    LocalDateTime startDate, LocalDateTime endDate,
                                    Double minAmount, Double maxAmount);

    String generateNextInvoiceNumber();
}
