package com.niyiment.invoice.model.dto.response;

import com.niyiment.invoice.model.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse extends BaseDTO {
    private String customerNumber;
    private String name;
    private String email;
    private String phone;
    private AddressResponse billingAddress;
    private AddressResponse shippingAddress;
    private String taxNumber;
    private CustomerType customerType;
    private String notes;
//    private List<InvoiceSummaryResponse> invoices;
}