package com.niyiment.invoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niyiment.invoice.model.dto.request.CustomerRequest;
import com.niyiment.invoice.model.dto.response.CustomerResponse;
import com.niyiment.invoice.model.dto.response.CustomerSummaryResponse;
import com.niyiment.invoice.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(CustomerController.class)
class CustomerControllerIntegrationTest {

    @TestConfiguration
    static class CustomerServiceImplTestContextConfiguration{
        @Bean
        public CustomerServiceImpl customerService() {
            return Mockito.mock(CustomerServiceImpl.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerServiceImpl customerService;

    private CustomerRequest validCustomerRequest;
    private CustomerRequest invalidCustomerRequest;
    private CustomerResponse customerResponse;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        // Valid customer request
        validCustomerRequest = new CustomerRequest();
        validCustomerRequest.setName("Steven Adeniyi");
        validCustomerRequest.setEmail("niyi@example.com");

        // Invalid customer request (missing email)
        invalidCustomerRequest = new CustomerRequest();
        invalidCustomerRequest.setName("Steven Adeniyi");

        // Expected response
        customerResponse = new CustomerResponse();
        customerResponse.setId(customerId);
        customerResponse.setName("Steven Adeniyi");
        customerResponse.setEmail("niyi@example.com");
    }

    @Test
    @DisplayName("Should create a customer and return 201 Created status")
    void createCustomer_ValidRequest_ReturnsCreatedStatus() throws Exception {

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("Steven Adeniyi"))
                .andExpect(jsonPath("$.email").value("niyi@example.com"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when customer data is invalid")
    void createCustomer_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return paginated list of customers")
    void getAllCustomers_ReturnsPagedResponse() throws Exception {
        // Create summary responses
        CustomerSummaryResponse summary = new CustomerSummaryResponse();
        summary.setId(customerId);
        summary.setName("Steven Adeniyi");

        Page<CustomerSummaryResponse> customerPage = new PageImpl<>(
                List.of(summary),
                PageRequest.of(0, 10),
                1
        );

        mockMvc.perform(get("/api/v1/customers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("Should return a customer when valid ID provided")
    void getCustomerById_ExistingId_ReturnsCustomer() throws Exception {

        mockMvc.perform(get("/api/v1/customers/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("Steven Adeniyi"))
                .andExpect(jsonPath("$.email").value("niyi@example.com"));

    }

    @Test
    @DisplayName("Should return 404 Not Found when customer ID doesn't exist")
    void getCustomerById_NonExistingId_ReturnsNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/customers/{customerId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update a customer and return updated data")
    void updateCustomer_ValidRequest_ReturnsUpdatedCustomer() throws Exception {

        mockMvc.perform(put("/api/v1/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(customerId.toString()))
                .andExpect(jsonPath("$.name").value("Steven Adeniyi"))
                .andExpect(jsonPath("$.email").value("niyi@example.com"));
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating non-existing customer")
    void updateCustomer_NonExistingId_ReturnsNotFound() throws Exception {

        mockMvc.perform(put("/api/v1/customers/{customerId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCustomerRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when update data is invalid")
    void updateCustomer_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete a customer and return 204 No Content")
    void deleteCustomer_ExistingId_ReturnsNoContent() throws Exception {

        mockMvc.perform(delete("/api/v1/customers/{customerId}", customerId))
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existing customer")
    void deleteCustomer_NonExistingId_ReturnsNotFound() throws Exception {
        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/customers/{customerId}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return list of customers matching the search name")
    void searchCustomerByName_ReturnsMatchingCustomers() throws Exception {
        // Create summary response
        CustomerSummaryResponse summary = new CustomerSummaryResponse();
        summary.setId(customerId);
        summary.setName("Steven Adeniyi");

        List<CustomerSummaryResponse> customerList = List.of(summary);

        mockMvc.perform(get("/api/v1/customers/search")
                        .param("name", "niyi"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(customerId.toString()))
                .andExpect(jsonPath("$[0].name").value("Steven Adeniyi"));

    }

    @Test
    @DisplayName("Should return empty list when no customers match search criteria")
    void searchCustomerByName_NoMatches_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/customers/search")
                        .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
