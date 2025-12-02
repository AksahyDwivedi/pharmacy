package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.CustomersTestSamples.*;
import static com.hm.pharmacy.domain.PrescriptionsTestSamples.*;
import static com.hm.pharmacy.domain.SalesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CustomersTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Customers.class);
        Customers customers1 = getCustomersSample1();
        Customers customers2 = new Customers();
        assertThat(customers1).isNotEqualTo(customers2);

        customers2.setId(customers1.getId());
        assertThat(customers1).isEqualTo(customers2);

        customers2 = getCustomersSample2();
        assertThat(customers1).isNotEqualTo(customers2);
    }

    @Test
    void prescriptionsTest() {
        Customers customers = getCustomersRandomSampleGenerator();
        Prescriptions prescriptionsBack = getPrescriptionsRandomSampleGenerator();

        customers.addPrescriptions(prescriptionsBack);
        assertThat(customers.getPrescriptions()).containsOnly(prescriptionsBack);
        assertThat(prescriptionsBack.getCustomers()).isEqualTo(customers);

        customers.removePrescriptions(prescriptionsBack);
        assertThat(customers.getPrescriptions()).doesNotContain(prescriptionsBack);
        assertThat(prescriptionsBack.getCustomers()).isNull();

        customers.prescriptions(new HashSet<>(Set.of(prescriptionsBack)));
        assertThat(customers.getPrescriptions()).containsOnly(prescriptionsBack);
        assertThat(prescriptionsBack.getCustomers()).isEqualTo(customers);

        customers.setPrescriptions(new HashSet<>());
        assertThat(customers.getPrescriptions()).doesNotContain(prescriptionsBack);
        assertThat(prescriptionsBack.getCustomers()).isNull();
    }

    @Test
    void salesTest() {
        Customers customers = getCustomersRandomSampleGenerator();
        Sales salesBack = getSalesRandomSampleGenerator();

        customers.addSales(salesBack);
        assertThat(customers.getSales()).containsOnly(salesBack);
        assertThat(salesBack.getCustomers()).isEqualTo(customers);

        customers.removeSales(salesBack);
        assertThat(customers.getSales()).doesNotContain(salesBack);
        assertThat(salesBack.getCustomers()).isNull();

        customers.sales(new HashSet<>(Set.of(salesBack)));
        assertThat(customers.getSales()).containsOnly(salesBack);
        assertThat(salesBack.getCustomers()).isEqualTo(customers);

        customers.setSales(new HashSet<>());
        assertThat(customers.getSales()).doesNotContain(salesBack);
        assertThat(salesBack.getCustomers()).isNull();
    }
}
