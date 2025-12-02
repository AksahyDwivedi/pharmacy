package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.PrescriptionsAsserts.*;
import static com.hm.pharmacy.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hm.pharmacy.IntegrationTest;
import com.hm.pharmacy.domain.Prescriptions;
import com.hm.pharmacy.repository.PrescriptionsRepository;
import com.hm.pharmacy.repository.search.PrescriptionsSearchRepository;
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
 * Integration tests for the {@link PrescriptionsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PrescriptionsResourceIT {

    private static final String DEFAULT_DOCTOR_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DOCTOR_NAME = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_PRESCRIPTION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PRESCRIPTION_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_NOTES = "AAAAAAAAAA";
    private static final String UPDATED_NOTES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/prescriptions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/prescriptions/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PrescriptionsRepository prescriptionsRepository;

    @Autowired
    private PrescriptionsSearchRepository prescriptionsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPrescriptionsMockMvc;

    private Prescriptions prescriptions;

    private Prescriptions insertedPrescriptions;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prescriptions createEntity() {
        return new Prescriptions().doctorName(DEFAULT_DOCTOR_NAME).prescriptionDate(DEFAULT_PRESCRIPTION_DATE).notes(DEFAULT_NOTES);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Prescriptions createUpdatedEntity() {
        return new Prescriptions().doctorName(UPDATED_DOCTOR_NAME).prescriptionDate(UPDATED_PRESCRIPTION_DATE).notes(UPDATED_NOTES);
    }

    @BeforeEach
    void initTest() {
        prescriptions = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPrescriptions != null) {
            prescriptionsRepository.delete(insertedPrescriptions);
            prescriptionsSearchRepository.delete(insertedPrescriptions);
            insertedPrescriptions = null;
        }
    }

    @Test
    @Transactional
    void createPrescriptions() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        // Create the Prescriptions
        var returnedPrescriptions = om.readValue(
            restPrescriptionsMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptions)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Prescriptions.class
        );

        // Validate the Prescriptions in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPrescriptionsUpdatableFieldsEquals(returnedPrescriptions, getPersistedPrescriptions(returnedPrescriptions));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedPrescriptions = returnedPrescriptions;
    }

    @Test
    @Transactional
    void createPrescriptionsWithExistingId() throws Exception {
        // Create the Prescriptions with an existing ID
        prescriptions.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPrescriptionsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptions)))
            .andExpect(status().isBadRequest());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPrescriptions() throws Exception {
        // Initialize the database
        insertedPrescriptions = prescriptionsRepository.saveAndFlush(prescriptions);

        // Get all the prescriptionsList
        restPrescriptionsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prescriptions.getId().intValue())))
            .andExpect(jsonPath("$.[*].doctorName").value(hasItem(DEFAULT_DOCTOR_NAME)))
            .andExpect(jsonPath("$.[*].prescriptionDate").value(hasItem(DEFAULT_PRESCRIPTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES)));
    }

    @Test
    @Transactional
    void getPrescriptions() throws Exception {
        // Initialize the database
        insertedPrescriptions = prescriptionsRepository.saveAndFlush(prescriptions);

        // Get the prescriptions
        restPrescriptionsMockMvc
            .perform(get(ENTITY_API_URL_ID, prescriptions.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(prescriptions.getId().intValue()))
            .andExpect(jsonPath("$.doctorName").value(DEFAULT_DOCTOR_NAME))
            .andExpect(jsonPath("$.prescriptionDate").value(DEFAULT_PRESCRIPTION_DATE.toString()))
            .andExpect(jsonPath("$.notes").value(DEFAULT_NOTES));
    }

    @Test
    @Transactional
    void getNonExistingPrescriptions() throws Exception {
        // Get the prescriptions
        restPrescriptionsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPrescriptions() throws Exception {
        // Initialize the database
        insertedPrescriptions = prescriptionsRepository.saveAndFlush(prescriptions);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        prescriptionsSearchRepository.save(prescriptions);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());

        // Update the prescriptions
        Prescriptions updatedPrescriptions = prescriptionsRepository.findById(prescriptions.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPrescriptions are not directly saved in db
        em.detach(updatedPrescriptions);
        updatedPrescriptions.doctorName(UPDATED_DOCTOR_NAME).prescriptionDate(UPDATED_PRESCRIPTION_DATE).notes(UPDATED_NOTES);

        restPrescriptionsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPrescriptions.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedPrescriptions))
            )
            .andExpect(status().isOk());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPrescriptionsToMatchAllProperties(updatedPrescriptions);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Prescriptions> prescriptionsSearchList = Streamable.of(prescriptionsSearchRepository.findAll()).toList();
                Prescriptions testPrescriptionsSearch = prescriptionsSearchList.get(searchDatabaseSizeAfter - 1);

                assertPrescriptionsAllPropertiesEquals(testPrescriptionsSearch, updatedPrescriptions);
            });
    }

    @Test
    @Transactional
    void putNonExistingPrescriptions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        prescriptions.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrescriptionsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, prescriptions.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptions))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPrescriptions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        prescriptions.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(prescriptions))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPrescriptions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        prescriptions.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(prescriptions)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePrescriptionsWithPatch() throws Exception {
        // Initialize the database
        insertedPrescriptions = prescriptionsRepository.saveAndFlush(prescriptions);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescriptions using partial update
        Prescriptions partialUpdatedPrescriptions = new Prescriptions();
        partialUpdatedPrescriptions.setId(prescriptions.getId());

        restPrescriptionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrescriptions.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrescriptions))
            )
            .andExpect(status().isOk());

        // Validate the Prescriptions in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrescriptionsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPrescriptions, prescriptions),
            getPersistedPrescriptions(prescriptions)
        );
    }

    @Test
    @Transactional
    void fullUpdatePrescriptionsWithPatch() throws Exception {
        // Initialize the database
        insertedPrescriptions = prescriptionsRepository.saveAndFlush(prescriptions);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the prescriptions using partial update
        Prescriptions partialUpdatedPrescriptions = new Prescriptions();
        partialUpdatedPrescriptions.setId(prescriptions.getId());

        partialUpdatedPrescriptions.doctorName(UPDATED_DOCTOR_NAME).prescriptionDate(UPDATED_PRESCRIPTION_DATE).notes(UPDATED_NOTES);

        restPrescriptionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPrescriptions.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPrescriptions))
            )
            .andExpect(status().isOk());

        // Validate the Prescriptions in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPrescriptionsUpdatableFieldsEquals(partialUpdatedPrescriptions, getPersistedPrescriptions(partialUpdatedPrescriptions));
    }

    @Test
    @Transactional
    void patchNonExistingPrescriptions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        prescriptions.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPrescriptionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, prescriptions.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prescriptions))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPrescriptions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        prescriptions.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(prescriptions))
            )
            .andExpect(status().isBadRequest());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPrescriptions() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        prescriptions.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPrescriptionsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(prescriptions)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Prescriptions in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePrescriptions() throws Exception {
        // Initialize the database
        insertedPrescriptions = prescriptionsRepository.saveAndFlush(prescriptions);
        prescriptionsRepository.save(prescriptions);
        prescriptionsSearchRepository.save(prescriptions);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the prescriptions
        restPrescriptionsMockMvc
            .perform(delete(ENTITY_API_URL_ID, prescriptions.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(prescriptionsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPrescriptions() throws Exception {
        // Initialize the database
        insertedPrescriptions = prescriptionsRepository.saveAndFlush(prescriptions);
        prescriptionsSearchRepository.save(prescriptions);

        // Search the prescriptions
        restPrescriptionsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + prescriptions.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(prescriptions.getId().intValue())))
            .andExpect(jsonPath("$.[*].doctorName").value(hasItem(DEFAULT_DOCTOR_NAME)))
            .andExpect(jsonPath("$.[*].prescriptionDate").value(hasItem(DEFAULT_PRESCRIPTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].notes").value(hasItem(DEFAULT_NOTES.toString())));
    }

    protected long getRepositoryCount() {
        return prescriptionsRepository.count();
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

    protected Prescriptions getPersistedPrescriptions(Prescriptions prescriptions) {
        return prescriptionsRepository.findById(prescriptions.getId()).orElseThrow();
    }

    protected void assertPersistedPrescriptionsToMatchAllProperties(Prescriptions expectedPrescriptions) {
        assertPrescriptionsAllPropertiesEquals(expectedPrescriptions, getPersistedPrescriptions(expectedPrescriptions));
    }

    protected void assertPersistedPrescriptionsToMatchUpdatableProperties(Prescriptions expectedPrescriptions) {
        assertPrescriptionsAllUpdatablePropertiesEquals(expectedPrescriptions, getPersistedPrescriptions(expectedPrescriptions));
    }
}
