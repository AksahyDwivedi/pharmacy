package com.hm.pharmacy.repository;

import com.hm.pharmacy.domain.Medicines;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Medicines entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MedicinesRepository extends JpaRepository<Medicines, Long> {}
