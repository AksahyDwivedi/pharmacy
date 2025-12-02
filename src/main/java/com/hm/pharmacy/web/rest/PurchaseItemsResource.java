package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.PurchaseItems;
import com.hm.pharmacy.repository.PurchaseItemsRepository;
import com.hm.pharmacy.repository.search.PurchaseItemsSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.PurchaseItems}.
 */
@RestController
@RequestMapping("/api/purchase-items")
@Transactional
public class PurchaseItemsResource {

    private static final Logger LOG = LoggerFactory.getLogger(PurchaseItemsResource.class);

    private static final String ENTITY_NAME = "purchaseItems";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PurchaseItemsRepository purchaseItemsRepository;

    private final PurchaseItemsSearchRepository purchaseItemsSearchRepository;

    public PurchaseItemsResource(
        PurchaseItemsRepository purchaseItemsRepository,
        PurchaseItemsSearchRepository purchaseItemsSearchRepository
    ) {
        this.purchaseItemsRepository = purchaseItemsRepository;
        this.purchaseItemsSearchRepository = purchaseItemsSearchRepository;
    }

    /**
     * {@code POST  /purchase-items} : Create a new purchaseItems.
     *
     * @param purchaseItems the purchaseItems to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new purchaseItems, or with status {@code 400 (Bad Request)} if the purchaseItems has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<PurchaseItems> createPurchaseItems(@RequestBody PurchaseItems purchaseItems) throws URISyntaxException {
        LOG.debug("REST request to save PurchaseItems : {}", purchaseItems);
        if (purchaseItems.getId() != null) {
            throw new BadRequestAlertException("A new purchaseItems cannot already have an ID", ENTITY_NAME, "idexists");
        }
        purchaseItems = purchaseItemsRepository.save(purchaseItems);
        purchaseItemsSearchRepository.index(purchaseItems);
        return ResponseEntity.created(new URI("/api/purchase-items/" + purchaseItems.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, purchaseItems.getId().toString()))
            .body(purchaseItems);
    }

    /**
     * {@code PUT  /purchase-items/:id} : Updates an existing purchaseItems.
     *
     * @param id the id of the purchaseItems to save.
     * @param purchaseItems the purchaseItems to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated purchaseItems,
     * or with status {@code 400 (Bad Request)} if the purchaseItems is not valid,
     * or with status {@code 500 (Internal Server Error)} if the purchaseItems couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseItems> updatePurchaseItems(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody PurchaseItems purchaseItems
    ) throws URISyntaxException {
        LOG.debug("REST request to update PurchaseItems : {}, {}", id, purchaseItems);
        if (purchaseItems.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, purchaseItems.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!purchaseItemsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        purchaseItems = purchaseItemsRepository.save(purchaseItems);
        purchaseItemsSearchRepository.index(purchaseItems);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, purchaseItems.getId().toString()))
            .body(purchaseItems);
    }

    /**
     * {@code PATCH  /purchase-items/:id} : Partial updates given fields of an existing purchaseItems, field will ignore if it is null
     *
     * @param id the id of the purchaseItems to save.
     * @param purchaseItems the purchaseItems to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated purchaseItems,
     * or with status {@code 400 (Bad Request)} if the purchaseItems is not valid,
     * or with status {@code 404 (Not Found)} if the purchaseItems is not found,
     * or with status {@code 500 (Internal Server Error)} if the purchaseItems couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<PurchaseItems> partialUpdatePurchaseItems(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody PurchaseItems purchaseItems
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update PurchaseItems partially : {}, {}", id, purchaseItems);
        if (purchaseItems.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, purchaseItems.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!purchaseItemsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PurchaseItems> result = purchaseItemsRepository
            .findById(purchaseItems.getId())
            .map(existingPurchaseItems -> {
                if (purchaseItems.getQuantity() != null) {
                    existingPurchaseItems.setQuantity(purchaseItems.getQuantity());
                }
                if (purchaseItems.getPrice() != null) {
                    existingPurchaseItems.setPrice(purchaseItems.getPrice());
                }

                return existingPurchaseItems;
            })
            .map(purchaseItemsRepository::save)
            .map(savedPurchaseItems -> {
                purchaseItemsSearchRepository.index(savedPurchaseItems);
                return savedPurchaseItems;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, purchaseItems.getId().toString())
        );
    }

    /**
     * {@code GET  /purchase-items} : get all the purchaseItems.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of purchaseItems in body.
     */
    @GetMapping("")
    public List<PurchaseItems> getAllPurchaseItems() {
        LOG.debug("REST request to get all PurchaseItems");
        return purchaseItemsRepository.findAll();
    }

    /**
     * {@code GET  /purchase-items/:id} : get the "id" purchaseItems.
     *
     * @param id the id of the purchaseItems to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the purchaseItems, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseItems> getPurchaseItems(@PathVariable("id") Long id) {
        LOG.debug("REST request to get PurchaseItems : {}", id);
        Optional<PurchaseItems> purchaseItems = purchaseItemsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(purchaseItems);
    }

    /**
     * {@code DELETE  /purchase-items/:id} : delete the "id" purchaseItems.
     *
     * @param id the id of the purchaseItems to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseItems(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete PurchaseItems : {}", id);
        purchaseItemsRepository.deleteById(id);
        purchaseItemsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /purchase-items/_search?query=:query} : search for the purchaseItems corresponding
     * to the query.
     *
     * @param query the query of the purchaseItems search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<PurchaseItems> searchPurchaseItems(@RequestParam("query") String query) {
        LOG.debug("REST request to search PurchaseItems for query {}", query);
        try {
            return StreamSupport.stream(purchaseItemsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
