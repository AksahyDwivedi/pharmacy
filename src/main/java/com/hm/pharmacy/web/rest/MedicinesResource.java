package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.Medicines;
import com.hm.pharmacy.repository.MedicinesRepository;
import com.hm.pharmacy.repository.search.MedicinesSearchRepository;
import com.hm.pharmacy.web.rest.errors.BadRequestAlertException;
import com.hm.pharmacy.web.rest.errors.ElasticsearchExceptionMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.hm.pharmacy.domain.Medicines}.
 */
@RestController
@RequestMapping("/api/medicines")
@Transactional
public class MedicinesResource {

    private static final Logger LOG = LoggerFactory.getLogger(MedicinesResource.class);

    private static final String ENTITY_NAME = "medicines";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MedicinesRepository medicinesRepository;

    private final MedicinesSearchRepository medicinesSearchRepository;

    public MedicinesResource(MedicinesRepository medicinesRepository, MedicinesSearchRepository medicinesSearchRepository) {
        this.medicinesRepository = medicinesRepository;
        this.medicinesSearchRepository = medicinesSearchRepository;
    }

    /**
     * {@code POST  /medicines} : Create a new medicines.
     *
     * @param medicines the medicines to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new medicines, or with status {@code 400 (Bad Request)} if the medicines has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Medicines> createMedicines(@RequestBody Medicines medicines) throws URISyntaxException {
        LOG.debug("REST request to save Medicines : {}", medicines);
        if (medicines.getId() != null) {
            throw new BadRequestAlertException("A new medicines cannot already have an ID", ENTITY_NAME, "idexists");
        }
        medicines = medicinesRepository.save(medicines);
        medicinesSearchRepository.index(medicines);
        return ResponseEntity.created(new URI("/api/medicines/" + medicines.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, medicines.getId().toString()))
            .body(medicines);
    }

    /**
     * {@code PUT  /medicines/:id} : Updates an existing medicines.
     *
     * @param id the id of the medicines to save.
     * @param medicines the medicines to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medicines,
     * or with status {@code 400 (Bad Request)} if the medicines is not valid,
     * or with status {@code 500 (Internal Server Error)} if the medicines couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Medicines> updateMedicines(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Medicines medicines
    ) throws URISyntaxException {
        LOG.debug("REST request to update Medicines : {}, {}", id, medicines);
        if (medicines.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medicines.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medicinesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        medicines = medicinesRepository.save(medicines);
        medicinesSearchRepository.index(medicines);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medicines.getId().toString()))
            .body(medicines);
    }

    /**
     * {@code PATCH  /medicines/:id} : Partial updates given fields of an existing medicines, field will ignore if it is null
     *
     * @param id the id of the medicines to save.
     * @param medicines the medicines to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medicines,
     * or with status {@code 400 (Bad Request)} if the medicines is not valid,
     * or with status {@code 404 (Not Found)} if the medicines is not found,
     * or with status {@code 500 (Internal Server Error)} if the medicines couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Medicines> partialUpdateMedicines(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Medicines medicines
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Medicines partially : {}, {}", id, medicines);
        if (medicines.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medicines.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medicinesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Medicines> result = medicinesRepository
            .findById(medicines.getId())
            .map(existingMedicines -> {
                if (medicines.getName() != null) {
                    existingMedicines.setName(medicines.getName());
                }
                if (medicines.getManufacturer() != null) {
                    existingMedicines.setManufacturer(medicines.getManufacturer());
                }
                if (medicines.getCategory() != null) {
                    existingMedicines.setCategory(medicines.getCategory());
                }
                if (medicines.getPrice() != null) {
                    existingMedicines.setPrice(medicines.getPrice());
                }
                if (medicines.getStock() != null) {
                    existingMedicines.setStock(medicines.getStock());
                }

                return existingMedicines;
            })
            .map(medicinesRepository::save)
            .map(savedMedicines -> {
                medicinesSearchRepository.index(savedMedicines);
                return savedMedicines;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medicines.getId().toString())
        );
    }

    /**
     * {@code GET  /medicines} : get all the medicines.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of medicines in body.
     */
    @GetMapping("")
    public List<Medicines> getAllMedicines() {
        LOG.debug("REST request to get all Medicines");
        return medicinesRepository.findAll();
    }

    /**
     * {@code GET  /medicines/:id} : get the "id" medicines.
     *
     * @param id the id of the medicines to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the medicines, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Medicines> getMedicines(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Medicines : {}", id);
        Optional<Medicines> medicines = medicinesRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(medicines);
    }

    /**
     * {@code DELETE  /medicines/:id} : delete the "id" medicines.
     *
     * @param id the id of the medicines to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicines(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Medicines : {}", id);
        medicinesRepository.deleteById(id);
        medicinesSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /medicines/_search?query=:query} : search for the medicines corresponding
     * to the query.
     *
     * @param query the query of the medicines search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Medicines> searchMedicines(@RequestParam("query") String query) {
        LOG.debug("REST request to search Medicines for query {}", query);
        try {
            return StreamSupport.stream(medicinesSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
