package com.hm.pharmacy.web.rest;

import static com.hm.pharmacy.domain.SalesAsserts.*;
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
import com.hm.pharmacy.domain.Sales;
import com.hm.pharmacy.repository.SalesRepository;
import com.hm.pharmacy.repository.search.SalesSearchRepository;
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
 * Integration tests for the {@link SalesResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SalesResourceIT {

    private static final Instant DEFAULT_SALE_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_SALE_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_INVOICE_NUMBER = "AAAAAAAAAA";
    private static final String UPDATED_INVOICE_NUMBER = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_TOTAL_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_AMOUNT = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/sales";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/sales/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SalesRepository salesRepository;

    @Autowired
    private SalesSearchRepository salesSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSalesMockMvc;

    private Sales sales;

    private Sales insertedSales;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sales createEntity() {
        return new Sales().saleDate(DEFAULT_SALE_DATE).invoiceNumber(DEFAULT_INVOICE_NUMBER).totalAmount(DEFAULT_TOTAL_AMOUNT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sales createUpdatedEntity() {
        return new Sales().saleDate(UPDATED_SALE_DATE).invoiceNumber(UPDATED_INVOICE_NUMBER).totalAmount(UPDATED_TOTAL_AMOUNT);
    }

    @BeforeEach
    void initTest() {
        sales = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedSales != null) {
            salesRepository.delete(insertedSales);
            salesSearchRepository.delete(insertedSales);
            insertedSales = null;
        }
    }

    @Test
    @Transactional
    void createSales() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        // Create the Sales
        var returnedSales = om.readValue(
            restSalesMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sales)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            Sales.class
        );

        // Validate the Sales in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertSalesUpdatableFieldsEquals(returnedSales, getPersistedSales(returnedSales));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedSales = returnedSales;
    }

    @Test
    @Transactional
    void createSalesWithExistingId() throws Exception {
        // Create the Sales with an existing ID
        sales.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restSalesMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sales)))
            .andExpect(status().isBadRequest());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllSales() throws Exception {
        // Initialize the database
        insertedSales = salesRepository.saveAndFlush(sales);

        // Get all the salesList
        restSalesMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sales.getId().intValue())))
            .andExpect(jsonPath("$.[*].saleDate").value(hasItem(DEFAULT_SALE_DATE.toString())))
            .andExpect(jsonPath("$.[*].invoiceNumber").value(hasItem(DEFAULT_INVOICE_NUMBER)))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(sameNumber(DEFAULT_TOTAL_AMOUNT))));
    }

    @Test
    @Transactional
    void getSales() throws Exception {
        // Initialize the database
        insertedSales = salesRepository.saveAndFlush(sales);

        // Get the sales
        restSalesMockMvc
            .perform(get(ENTITY_API_URL_ID, sales.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(sales.getId().intValue()))
            .andExpect(jsonPath("$.saleDate").value(DEFAULT_SALE_DATE.toString()))
            .andExpect(jsonPath("$.invoiceNumber").value(DEFAULT_INVOICE_NUMBER))
            .andExpect(jsonPath("$.totalAmount").value(sameNumber(DEFAULT_TOTAL_AMOUNT)));
    }

    @Test
    @Transactional
    void getNonExistingSales() throws Exception {
        // Get the sales
        restSalesMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSales() throws Exception {
        // Initialize the database
        insertedSales = salesRepository.saveAndFlush(sales);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        salesSearchRepository.save(sales);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());

        // Update the sales
        Sales updatedSales = salesRepository.findById(sales.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSales are not directly saved in db
        em.detach(updatedSales);
        updatedSales.saleDate(UPDATED_SALE_DATE).invoiceNumber(UPDATED_INVOICE_NUMBER).totalAmount(UPDATED_TOTAL_AMOUNT);

        restSalesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedSales.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedSales))
            )
            .andExpect(status().isOk());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSalesToMatchAllProperties(updatedSales);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Sales> salesSearchList = Streamable.of(salesSearchRepository.findAll()).toList();
                Sales testSalesSearch = salesSearchList.get(searchDatabaseSizeAfter - 1);

                assertSalesAllPropertiesEquals(testSalesSearch, updatedSales);
            });
    }

    @Test
    @Transactional
    void putNonExistingSales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        sales.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSalesMockMvc
            .perform(put(ENTITY_API_URL_ID, sales.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sales)))
            .andExpect(status().isBadRequest());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchSales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        sales.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(sales))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        sales.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sales)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateSalesWithPatch() throws Exception {
        // Initialize the database
        insertedSales = salesRepository.saveAndFlush(sales);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sales using partial update
        Sales partialUpdatedSales = new Sales();
        partialUpdatedSales.setId(sales.getId());

        partialUpdatedSales.totalAmount(UPDATED_TOTAL_AMOUNT);

        restSalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSales.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSales))
            )
            .andExpect(status().isOk());

        // Validate the Sales in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSalesUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedSales, sales), getPersistedSales(sales));
    }

    @Test
    @Transactional
    void fullUpdateSalesWithPatch() throws Exception {
        // Initialize the database
        insertedSales = salesRepository.saveAndFlush(sales);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sales using partial update
        Sales partialUpdatedSales = new Sales();
        partialUpdatedSales.setId(sales.getId());

        partialUpdatedSales.saleDate(UPDATED_SALE_DATE).invoiceNumber(UPDATED_INVOICE_NUMBER).totalAmount(UPDATED_TOTAL_AMOUNT);

        restSalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSales.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSales))
            )
            .andExpect(status().isOk());

        // Validate the Sales in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSalesUpdatableFieldsEquals(partialUpdatedSales, getPersistedSales(partialUpdatedSales));
    }

    @Test
    @Transactional
    void patchNonExistingSales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        sales.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, sales.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(sales))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        sales.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(sales))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSales() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        sales.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSalesMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(sales)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sales in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteSales() throws Exception {
        // Initialize the database
        insertedSales = salesRepository.saveAndFlush(sales);
        salesRepository.save(sales);
        salesSearchRepository.save(sales);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the sales
        restSalesMockMvc
            .perform(delete(ENTITY_API_URL_ID, sales.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(salesSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchSales() throws Exception {
        // Initialize the database
        insertedSales = salesRepository.saveAndFlush(sales);
        salesSearchRepository.save(sales);

        // Search the sales
        restSalesMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + sales.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sales.getId().intValue())))
            .andExpect(jsonPath("$.[*].saleDate").value(hasItem(DEFAULT_SALE_DATE.toString())))
            .andExpect(jsonPath("$.[*].invoiceNumber").value(hasItem(DEFAULT_INVOICE_NUMBER)))
            .andExpect(jsonPath("$.[*].totalAmount").value(hasItem(sameNumber(DEFAULT_TOTAL_AMOUNT))));
    }

    protected long getRepositoryCount() {
        return salesRepository.count();
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

    protected Sales getPersistedSales(Sales sales) {
        return salesRepository.findById(sales.getId()).orElseThrow();
    }

    protected void assertPersistedSalesToMatchAllProperties(Sales expectedSales) {
        assertSalesAllPropertiesEquals(expectedSales, getPersistedSales(expectedSales));
    }

    protected void assertPersistedSalesToMatchUpdatableProperties(Sales expectedSales) {
        assertSalesAllUpdatablePropertiesEquals(expectedSales, getPersistedSales(expectedSales));
    }
}
