package com.niyiment.invoice.service;


import com.niyiment.invoice.model.dto.request.CustomerRequest;
import com.niyiment.invoice.model.dto.response.CustomerResponse;
import com.niyiment.invoice.model.dto.response.CustomerSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request);
    CustomerResponse updateCustomer(UUID customerId, CustomerRequest request);
    void deleteCustomer(UUID customerId);
    CustomerResponse getCustomerById(UUID customerId);
    Page<CustomerSummaryResponse> getAllCustomers(Pageable pageable);
    List<CustomerSummaryResponse> searchCustomerByName(String name);
    boolean existsByEmail(String email);
    Optional<CustomerResponse> getCustomerByEmail(String email);
}
