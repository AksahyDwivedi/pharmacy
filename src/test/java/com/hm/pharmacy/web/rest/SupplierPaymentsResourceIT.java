package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.SupplierPaymentsAsserts.*;
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
import com.hm.pharmacy.domain.SupplierPayments;
import com.hm.pharmacy.repository.SupplierPaymentsRepository;
import com.hm.pharmacy.repository.search.SupplierPaymentsSearchRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link SupplierPaymentsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SupplierPaymentsResourceIT {

    private static final Instant DEFAULT_PAYMENT_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PAYMENT_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_PAYMENT_METHOD = "AAAAAAAAAA";
    private static final String UPDATED_PAYMENT_METHOD = "BBBBBBBBBB";

    private static final String DEFAULT_PAYMENT_STATUS = "AAAAAAAAAA";
    private static final String UPDATED_PAYMENT_STATUS = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_AMOUNT_PAID = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT_PAID = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/supplier-payments";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/supplier-payments/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SupplierPaymentsRepository supplierPaymentsRepository;

    @Autowired
    private SupplierPaymentsSearchRepository supplierPaymentsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSupplierPaymentsMockMvc;

    private SupplierPayments supplierPayments;

    private SupplierPayments insertedSupplierPayments;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SupplierPayments createEntity() {
        return new SupplierPayments()
            .paymentDate(DEFAULT_PAYMENT_DATE)
            .paymentMethod(DEFAULT_PAYMENT_METHOD)
            .paymentStatus(DEFAULT_PAYMENT_STATUS)
            .amountPaid(DEFAULT_AMOUNT_PAID);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SupplierPayments createUpdatedEntity() {
        return new SupplierPayments()
            .paymentDate(UPDATED_PAYMENT_DATE)
            .paymentMethod(UPDATED_PAYMENT_METHOD)
            .paymentStatus(UPDATED_PAYMENT_STATUS)
            .amountPaid(UPDATED_AMOUNT_PAID);
    }

    @BeforeEach
    void initTest() {
        supplierPayments = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSupplierPayments != null) {
            supplierPaymentsRepository.delete(insertedSupplierPayments);
            supplierPaymentsSearchRepository.delete(insertedSupplierPayments);
            insertedSupplierPayments = null;
        }
    }

    @Test
    @Transactional
    void createSupplierPayments() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        // Create the SupplierPayments
        var returnedSupplierPayments = om.readValue(
            restSupplierPaymentsMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(supplierPayments)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SupplierPayments.class
        );

        // Validate the SupplierPayments in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSupplierPaymentsUpdatableFieldsEquals(returnedSupplierPayments, getPersistedSupplierPayments(returnedSupplierPayments));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedSupplierPayments = returnedSupplierPayments;
    }

    @Test
    @Transactional
    void createSupplierPaymentsWithExistingId() throws Exception {
        // Create the SupplierPayments with an existing ID
        supplierPayments.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restSupplierPaymentsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(supplierPayments)))
            .andExpect(status().isBadRequest());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllSupplierPayments() throws Exception {
        // Initialize the database
        insertedSupplierPayments = supplierPaymentsRepository.saveAndFlush(supplierPayments);

        // Get all the supplierPaymentsList
        restSupplierPaymentsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(supplierPayments.getId().intValue())))
            .andExpect(jsonPath("$.[*].paymentDate").value(hasItem(DEFAULT_PAYMENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].paymentMethod").value(hasItem(DEFAULT_PAYMENT_METHOD)))
            .andExpect(jsonPath("$.[*].paymentStatus").value(hasItem(DEFAULT_PAYMENT_STATUS)))
            .andExpect(jsonPath("$.[*].amountPaid").value(hasItem(sameNumber(DEFAULT_AMOUNT_PAID))));
    }

    @Test
    @Transactional
    void getSupplierPayments() throws Exception {
        // Initialize the database
        insertedSupplierPayments = supplierPaymentsRepository.saveAndFlush(supplierPayments);

        // Get the supplierPayments
        restSupplierPaymentsMockMvc
            .perform(get(ENTITY_API_URL_ID, supplierPayments.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(supplierPayments.getId().intValue()))
            .andExpect(jsonPath("$.paymentDate").value(DEFAULT_PAYMENT_DATE.toString()))
            .andExpect(jsonPath("$.paymentMethod").value(DEFAULT_PAYMENT_METHOD))
            .andExpect(jsonPath("$.paymentStatus").value(DEFAULT_PAYMENT_STATUS))
            .andExpect(jsonPath("$.amountPaid").value(sameNumber(DEFAULT_AMOUNT_PAID)));
    }

    @Test
    @Transactional
    void getNonExistingSupplierPayments() throws Exception {
        // Get the supplierPayments
        restSupplierPaymentsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSupplierPayments() throws Exception {
        // Initialize the database
        insertedSupplierPayments = supplierPaymentsRepository.saveAndFlush(supplierPayments);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        supplierPaymentsSearchRepository.save(supplierPayments);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());

        // Update the supplierPayments
        SupplierPayments updatedSupplierPayments = supplierPaymentsRepository.findById(supplierPayments.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSupplierPayments are not directly saved in db
        em.detach(updatedSupplierPayments);
        updatedSupplierPayments
            .paymentDate(UPDATED_PAYMENT_DATE)
            .paymentMethod(UPDATED_PAYMENT_METHOD)
            .paymentStatus(UPDATED_PAYMENT_STATUS)
            .amountPaid(UPDATED_AMOUNT_PAID);

        restSupplierPaymentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSupplierPayments.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSupplierPayments))
            )
            .andExpect(status().isOk());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSupplierPaymentsToMatchAllProperties(updatedSupplierPayments);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<SupplierPayments> supplierPaymentsSearchList = Streamable.of(supplierPaymentsSearchRepository.findAll()).toList();
                SupplierPayments testSupplierPaymentsSearch = supplierPaymentsSearchList.get(searchDatabaseSizeAfter - 1);

                assertSupplierPaymentsAllPropertiesEquals(testSupplierPaymentsSearch, updatedSupplierPayments);
            });
    }

    @Test
    @Transactional
    void putNonExistingSupplierPayments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        supplierPayments.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSupplierPaymentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, supplierPayments.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(supplierPayments))
            )
            .andExpect(status().isBadRequest());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchSupplierPayments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        supplierPayments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSupplierPaymentsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(supplierPayments))
            )
            .andExpect(status().isBadRequest());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSupplierPayments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        supplierPayments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSupplierPaymentsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(supplierPayments)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateSupplierPaymentsWithPatch() throws Exception {
        // Initialize the database
        insertedSupplierPayments = supplierPaymentsRepository.saveAndFlush(supplierPayments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the supplierPayments using partial update
        SupplierPayments partialUpdatedSupplierPayments = new SupplierPayments();
        partialUpdatedSupplierPayments.setId(supplierPayments.getId());

        partialUpdatedSupplierPayments.paymentMethod(UPDATED_PAYMENT_METHOD);

        restSupplierPaymentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSupplierPayments.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSupplierPayments))
            )
            .andExpect(status().isOk());

        // Validate the SupplierPayments in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSupplierPaymentsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSupplierPayments, supplierPayments),
            getPersistedSupplierPayments(supplierPayments)
        );
    }

    @Test
    @Transactional
    void fullUpdateSupplierPaymentsWithPatch() throws Exception {
        // Initialize the database
        insertedSupplierPayments = supplierPaymentsRepository.saveAndFlush(supplierPayments);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the supplierPayments using partial update
        SupplierPayments partialUpdatedSupplierPayments = new SupplierPayments();
        partialUpdatedSupplierPayments.setId(supplierPayments.getId());

        partialUpdatedSupplierPayments
            .paymentDate(UPDATED_PAYMENT_DATE)
            .paymentMethod(UPDATED_PAYMENT_METHOD)
            .paymentStatus(UPDATED_PAYMENT_STATUS)
            .amountPaid(UPDATED_AMOUNT_PAID);

        restSupplierPaymentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSupplierPayments.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSupplierPayments))
            )
            .andExpect(status().isOk());

        // Validate the SupplierPayments in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSupplierPaymentsUpdatableFieldsEquals(
            partialUpdatedSupplierPayments,
            getPersistedSupplierPayments(partialUpdatedSupplierPayments)
        );
    }

    @Test
    @Transactional
    void patchNonExistingSupplierPayments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        supplierPayments.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSupplierPaymentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, supplierPayments.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(supplierPayments))
            )
            .andExpect(status().isBadRequest());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSupplierPayments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        supplierPayments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSupplierPaymentsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(supplierPayments))
            )
            .andExpect(status().isBadRequest());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSupplierPayments() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        supplierPayments.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSupplierPaymentsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(supplierPayments)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SupplierPayments in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteSupplierPayments() throws Exception {
        // Initialize the database
        insertedSupplierPayments = supplierPaymentsRepository.saveAndFlush(supplierPayments);
        supplierPaymentsRepository.save(supplierPayments);
        supplierPaymentsSearchRepository.save(supplierPayments);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the supplierPayments
        restSupplierPaymentsMockMvc
            .perform(delete(ENTITY_API_URL_ID, supplierPayments.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(supplierPaymentsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchSupplierPayments() throws Exception {
        // Initialize the database
        insertedSupplierPayments = supplierPaymentsRepository.saveAndFlush(supplierPayments);
        supplierPaymentsSearchRepository.save(supplierPayments);

        // Search the supplierPayments
        restSupplierPaymentsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + supplierPayments.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(supplierPayments.getId().intValue())))
            .andExpect(jsonPath("$.[*].paymentDate").value(hasItem(DEFAULT_PAYMENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].paymentMethod").value(hasItem(DEFAULT_PAYMENT_METHOD)))
            .andExpect(jsonPath("$.[*].paymentStatus").value(hasItem(DEFAULT_PAYMENT_STATUS)))
            .andExpect(jsonPath("$.[*].amountPaid").value(hasItem(sameNumber(DEFAULT_AMOUNT_PAID))));
    }

    protected long getRepositoryCount() {
        return supplierPaymentsRepository.count();
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

    protected SupplierPayments getPersistedSupplierPayments(SupplierPayments supplierPayments) {
        return supplierPaymentsRepository.findById(supplierPayments.getId()).orElseThrow();
    }

    protected void assertPersistedSupplierPaymentsToMatchAllProperties(SupplierPayments expectedSupplierPayments) {
        assertSupplierPaymentsAllPropertiesEquals(expectedSupplierPayments, getPersistedSupplierPayments(expectedSupplierPayments));
    }

    protected void assertPersistedSupplierPaymentsToMatchUpdatableProperties(SupplierPayments expectedSupplierPayments) {
        assertSupplierPaymentsAllUpdatablePropertiesEquals(
            expectedSupplierPayments,
            getPersistedSupplierPayments(expectedSupplierPayments)
        );
    }
}
