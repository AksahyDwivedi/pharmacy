package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.MedicineBatchesAsserts.*;
import static com.hm.pharmacy.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hm.pharmacy.IntegrationTest;
import com.hm.pharmacy.domain.MedicineBatches;
import com.hm.pharmacy.repository.MedicineBatchesRepository;
import com.hm.pharmacy.repository.search.MedicineBatchesSearchRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link MedicineBatchesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MedicineBatchesResourceIT {

    private static final String DEFAULT_BATCH_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_BATCH_NUMBER = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EXPIRY_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EXPIRY_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final String ENTITY_API_URL = "/api/medicine-batches";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/medicine-batches/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MedicineBatchesRepository medicineBatchesRepository;

    @Autowired
    private MedicineBatchesSearchRepository medicineBatchesSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMedicineBatchesMockMvc;

    private MedicineBatches medicineBatches;

    private MedicineBatches insertedMedicineBatches;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MedicineBatches createEntity() {
        return new MedicineBatches().batchNumber(DEFAULT_BATCH_NUMBER).expiryDate(DEFAULT_EXPIRY_DATE).quantity(DEFAULT_QUANTITY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MedicineBatches createUpdatedEntity() {
        return new MedicineBatches().batchNumber(UPDATED_BATCH_NUMBER).expiryDate(UPDATED_EXPIRY_DATE).quantity(UPDATED_QUANTITY);
    }

    @BeforeEach
    void initTest() {
        medicineBatches = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedMedicineBatches != null) {
            medicineBatchesRepository.delete(insertedMedicineBatches);
            medicineBatchesSearchRepository.delete(insertedMedicineBatches);
            insertedMedicineBatches = null;
        }
    }

    @Test
    @Transactional
    void createMedicineBatches() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        // Create the MedicineBatches
        var returnedMedicineBatches = om.readValue(
            restMedicineBatchesMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicineBatches)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MedicineBatches.class
        );

        // Validate the MedicineBatches in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertMedicineBatchesUpdatableFieldsEquals(returnedMedicineBatches, getPersistedMedicineBatches(returnedMedicineBatches));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedMedicineBatches = returnedMedicineBatches;
    }

    @Test
    @Transactional
    void createMedicineBatchesWithExistingId() throws Exception {
        // Create the MedicineBatches with an existing ID
        medicineBatches.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restMedicineBatchesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicineBatches)))
            .andExpect(status().isBadRequest());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllMedicineBatches() throws Exception {
        // Initialize the database
        insertedMedicineBatches = medicineBatchesRepository.saveAndFlush(medicineBatches);

        // Get all the medicineBatchesList
        restMedicineBatchesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicineBatches.getId().intValue())))
            .andExpect(jsonPath("$.[*].batchNumber").value(hasItem(DEFAULT_BATCH_NUMBER)))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(DEFAULT_EXPIRY_DATE.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)));
    }

    @Test
    @Transactional
    void getMedicineBatches() throws Exception {
        // Initialize the database
        insertedMedicineBatches = medicineBatchesRepository.saveAndFlush(medicineBatches);

        // Get the medicineBatches
        restMedicineBatchesMockMvc
            .perform(get(ENTITY_API_URL_ID, medicineBatches.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(medicineBatches.getId().intValue()))
            .andExpect(jsonPath("$.batchNumber").value(DEFAULT_BATCH_NUMBER))
            .andExpect(jsonPath("$.expiryDate").value(DEFAULT_EXPIRY_DATE.toString()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY));
    }

    @Test
    @Transactional
    void getNonExistingMedicineBatches() throws Exception {
        // Get the medicineBatches
        restMedicineBatchesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMedicineBatches() throws Exception {
        // Initialize the database
        insertedMedicineBatches = medicineBatchesRepository.saveAndFlush(medicineBatches);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        medicineBatchesSearchRepository.save(medicineBatches);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());

        // Update the medicineBatches
        MedicineBatches updatedMedicineBatches = medicineBatchesRepository.findById(medicineBatches.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMedicineBatches are not directly saved in db
        em.detach(updatedMedicineBatches);
        updatedMedicineBatches.batchNumber(UPDATED_BATCH_NUMBER).expiryDate(UPDATED_EXPIRY_DATE).quantity(UPDATED_QUANTITY);

        restMedicineBatchesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedMedicineBatches.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedMedicineBatches))
            )
            .andExpect(status().isOk());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMedicineBatchesToMatchAllProperties(updatedMedicineBatches);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<MedicineBatches> medicineBatchesSearchList = Streamable.of(medicineBatchesSearchRepository.findAll()).toList();
                MedicineBatches testMedicineBatchesSearch = medicineBatchesSearchList.get(searchDatabaseSizeAfter - 1);

                assertMedicineBatchesAllPropertiesEquals(testMedicineBatchesSearch, updatedMedicineBatches);
            });
    }

    @Test
    @Transactional
    void putNonExistingMedicineBatches() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        medicineBatches.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicineBatchesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, medicineBatches.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicineBatches))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchMedicineBatches() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        medicineBatches.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicineBatchesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(medicineBatches))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMedicineBatches() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        medicineBatches.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicineBatchesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(medicineBatches)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateMedicineBatchesWithPatch() throws Exception {
        // Initialize the database
        insertedMedicineBatches = medicineBatchesRepository.saveAndFlush(medicineBatches);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicineBatches using partial update
        MedicineBatches partialUpdatedMedicineBatches = new MedicineBatches();
        partialUpdatedMedicineBatches.setId(medicineBatches.getId());

        partialUpdatedMedicineBatches.quantity(UPDATED_QUANTITY);

        restMedicineBatchesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicineBatches.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicineBatches))
            )
            .andExpect(status().isOk());

        // Validate the MedicineBatches in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicineBatchesUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedMedicineBatches, medicineBatches),
            getPersistedMedicineBatches(medicineBatches)
        );
    }

    @Test
    @Transactional
    void fullUpdateMedicineBatchesWithPatch() throws Exception {
        // Initialize the database
        insertedMedicineBatches = medicineBatchesRepository.saveAndFlush(medicineBatches);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the medicineBatches using partial update
        MedicineBatches partialUpdatedMedicineBatches = new MedicineBatches();
        partialUpdatedMedicineBatches.setId(medicineBatches.getId());

        partialUpdatedMedicineBatches.batchNumber(UPDATED_BATCH_NUMBER).expiryDate(UPDATED_EXPIRY_DATE).quantity(UPDATED_QUANTITY);

        restMedicineBatchesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMedicineBatches.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMedicineBatches))
            )
            .andExpect(status().isOk());

        // Validate the MedicineBatches in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMedicineBatchesUpdatableFieldsEquals(
            partialUpdatedMedicineBatches,
            getPersistedMedicineBatches(partialUpdatedMedicineBatches)
        );
    }

    @Test
    @Transactional
    void patchNonExistingMedicineBatches() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        medicineBatches.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicineBatchesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, medicineBatches.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicineBatches))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMedicineBatches() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        medicineBatches.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicineBatchesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(medicineBatches))
            )
            .andExpect(status().isBadRequest());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMedicineBatches() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        medicineBatches.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMedicineBatchesMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(medicineBatches)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the MedicineBatches in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteMedicineBatches() throws Exception {
        // Initialize the database
        insertedMedicineBatches = medicineBatchesRepository.saveAndFlush(medicineBatches);
        medicineBatchesRepository.save(medicineBatches);
        medicineBatchesSearchRepository.save(medicineBatches);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the medicineBatches
        restMedicineBatchesMockMvc
            .perform(delete(ENTITY_API_URL_ID, medicineBatches.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(medicineBatchesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchMedicineBatches() throws Exception {
        // Initialize the database
        insertedMedicineBatches = medicineBatchesRepository.saveAndFlush(medicineBatches);
        medicineBatchesSearchRepository.save(medicineBatches);

        // Search the medicineBatches
        restMedicineBatchesMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + medicineBatches.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicineBatches.getId().intValue())))
            .andExpect(jsonPath("$.[*].batchNumber").value(hasItem(DEFAULT_BATCH_NUMBER)))
            .andExpect(jsonPath("$.[*].expiryDate").value(hasItem(DEFAULT_EXPIRY_DATE.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)));
    }

    protected long getRepositoryCount() {
        return medicineBatchesRepository.count();
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

    protected MedicineBatches getPersistedMedicineBatches(MedicineBatches medicineBatches) {
        return medicineBatchesRepository.findById(medicineBatches.getId()).orElseThrow();
    }

    protected void assertPersistedMedicineBatchesToMatchAllProperties(MedicineBatches expectedMedicineBatches) {
        assertMedicineBatchesAllPropertiesEquals(expectedMedicineBatches, getPersistedMedicineBatches(expectedMedicineBatches));
    }

    protected void assertPersistedMedicineBatchesToMatchUpdatableProperties(MedicineBatches expectedMedicineBatches) {
        assertMedicineBatchesAllUpdatablePropertiesEquals(expectedMedicineBatches, getPersistedMedicineBatches(expectedMedicineBatches));
    }
}
