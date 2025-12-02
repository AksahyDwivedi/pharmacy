package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.Purchases;
import com.hm.pharmacy.repository.PurchasesRepository;
import com.hm.pharmacy.repository.search.PurchasesSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.Purchases}.
 */
@RestController
@RequestMapping("/api/purchases")
@Transactional
public class PurchasesResource {

    private static final Logger LOG = LoggerFactory.getLogger(PurchasesResource.class);

    private static final String ENTITY_NAME = "purchases";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PurchasesRepository purchasesRepository;

    private final PurchasesSearchRepository purchasesSearchRepository;

    public PurchasesResource(PurchasesRepository purchasesRepository, PurchasesSearchRepository purchasesSearchRepository) {
        this.purchasesRepository = purchasesRepository;
        this.purchasesSearchRepository = purchasesSearchRepository;
    }

    /**
     * {@code POST  /purchases} : Create a new purchases.
     *
     * @param purchases the purchases to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new purchases, or with status {@code 400 (Bad Request)} if the purchases has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Purchases> createPurchases(@RequestBody Purchases purchases) throws URISyntaxException {
        LOG.debug("REST request to save Purchases : {}", purchases);
        if (purchases.getId() != null) {
            throw new BadRequestAlertException("A new purchases cannot already have an ID", ENTITY_NAME, "idexists");
        }
        purchases = purchasesRepository.save(purchases);
        purchasesSearchRepository.index(purchases);
        return ResponseEntity.created(new URI("/api/purchases/" + purchases.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, purchases.getId().toString()))
            .body(purchases);
    }

    /**
     * {@code PUT  /purchases/:id} : Updates an existing purchases.
     *
     * @param id the id of the purchases to save.
     * @param purchases the purchases to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated purchases,
     * or with status {@code 400 (Bad Request)} if the purchases is not valid,
     * or with status {@code 500 (Internal Server Error)} if the purchases couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Purchases> updatePurchases(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Purchases purchases
    ) throws URISyntaxException {
        LOG.debug("REST request to update Purchases : {}, {}", id, purchases);
        if (purchases.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, purchases.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!purchasesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        purchases = purchasesRepository.save(purchases);
        purchasesSearchRepository.index(purchases);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, purchases.getId().toString()))
            .body(purchases);
    }

    /**
     * {@code PATCH  /purchases/:id} : Partial updates given fields of an existing purchases, field will ignore if it is null
     *
     * @param id the id of the purchases to save.
     * @param purchases the purchases to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated purchases,
     * or with status {@code 400 (Bad Request)} if the purchases is not valid,
     * or with status {@code 404 (Not Found)} if the purchases is not found,
     * or with status {@code 500 (Internal Server Error)} if the purchases couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Purchases> partialUpdatePurchases(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Purchases purchases
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Purchases partially : {}, {}", id, purchases);
        if (purchases.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, purchases.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!purchasesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Purchases> result = purchasesRepository
            .findById(purchases.getId())
            .map(existingPurchases -> {
                if (purchases.getPurchaseDate() != null) {
                    existingPurchases.setPurchaseDate(purchases.getPurchaseDate());
                }
                if (purchases.getInvoiceNumber() != null) {
                    existingPurchases.setInvoiceNumber(purchases.getInvoiceNumber());
                }
                if (purchases.getTotalAmount() != null) {
                    existingPurchases.setTotalAmount(purchases.getTotalAmount());
                }

                return existingPurchases;
            })
            .map(purchasesRepository::save)
            .map(savedPurchases -> {
                purchasesSearchRepository.index(savedPurchases);
                return savedPurchases;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, purchases.getId().toString())
        );
    }

    /**
     * {@code GET  /purchases} : get all the purchases.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of purchases in body.
     */
    @GetMapping("")
    public List<Purchases> getAllPurchases() {
        LOG.debug("REST request to get all Purchases");
        return purchasesRepository.findAll();
    }

    /**
     * {@code GET  /purchases/:id} : get the "id" purchases.
     *
     * @param id the id of the purchases to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the purchases, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Purchases> getPurchases(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Purchases : {}", id);
        Optional<Purchases> purchases = purchasesRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(purchases);
    }

    /**
     * {@code DELETE  /purchases/:id} : delete the "id" purchases.
     *
     * @param id the id of the purchases to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchases(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Purchases : {}", id);
        purchasesRepository.deleteById(id);
        purchasesSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /purchases/_search?query=:query} : search for the purchases corresponding
     * to the query.
     *
     * @param query the query of the purchases search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Purchases> searchPurchases(@RequestParam("query") String query) {
        LOG.debug("REST request to search Purchases for query {}", query);
        try {
            return StreamSupport.stream(purchasesSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
