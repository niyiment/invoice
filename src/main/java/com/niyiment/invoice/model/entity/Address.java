package com.niyiment.invoice.model.entity;



import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null) sb.append(street).append(", ");
        if (city != null) sb.append(city).append(", ");
        if (state != null) sb.append(state).append(" ");
        if (postalCode != null) sb.append(postalCode).append(", ");
        if (country != null) sb.append(country);

        return sb.toString().trim();
    }
}

