package com.hm.pharmacy.repository;

import com.hm.pharmacy.domain.PurchaseItems;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PurchaseItems entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PurchaseItemsRepository extends JpaRepository<PurchaseItems, Long> {}
