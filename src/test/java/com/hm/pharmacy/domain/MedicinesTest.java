package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.MedicineBatchesTestSamples.*;
import static com.hm.pharmacy.domain.MedicinesTestSamples.*;
import static com.hm.pharmacy.domain.PurchaseItemsTestSamples.*;
import static com.hm.pharmacy.domain.SaleItemsTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MedicinesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Medicines.class);
        Medicines medicines1 = getMedicinesSample1();
        Medicines medicines2 = new Medicines();
        assertThat(medicines1).isNotEqualTo(medicines2);

        medicines2.setId(medicines1.getId());
        assertThat(medicines1).isEqualTo(medicines2);

        medicines2 = getMedicinesSample2();
        assertThat(medicines1).isNotEqualTo(medicines2);
    }

    @Test
    void purchaseItemsTest() {
        Medicines medicines = getMedicinesRandomSampleGenerator();
        PurchaseItems purchaseItemsBack = getPurchaseItemsRandomSampleGenerator();

        medicines.addPurchaseItems(purchaseItemsBack);
        assertThat(medicines.getPurchaseItems()).containsOnly(purchaseItemsBack);
        assertThat(purchaseItemsBack.getMedicines()).isEqualTo(medicines);

        medicines.removePurchaseItems(purchaseItemsBack);
        assertThat(medicines.getPurchaseItems()).doesNotContain(purchaseItemsBack);
        assertThat(purchaseItemsBack.getMedicines()).isNull();

        medicines.purchaseItems(new HashSet<>(Set.of(purchaseItemsBack)));
        assertThat(medicines.getPurchaseItems()).containsOnly(purchaseItemsBack);
        assertThat(purchaseItemsBack.getMedicines()).isEqualTo(medicines);

        medicines.setPurchaseItems(new HashSet<>());
        assertThat(medicines.getPurchaseItems()).doesNotContain(purchaseItemsBack);
        assertThat(purchaseItemsBack.getMedicines()).isNull();
    }

    @Test
    void medicineBatchesTest() {
        Medicines medicines = getMedicinesRandomSampleGenerator();
        MedicineBatches medicineBatchesBack = getMedicineBatchesRandomSampleGenerator();

        medicines.addMedicineBatches(medicineBatchesBack);
        assertThat(medicines.getMedicineBatches()).containsOnly(medicineBatchesBack);
        assertThat(medicineBatchesBack.getMedicines()).isEqualTo(medicines);

        medicines.removeMedicineBatches(medicineBatchesBack);
        assertThat(medicines.getMedicineBatches()).doesNotContain(medicineBatchesBack);
        assertThat(medicineBatchesBack.getMedicines()).isNull();

        medicines.medicineBatches(new HashSet<>(Set.of(medicineBatchesBack)));
        assertThat(medicines.getMedicineBatches()).containsOnly(medicineBatchesBack);
        assertThat(medicineBatchesBack.getMedicines()).isEqualTo(medicines);

        medicines.setMedicineBatches(new HashSet<>());
        assertThat(medicines.getMedicineBatches()).doesNotContain(medicineBatchesBack);
        assertThat(medicineBatchesBack.getMedicines()).isNull();
    }

    @Test
    void saleItemsTest() {
        Medicines medicines = getMedicinesRandomSampleGenerator();
        SaleItems saleItemsBack = getSaleItemsRandomSampleGenerator();

        medicines.addSaleItems(saleItemsBack);
        assertThat(medicines.getSaleItems()).containsOnly(saleItemsBack);
        assertThat(saleItemsBack.getMedicines()).isEqualTo(medicines);

        medicines.removeSaleItems(saleItemsBack);
        assertThat(medicines.getSaleItems()).doesNotContain(saleItemsBack);
        assertThat(saleItemsBack.getMedicines()).isNull();

        medicines.saleItems(new HashSet<>(Set.of(saleItemsBack)));
        assertThat(medicines.getSaleItems()).containsOnly(saleItemsBack);
        assertThat(saleItemsBack.getMedicines()).isEqualTo(medicines);

        medicines.setSaleItems(new HashSet<>());
        assertThat(medicines.getSaleItems()).doesNotContain(saleItemsBack);
        assertThat(saleItemsBack.getMedicines()).isNull();
    }
}
