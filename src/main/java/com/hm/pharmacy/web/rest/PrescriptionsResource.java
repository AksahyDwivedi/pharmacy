package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.Prescriptions;
import com.hm.pharmacy.repository.PrescriptionsRepository;
import com.hm.pharmacy.repository.search.PrescriptionsSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.Prescriptions}.
 */
@RestController
@RequestMapping("/api/prescriptions")
@Transactional
public class PrescriptionsResource {

    private static final Logger LOG = LoggerFactory.getLogger(PrescriptionsResource.class);

    private static final String ENTITY_NAME = "prescriptions";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PrescriptionsRepository prescriptionsRepository;

    private final PrescriptionsSearchRepository prescriptionsSearchRepository;

    public PrescriptionsResource(
        PrescriptionsRepository prescriptionsRepository,
        PrescriptionsSearchRepository prescriptionsSearchRepository
    ) {
        this.prescriptionsRepository = prescriptionsRepository;
        this.prescriptionsSearchRepository = prescriptionsSearchRepository;
    }

    /**
     * {@code POST  /prescriptions} : Create a new prescriptions.
     *
     * @param prescriptions the prescriptions to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new prescriptions, or with status {@code 400 (Bad Request)} if the prescriptions has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Prescriptions> createPrescriptions(@RequestBody Prescriptions prescriptions) throws URISyntaxException {
        LOG.debug("REST request to save Prescriptions : {}", prescriptions);
        if (prescriptions.getId() != null) {
            throw new BadRequestAlertException("A new prescriptions cannot already have an ID", ENTITY_NAME, "idexists");
        }
        prescriptions = prescriptionsRepository.save(prescriptions);
        prescriptionsSearchRepository.index(prescriptions);
        return ResponseEntity.created(new URI("/api/prescriptions/" + prescriptions.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, prescriptions.getId().toString()))
            .body(prescriptions);
    }

    /**
     * {@code PUT  /prescriptions/:id} : Updates an existing prescriptions.
     *
     * @param id the id of the prescriptions to save.
     * @param prescriptions the prescriptions to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prescriptions,
     * or with status {@code 400 (Bad Request)} if the prescriptions is not valid,
     * or with status {@code 500 (Internal Server Error)} if the prescriptions couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Prescriptions> updatePrescriptions(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Prescriptions prescriptions
    ) throws URISyntaxException {
        LOG.debug("REST request to update Prescriptions : {}, {}", id, prescriptions);
        if (prescriptions.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prescriptions.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prescriptionsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        prescriptions = prescriptionsRepository.save(prescriptions);
        prescriptionsSearchRepository.index(prescriptions);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, prescriptions.getId().toString()))
            .body(prescriptions);
    }

    /**
     * {@code PATCH  /prescriptions/:id} : Partial updates given fields of an existing prescriptions, field will ignore if it is null
     *
     * @param id the id of the prescriptions to save.
     * @param prescriptions the prescriptions to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated prescriptions,
     * or with status {@code 400 (Bad Request)} if the prescriptions is not valid,
     * or with status {@code 404 (Not Found)} if the prescriptions is not found,
     * or with status {@code 500 (Internal Server Error)} if the prescriptions couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Prescriptions> partialUpdatePrescriptions(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Prescriptions prescriptions
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Prescriptions partially : {}, {}", id, prescriptions);
        if (prescriptions.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, prescriptions.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!prescriptionsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Prescriptions> result = prescriptionsRepository
            .findById(prescriptions.getId())
            .map(existingPrescriptions -> {
                if (prescriptions.getDoctorName() != null) {
                    existingPrescriptions.setDoctorName(prescriptions.getDoctorName());
                }
                if (prescriptions.getPrescriptionDate() != null) {
                    existingPrescriptions.setPrescriptionDate(prescriptions.getPrescriptionDate());
                }
                if (prescriptions.getNotes() != null) {
                    existingPrescriptions.setNotes(prescriptions.getNotes());
                }

                return existingPrescriptions;
            })
            .map(prescriptionsRepository::save)
            .map(savedPrescriptions -> {
                prescriptionsSearchRepository.index(savedPrescriptions);
                return savedPrescriptions;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, prescriptions.getId().toString())
        );
    }

    /**
     * {@code GET  /prescriptions} : get all the prescriptions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of prescriptions in body.
     */
    @GetMapping("")
    public List<Prescriptions> getAllPrescriptions() {
        LOG.debug("REST request to get all Prescriptions");
        return prescriptionsRepository.findAll();
    }

    /**
     * {@code GET  /prescriptions/:id} : get the "id" prescriptions.
     *
     * @param id the id of the prescriptions to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the prescriptions, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Prescriptions> getPrescriptions(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Prescriptions : {}", id);
        Optional<Prescriptions> prescriptions = prescriptionsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(prescriptions);
    }

    /**
     * {@code DELETE  /prescriptions/:id} : delete the "id" prescriptions.
     *
     * @param id the id of the prescriptions to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescriptions(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Prescriptions : {}", id);
        prescriptionsRepository.deleteById(id);
        prescriptionsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /prescriptions/_search?query=:query} : search for the prescriptions corresponding
     * to the query.
     *
     * @param query the query of the prescriptions search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Prescriptions> searchPrescriptions(@RequestParam("query") String query) {
        LOG.debug("REST request to search Prescriptions for query {}", query);
        try {
            return StreamSupport.stream(prescriptionsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
