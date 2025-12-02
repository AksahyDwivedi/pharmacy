package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.CustomersTestSamples.*;
import static com.hm.pharmacy.domain.PaymentsTestSamples.*;
import static com.hm.pharmacy.domain.SaleItemsTestSamples.*;
import static com.hm.pharmacy.domain.SalesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SalesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Sales.class);
        Sales sales1 = getSalesSample1();
        Sales sales2 = new Sales();
        assertThat(sales1).isNotEqualTo(sales2);

        sales2.setId(sales1.getId());
        assertThat(sales1).isEqualTo(sales2);

        sales2 = getSalesSample2();
        assertThat(sales1).isNotEqualTo(sales2);
    }

    @Test
    void saleItemsTest() {
        Sales sales = getSalesRandomSampleGenerator();
        SaleItems saleItemsBack = getSaleItemsRandomSampleGenerator();

        sales.addSaleItems(saleItemsBack);
        assertThat(sales.getSaleItems()).containsOnly(saleItemsBack);
        assertThat(saleItemsBack.getSales()).isEqualTo(sales);

        sales.removeSaleItems(saleItemsBack);
        assertThat(sales.getSaleItems()).doesNotContain(saleItemsBack);
        assertThat(saleItemsBack.getSales()).isNull();

        sales.saleItems(new HashSet<>(Set.of(saleItemsBack)));
        assertThat(sales.getSaleItems()).containsOnly(saleItemsBack);
        assertThat(saleItemsBack.getSales()).isEqualTo(sales);

        sales.setSaleItems(new HashSet<>());
        assertThat(sales.getSaleItems()).doesNotContain(saleItemsBack);
        assertThat(saleItemsBack.getSales()).isNull();
    }

    @Test
    void paymentsTest() {
        Sales sales = getSalesRandomSampleGenerator();
        Payments paymentsBack = getPaymentsRandomSampleGenerator();

        sales.addPayments(paymentsBack);
        assertThat(sales.getPayments()).containsOnly(paymentsBack);
        assertThat(paymentsBack.getSales()).isEqualTo(sales);

        sales.removePayments(paymentsBack);
        assertThat(sales.getPayments()).doesNotContain(paymentsBack);
        assertThat(paymentsBack.getSales()).isNull();

        sales.payments(new HashSet<>(Set.of(paymentsBack)));
        assertThat(sales.getPayments()).containsOnly(paymentsBack);
        assertThat(paymentsBack.getSales()).isEqualTo(sales);

        sales.setPayments(new HashSet<>());
        assertThat(sales.getPayments()).doesNotContain(paymentsBack);
        assertThat(paymentsBack.getSales()).isNull();
    }

    @Test
    void customersTest() {
        Sales sales = getSalesRandomSampleGenerator();
        Customers customersBack = getCustomersRandomSampleGenerator();

        sales.setCustomers(customersBack);
        assertThat(sales.getCustomers()).isEqualTo(customersBack);

        sales.customers(null);
        assertThat(sales.getCustomers()).isNull();
    }
}
