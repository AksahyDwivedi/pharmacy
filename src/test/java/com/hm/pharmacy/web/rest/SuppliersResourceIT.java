package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.SuppliersAsserts.*;
import static com.hm.pharmacy.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hm.pharmacy.IntegrationTest;
import com.hm.pharmacy.domain.Suppliers;
import com.hm.pharmacy.repository.SuppliersRepository;
import com.hm.pharmacy.repository.search.SuppliersSearchRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SuppliersResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SuppliersResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CONTACT_PERSON = "AAAAAAAAAA";
    private static final String UPDATED_CONTACT_PERSON = "BBBBBBBBBB";

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/suppliers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/suppliers/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SuppliersRepository suppliersRepository;

    @Autowired
    private SuppliersSearchRepository suppliersSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSuppliersMockMvc;

    private Suppliers suppliers;

    private Suppliers insertedSuppliers;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Suppliers createEntity() {
        return new Suppliers()
            .name(DEFAULT_NAME)
            .contactPerson(DEFAULT_CONTACT_PERSON)
            .phone(DEFAULT_PHONE)
            .email(DEFAULT_EMAIL)
            .address(DEFAULT_ADDRESS);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Suppliers createUpdatedEntity() {
        return new Suppliers()
            .name(UPDATED_NAME)
            .contactPerson(UPDATED_CONTACT_PERSON)
            .phone(UPDATED_PHONE)
            .email(UPDATED_EMAIL)
            .address(UPDATED_ADDRESS);
    }

    @BeforeEach
    void initTest() {
        suppliers = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSuppliers != null) {
            suppliersRepository.delete(insertedSuppliers);
            suppliersSearchRepository.delete(insertedSuppliers);
            insertedSuppliers = null;
        }
    }

    @Test
    @Transactional
    void createSuppliers() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        // Create the Suppliers
        var returnedSuppliers = om.readValue(
            restSuppliersMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(suppliers)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Suppliers.class
        );

        // Validate the Suppliers in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSuppliersUpdatableFieldsEquals(returnedSuppliers, getPersistedSuppliers(returnedSuppliers));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedSuppliers = returnedSuppliers;
    }

    @Test
    @Transactional
    void createSuppliersWithExistingId() throws Exception {
        // Create the Suppliers with an existing ID
        suppliers.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restSuppliersMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(suppliers)))
            .andExpect(status().isBadRequest());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllSuppliers() throws Exception {
        // Initialize the database
        insertedSuppliers = suppliersRepository.saveAndFlush(suppliers);

        // Get all the suppliersList
        restSuppliersMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(suppliers.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].contactPerson").value(hasItem(DEFAULT_CONTACT_PERSON)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS)));
    }

    @Test
    @Transactional
    void getSuppliers() throws Exception {
        // Initialize the database
        insertedSuppliers = suppliersRepository.saveAndFlush(suppliers);

        // Get the suppliers
        restSuppliersMockMvc
            .perform(get(ENTITY_API_URL_ID, suppliers.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(suppliers.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.contactPerson").value(DEFAULT_CONTACT_PERSON))
            .andExpect(jsonPath("$.phone").value(DEFAULT_PHONE))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.address").value(DEFAULT_ADDRESS));
    }

    @Test
    @Transactional
    void getNonExistingSuppliers() throws Exception {
        // Get the suppliers
        restSuppliersMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSuppliers() throws Exception {
        // Initialize the database
        insertedSuppliers = suppliersRepository.saveAndFlush(suppliers);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        suppliersSearchRepository.save(suppliers);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());

        // Update the suppliers
        Suppliers updatedSuppliers = suppliersRepository.findById(suppliers.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSuppliers are not directly saved in db
        em.detach(updatedSuppliers);
        updatedSuppliers
            .name(UPDATED_NAME)
            .contactPerson(UPDATED_CONTACT_PERSON)
            .phone(UPDATED_PHONE)
            .email(UPDATED_EMAIL)
            .address(UPDATED_ADDRESS);

        restSuppliersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSuppliers.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSuppliers))
            )
            .andExpect(status().isOk());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSuppliersToMatchAllProperties(updatedSuppliers);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Suppliers> suppliersSearchList = Streamable.of(suppliersSearchRepository.findAll()).toList();
                Suppliers testSuppliersSearch = suppliersSearchList.get(searchDatabaseSizeAfter - 1);

                assertSuppliersAllPropertiesEquals(testSuppliersSearch, updatedSuppliers);
            });
    }

    @Test
    @Transactional
    void putNonExistingSuppliers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        suppliers.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSuppliersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, suppliers.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(suppliers))
            )
            .andExpect(status().isBadRequest());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchSuppliers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        suppliers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuppliersMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(suppliers))
            )
            .andExpect(status().isBadRequest());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSuppliers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        suppliers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuppliersMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(suppliers)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateSuppliersWithPatch() throws Exception {
        // Initialize the database
        insertedSuppliers = suppliersRepository.saveAndFlush(suppliers);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the suppliers using partial update
        Suppliers partialUpdatedSuppliers = new Suppliers();
        partialUpdatedSuppliers.setId(suppliers.getId());

        partialUpdatedSuppliers.name(UPDATED_NAME).contactPerson(UPDATED_CONTACT_PERSON).phone(UPDATED_PHONE).email(UPDATED_EMAIL);

        restSuppliersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSuppliers.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSuppliers))
            )
            .andExpect(status().isOk());

        // Validate the Suppliers in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSuppliersUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSuppliers, suppliers),
            getPersistedSuppliers(suppliers)
        );
    }

    @Test
    @Transactional
    void fullUpdateSuppliersWithPatch() throws Exception {
        // Initialize the database
        insertedSuppliers = suppliersRepository.saveAndFlush(suppliers);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the suppliers using partial update
        Suppliers partialUpdatedSuppliers = new Suppliers();
        partialUpdatedSuppliers.setId(suppliers.getId());

        partialUpdatedSuppliers
            .name(UPDATED_NAME)
            .contactPerson(UPDATED_CONTACT_PERSON)
            .phone(UPDATED_PHONE)
            .email(UPDATED_EMAIL)
            .address(UPDATED_ADDRESS);

        restSuppliersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSuppliers.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSuppliers))
            )
            .andExpect(status().isOk());

        // Validate the Suppliers in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSuppliersUpdatableFieldsEquals(partialUpdatedSuppliers, getPersistedSuppliers(partialUpdatedSuppliers));
    }

    @Test
    @Transactional
    void patchNonExistingSuppliers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        suppliers.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSuppliersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, suppliers.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(suppliers))
            )
            .andExpect(status().isBadRequest());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSuppliers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        suppliers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuppliersMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(suppliers))
            )
            .andExpect(status().isBadRequest());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSuppliers() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        suppliers.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuppliersMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(suppliers)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Suppliers in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteSuppliers() throws Exception {
        // Initialize the database
        insertedSuppliers = suppliersRepository.saveAndFlush(suppliers);
        suppliersRepository.save(suppliers);
        suppliersSearchRepository.save(suppliers);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the suppliers
        restSuppliersMockMvc
            .perform(delete(ENTITY_API_URL_ID, suppliers.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(suppliersSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchSuppliers() throws Exception {
        // Initialize the database
        insertedSuppliers = suppliersRepository.saveAndFlush(suppliers);
        suppliersSearchRepository.save(suppliers);

        // Search the suppliers
        restSuppliersMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + suppliers.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(suppliers.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].contactPerson").value(hasItem(DEFAULT_CONTACT_PERSON)))
            .andExpect(jsonPath("$.[*].phone").value(hasItem(DEFAULT_PHONE)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].address").value(hasItem(DEFAULT_ADDRESS.toString())));
    }

    protected long getRepositoryCount() {
        return suppliersRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Suppliers getPersistedSuppliers(Suppliers suppliers) {
        return suppliersRepository.findById(suppliers.getId()).orElseThrow();
    }

    protected void assertPersistedSuppliersToMatchAllProperties(Suppliers expectedSuppliers) {
        assertSuppliersAllPropertiesEquals(expectedSuppliers, getPersistedSuppliers(expectedSuppliers));
    }

    protected void assertPersistedSuppliersToMatchUpdatableProperties(Suppliers expectedSuppliers) {
        assertSuppliersAllUpdatablePropertiesEquals(expectedSuppliers, getPersistedSuppliers(expectedSuppliers));
    }
}
