package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.SaleItemsAsserts.*;
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
import com.hm.pharmacy.domain.SaleItems;
import com.hm.pharmacy.repository.SaleItemsRepository;
import com.hm.pharmacy.repository.search.SaleItemsSearchRepository;
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
 * Integration tests for the {@link SaleItemsResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SaleItemsResourceIT {

    private static final Integer DEFAULT_QUANTITY = 1;
    private static final Integer UPDATED_QUANTITY = 2;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/sale-items";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/sale-items/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SaleItemsRepository saleItemsRepository;

    @Autowired
    private SaleItemsSearchRepository saleItemsSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSaleItemsMockMvc;

    private SaleItems saleItems;

    private SaleItems insertedSaleItems;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SaleItems createEntity() {
        return new SaleItems().quantity(DEFAULT_QUANTITY).price(DEFAULT_PRICE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SaleItems createUpdatedEntity() {
        return new SaleItems().quantity(UPDATED_QUANTITY).price(UPDATED_PRICE);
    }

    @BeforeEach
    void initTest() {
        saleItems = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSaleItems != null) {
            saleItemsRepository.delete(insertedSaleItems);
            saleItemsSearchRepository.delete(insertedSaleItems);
            insertedSaleItems = null;
        }
    }

    @Test
    @Transactional
    void createSaleItems() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        // Create the SaleItems
        var returnedSaleItems = om.readValue(
            restSaleItemsMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(saleItems)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SaleItems.class
        );

        // Validate the SaleItems in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSaleItemsUpdatableFieldsEquals(returnedSaleItems, getPersistedSaleItems(returnedSaleItems));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedSaleItems = returnedSaleItems;
    }

    @Test
    @Transactional
    void createSaleItemsWithExistingId() throws Exception {
        // Create the SaleItems with an existing ID
        saleItems.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restSaleItemsMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(saleItems)))
            .andExpect(status().isBadRequest());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllSaleItems() throws Exception {
        // Initialize the database
        insertedSaleItems = saleItemsRepository.saveAndFlush(saleItems);

        // Get all the saleItemsList
        restSaleItemsMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(saleItems.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))));
    }

    @Test
    @Transactional
    void getSaleItems() throws Exception {
        // Initialize the database
        insertedSaleItems = saleItemsRepository.saveAndFlush(saleItems);

        // Get the saleItems
        restSaleItemsMockMvc
            .perform(get(ENTITY_API_URL_ID, saleItems.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(saleItems.getId().intValue()))
            .andExpect(jsonPath("$.quantity").value(DEFAULT_QUANTITY))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)));
    }

    @Test
    @Transactional
    void getNonExistingSaleItems() throws Exception {
        // Get the saleItems
        restSaleItemsMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSaleItems() throws Exception {
        // Initialize the database
        insertedSaleItems = saleItemsRepository.saveAndFlush(saleItems);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        saleItemsSearchRepository.save(saleItems);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());

        // Update the saleItems
        SaleItems updatedSaleItems = saleItemsRepository.findById(saleItems.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSaleItems are not directly saved in db
        em.detach(updatedSaleItems);
        updatedSaleItems.quantity(UPDATED_QUANTITY).price(UPDATED_PRICE);

        restSaleItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSaleItems.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSaleItems))
            )
            .andExpect(status().isOk());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSaleItemsToMatchAllProperties(updatedSaleItems);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<SaleItems> saleItemsSearchList = Streamable.of(saleItemsSearchRepository.findAll()).toList();
                SaleItems testSaleItemsSearch = saleItemsSearchList.get(searchDatabaseSizeAfter - 1);

                assertSaleItemsAllPropertiesEquals(testSaleItemsSearch, updatedSaleItems);
            });
    }

    @Test
    @Transactional
    void putNonExistingSaleItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        saleItems.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSaleItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, saleItems.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(saleItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchSaleItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        saleItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSaleItemsMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(saleItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSaleItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        saleItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSaleItemsMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(saleItems)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateSaleItemsWithPatch() throws Exception {
        // Initialize the database
        insertedSaleItems = saleItemsRepository.saveAndFlush(saleItems);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the saleItems using partial update
        SaleItems partialUpdatedSaleItems = new SaleItems();
        partialUpdatedSaleItems.setId(saleItems.getId());

        restSaleItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSaleItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSaleItems))
            )
            .andExpect(status().isOk());

        // Validate the SaleItems in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSaleItemsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedSaleItems, saleItems),
            getPersistedSaleItems(saleItems)
        );
    }

    @Test
    @Transactional
    void fullUpdateSaleItemsWithPatch() throws Exception {
        // Initialize the database
        insertedSaleItems = saleItemsRepository.saveAndFlush(saleItems);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the saleItems using partial update
        SaleItems partialUpdatedSaleItems = new SaleItems();
        partialUpdatedSaleItems.setId(saleItems.getId());

        partialUpdatedSaleItems.quantity(UPDATED_QUANTITY).price(UPDATED_PRICE);

        restSaleItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSaleItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSaleItems))
            )
            .andExpect(status().isOk());

        // Validate the SaleItems in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSaleItemsUpdatableFieldsEquals(partialUpdatedSaleItems, getPersistedSaleItems(partialUpdatedSaleItems));
    }

    @Test
    @Transactional
    void patchNonExistingSaleItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        saleItems.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSaleItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, saleItems.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(saleItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSaleItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        saleItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSaleItemsMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(saleItems))
            )
            .andExpect(status().isBadRequest());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSaleItems() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        saleItems.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSaleItemsMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(saleItems)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SaleItems in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteSaleItems() throws Exception {
        // Initialize the database
        insertedSaleItems = saleItemsRepository.saveAndFlush(saleItems);
        saleItemsRepository.save(saleItems);
        saleItemsSearchRepository.save(saleItems);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the saleItems
        restSaleItemsMockMvc
            .perform(delete(ENTITY_API_URL_ID, saleItems.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(saleItemsSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchSaleItems() throws Exception {
        // Initialize the database
        insertedSaleItems = saleItemsRepository.saveAndFlush(saleItems);
        saleItemsSearchRepository.save(saleItems);

        // Search the saleItems
        restSaleItemsMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + saleItems.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(saleItems.getId().intValue())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))));
    }

    protected long getRepositoryCount() {
        return saleItemsRepository.count();
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

    protected SaleItems getPersistedSaleItems(SaleItems saleItems) {
        return saleItemsRepository.findById(saleItems.getId()).orElseThrow();
    }

    protected void assertPersistedSaleItemsToMatchAllProperties(SaleItems expectedSaleItems) {
        assertSaleItemsAllPropertiesEquals(expectedSaleItems, getPersistedSaleItems(expectedSaleItems));
    }

    protected void assertPersistedSaleItemsToMatchUpdatableProperties(SaleItems expectedSaleItems) {
        assertSaleItemsAllUpdatablePropertiesEquals(expectedSaleItems, getPersistedSaleItems(expectedSaleItems));
    }
}
