package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.Sales;
import com.hm.pharmacy.repository.SalesRepository;
import com.hm.pharmacy.repository.search.SalesSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.Sales}.
 */
@RestController
@RequestMapping("/api/sales")
@Transactional
public class SalesResource {

    private static final Logger LOG = LoggerFactory.getLogger(SalesResource.class);

    private static final String ENTITY_NAME = "sales";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SalesRepository salesRepository;

    private final SalesSearchRepository salesSearchRepository;

    public SalesResource(SalesRepository salesRepository, SalesSearchRepository salesSearchRepository) {
        this.salesRepository = salesRepository;
        this.salesSearchRepository = salesSearchRepository;
    }

    /**
     * {@code POST  /sales} : Create a new sales.
     *
     * @param sales the sales to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new sales, or with status {@code 400 (Bad Request)} if the sales has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Sales> createSales(@RequestBody Sales sales) throws URISyntaxException {
        LOG.debug("REST request to save Sales : {}", sales);
        if (sales.getId() != null) {
            throw new BadRequestAlertException("A new sales cannot already have an ID", ENTITY_NAME, "idexists");
        }
        sales = salesRepository.save(sales);
        salesSearchRepository.index(sales);
        return ResponseEntity.created(new URI("/api/sales/" + sales.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, sales.getId().toString()))
            .body(sales);
    }

    /**
     * {@code PUT  /sales/:id} : Updates an existing sales.
     *
     * @param id the id of the sales to save.
     * @param sales the sales to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sales,
     * or with status {@code 400 (Bad Request)} if the sales is not valid,
     * or with status {@code 500 (Internal Server Error)} if the sales couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Sales> updateSales(@PathVariable(value = "id", required = false) final Long id, @RequestBody Sales sales)
        throws URISyntaxException {
        LOG.debug("REST request to update Sales : {}, {}", id, sales);
        if (sales.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sales.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!salesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        sales = salesRepository.save(sales);
        salesSearchRepository.index(sales);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sales.getId().toString()))
            .body(sales);
    }

    /**
     * {@code PATCH  /sales/:id} : Partial updates given fields of an existing sales, field will ignore if it is null
     *
     * @param id the id of the sales to save.
     * @param sales the sales to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sales,
     * or with status {@code 400 (Bad Request)} if the sales is not valid,
     * or with status {@code 404 (Not Found)} if the sales is not found,
     * or with status {@code 500 (Internal Server Error)} if the sales couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Sales> partialUpdateSales(@PathVariable(value = "id", required = false) final Long id, @RequestBody Sales sales)
        throws URISyntaxException {
        LOG.debug("REST request to partial update Sales partially : {}, {}", id, sales);
        if (sales.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sales.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!salesRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Sales> result = salesRepository
            .findById(sales.getId())
            .map(existingSales -> {
                if (sales.getSaleDate() != null) {
                    existingSales.setSaleDate(sales.getSaleDate());
                }
                if (sales.getInvoiceNumber() != null) {
                    existingSales.setInvoiceNumber(sales.getInvoiceNumber());
                }
                if (sales.getTotalAmount() != null) {
                    existingSales.setTotalAmount(sales.getTotalAmount());
                }

                return existingSales;
            })
            .map(salesRepository::save)
            .map(savedSales -> {
                salesSearchRepository.index(savedSales);
                return savedSales;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sales.getId().toString())
        );
    }

    /**
     * {@code GET  /sales} : get all the sales.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of sales in body.
     */
    @GetMapping("")
    public List<Sales> getAllSales() {
        LOG.debug("REST request to get all Sales");
        return salesRepository.findAll();
    }

    /**
     * {@code GET  /sales/:id} : get the "id" sales.
     *
     * @param id the id of the sales to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the sales, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sales> getSales(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Sales : {}", id);
        Optional<Sales> sales = salesRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(sales);
    }

    /**
     * {@code DELETE  /sales/:id} : delete the "id" sales.
     *
     * @param id the id of the sales to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSales(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Sales : {}", id);
        salesRepository.deleteById(id);
        salesSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /sales/_search?query=:query} : search for the sales corresponding
     * to the query.
     *
     * @param query the query of the sales search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Sales> searchSales(@RequestParam("query") String query) {
        LOG.debug("REST request to search Sales for query {}", query);
        try {
            return StreamSupport.stream(salesSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
