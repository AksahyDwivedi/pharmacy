package com.hm.pharmacy.repository;

import com.hm.pharmacy.domain.Purchases;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Purchases entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PurchasesRepository extends JpaRepository<Purchases, Long> {}
