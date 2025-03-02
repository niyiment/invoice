package com.niyiment.invoice.service.impl;

import com.niyiment.invoice.exception.DuplicateException;
import com.niyiment.invoice.exception.ResourceNotFoundException;
import com.niyiment.invoice.model.dto.request.CustomerRequest;
import com.niyiment.invoice.model.dto.response.CustomerResponse;
import com.niyiment.invoice.model.dto.response.CustomerSummaryResponse;
import com.niyiment.invoice.model.entity.Address;
import com.niyiment.invoice.model.entity.Customer;
import com.niyiment.invoice.model.enums.CustomerType;
import com.niyiment.invoice.repository.CustomerRepository;
import com.niyiment.invoice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private CustomerRepository customerRepository;
    private ModelMapper modelMapper;
    private static final String NOT_FOUND = "Customer not found with id: ";

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("Customer with email " + request.getEmail() + " already exists");
        }
        Customer customer = modelMapper.map(request, Customer.class);
        customer.setCustomerNumber(generateCustomerNumber());
        Customer savedCustomer = customerRepository.save(customer);

        return modelMapper.map(savedCustomer, CustomerResponse.class);
    }

    @Override
    public CustomerResponse updateCustomer(UUID customerId, CustomerRequest request) {
       Customer existingCustomer = customerRepository.findById(customerId)
               .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + customerId));

       existingCustomer.setName(request.getName());
       existingCustomer.setEmail(request.getEmail());
       existingCustomer.setPhone(request.getPhone());
       existingCustomer.setBillingAddress(modelMapper.map(request.getBillingAddress(), Address.class));
       existingCustomer.setShippingAddress(modelMapper.map(request.getShippingAddress(), Address.class));
       existingCustomer.setCustomerType(request.getCustomerType() != null ? request.getCustomerType() : CustomerType.INDIVIDUAL);
       existingCustomer.setNotes(request.getNotes());
       Customer updatedCustomer = customerRepository.save(existingCustomer);

        return modelMapper.map(updatedCustomer, CustomerResponse.class);
    }

    @Override
    public void deleteCustomer(UUID customerId) {
        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + customerId));
        existingCustomer.setActive(false);
        customerRepository.save(existingCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND + customerId));
        return modelMapper.map(customer, CustomerResponse.class);
    }

    @Override
    public Page<CustomerSummaryResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(customer -> modelMapper.map(customer, CustomerSummaryResponse.class));
    }

    @Override
    public List<CustomerSummaryResponse> searchCustomerByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name)
                .stream().map(customer -> modelMapper.map(customer, CustomerSummaryResponse.class)).toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    public Optional<CustomerResponse> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email.toLowerCase())
                .map(customer -> modelMapper.map(customer, CustomerResponse.class));
    }

    private String generateCustomerNumber() {
        long count = customerRepository.count() + 1;
        return "CUST-" + String.format("%06d", count);
    }
}
