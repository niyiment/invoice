package com.niyiment.invoice.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.niyiment.invoice.domain.dto.InvoiceDto;
import com.niyiment.invoice.domain.dto.InvoiceItemDto;
import com.niyiment.invoice.domain.entity.Invoice;
import com.niyiment.invoice.domain.entity.InvoiceItem;
import com.niyiment.invoice.domain.enums.InvoiceStatus;
import com.niyiment.invoice.domain.mapper.InvoiceMapper;
import com.niyiment.invoice.domain.repository.InvoiceRepository;
import com.niyiment.invoice.exception.InvoiceNotFoundException;
import com.niyiment.invoice.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Invoice invoice;
    private InvoiceDto invoiceDto;
    private String invoiceId;
    private String invoiceNumber;

    @BeforeEach
    void setUp() {
        invoiceId = "1";
        invoiceNumber = "INV-2025-001";
        invoice = new Invoice(invoiceNumber);
        invoice.setId(invoiceId);
        invoice.setCustomerName("Test Customer");
        invoice.setCustomerEmail("customer@example.com");
        invoice.setItems(List.of(new InvoiceItem("Item 1", 2, 10.0)));
        invoice.setTaxRate(10.0);
        invoice.reCalculateAmount();

        invoiceDto = new InvoiceDto();
        invoiceDto.setId(invoiceId);
        invoiceDto.setInvoiceNumber(invoiceNumber);
        invoiceDto.setCustomerName("Test Customer");
        invoiceDto.setCustomerEmail("customer@example.com");

        InvoiceItemDto itemDto = new InvoiceItemDto();
        itemDto.setDescription("Item 1");
        itemDto.setQuantity(2);
        itemDto.setUnitPrice(10.0);
        invoiceDto.setItems(List.of(itemDto));

        invoiceDto.setTaxRate(10.0);
        invoiceDto.setSubtotal(20.0);
        invoiceDto.setTaxAmount(2.0);
        invoiceDto.setTotalAmount(22.0);
    }

    @Test
    void shouldCreateInvoiceSuccessfully() {
        when(invoiceRepository.existsByInvoiceNumber(invoiceNumber)).thenReturn(false);
        when(invoiceMapper.toEntity(invoiceDto)).thenReturn(invoice);
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);

        InvoiceDto result = invoiceService.createInvoice(invoiceDto);

        assertNotNull(result);
        assertEquals(invoiceDto, result);
        verify(invoiceRepository).existsByInvoiceNumber(invoiceNumber);
        verify(invoiceMapper).toEntity(invoiceDto);
        verify(invoiceRepository).save(invoice);
        verify(invoiceMapper).toDto(invoice);
    }

    @Test
    void shouldGenerateInvoiceNumberWhenNotProvided() {
        invoiceDto.setInvoiceNumber(null);

        when(invoiceRepository.findAll()).thenReturn(Arrays.asList());
        when(invoiceMapper.toEntity(invoiceDto)).thenReturn(invoice);
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);

        invoiceService.createInvoice(invoiceDto);

        verify(invoiceMapper).toEntity(invoiceDto);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void shouldThrowExceptionWhenInvoiceNumberExists() {
        when(invoiceRepository.existsByInvoiceNumber(invoiceNumber)).thenReturn(true);
        
        assertThrows(BadRequestException.class, () -> {
            invoiceService.createInvoice(invoiceDto);
        });

        verify(invoiceRepository).existsByInvoiceNumber(invoiceNumber);
        verify(invoiceMapper, never()).toEntity(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void shouldGetInvoiceByIdSuccessfully() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);

        InvoiceDto result = invoiceService.getInvoiceById(invoiceId);
        
        assertNotNull(result);
        assertEquals(invoiceDto, result);
        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceMapper).toDto(invoice);
    }

    @Test
    void shouldThrowExceptionWhenInvoiceNotFoundById() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> {
            invoiceService.getInvoiceById(invoiceId);
        });

        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceMapper, never()).toDto(any());
    }

    @Test
    void shouldGetInvoiceByNumberSuccessfully() {
        when(invoiceRepository.findByInvoiceNumber(invoiceNumber)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);

        InvoiceDto result = invoiceService.getInvoiceByNumber(invoiceNumber);

        assertNotNull(result);
        assertEquals(invoiceDto, result);
        verify(invoiceRepository).findByInvoiceNumber(invoiceNumber);
        verify(invoiceMapper).toDto(invoice);
    }

    @Test
    void shouldGetAllInvoicesSuccessfully() {
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Invoice> invoices = Arrays.asList(invoice);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());

        when(invoiceRepository.findAll(pageable)).thenReturn(invoicePage);
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);

        
        Page<InvoiceDto> result = invoiceService.getAllInvoices(pageable);

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(invoiceDto, result.getContent().get(0));
        verify(invoiceRepository).findAll(pageable);
        verify(invoiceMapper).toDto(invoice);
    }

    @Test
    void shouldUpdateInvoiceSuccessfully() {
        
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceMapper.updateEntityFromDto(invoice, invoiceDto)).thenReturn(invoice);
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);

        
        InvoiceDto result = invoiceService.updateInvoice(invoiceId, invoiceDto);

        
        assertNotNull(result);
        assertEquals(invoiceDto, result);
        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceMapper).updateEntityFromDto(invoice, invoiceDto);
        verify(invoiceRepository).save(invoice);
        verify(invoiceMapper).toDto(invoice);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingInvoiceInFinalState() {
        
        invoice.setStatus(InvoiceStatus.PAID); // Final state

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        assertThrows(BadRequestException.class, () -> {
            invoiceService.updateInvoice(invoiceId, invoiceDto);
        });

        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceMapper, never()).updateEntityFromDto(any(), any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void shouldUpdateInvoiceStatusSuccessfully() {
        InvoiceStatus newStatus = InvoiceStatus.SENT;
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);
        
        InvoiceDto result = invoiceService.updateInvoiceStatus(invoiceId, newStatus);

        assertNotNull(result);
        assertEquals(invoiceDto, result);
        assertEquals(newStatus, invoice.getStatus());
        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceRepository).save(invoice);
        verify(invoiceMapper).toDto(invoice);
    }

    @Test
    void shouldThrowExceptionWhenStatusTransitionIsInvalid() {
        invoice.setStatus(InvoiceStatus.PAID);
        InvoiceStatus newStatus = InvoiceStatus.SENT;

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        assertThrows(BadRequestException.class, () -> {
            invoiceService.updateInvoiceStatus(invoiceId, newStatus);
        });

        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void shouldDeleteInvoiceSuccessfully() {
        
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        
        invoiceService.deleteInvoice(invoiceId);

        
        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceRepository).deleteById(invoiceId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingInvoiceInFinalState() {
        
        invoice.setStatus(InvoiceStatus.PAID); // Final state

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        assertThrows(BadRequestException.class, () -> {
            invoiceService.deleteInvoice(invoiceId);
        });

        verify(invoiceRepository).findById(invoiceId);
        verify(invoiceRepository, never()).deleteById(any());
    }

    @Test
    void shouldGetInvoicesByStatusSuccessfully() {
        
        Pageable pageable = PageRequest.of(0, 10);
        List<Invoice> invoices = Arrays.asList(invoice);
        Page<Invoice> invoicePage = new PageImpl<>(invoices, pageable, invoices.size());
        InvoiceStatus status = InvoiceStatus.DRAFT;

        when(invoiceRepository.findByStatus(status, pageable)).thenReturn(invoicePage);
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDto);
        
        Page<InvoiceDto> result = invoiceService.getInvoicesByStatus(status, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(invoiceDto, result.getContent().get(0));
        verify(invoiceRepository).findByStatus(status, pageable);
        verify(invoiceMapper).toDto(invoice);
    }

    @Test
    void shouldGenerateNextInvoiceNumberCorrectly() {
        
        Invoice invoice1 = new Invoice("INV-2025-03-001");
        Invoice invoice2 = new Invoice("INV-2025-03-002");

        when(invoiceRepository.findAll()).thenReturn(Arrays.asList(invoice1, invoice2));

        
        String result = invoiceService.generateNextInvoiceNumber();
        
        assertTrue(result.matches("INV-\\d{4}-\\d{2}-004"));
        verify(invoiceRepository).findAll();
    }
}

