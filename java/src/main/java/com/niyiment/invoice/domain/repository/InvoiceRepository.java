package com.niyiment.invoice.domain.repository;

import com.niyiment.invoice.domain.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.niyiment.invoice.domain.entity.Invoice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByCustomerEmail(String customerEmail, Pageable pageable);

    Page<Invoice> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Invoice> findByTotalAmountGreaterThanEqual(double amount, Pageable pageable);

    Page<Invoice> findByTotalAmountLessThanEqual(double amount, Pageable pageable);

    @Query("{'customerName': {$regex: ?0, $options: 'i'}}")
    Page<Invoice> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);

    @Query("{'dueDate': {$lt:?0}, 'status': {$nin: ['PAID', 'CANCELLED']} }")
    Page<Invoice> findOverdueInvoices(LocalDateTime now, Pageable pageable);


    @Query("""
           { $and[
            { $or: [ {'customerName': {$regex: ?0, $options: 'i' }}, {?0: null} ] },
            { $or: [ {'status': ?1 }, {?1: null} ] },
            { $or: [ {'dueDate': {$gte: ?2 }}, {?2: null} ] },
            { $or: [ {'dueDate': {$lte: ?3 }}, {?3: null} ] },
            { $or: [ {'totalAmount': {$gte: ?4 }}, {?4: null} ] },
            { $or: [ {'totalAmount': {$lte: ?5 }}, {?5: null} ] },
            ] }
            """)
    List<Invoice> advanceSearch(String clientName, InvoiceStatus status, LocalDateTime startDate,
                                LocalDateTime endDate, Double minAmount, Double maxAmount);
}
