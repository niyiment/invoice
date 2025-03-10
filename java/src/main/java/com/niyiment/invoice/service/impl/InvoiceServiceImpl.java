package com.niyiment.invoice.service.impl;

import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.entity.Invoice;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import com.niyiment.invoice.domain.mapper.InvoiceMapper;
import com.niyiment.invoice.domain.repository.InvoiceRepository;
import com.niyiment.invoice.exception.InvoiceNotFoundException;
import com.niyiment.invoice.service.InvoiceService;
import com.niyiment.invoice.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;


    @Override
    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        if (invoiceDto.getInvoiceNumber() == null || invoiceDto.getInvoiceNumber().isEmpty()) {
            invoiceDto.setInvoiceNumber(generateNextInvoiceNumber());
        } else if (invoiceRepository.existsByInvoiceNumber(invoiceDto.getInvoiceNumber())) {
            throw new BadRequestException("Invoice number already exists: " + invoiceDto.getInvoiceNumber());
        }
        Invoice invoice = invoiceMapper.toEntity(invoiceDto);
        invoice.reCalculateAmount();
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toDto(savedInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(String id) {
        Invoice invoice = findInvoiceById(id);

        return invoiceMapper.toDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with number: " + invoiceNumber));

        return invoiceMapper.toDto(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> getAllInvoices(Pageable pageable) {
        Page<Invoice> invoices  = invoiceRepository.findAll(pageable);

        return invoices.map(invoiceMapper::toDto);
    }

    @Override
    @Transactional
    public InvoiceDto updateInvoice(String id, InvoiceDto invoiceDto) {
        Invoice existingInvoice = findInvoiceById(id);
        if (existingInvoice.getStatus().isFinalState()) {
            throw new BadRequestException("Cannot update invoice in: " + existingInvoice.getStatus() + " state");
        }

        invoiceMapper.updateEntityFromDto(existingInvoice, invoiceDto);
        existingInvoice.reCalculateAmount();
        Invoice savedInvoice = invoiceRepository.save(existingInvoice);

        return invoiceMapper.toDto(savedInvoice);
    }

    @Override
    @Transactional
    public InvoiceDto updateInvoiceStatus(String id, InvoiceStatus invoiceStatus) {
        Invoice existingInvoice = findInvoiceById(id);
        if (existingInvoice.getStatus().canTransitionTo(invoiceStatus)) {
            throw new BadRequestException("Cannot transition from " + existingInvoice.getStatus() +
             " to " + invoiceStatus);
        }
        existingInvoice.setStatus(invoiceStatus);
        if (invoiceStatus == InvoiceStatus.OVERDUE && existingInvoice.getDueDate().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot mark as OVERDUE because due date is in the future");
        }
        Invoice savedInvoice = invoiceRepository.save(existingInvoice);

        return invoiceMapper.toDto(savedInvoice);
    }

    @Override
    @Transactional
    public void deleteInvoice(String id) {
        Invoice invoice = findInvoiceById(id);
        if (invoice.getStatus().isFinalState()) {
            throw new BadRequestException("Cannot delete invoice in: " + invoice.getStatus() + " state");
        }
        invoiceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoiceByCustomerEmail(String customerEmail, Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findByCustomerEmail(customerEmail, pageable);

        return invoices.map(invoiceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByDueDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findByDueDateBetween(startDate, endDate, pageable);

        return invoices.map(invoiceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> getOverdueInvoices(Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findOverdueInvoices(LocalDateTime.now(), pageable);

        return invoices.map(invoiceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByTotalAmountRange(double amount, Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findByTotalAmountGreaterThanEqual(amount, pageable);

        return invoices.map(invoiceMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDto> advancedSearch(String customerName, InvoiceStatus invoiceStatus, LocalDateTime startDate,
                                           LocalDateTime endDate, Double minAmount, Double maxAmount) {
        List<Invoice> invoices = invoiceRepository.advanceSearch(customerName, invoiceStatus, startDate, endDate, minAmount, maxAmount);

        return invoices.stream().map(invoiceMapper::toDto).toList();
    }

    @Override
    public String generateNextInvoiceNumber() {
        String prefix = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Invoice> invoicesWithPrefix = invoiceRepository.findAll().stream()
                .filter(inv -> inv.getInvoiceNumber().startsWith(prefix))
                .collect(Collectors.toList());

        int maxNumber = 0;

        for (Invoice invoice : invoicesWithPrefix) {
            try {
                String[] parts = invoice.getInvoiceNumber().split("-");
                if (parts.length >= 3) {
                    int number = Integer.parseInt(parts[2]);
                    maxNumber = Math.max(maxNumber, number);
                }
            } catch (NumberFormatException e) {
                log.error("Invoice number {}", invoice.getInvoiceNumber());
            }
        }

        return String.format("%s-%03d", prefix, maxNumber + 1);
    }

    private Invoice findInvoiceById(String id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with id: " + id));
    }
}
