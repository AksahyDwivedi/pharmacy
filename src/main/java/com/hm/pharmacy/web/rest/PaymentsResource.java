package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.Payments;
import com.hm.pharmacy.repository.PaymentsRepository;
import com.hm.pharmacy.repository.search.PaymentsSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.Payments}.
 */
@RestController
@RequestMapping("/api/payments")
@Transactional
public class PaymentsResource {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentsResource.class);

    private static final String ENTITY_NAME = "payments";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PaymentsRepository paymentsRepository;

    private final PaymentsSearchRepository paymentsSearchRepository;

    public PaymentsResource(PaymentsRepository paymentsRepository, PaymentsSearchRepository paymentsSearchRepository) {
        this.paymentsRepository = paymentsRepository;
        this.paymentsSearchRepository = paymentsSearchRepository;
    }

    /**
     * {@code POST  /payments} : Create a new payments.
     *
     * @param payments the payments to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new payments, or with status {@code 400 (Bad Request)} if the payments has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Payments> createPayments(@RequestBody Payments payments) throws URISyntaxException {
        LOG.debug("REST request to save Payments : {}", payments);
        if (payments.getId() != null) {
            throw new BadRequestAlertException("A new payments cannot already have an ID", ENTITY_NAME, "idexists");
        }
        payments = paymentsRepository.save(payments);
        paymentsSearchRepository.index(payments);
        return ResponseEntity.created(new URI("/api/payments/" + payments.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, payments.getId().toString()))
            .body(payments);
    }

    /**
     * {@code PUT  /payments/:id} : Updates an existing payments.
     *
     * @param id the id of the payments to save.
     * @param payments the payments to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated payments,
     * or with status {@code 400 (Bad Request)} if the payments is not valid,
     * or with status {@code 500 (Internal Server Error)} if the payments couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Payments> updatePayments(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Payments payments
    ) throws URISyntaxException {
        LOG.debug("REST request to update Payments : {}, {}", id, payments);
        if (payments.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, payments.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!paymentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        payments = paymentsRepository.save(payments);
        paymentsSearchRepository.index(payments);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, payments.getId().toString()))
            .body(payments);
    }

    /**
     * {@code PATCH  /payments/:id} : Partial updates given fields of an existing payments, field will ignore if it is null
     *
     * @param id the id of the payments to save.
     * @param payments the payments to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated payments,
     * or with status {@code 400 (Bad Request)} if the payments is not valid,
     * or with status {@code 404 (Not Found)} if the payments is not found,
     * or with status {@code 500 (Internal Server Error)} if the payments couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Payments> partialUpdatePayments(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Payments payments
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Payments partially : {}, {}", id, payments);
        if (payments.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, payments.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!paymentsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Payments> result = paymentsRepository
            .findById(payments.getId())
            .map(existingPayments -> {
                if (payments.getPaymentDate() != null) {
                    existingPayments.setPaymentDate(payments.getPaymentDate());
                }
                if (payments.getPaymentMethod() != null) {
                    existingPayments.setPaymentMethod(payments.getPaymentMethod());
                }
                if (payments.getPaymentStatus() != null) {
                    existingPayments.setPaymentStatus(payments.getPaymentStatus());
                }
                if (payments.getAmount() != null) {
                    existingPayments.setAmount(payments.getAmount());
                }

                return existingPayments;
            })
            .map(paymentsRepository::save)
            .map(savedPayments -> {
                paymentsSearchRepository.index(savedPayments);
                return savedPayments;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, payments.getId().toString())
        );
    }

    /**
     * {@code GET  /payments} : get all the payments.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of payments in body.
     */
    @GetMapping("")
    public List<Payments> getAllPayments() {
        LOG.debug("REST request to get all Payments");
        return paymentsRepository.findAll();
    }

    /**
     * {@code GET  /payments/:id} : get the "id" payments.
     *
     * @param id the id of the payments to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the payments, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Payments> getPayments(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Payments : {}", id);
        Optional<Payments> payments = paymentsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(payments);
    }

    /**
     * {@code DELETE  /payments/:id} : delete the "id" payments.
     *
     * @param id the id of the payments to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayments(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Payments : {}", id);
        paymentsRepository.deleteById(id);
        paymentsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /payments/_search?query=:query} : search for the payments corresponding
     * to the query.
     *
     * @param query the query of the payments search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Payments> searchPayments(@RequestParam("query") String query) {
        LOG.debug("REST request to search Payments for query {}", query);
        try {
            return StreamSupport.stream(paymentsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
