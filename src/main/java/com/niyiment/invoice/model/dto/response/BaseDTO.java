package com.niyiment.invoice.model.dto.response;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@MappedSuperclass
@Getter
@Setter
public class BaseDTO {
    private UUID id;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
