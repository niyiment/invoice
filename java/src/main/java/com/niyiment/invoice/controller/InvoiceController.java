package com.niyiment.invoice.controller;


import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.dto.StatusUpdateDto;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import com.niyiment.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


/**
 * REST controller for invoice management.
 */
@Slf4j
@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice", description = "Invoice Management API")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    @Operation(summary = "Create a new invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invoice created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Invoice number already exists")
    })
    public ResponseEntity<InvoiceDto> createInvoice(@RequestBody InvoiceDto invoiceDto) {
        log.info("Invoice object: {}", invoiceDto);
        InvoiceDto createdInvoice = invoiceService.createInvoice(invoiceDto);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an invoice by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice found"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<InvoiceDto> getInvoiceById(
            @Parameter(description = "Invoice ID", required = true)
            @PathVariable String id) {
        InvoiceDto invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Get an invoice by invoice number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice found"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<InvoiceDto> getInvoiceByNumber(
            @Parameter(description = "Invoice number", required = true)
            @PathVariable String invoiceNumber) {
        InvoiceDto invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping
    @Operation(summary = "Get all invoices with pagination")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(invoices);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or invoice is in final state"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<InvoiceDto> updateInvoice(
            @Parameter(description = "Invoice ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto updatedInvoice = invoiceService.updateInvoice(id, invoiceDto);
        return ResponseEntity.ok(updatedInvoice);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update invoice status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<InvoiceDto> updateInvoiceStatus(
            @Parameter(description = "Invoice ID", required = true)
            @PathVariable String id,
            @Parameter(description = "New status", required = true)
            @RequestBody StatusUpdateDto statusDto) {
        InvoiceDto updatedInvoice = invoiceService.updateInvoiceStatus(id, statusDto.getStatus());
        return ResponseEntity.ok(updatedInvoice);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Invoice deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invoice is in final state"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<Void> deleteInvoice(
            @Parameter(description = "Invoice ID", required = true)
            @PathVariable String id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get invoices by status")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceDto>> getInvoicesByStatus(
            @Parameter(description = "Invoice status", required = true)
            @PathVariable InvoiceStatus status,
            Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getInvoicesByStatus(status, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/amount-greater/{amount}")
    @Operation(summary = "Get invoices greater than or equal to the given amount ")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceDto>> getInvoicesByTotalAmountGreaterThanEquals(
            @Parameter(description = "Invoice status", required = true)
            @PathVariable Double amount, Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getInvoicesByTotalAmountGreaterThanEquals(amount, pageable);

        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/amount-less/{amount}")
    @Operation(summary = "Get invoices less than or equal to the given amount ")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceDto>> getInvoicesByTotalAmountLessThanEquals(
            @Parameter(description = "Invoice status", required = true)
            @PathVariable Double amount, Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getInvoicesByTotalAmountLessThanEquals(amount, pageable);

        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/customer/{customerEmail}")
    @Operation(summary = "Get invoices by customer email")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceDto>> getInvoiceByCustomerEmail(
            @Parameter(description = "Customer email", required = true)
            @PathVariable String customerEmail,
            Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getInvoiceByCustomerEmail(customerEmail, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/due-date")
    @Operation(summary = "Get invoices by due date range")
    @ApiResponse(responseCode = "200", description = "List of invoices")
    public ResponseEntity<Page<InvoiceDto>> getInvoicesByDueDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getInvoicesByDueDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue invoices")
    @ApiResponse(responseCode = "200", description = "List of overdue invoices")
    public ResponseEntity<Page<InvoiceDto>> getOverdueInvoices(Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getOverdueInvoices(pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/search")
    @Operation(summary = "Advanced search with multiple filters")
    @ApiResponse(responseCode = "200", description = "List of matching invoices")
    public ResponseEntity<List<InvoiceDto>> advancedSearch(
            @Parameter(description = "Client name (partial match)")
            @RequestParam(required = false) String clientName,
            @Parameter(description = "Invoice status")
            @RequestParam(required = false) InvoiceStatus status,
            @Parameter(description = "Start date (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Minimum total amount")
            @RequestParam(required = false) Double minAmount,
            @Parameter(description = "Maximum total amount")
            @RequestParam(required = false) Double maxAmount) {
        List<InvoiceDto> invoices = invoiceService.advancedSearch(clientName, status, startDate, endDate, minAmount, maxAmount);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/generate-number")
    @Operation(summary = "Generate a new invoice number")
    @ApiResponse(responseCode = "200", description = "Generated invoice number")
    public ResponseEntity<String> generateInvoiceNumber() {
        String invoiceNumber = invoiceService.generateNextInvoiceNumber();
        return ResponseEntity.ok(invoiceNumber);
    }
}

