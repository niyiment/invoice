package com.niyiment.invoice.model.dto.request;

import com.niyiment.invoice.model.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in international format (e.g., +2347567890)")
    private String phone;
    
    private AddressRequest billingAddress;
    
    private AddressRequest shippingAddress;
    
    private String taxNumber;
    
    private CustomerType customerType;
    
    private String notes;
}