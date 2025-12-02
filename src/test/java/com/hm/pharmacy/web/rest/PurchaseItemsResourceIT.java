package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.PurchaseItemsAsserts.*;
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
import com.hm.pharmacy.domain.PurchaseItems;
import com.hm.pharmacy.repository.PurchaseItemsRepository;
import com.hm.pharmacy.repository.search.PurchaseItemsSearchRepository;
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
 * Integration tests for the {@link PurchaseItemsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PurchaseItemsResourceIT {

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/purchase-items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/purchase-items/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PurchaseItemsRepository purchaseItemsRepository;

    @Autowired
    private PurchaseItemsSearchRepository purchaseItemsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPurchaseItemsMockMvc;

    private PurchaseItems purchaseItems;

    private PurchaseItems insertedPurchaseItems;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PurchaseItems createEntity() {
        return new PurchaseItems().quantity(DEFAULT_QUANTITY).price(DEFAULT_PRICE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static PurchaseItems createUpdatedEntity() {
        return new PurchaseItems().quantity(UPDATED_QUANTITY).price(UPDATED_PRICE);
    }

    @BeforeEach
    void initTest() {
        purchaseItems = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedPurchaseItems != null) {
            purchaseItemsRepository.delete(insertedPurchaseItems);
            purchaseItemsSearchRepository.delete(insertedPurchaseItems);
            insertedPurchaseItems = null;
        }
    }

    @Test
    @Transactional
    void createPurchaseItems() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        // Create the PurchaseItems
        var returnedPurchaseItems = om.readValue(
            restPurchaseItemsMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(purchaseItems)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PurchaseItems.class
        );

        // Validate the PurchaseItems in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertPurchaseItemsUpdatableFieldsEquals(returnedPurchaseItems, getPersistedPurchaseItems(returnedPurchaseItems));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedPurchaseItems = returnedPurchaseItems;
    }

    @Test
    @Transactional
    void createPurchaseItemsWithExistingId() throws Exception {
        // Create the PurchaseItems with an existing ID
        purchaseItems.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPurchaseItemsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(purchaseItems)))
            .andExpect(status().isBadRequest());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPurchaseItems() throws Exception {
        // Initialize the database
        insertedPurchaseItems = purchaseItemsRepository.saveAndFlush(purchaseItems);

        // Get all the purchaseItemsList
        restPurchaseItemsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(purchaseItems.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))));
    }

    @Test
    @Transactional
    void getPurchaseItems() throws Exception {
        // Initialize the database
        insertedPurchaseItems = purchaseItemsRepository.saveAndFlush(purchaseItems);

        // Get the purchaseItems
        restPurchaseItemsMockMvc
            .perform(get(ENTITY_API_URL_ID, purchaseItems.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(purchaseItems.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)));
    }

    @Test
    @Transactional
    void getNonExistingPurchaseItems() throws Exception {
        // Get the purchaseItems
        restPurchaseItemsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPurchaseItems() throws Exception {
        // Initialize the database
        insertedPurchaseItems = purchaseItemsRepository.saveAndFlush(purchaseItems);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        purchaseItemsSearchRepository.save(purchaseItems);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());

        // Update the purchaseItems
        PurchaseItems updatedPurchaseItems = purchaseItemsRepository.findById(purchaseItems.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPurchaseItems are not directly saved in db
        em.detach(updatedPurchaseItems);
        updatedPurchaseItems.quantity(UPDATED_QUANTITY).price(UPDATED_PRICE);

        restPurchaseItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedPurchaseItems.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedPurchaseItems))
            )
            .andExpect(status().isOk());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPurchaseItemsToMatchAllProperties(updatedPurchaseItems);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<PurchaseItems> purchaseItemsSearchList = Streamable.of(purchaseItemsSearchRepository.findAll()).toList();
                PurchaseItems testPurchaseItemsSearch = purchaseItemsSearchList.get(searchDatabaseSizeAfter - 1);

                assertPurchaseItemsAllPropertiesEquals(testPurchaseItemsSearch, updatedPurchaseItems);
            });
    }

    @Test
    @Transactional
    void putNonExistingPurchaseItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        purchaseItems.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPurchaseItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, purchaseItems.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(purchaseItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPurchaseItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        purchaseItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(purchaseItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPurchaseItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        purchaseItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseItemsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(purchaseItems)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePurchaseItemsWithPatch() throws Exception {
        // Initialize the database
        insertedPurchaseItems = purchaseItemsRepository.saveAndFlush(purchaseItems);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the purchaseItems using partial update
        PurchaseItems partialUpdatedPurchaseItems = new PurchaseItems();
        partialUpdatedPurchaseItems.setId(purchaseItems.getId());

        partialUpdatedPurchaseItems.price(UPDATED_PRICE);

        restPurchaseItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPurchaseItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPurchaseItems))
            )
            .andExpect(status().isOk());

        // Validate the PurchaseItems in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPurchaseItemsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPurchaseItems, purchaseItems),
            getPersistedPurchaseItems(purchaseItems)
        );
    }

    @Test
    @Transactional
    void fullUpdatePurchaseItemsWithPatch() throws Exception {
        // Initialize the database
        insertedPurchaseItems = purchaseItemsRepository.saveAndFlush(purchaseItems);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the purchaseItems using partial update
        PurchaseItems partialUpdatedPurchaseItems = new PurchaseItems();
        partialUpdatedPurchaseItems.setId(purchaseItems.getId());

        partialUpdatedPurchaseItems.quantity(UPDATED_QUANTITY).price(UPDATED_PRICE);

        restPurchaseItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPurchaseItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPurchaseItems))
            )
            .andExpect(status().isOk());

        // Validate the PurchaseItems in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPurchaseItemsUpdatableFieldsEquals(partialUpdatedPurchaseItems, getPersistedPurchaseItems(partialUpdatedPurchaseItems));
    }

    @Test
    @Transactional
    void patchNonExistingPurchaseItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        purchaseItems.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPurchaseItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, purchaseItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(purchaseItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPurchaseItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        purchaseItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(purchaseItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPurchaseItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        purchaseItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPurchaseItemsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(purchaseItems)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the PurchaseItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePurchaseItems() throws Exception {
        // Initialize the database
        insertedPurchaseItems = purchaseItemsRepository.saveAndFlush(purchaseItems);
        purchaseItemsRepository.save(purchaseItems);
        purchaseItemsSearchRepository.save(purchaseItems);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the purchaseItems
        restPurchaseItemsMockMvc
            .perform(delete(ENTITY_API_URL_ID, purchaseItems.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(purchaseItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPurchaseItems() throws Exception {
        // Initialize the database
        insertedPurchaseItems = purchaseItemsRepository.saveAndFlush(purchaseItems);
        purchaseItemsSearchRepository.save(purchaseItems);

        // Search the purchaseItems
        restPurchaseItemsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + purchaseItems.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(purchaseItems.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))));
    }

    protected long getRepositoryCount() {
        return purchaseItemsRepository.count();
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

    protected PurchaseItems getPersistedPurchaseItems(PurchaseItems purchaseItems) {
        return purchaseItemsRepository.findById(purchaseItems.getId()).orElseThrow();
    }

    protected void assertPersistedPurchaseItemsToMatchAllProperties(PurchaseItems expectedPurchaseItems) {
        assertPurchaseItemsAllPropertiesEquals(expectedPurchaseItems, getPersistedPurchaseItems(expectedPurchaseItems));
    }

    protected void assertPersistedPurchaseItemsToMatchUpdatableProperties(PurchaseItems expectedPurchaseItems) {
        assertPurchaseItemsAllUpdatablePropertiesEquals(expectedPurchaseItems, getPersistedPurchaseItems(expectedPurchaseItems));
    }
}
