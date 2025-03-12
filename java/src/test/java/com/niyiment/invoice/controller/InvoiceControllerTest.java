package com.niyiment.invoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.dto.InvoiceItemDto;
import com.niyiment.invoice.domain.dto.StatusUpdateDto;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import com.niyiment.invoice.exception.BadRequestException;
import com.niyiment.invoice.exception.InvoiceNotFoundException;
import com.niyiment.invoice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    private InvoiceDto invoiceDto;
    private String invoiceId;
    private String invoiceNumber;

    @BeforeEach
    void setUp() {
        invoiceId = "1";
        invoiceNumber = "INV-2025-001";

        // Setup invoice DTO
        invoiceDto = new InvoiceDto();
        invoiceDto.setId(invoiceId);
        invoiceDto.setInvoiceNumber(invoiceNumber);
        invoiceDto.setCustomerName("Test Customer");
        invoiceDto.setCustomerEmail("customer@example.com");
        invoiceDto.setInvoiceDate(LocalDateTime.now());
        invoiceDto.setDueDate(LocalDateTime.now().plusDays(30));

        InvoiceItemDto itemDto = new InvoiceItemDto();
        itemDto.setDescription("Item 1");
        itemDto.setQuantity(2);
        itemDto.setUnitPrice(10.0);
        invoiceDto.setItems(Arrays.asList(itemDto));

        invoiceDto.setTaxRate(10.0);
        invoiceDto.setSubtotal(20.0);
        invoiceDto.setTaxAmount(2.0);
        invoiceDto.setTotalAmount(22.0);
        invoiceDto.setStatus(InvoiceStatus.DRAFT);
    }

    @Test
    void shouldCreateInvoiceSuccessfully() throws Exception {
        when(invoiceService.createInvoice(any(InvoiceDto.class))).thenReturn(invoiceDto);

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(invoiceId)))
                .andExpect(jsonPath("$.invoiceNumber", is(invoiceNumber)))
                .andExpect(jsonPath("$.customerName", is("Test Customer")))
                .andExpect(jsonPath("$.status", is("DRAFT")));

        verify(invoiceService).createInvoice(any(InvoiceDto.class));
    }

    @Test
    void shouldGetInvoiceByIdSuccessfully() throws Exception {
        when(invoiceService.getInvoiceById(invoiceId)).thenReturn(invoiceDto);

        mockMvc.perform(get("/api/invoices/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoiceId)))
                .andExpect(jsonPath("$.invoiceNumber", is(invoiceNumber)));

        verify(invoiceService).getInvoiceById(invoiceId);
    }

    @Test
    void shouldReturnNotFoundWhenInvoiceDoesNotExist() throws Exception {
        when(invoiceService.getInvoiceById(invoiceId)).thenThrow(new InvoiceNotFoundException("Invoice not found"));

        mockMvc.perform(get("/api/invoices/{id}", invoiceId))
                .andExpect(status().isNotFound());

        verify(invoiceService).getInvoiceById(invoiceId);
    }

    @Test
    void shouldGetInvoiceByNumberSuccessfully() throws Exception {
        when(invoiceService.getInvoiceByNumber(invoiceNumber)).thenReturn(invoiceDto);

        mockMvc.perform(get("/api/invoices/number/{invoiceNumber}", invoiceNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoiceId)))
                .andExpect(jsonPath("$.invoiceNumber", is(invoiceNumber)));

        verify(invoiceService).getInvoiceByNumber(invoiceNumber);
    }

    @Test
    void shouldGetAllInvoicesSuccessfully() throws Exception {
        List<InvoiceDto> invoices = List.of(invoiceDto);
        when(invoiceService.getAllInvoices(any(Pageable.class)))
                .thenReturn(new PageImpl<>(invoices));

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(invoiceId)));

        verify(invoiceService).getAllInvoices(any(Pageable.class));
    }

    @Test
    void shouldUpdateInvoiceSuccessfully() throws Exception {
        InvoiceDto updatedInvoiceDto = new InvoiceDto();
        updatedInvoiceDto.setId(invoiceId);
        updatedInvoiceDto.setCustomerName("Updated Customer");

        when(invoiceService.updateInvoice(eq(invoiceId), any(InvoiceDto.class))).thenReturn(updatedInvoiceDto);

        mockMvc.perform(put("/api/invoices/{id}", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoiceId)))
                .andExpect(jsonPath("$.customerName", is("Updated Customer")));

        verify(invoiceService).updateInvoice(eq(invoiceId), any(InvoiceDto.class));
    }

    @Test
    void shouldUpdateInvoiceStatusSuccessfully() throws Exception {
        StatusUpdateDto statusUpdateDto = new StatusUpdateDto();
        statusUpdateDto.setStatus(InvoiceStatus.SENT);

        InvoiceDto updatedInvoiceDto = new InvoiceDto();
        updatedInvoiceDto.setId(invoiceId);
        updatedInvoiceDto.setStatus(InvoiceStatus.SENT);

        when(invoiceService.updateInvoiceStatus(invoiceId, InvoiceStatus.SENT)).thenReturn(updatedInvoiceDto);

        mockMvc.perform(patch("/api/invoices/{id}/status", invoiceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(invoiceId)))
                .andExpect(jsonPath("$.status", is("SENT")));

        verify(invoiceService).updateInvoiceStatus(invoiceId, InvoiceStatus.SENT);
    }

    @Test
    void shouldDeleteInvoiceSuccessfully() throws Exception {
        doNothing().when(invoiceService).deleteInvoice(invoiceId);

        mockMvc.perform(delete("/api/invoices/{id}", invoiceId))
                .andExpect(status().isNoContent());

        verify(invoiceService).deleteInvoice(invoiceId);
    }

    @Test
    void shouldReturnBadRequestWhenDeletingInvoiceInFinalState() throws Exception {
        doThrow(new BadRequestException("Cannot delete invoice in final state"))
                .when(invoiceService).deleteInvoice(invoiceId);

        mockMvc.perform(delete("/api/invoices/{id}", invoiceId))
                .andExpect(status().isBadRequest());

        verify(invoiceService).deleteInvoice(invoiceId);
    }

    @Test
    void shouldGetInvoicesByStatusSuccessfully() throws Exception {
        List<InvoiceDto> invoices = Arrays.asList(invoiceDto);
        when(invoiceService.getInvoicesByStatus(eq(InvoiceStatus.DRAFT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(invoices));

        mockMvc.perform(get("/api/invoices/status/{status}", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("DRAFT")));

        verify(invoiceService).getInvoicesByStatus(eq(InvoiceStatus.DRAFT), any(Pageable.class));
    }

    @Test
    void shouldGenerateInvoiceNumberSuccessfully() throws Exception {
        String generatedNumber = "INV-2025-001";
        when(invoiceService.generateNextInvoiceNumber()).thenReturn(generatedNumber);

        mockMvc.perform(get("/api/invoices/generate-number"))
                .andExpect(status().isOk())
                .andExpect(content().string(generatedNumber));

        verify(invoiceService).generateNextInvoiceNumber();
    }
}