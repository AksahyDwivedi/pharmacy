package com.hm.pharmacy.repository;

import com.hm.pharmacy.domain.Prescriptions;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Prescriptions entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PrescriptionsRepository extends JpaRepository<Prescriptions, Long> {}
