package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.SaleItems;
import com.hm.pharmacy.repository.SaleItemsRepository;
import com.hm.pharmacy.repository.search.SaleItemsSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.SaleItems}.
 */
@RestController
@RequestMapping("/api/sale-items")
@Transactional
public class SaleItemsResource {

    private static final Logger LOG = LoggerFactory.getLogger(SaleItemsResource.class);

    private static final String ENTITY_NAME = "saleItems";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SaleItemsRepository saleItemsRepository;

    private final SaleItemsSearchRepository saleItemsSearchRepository;

    public SaleItemsResource(SaleItemsRepository saleItemsRepository, SaleItemsSearchRepository saleItemsSearchRepository) {
        this.saleItemsRepository = saleItemsRepository;
        this.saleItemsSearchRepository = saleItemsSearchRepository;
    }

    /**
     * {@code POST  /sale-items} : Create a new saleItems.
     *
     * @param saleItems the saleItems to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new saleItems, or with status {@code 400 (Bad Request)} if the saleItems has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SaleItems> createSaleItems(@RequestBody SaleItems saleItems) throws URISyntaxException {
        LOG.debug("REST request to save SaleItems : {}", saleItems);
        if (saleItems.getId() != null) {
            throw new BadRequestAlertException("A new saleItems cannot already have an ID", ENTITY_NAME, "idexists");
        }
        saleItems = saleItemsRepository.save(saleItems);
        saleItemsSearchRepository.index(saleItems);
        return ResponseEntity.created(new URI("/api/sale-items/" + saleItems.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, saleItems.getId().toString()))
            .body(saleItems);
    }

    /**
     * {@code PUT  /sale-items/:id} : Updates an existing saleItems.
     *
     * @param id the id of the saleItems to save.
     * @param saleItems the saleItems to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated saleItems,
     * or with status {@code 400 (Bad Request)} if the saleItems is not valid,
     * or with status {@code 500 (Internal Server Error)} if the saleItems couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SaleItems> updateSaleItems(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SaleItems saleItems
    ) throws URISyntaxException {
        LOG.debug("REST request to update SaleItems : {}, {}", id, saleItems);
        if (saleItems.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, saleItems.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!saleItemsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        saleItems = saleItemsRepository.save(saleItems);
        saleItemsSearchRepository.index(saleItems);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, saleItems.getId().toString()))
            .body(saleItems);
    }

    /**
     * {@code PATCH  /sale-items/:id} : Partial updates given fields of an existing saleItems, field will ignore if it is null
     *
     * @param id the id of the saleItems to save.
     * @param saleItems the saleItems to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated saleItems,
     * or with status {@code 400 (Bad Request)} if the saleItems is not valid,
     * or with status {@code 404 (Not Found)} if the saleItems is not found,
     * or with status {@code 500 (Internal Server Error)} if the saleItems couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SaleItems> partialUpdateSaleItems(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SaleItems saleItems
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SaleItems partially : {}, {}", id, saleItems);
        if (saleItems.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, saleItems.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!saleItemsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SaleItems> result = saleItemsRepository
            .findById(saleItems.getId())
            .map(existingSaleItems -> {
                if (saleItems.getQuantity() != null) {
                    existingSaleItems.setQuantity(saleItems.getQuantity());
                }
                if (saleItems.getPrice() != null) {
                    existingSaleItems.setPrice(saleItems.getPrice());
                }

                return existingSaleItems;
            })
            .map(saleItemsRepository::save)
            .map(savedSaleItems -> {
                saleItemsSearchRepository.index(savedSaleItems);
                return savedSaleItems;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, saleItems.getId().toString())
        );
    }

    /**
     * {@code GET  /sale-items} : get all the saleItems.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of saleItems in body.
     */
    @GetMapping("")
    public List<SaleItems> getAllSaleItems() {
        LOG.debug("REST request to get all SaleItems");
        return saleItemsRepository.findAll();
    }

    /**
     * {@code GET  /sale-items/:id} : get the "id" saleItems.
     *
     * @param id the id of the saleItems to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the saleItems, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SaleItems> getSaleItems(@PathVariable("id") Long id) {
        LOG.debug("REST request to get SaleItems : {}", id);
        Optional<SaleItems> saleItems = saleItemsRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(saleItems);
    }

    /**
     * {@code DELETE  /sale-items/:id} : delete the "id" saleItems.
     *
     * @param id the id of the saleItems to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleItems(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete SaleItems : {}", id);
        saleItemsRepository.deleteById(id);
        saleItemsSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /sale-items/_search?query=:query} : search for the saleItems corresponding
     * to the query.
     *
     * @param query the query of the saleItems search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<SaleItems> searchSaleItems(@RequestParam("query") String query) {
        LOG.debug("REST request to search SaleItems for query {}", query);
        try {
            return StreamSupport.stream(saleItemsSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
