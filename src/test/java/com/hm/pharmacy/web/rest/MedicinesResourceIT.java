package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.MedicinesAsserts.*;
import static com.hm.pharmacy.web.rest.TestUtil.createUpdateProxyForBean;
import static com.hm.pharmacy.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hm.pharmacy.IntegrationTest;
import com.hm.pharmacy.domain.Medicines;
import com.hm.pharmacy.repository.MedicinesRepository;
import com.hm.pharmacy.repository.search.MedicinesSearchRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link MedicinesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MedicinesResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_MANUFACTURER = "AAAAAAAAAA";
    private static final String UPDATED_MANUFACTURER = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    private static final Integer DEFAULT_STOCK = 1;
    private static final Integer UPDATED_STOCK = 2;

    private static final String ENTITY_API_URL = "/api/medicines";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/medicines/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MedicinesRepository medicinesRepository;

    @Autowired
    private MedicinesSearchRepository medicinesSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMedicinesMockMvc;

    private Medicines medicines;

    private Medicines insertedMedicines;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Medicines createEntity() {
        return new Medicines()
            .name(DEFAULT_NAME)
            .manufacturer(DEFAULT_MANUFACTURER)
            .category(DEFAULT_CATEGORY)
            .price(DEFAULT_PRICE)
            .stock(DEFAULT_STOCK);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Medicines createUpdatedEntity() {
        return new Medicines()
            .name(UPDATED_NAME)
            .manufacturer(UPDATED_MANUFACTURER)
            .category(UPDATED_CATEGORY)
            .price(UPDATED_PRICE)
            .stock(UPDATED_STOCK);
    }

    @BeforeEach
    void initTest() {
        medicines = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMedicines != null) {
            medicinesRepository.delete(insertedMedicines);
            medicinesSearchRepository.delete(insertedMedicines);
            insertedMedicines = null;
        }
    }

    @Test
    @Transactional
    void createMedicines() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        // Create the Medicines
        var returnedMedicines = om.readValue(
            restMedicinesMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicines)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Medicines.class
        );

        // Validate the Medicines in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertMedicinesUpdatableFieldsEquals(returnedMedicines, getPersistedMedicines(returnedMedicines));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedMedicines = returnedMedicines;
    }

    @Test
    @Transactional
    void createMedicinesWithExistingId() throws Exception {
        // Create the Medicines with an existing ID
        medicines.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restMedicinesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicines)))
            .andExpect(status().isBadRequest());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllMedicines() throws Exception {
        // Initialize the database
        insertedMedicines = medicinesRepository.saveAndFlush(medicines);

        // Get all the medicinesList
        restMedicinesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicines.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].manufacturer").value(hasItem(DEFAULT_MANUFACTURER)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].stock").value(hasItem(DEFAULT_STOCK)));
    }

    @Test
    @Transactional
    void getMedicines() throws Exception {
        // Initialize the database
        insertedMedicines = medicinesRepository.saveAndFlush(medicines);

        // Get the medicines
        restMedicinesMockMvc
            .perform(get(ENTITY_API_URL_ID, medicines.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(medicines.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.manufacturer").value(DEFAULT_MANUFACTURER))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.stock").value(DEFAULT_STOCK));
    }

    @Test
    @Transactional
    void getNonExistingMedicines() throws Exception {
        // Get the medicines
        restMedicinesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMedicines() throws Exception {
        // Initialize the database
        insertedMedicines = medicinesRepository.saveAndFlush(medicines);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicinesSearchRepository.save(medicines);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());

        // Update the medicines
        Medicines updatedMedicines = medicinesRepository.findById(medicines.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMedicines are not directly saved in db
        em.detach(updatedMedicines);
        updatedMedicines
            .name(UPDATED_NAME)
            .manufacturer(UPDATED_MANUFACTURER)
            .category(UPDATED_CATEGORY)
            .price(UPDATED_PRICE)
            .stock(UPDATED_STOCK);

        restMedicinesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedMedicines.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedMedicines))
            )
            .andExpect(status().isOk());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMedicinesToMatchAllProperties(updatedMedicines);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Medicines> medicinesSearchList = Streamable.of(medicinesSearchRepository.findAll()).toList();
                Medicines testMedicinesSearch = medicinesSearchList.get(searchDatabaseSizeAfter - 1);

                assertMedicinesAllPropertiesEquals(testMedicinesSearch, updatedMedicines);
            });
    }

    @Test
    @Transactional
    void putNonExistingMedicines() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        medicines.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicinesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medicines.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicines))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchMedicines() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        medicines.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicinesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicines))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMedicines() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        medicines.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicinesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicines)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateMedicinesWithPatch() throws Exception {
        // Initialize the database
        insertedMedicines = medicinesRepository.saveAndFlush(medicines);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicines using partial update
        Medicines partialUpdatedMedicines = new Medicines();
        partialUpdatedMedicines.setId(medicines.getId());

        partialUpdatedMedicines
            .name(UPDATED_NAME)
            .manufacturer(UPDATED_MANUFACTURER)
            .category(UPDATED_CATEGORY)
            .price(UPDATED_PRICE)
            .stock(UPDATED_STOCK);

        restMedicinesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicines.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicines))
            )
            .andExpect(status().isOk());

        // Validate the Medicines in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicinesUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMedicines, medicines),
            getPersistedMedicines(medicines)
        );
    }

    @Test
    @Transactional
    void fullUpdateMedicinesWithPatch() throws Exception {
        // Initialize the database
        insertedMedicines = medicinesRepository.saveAndFlush(medicines);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicines using partial update
        Medicines partialUpdatedMedicines = new Medicines();
        partialUpdatedMedicines.setId(medicines.getId());

        partialUpdatedMedicines
            .name(UPDATED_NAME)
            .manufacturer(UPDATED_MANUFACTURER)
            .category(UPDATED_CATEGORY)
            .price(UPDATED_PRICE)
            .stock(UPDATED_STOCK);

        restMedicinesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicines.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicines))
            )
            .andExpect(status().isOk());

        // Validate the Medicines in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicinesUpdatableFieldsEquals(partialUpdatedMedicines, getPersistedMedicines(partialUpdatedMedicines));
    }

    @Test
    @Transactional
    void patchNonExistingMedicines() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        medicines.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicinesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, medicines.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicines))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMedicines() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        medicines.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicinesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicines))
            )
            .andExpect(status().isBadRequest());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMedicines() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        medicines.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicinesMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(medicines)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Medicines in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteMedicines() throws Exception {
        // Initialize the database
        insertedMedicines = medicinesRepository.saveAndFlush(medicines);
        medicinesRepository.save(medicines);
        medicinesSearchRepository.save(medicines);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the medicines
        restMedicinesMockMvc
            .perform(delete(ENTITY_API_URL_ID, medicines.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicinesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchMedicines() throws Exception {
        // Initialize the database
        insertedMedicines = medicinesRepository.saveAndFlush(medicines);
        medicinesSearchRepository.save(medicines);

        // Search the medicines
        restMedicinesMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + medicines.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicines.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].manufacturer").value(hasItem(DEFAULT_MANUFACTURER)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].stock").value(hasItem(DEFAULT_STOCK)));
    }

    protected long getRepositoryCount() {
        return medicinesRepository.count();
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

    protected Medicines getPersistedMedicines(Medicines medicines) {
        return medicinesRepository.findById(medicines.getId()).orElseThrow();
    }

    protected void assertPersistedMedicinesToMatchAllProperties(Medicines expectedMedicines) {
        assertMedicinesAllPropertiesEquals(expectedMedicines, getPersistedMedicines(expectedMedicines));
    }

    protected void assertPersistedMedicinesToMatchUpdatableProperties(Medicines expectedMedicines) {
        assertMedicinesAllUpdatablePropertiesEquals(expectedMedicines, getPersistedMedicines(expectedMedicines));
    }
}
