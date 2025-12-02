package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.MedicinesTestSamples.*;
import static com.hm.pharmacy.domain.SaleItemsTestSamples.*;
import static com.hm.pharmacy.domain.SalesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SaleItemsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SaleItems.class);
        SaleItems saleItems1 = getSaleItemsSample1();
        SaleItems saleItems2 = new SaleItems();
        assertThat(saleItems1).isNotEqualTo(saleItems2);

        saleItems2.setId(saleItems1.getId());
        assertThat(saleItems1).isEqualTo(saleItems2);

        saleItems2 = getSaleItemsSample2();
        assertThat(saleItems1).isNotEqualTo(saleItems2);
    }

    @Test
    void medicinesTest() {
        SaleItems saleItems = getSaleItemsRandomSampleGenerator();
        Medicines medicinesBack = getMedicinesRandomSampleGenerator();

        saleItems.setMedicines(medicinesBack);
        assertThat(saleItems.getMedicines()).isEqualTo(medicinesBack);

        saleItems.medicines(null);
        assertThat(saleItems.getMedicines()).isNull();
    }

    @Test
    void salesTest() {
        SaleItems saleItems = getSaleItemsRandomSampleGenerator();
        Sales salesBack = getSalesRandomSampleGenerator();

        saleItems.setSales(salesBack);
        assertThat(saleItems.getSales()).isEqualTo(salesBack);

        saleItems.sales(null);
        assertThat(saleItems.getSales()).isNull();
    }
}
