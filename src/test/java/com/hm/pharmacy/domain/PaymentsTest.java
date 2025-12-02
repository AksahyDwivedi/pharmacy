package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.PaymentsTestSamples.*;
import static com.hm.pharmacy.domain.SalesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PaymentsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Payments.class);
        Payments payments1 = getPaymentsSample1();
        Payments payments2 = new Payments();
        assertThat(payments1).isNotEqualTo(payments2);

        payments2.setId(payments1.getId());
        assertThat(payments1).isEqualTo(payments2);

        payments2 = getPaymentsSample2();
        assertThat(payments1).isNotEqualTo(payments2);
    }

    @Test
    void salesTest() {
        Payments payments = getPaymentsRandomSampleGenerator();
        Sales salesBack = getSalesRandomSampleGenerator();

        payments.setSales(salesBack);
        assertThat(payments.getSales()).isEqualTo(salesBack);

        payments.sales(null);
        assertThat(payments.getSales()).isNull();
    }
}
