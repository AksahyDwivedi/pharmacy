package com.hm.pharmacy.repository;

import com.hm.pharmacy.domain.SupplierPayments;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SupplierPayments entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SupplierPaymentsRepository extends JpaRepository<SupplierPayments, Long> {}
