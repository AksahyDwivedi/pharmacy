package com.hm.pharmacy.repository;

import com.hm.pharmacy.domain.SaleItems;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SaleItems entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SaleItemsRepository extends JpaRepository<SaleItems, Long> {}
