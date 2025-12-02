package com.hm.pharmacy.web.rest;

import com.hm.pharmacy.domain.Suppliers;
import com.hm.pharmacy.repository.SuppliersRepository;
import com.hm.pharmacy.repository.search.SuppliersSearchRepository;
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
 * REST controller for managing {@link com.hm.pharmacy.domain.Suppliers}.
 */
@RestController
@RequestMapping("/api/suppliers")
@Transactional
public class SuppliersResource {

    private static final Logger LOG = LoggerFactory.getLogger(SuppliersResource.class);

    private static final String ENTITY_NAME = "suppliers";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SuppliersRepository suppliersRepository;

    private final SuppliersSearchRepository suppliersSearchRepository;

    public SuppliersResource(SuppliersRepository suppliersRepository, SuppliersSearchRepository suppliersSearchRepository) {
        this.suppliersRepository = suppliersRepository;
        this.suppliersSearchRepository = suppliersSearchRepository;
    }

    /**
     * {@code POST  /suppliers} : Create a new suppliers.
     *
     * @param suppliers the suppliers to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new suppliers, or with status {@code 400 (Bad Request)} if the suppliers has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Suppliers> createSuppliers(@RequestBody Suppliers suppliers) throws URISyntaxException {
        LOG.debug("REST request to save Suppliers : {}", suppliers);
        if (suppliers.getId() != null) {
            throw new BadRequestAlertException("A new suppliers cannot already have an ID", ENTITY_NAME, "idexists");
        }
        suppliers = suppliersRepository.save(suppliers);
        suppliersSearchRepository.index(suppliers);
        return ResponseEntity.created(new URI("/api/suppliers/" + suppliers.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, suppliers.getId().toString()))
            .body(suppliers);
    }

    /**
     * {@code PUT  /suppliers/:id} : Updates an existing suppliers.
     *
     * @param id the id of the suppliers to save.
     * @param suppliers the suppliers to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated suppliers,
     * or with status {@code 400 (Bad Request)} if the suppliers is not valid,
     * or with status {@code 500 (Internal Server Error)} if the suppliers couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Suppliers> updateSuppliers(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Suppliers suppliers
    ) throws URISyntaxException {
        LOG.debug("REST request to update Suppliers : {}, {}", id, suppliers);
        if (suppliers.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, suppliers.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!suppliersRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        suppliers = suppliersRepository.save(suppliers);
        suppliersSearchRepository.index(suppliers);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, suppliers.getId().toString()))
            .body(suppliers);
    }

    /**
     * {@code PATCH  /suppliers/:id} : Partial updates given fields of an existing suppliers, field will ignore if it is null
     *
     * @param id the id of the suppliers to save.
     * @param suppliers the suppliers to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated suppliers,
     * or with status {@code 400 (Bad Request)} if the suppliers is not valid,
     * or with status {@code 404 (Not Found)} if the suppliers is not found,
     * or with status {@code 500 (Internal Server Error)} if the suppliers couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Suppliers> partialUpdateSuppliers(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Suppliers suppliers
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Suppliers partially : {}, {}", id, suppliers);
        if (suppliers.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, suppliers.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!suppliersRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Suppliers> result = suppliersRepository
            .findById(suppliers.getId())
            .map(existingSuppliers -> {
                if (suppliers.getName() != null) {
                    existingSuppliers.setName(suppliers.getName());
                }
                if (suppliers.getContactPerson() != null) {
                    existingSuppliers.setContactPerson(suppliers.getContactPerson());
                }
                if (suppliers.getPhone() != null) {
                    existingSuppliers.setPhone(suppliers.getPhone());
                }
                if (suppliers.getEmail() != null) {
                    existingSuppliers.setEmail(suppliers.getEmail());
                }
                if (suppliers.getAddress() != null) {
                    existingSuppliers.setAddress(suppliers.getAddress());
                }

                return existingSuppliers;
            })
            .map(suppliersRepository::save)
            .map(savedSuppliers -> {
                suppliersSearchRepository.index(savedSuppliers);
                return savedSuppliers;
            });

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, suppliers.getId().toString())
        );
    }

    /**
     * {@code GET  /suppliers} : get all the suppliers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of suppliers in body.
     */
    @GetMapping("")
    public List<Suppliers> getAllSuppliers() {
        LOG.debug("REST request to get all Suppliers");
        return suppliersRepository.findAll();
    }

    /**
     * {@code GET  /suppliers/:id} : get the "id" suppliers.
     *
     * @param id the id of the suppliers to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the suppliers, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Suppliers> getSuppliers(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Suppliers : {}", id);
        Optional<Suppliers> suppliers = suppliersRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(suppliers);
    }

    /**
     * {@code DELETE  /suppliers/:id} : delete the "id" suppliers.
     *
     * @param id the id of the suppliers to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSuppliers(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Suppliers : {}", id);
        suppliersRepository.deleteById(id);
        suppliersSearchRepository.deleteFromIndexById(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /suppliers/_search?query=:query} : search for the suppliers corresponding
     * to the query.
     *
     * @param query the query of the suppliers search.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public List<Suppliers> searchSuppliers(@RequestParam("query") String query) {
        LOG.debug("REST request to search Suppliers for query {}", query);
        try {
            return StreamSupport.stream(suppliersSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
