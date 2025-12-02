package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.PurchasesAsserts.*;
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
import com.hm.pharmacy.domain.Purchases;
import com.hm.pharmacy.repository.PurchasesRepository;
import com.hm.pharmacy.repository.search.PurchasesSearchRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
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
 * Integration tests for the {@link PurchasesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PurchasesResourceIT {

    private static final LocalDate DEFAULT_PURCHASE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PURCHASE_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_INVOICE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_INVOICE_NUMBER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_AMOUNT = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/purchases";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/purchases/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PurchasesRepository purchasesRepository;

    @Autowired
    private PurchasesSearchRepository purchasesSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPurchasesMockMvc;

    private Purchases purchases;

    private Purchases insertedPurchases;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Purchases createEntity() {
        return new Purchases().purchaseDate(DEFAULT_PURCHASE_DATE).invoiceNumber(DEFAULT_INVOICE_NUMBER).totalAmount(DEFAULT_TOTAL_AMOUNT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Purchases createUpdatedEntity() {
        return new Purchases().purchaseDate(UPDATED_PURCHASE_DATE).invoiceNumber(UPDATED_INVOICE_NUMBER).totalAmount(UPDATED_TOTAL_AMOUNT);
    }

    @BeforeEach
    void initTest() {
        purchases = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPurchases != null) {
            purchasesRepository.delete(insertedPurchases);
            purchasesSearchRepository.delete(insertedPurchases);
            insertedPurchases = null;
        }
    }

    @Test
    @Transactional
    void createPurchases() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        // Create the Purchases
        var returnedPurchases = om.readValue(
            restPurchasesMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(purchases)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Purchases.class
        );

        // Validate the Purchases in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPurchasesUpdatableFieldsEquals(returnedPurchases, getPersistedPurchases(returnedPurchases));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedPurchases = returnedPurchases;
    }

    @Test
    @Transactional
    void createPurchasesWithExistingId() throws Exception {
        // Create the Purchases with an existing ID
        purchases.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPurchasesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(purchases)))
            .andExpect(status().isBadRequest());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPurchases() throws Exception {
        // Initialize the database
        insertedPurchases = purchasesRepository.saveAndFlush(purchases);

        // Get all the purchasesList
        restPurchasesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(purchases.getId().intValue())))
            .andExpect(jsonPath("$.[*].purchaseDate").value(hasItem(DEFAULT_PURCHASE_DATE.toString())))
            .andExpect(jsonPath("$.[*].invoiceNumber").value(hasItem(DEFAULT_INVOICE_NUMBER)))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(sameNumber(DEFAULT_TOTAL_AMOUNT))));
    }

    @Test
    @Transactional
    void getPurchases() throws Exception {
        // Initialize the database
        insertedPurchases = purchasesRepository.saveAndFlush(purchases);

        // Get the purchases
        restPurchasesMockMvc
            .perform(get(ENTITY_API_URL_ID, purchases.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(purchases.getId().intValue()))
            .andExpect(jsonPath("$.purchaseDate").value(DEFAULT_PURCHASE_DATE.toString()))
            .andExpect(jsonPath("$.invoiceNumber").value(DEFAULT_INVOICE_NUMBER))
            .andExpect(jsonPath("$.totalAmount").value(sameNumber(DEFAULT_TOTAL_AMOUNT)));
    }

    @Test
    @Transactional
    void getNonExistingPurchases() throws Exception {
        // Get the purchases
        restPurchasesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPurchases() throws Exception {
        // Initialize the database
        insertedPurchases = purchasesRepository.saveAndFlush(purchases);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        purchasesSearchRepository.save(purchases);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());

        // Update the purchases
        Purchases updatedPurchases = purchasesRepository.findById(purchases.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPurchases are not directly saved in db
        em.detach(updatedPurchases);
        updatedPurchases.purchaseDate(UPDATED_PURCHASE_DATE).invoiceNumber(UPDATED_INVOICE_NUMBER).totalAmount(UPDATED_TOTAL_AMOUNT);

        restPurchasesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPurchases.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedPurchases))
            )
            .andExpect(status().isOk());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPurchasesToMatchAllProperties(updatedPurchases);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Purchases> purchasesSearchList = Streamable.of(purchasesSearchRepository.findAll()).toList();
                Purchases testPurchasesSearch = purchasesSearchList.get(searchDatabaseSizeAfter - 1);

                assertPurchasesAllPropertiesEquals(testPurchasesSearch, updatedPurchases);
            });
    }

    @Test
    @Transactional
    void putNonExistingPurchases() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        purchases.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPurchasesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, purchases.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(purchases))
            )
            .andExpect(status().isBadRequest());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPurchases() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        purchases.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchasesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(purchases))
            )
            .andExpect(status().isBadRequest());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPurchases() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        purchases.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchasesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(purchases)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePurchasesWithPatch() throws Exception {
        // Initialize the database
        insertedPurchases = purchasesRepository.saveAndFlush(purchases);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the purchases using partial update
        Purchases partialUpdatedPurchases = new Purchases();
        partialUpdatedPurchases.setId(purchases.getId());

        partialUpdatedPurchases.invoiceNumber(UPDATED_INVOICE_NUMBER);

        restPurchasesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPurchases.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPurchases))
            )
            .andExpect(status().isOk());

        // Validate the Purchases in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPurchasesUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPurchases, purchases),
            getPersistedPurchases(purchases)
        );
    }

    @Test
    @Transactional
    void fullUpdatePurchasesWithPatch() throws Exception {
        // Initialize the database
        insertedPurchases = purchasesRepository.saveAndFlush(purchases);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the purchases using partial update
        Purchases partialUpdatedPurchases = new Purchases();
        partialUpdatedPurchases.setId(purchases.getId());

        partialUpdatedPurchases.purchaseDate(UPDATED_PURCHASE_DATE).invoiceNumber(UPDATED_INVOICE_NUMBER).totalAmount(UPDATED_TOTAL_AMOUNT);

        restPurchasesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPurchases.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPurchases))
            )
            .andExpect(status().isOk());

        // Validate the Purchases in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPurchasesUpdatableFieldsEquals(partialUpdatedPurchases, getPersistedPurchases(partialUpdatedPurchases));
    }

    @Test
    @Transactional
    void patchNonExistingPurchases() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        purchases.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPurchasesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, purchases.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(purchases))
            )
            .andExpect(status().isBadRequest());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPurchases() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        purchases.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchasesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(purchases))
            )
            .andExpect(status().isBadRequest());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPurchases() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        purchases.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchasesMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(purchases)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Purchases in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePurchases() throws Exception {
        // Initialize the database
        insertedPurchases = purchasesRepository.saveAndFlush(purchases);
        purchasesRepository.save(purchases);
        purchasesSearchRepository.save(purchases);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the purchases
        restPurchasesMockMvc
            .perform(delete(ENTITY_API_URL_ID, purchases.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchasesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPurchases() throws Exception {
        // Initialize the database
        insertedPurchases = purchasesRepository.saveAndFlush(purchases);
        purchasesSearchRepository.save(purchases);

        // Search the purchases
        restPurchasesMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + purchases.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(purchases.getId().intValue())))
            .andExpect(jsonPath("$.[*].purchaseDate").value(hasItem(DEFAULT_PURCHASE_DATE.toString())))
            .andExpect(jsonPath("$.[*].invoiceNumber").value(hasItem(DEFAULT_INVOICE_NUMBER)))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(sameNumber(DEFAULT_TOTAL_AMOUNT))));
    }

    protected long getRepositoryCount() {
        return purchasesRepository.count();
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

    protected Purchases getPersistedPurchases(Purchases purchases) {
        return purchasesRepository.findById(purchases.getId()).orElseThrow();
    }

    protected void assertPersistedPurchasesToMatchAllProperties(Purchases expectedPurchases) {
        assertPurchasesAllPropertiesEquals(expectedPurchases, getPersistedPurchases(expectedPurchases));
    }

    protected void assertPersistedPurchasesToMatchUpdatableProperties(Purchases expectedPurchases) {
        assertPurchasesAllUpdatablePropertiesEquals(expectedPurchases, getPersistedPurchases(expectedPurchases));
    }
}
