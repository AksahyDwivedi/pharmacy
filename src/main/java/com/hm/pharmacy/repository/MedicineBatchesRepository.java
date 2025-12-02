package com.hm.pharmacy.repository;

import com.hm.pharmacy.domain.MedicineBatches;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MedicineBatches entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MedicineBatchesRepository extends JpaRepository<MedicineBatches, Long> {}
