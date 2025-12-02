package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.MedicinesTestSamples.*;
import static com.hm.pharmacy.domain.PurchaseItemsTestSamples.*;
import static com.hm.pharmacy.domain.PurchasesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PurchaseItemsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(PurchaseItems.class);
        PurchaseItems purchaseItems1 = getPurchaseItemsSample1();
        PurchaseItems purchaseItems2 = new PurchaseItems();
        assertThat(purchaseItems1).isNotEqualTo(purchaseItems2);

        purchaseItems2.setId(purchaseItems1.getId());
        assertThat(purchaseItems1).isEqualTo(purchaseItems2);

        purchaseItems2 = getPurchaseItemsSample2();
        assertThat(purchaseItems1).isNotEqualTo(purchaseItems2);
    }

    @Test
    void purchasesTest() {
        PurchaseItems purchaseItems = getPurchaseItemsRandomSampleGenerator();
        Purchases purchasesBack = getPurchasesRandomSampleGenerator();

        purchaseItems.setPurchases(purchasesBack);
        assertThat(purchaseItems.getPurchases()).isEqualTo(purchasesBack);

        purchaseItems.purchases(null);
        assertThat(purchaseItems.getPurchases()).isNull();
    }

    @Test
    void medicinesTest() {
        PurchaseItems purchaseItems = getPurchaseItemsRandomSampleGenerator();
        Medicines medicinesBack = getMedicinesRandomSampleGenerator();

        purchaseItems.setMedicines(medicinesBack);
        assertThat(purchaseItems.getMedicines()).isEqualTo(medicinesBack);

        purchaseItems.medicines(null);
        assertThat(purchaseItems.getMedicines()).isNull();
    }
}
