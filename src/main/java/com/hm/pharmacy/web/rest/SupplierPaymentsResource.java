package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.SupplierPayments;
import com.hm.pharmacy.repository.SupplierPaymentsRepository;
import com.hm.pharmacy.repository.search.SupplierPaymentsSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.SupplierPayments}.
 */
@RestController
@RequestMapping("/api/supplier-payments")
@Transactional
public class SupplierPaymentsResource {

    private static final Logger LOG = LoggerFactory.getLogger(SupplierPaymentsResource.class);

    private static final String ENTITY_NAME = "supplierPayments";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SupplierPaymentsRepository supplierPaymentsRepository;

    private final SupplierPaymentsSearchRepository supplierPaymentsSearchRepository;

    public SupplierPaymentsResource(
        SupplierPaymentsRepository supplierPaymentsRepository,
        SupplierPaymentsSearchRepository supplierPaymentsSearchRepository
    ) {
        this.supplierPaymentsRepository = supplierPaymentsRepository;
        this.supplierPaymentsSearchRepository = supplierPaymentsSearchRepository;
    }

    /**
     * {@code POST  /supplier-payments} : Create a new supplierPayments.
     *
     * @param supplierPayments the supplierPayments to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new supplierPayments, or with status {@code 400 (Bad Request)} if the supplierPayments has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SupplierPayments> createSupplierPayments(@RequestBody SupplierPayments supplierPayments)
        throws URISyntaxException {
        LOG.debug("REST request to save SupplierPayments : {}", supplierPayments);
        if (supplierPayments.getId() != null) {
            throw new BadRequestAlertException("A new supplierPayments cannot already have an ID", ENTITY_NAME, "idexists");
        }
        supplierPayments = supplierPaymentsRepository.save(supplierPayments);
        supplierPaymentsSearchRepository.index(supplierPayments);
        return ResponseEntity.created(new URI("/api/supplier-payments/" + supplierPayments.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, supplierPayments.getId().toString()))
            .body(supplierPayments);
    }

    /**
     * {@code PUT  /supplier-payments/:id} : Updates an existing supplierPayments.
     *
     * @param id the id of the supplierPayments to save.
     * @param supplierPayments the supplierPayments to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated supplierPayments,
     * or with status {@code 400 (Bad Request)} if the supplierPayments is not valid,
     * or with status {@code 500 (Internal Server Error)} if the supplierPayments couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplierPayments> updateSupplierPayments(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SupplierPayments supplierPayments
    ) throws URISyntaxException {
        LOG.debug("REST request to update SupplierPayments : {}, {}", id, supplierPayments);
        if (supplierPayments.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, supplierPayments.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!supplierPaymentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        supplierPayments = supplierPaymentsRepository.save(supplierPayments);
        supplierPaymentsSearchRepository.index(supplierPayments);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, supplierPayments.getId().toString()))
            .body(supplierPayments);
    }

    /**
     * {@code PATCH  /supplier-payments/:id} : Partial updates given fields of an existing supplierPayments, field will ignore if it is null
     *
     * @param id the id of the supplierPayments to save.
     * @param supplierPayments the supplierPayments to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated supplierPayments,
     * or with status {@code 400 (Bad Request)} if the supplierPayments is not valid,
     * or with status {@code 404 (Not Found)} if the supplierPayments is not found,
     * or with status {@code 500 (Internal Server Error)} if the supplierPayments couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SupplierPayments> partialUpdateSupplierPayments(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SupplierPayments supplierPayments
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SupplierPayments partially : {}, {}", id, supplierPayments);
        if (supplierPayments.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, supplierPayments.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!supplierPaymentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SupplierPayments> result = supplierPaymentsRepository
            .findById(supplierPayments.getId())
            .map(existingSupplierPayments -> {
                if (supplierPayments.getPaymentDate() != null) {
                    existingSupplierPayments.setPaymentDate(supplierPayments.getPaymentDate());
                }
                if (supplierPayments.getPaymentMethod() != null) {
                    existingSupplierPayments.setPaymentMethod(supplierPayments.getPaymentMethod());
                }
                if (supplierPayments.getPaymentStatus() != null) {
                    existingSupplierPayments.setPaymentStatus(supplierPayments.getPaymentStatus());
                }
                if (supplierPayments.getAmountPaid() != null) {
                    existingSupplierPayments.setAmountPaid(supplierPayments.getAmountPaid());
                }

                return existingSupplierPayments;
            })
            .map(supplierPaymentsRepository::save)
            .map(savedSupplierPayments -> {
                supplierPaymentsSearchRepository.index(savedSupplierPayments);
                return savedSupplierPayments;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, supplierPayments.getId().toString())
        );
    }

    /**
     * {@code GET  /supplier-payments} : get all the supplierPayments.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of supplierPayments in body.
     */
    @GetMapping("")
    public List<SupplierPayments> getAllSupplierPayments() {
        LOG.debug("REST request to get all SupplierPayments");
        return supplierPaymentsRepository.findAll();
    }

    /**
     * {@code GET  /supplier-payments/:id} : get the "id" supplierPayments.
     *
     * @param id the id of the supplierPayments to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the supplierPayments, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierPayments> getSupplierPayments(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SupplierPayments : {}", id);
        Optional<SupplierPayments> supplierPayments = supplierPaymentsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(supplierPayments);
    }

    /**
     * {@code DELETE  /supplier-payments/:id} : delete the "id" supplierPayments.
     *
     * @param id the id of the supplierPayments to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplierPayments(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SupplierPayments : {}", id);
        supplierPaymentsRepository.deleteById(id);
        supplierPaymentsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /supplier-payments/_search?query=:query} : search for the supplierPayments corresponding
     * to the query.
     *
     * @param query the query of the supplierPayments search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<SupplierPayments> searchSupplierPayments(@RequestParam("query") String query) {
        LOG.debug("REST request to search SupplierPayments for query {}", query);
        try {
            return StreamSupport.stream(supplierPaymentsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
