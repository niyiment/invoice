package com.niyiment.invoice.controller;


import com.niyiment.invoice.model.dto.request.CustomerRequest;
import com.niyiment.invoice.model.dto.response.CustomerResponse;
import com.niyiment.invoice.model.dto.response.CustomerSummaryResponse;
import com.niyiment.invoice.report.dto.ReportResponse;
import com.niyiment.invoice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RequestMapping("/api/v1/customers")
@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }


    @GetMapping
    public ResponseEntity<Page<CustomerSummaryResponse>> getAllCustomers(@PageableDefault Pageable pageable) {

        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable UUID customerId, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerSummaryResponse>> searchCustomerByName(@RequestParam String name) {
        return ResponseEntity.ok(customerService.searchCustomerByName(name));
    }

    @GetMapping("/reports/{format}/generate")
    public ResponseEntity<byte[]> generateReport(@PathVariable("format") String format) {
        ReportResponse response = customerService.generateCustomerReport(format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(response.getContentType()));
        headers.setContentDispositionFormData("attachment", response.getFileName());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(response.getReportBytes(), headers, HttpStatus.OK);
    }

}
