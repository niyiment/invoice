package com.niyiment.invoice.service.impl;

import com.niyiment.invoice.exception.DuplicateException;
import com.niyiment.invoice.exception.ResourceNotFoundException;
import com.niyiment.invoice.model.dto.request.CustomerRequest;
import com.niyiment.invoice.model.dto.response.CustomerResponse;
import com.niyiment.invoice.model.dto.response.CustomerSummaryResponse;
import com.niyiment.invoice.model.entity.Customer;
import com.niyiment.invoice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRequest customerRequest;
    private Customer customer;
    private CustomerResponse customerResponse;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        customerRequest = new CustomerRequest();
        customerRequest.setName("Steven Adeniyi");
        customerRequest.setEmail("niyi@example.com");

        customer = new Customer();
        customer.setId(customerId);
        customer.setName("Steven Adeniyi");
        customer.setEmail("niyi@example.com");

        customerResponse = new CustomerResponse();
        customerResponse.setId(customerId);
        customerResponse.setName("Steven Adeniyi");
        customerResponse.setEmail("niyi@example.com");
    }

    @Test
    void createCustomer_Success() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(modelMapper.map(any(CustomerRequest.class), eq(Customer.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelMapper.map(any(Customer.class), eq(CustomerResponse.class))).thenReturn(customerResponse);

        CustomerResponse result = customerService.createCustomer(customerRequest);

        assertNotNull(result);
        assertEquals(customerResponse.getId(), result.getId());
        assertEquals(customerResponse.getName(), result.getName());
        assertEquals(customerResponse.getEmail(), result.getEmail());

        verify(customerRepository).existsByEmail(customerRequest.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_DuplicateEmail() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateException.class, () -> customerService.createCustomer(customerRequest));

        verify(customerRepository).existsByEmail(customerRequest.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_Success() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelMapper.map(any(Customer.class), eq(CustomerResponse.class))).thenReturn(customerResponse);

        CustomerResponse result = customerService.updateCustomer(customerId, customerRequest);

        assertNotNull(result);
        assertEquals(customerResponse.getId(), result.getId());
        assertEquals(customerResponse.getName(), result.getName());
        assertEquals(customerResponse.getEmail(), result.getEmail());

        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(customerId, customerRequest));

        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_Success() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(customerId);

        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(customer);
        assertFalse(customer.isActive());
    }

    @Test
    void deleteCustomer_NotFound() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer(customerId));

        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.of(customer));
        when(modelMapper.map(any(Customer.class), eq(CustomerResponse.class))).thenReturn(customerResponse);

        CustomerResponse result = customerService.getCustomerById(customerId);

        assertNotNull(result);
        assertEquals(customerResponse.getId(), result.getId());
        assertEquals(customerResponse.getName(), result.getName());
        assertEquals(customerResponse.getEmail(), result.getEmail());

        verify(customerRepository).findById(customerId);
    }

    @Test
    void getCustomerById_NotFound() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(customerId));

        verify(customerRepository).findById(customerId);
    }

    @Test
    void getAllCustomers() {
        Page<Customer> customerPage = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(customerPage);
        when(modelMapper.map(any(Customer.class), eq(CustomerSummaryResponse.class))).thenReturn(new CustomerSummaryResponse());

        Page<CustomerSummaryResponse> result = customerService.getAllCustomers(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(customerRepository).findAll(any(Pageable.class));
    }

    @Test
    void searchCustomerByName() {
        when(customerRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of(customer));
        when(modelMapper.map(any(Customer.class), eq(CustomerSummaryResponse.class))).thenReturn(new CustomerSummaryResponse());

        List<CustomerSummaryResponse> result = customerService.searchCustomerByName("Steven");

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(customerRepository).findByNameContainingIgnoreCase("Steven");
    }

    @Test
    void existsByEmail() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        boolean result = customerService.existsByEmail("niyi@example.com");

        assertTrue(result);
        verify(customerRepository).existsByEmail("niyi@example.com");
    }

    @Test
    void getCustomerByEmail() {
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(modelMapper.map(any(Customer.class), eq(CustomerResponse.class))).thenReturn(customerResponse);

        Optional<CustomerResponse> result = customerService.getCustomerByEmail("niyi@example.com");

        assertTrue(result.isPresent());
        assertEquals(customerResponse.getId(), result.get().getId());
        assertEquals(customerResponse.getName(), result.get().getName());
        assertEquals(customerResponse.getEmail(), result.get().getEmail());

        verify(customerRepository).findByEmail("niyi@example.com");
    }
}