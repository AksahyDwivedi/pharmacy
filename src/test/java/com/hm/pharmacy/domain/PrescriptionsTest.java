package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.CustomersTestSamples.*;
import static com.hm.pharmacy.domain.PrescriptionsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PrescriptionsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Prescriptions.class);
        Prescriptions prescriptions1 = getPrescriptionsSample1();
        Prescriptions prescriptions2 = new Prescriptions();
        assertThat(prescriptions1).isNotEqualTo(prescriptions2);

        prescriptions2.setId(prescriptions1.getId());
        assertThat(prescriptions1).isEqualTo(prescriptions2);

        prescriptions2 = getPrescriptionsSample2();
        assertThat(prescriptions1).isNotEqualTo(prescriptions2);
    }

    @Test
    void customersTest() {
        Prescriptions prescriptions = getPrescriptionsRandomSampleGenerator();
        Customers customersBack = getCustomersRandomSampleGenerator();

        prescriptions.setCustomers(customersBack);
        assertThat(prescriptions.getCustomers()).isEqualTo(customersBack);

        prescriptions.customers(null);
        assertThat(prescriptions.getCustomers()).isNull();
    }
}
