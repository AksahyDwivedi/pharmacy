package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.MedicineBatches;
import com.hm.pharmacy.repository.MedicineBatchesRepository;
import com.hm.pharmacy.repository.search.MedicineBatchesSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.MedicineBatches}.
 */
@RestController
@RequestMapping("/api/medicine-batches")
@Transactional
public class MedicineBatchesResource {

    private static final Logger LOG = LoggerFactory.getLogger(MedicineBatchesResource.class);

    private static final String ENTITY_NAME = "medicineBatches";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MedicineBatchesRepository medicineBatchesRepository;

    private final MedicineBatchesSearchRepository medicineBatchesSearchRepository;

    public MedicineBatchesResource(
        MedicineBatchesRepository medicineBatchesRepository,
        MedicineBatchesSearchRepository medicineBatchesSearchRepository
    ) {
        this.medicineBatchesRepository = medicineBatchesRepository;
        this.medicineBatchesSearchRepository = medicineBatchesSearchRepository;
    }

    /**
     * {@code POST  /medicine-batches} : Create a new medicineBatches.
     *
     * @param medicineBatches the medicineBatches to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new medicineBatches, or with status {@code 400 (Bad Request)} if the medicineBatches has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MedicineBatches> createMedicineBatches(@RequestBody MedicineBatches medicineBatches) throws URISyntaxException {
        LOG.debug("REST request to save MedicineBatches : {}", medicineBatches);
        if (medicineBatches.getId() != null) {
            throw new BadRequestAlertException("A new medicineBatches cannot already have an ID", ENTITY_NAME, "idexists");
        }
        medicineBatches = medicineBatchesRepository.save(medicineBatches);
        medicineBatchesSearchRepository.index(medicineBatches);
        return ResponseEntity.created(new URI("/api/medicine-batches/" + medicineBatches.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, medicineBatches.getId().toString()))
            .body(medicineBatches);
    }

    /**
     * {@code PUT  /medicine-batches/:id} : Updates an existing medicineBatches.
     *
     * @param id the id of the medicineBatches to save.
     * @param medicineBatches the medicineBatches to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medicineBatches,
     * or with status {@code 400 (Bad Request)} if the medicineBatches is not valid,
     * or with status {@code 500 (Internal Server Error)} if the medicineBatches couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicineBatches> updateMedicineBatches(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MedicineBatches medicineBatches
    ) throws URISyntaxException {
        LOG.debug("REST request to update MedicineBatches : {}, {}", id, medicineBatches);
        if (medicineBatches.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medicineBatches.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medicineBatchesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        medicineBatches = medicineBatchesRepository.save(medicineBatches);
        medicineBatchesSearchRepository.index(medicineBatches);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medicineBatches.getId().toString()))
            .body(medicineBatches);
    }

    /**
     * {@code PATCH  /medicine-batches/:id} : Partial updates given fields of an existing medicineBatches, field will ignore if it is null
     *
     * @param id the id of the medicineBatches to save.
     * @param medicineBatches the medicineBatches to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated medicineBatches,
     * or with status {@code 400 (Bad Request)} if the medicineBatches is not valid,
     * or with status {@code 404 (Not Found)} if the medicineBatches is not found,
     * or with status {@code 500 (Internal Server Error)} if the medicineBatches couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MedicineBatches> partialUpdateMedicineBatches(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MedicineBatches medicineBatches
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update MedicineBatches partially : {}, {}", id, medicineBatches);
        if (medicineBatches.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, medicineBatches.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!medicineBatchesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MedicineBatches> result = medicineBatchesRepository
            .findById(medicineBatches.getId())
            .map(existingMedicineBatches -> {
                if (medicineBatches.getBatchNumber() != null) {
                    existingMedicineBatches.setBatchNumber(medicineBatches.getBatchNumber());
                }
                if (medicineBatches.getExpiryDate() != null) {
                    existingMedicineBatches.setExpiryDate(medicineBatches.getExpiryDate());
                }
                if (medicineBatches.getQuantity() != null) {
                    existingMedicineBatches.setQuantity(medicineBatches.getQuantity());
                }

                return existingMedicineBatches;
            })
            .map(medicineBatchesRepository::save)
            .map(savedMedicineBatches -> {
                medicineBatchesSearchRepository.index(savedMedicineBatches);
                return savedMedicineBatches;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, medicineBatches.getId().toString())
        );
    }

    /**
     * {@code GET  /medicine-batches} : get all the medicineBatches.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of medicineBatches in body.
     */
    @GetMapping("")
    public List<MedicineBatches> getAllMedicineBatches() {
        LOG.debug("REST request to get all MedicineBatches");
        return medicineBatchesRepository.findAll();
    }

    /**
     * {@code GET  /medicine-batches/:id} : get the "id" medicineBatches.
     *
     * @param id the id of the medicineBatches to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the medicineBatches, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicineBatches> getMedicineBatches(@PathVariable("id") Long id) {
        LOG.debug("REST request to get MedicineBatches : {}", id);
        Optional<MedicineBatches> medicineBatches = medicineBatchesRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(medicineBatches);
    }

    /**
     * {@code DELETE  /medicine-batches/:id} : delete the "id" medicineBatches.
     *
     * @param id the id of the medicineBatches to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicineBatches(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete MedicineBatches : {}", id);
        medicineBatchesRepository.deleteById(id);
        medicineBatchesSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /medicine-batches/_search?query=:query} : search for the medicineBatches corresponding
     * to the query.
     *
     * @param query the query of the medicineBatches search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<MedicineBatches> searchMedicineBatches(@RequestParam("query") String query) {
        LOG.debug("REST request to search MedicineBatches for query {}", query);
        try {
            return StreamSupport.stream(medicineBatchesSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
