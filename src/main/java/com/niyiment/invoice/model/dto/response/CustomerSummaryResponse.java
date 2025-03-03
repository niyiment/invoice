package com.niyiment.invoice.model.dto.response;

import com.niyiment.invoice.model.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryResponse {
    
    private UUID id;
    private String customerNumber;
    private String name;
    private String email;
    private AddressResponse billingAddress;
    private CustomerType customerType;
}