package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.MedicineBatchesTestSamples.*;
import static com.hm.pharmacy.domain.MedicinesTestSamples.*;
import static com.hm.pharmacy.domain.PurchasesTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MedicineBatchesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedicineBatches.class);
        MedicineBatches medicineBatches1 = getMedicineBatchesSample1();
        MedicineBatches medicineBatches2 = new MedicineBatches();
        assertThat(medicineBatches1).isNotEqualTo(medicineBatches2);

        medicineBatches2.setId(medicineBatches1.getId());
        assertThat(medicineBatches1).isEqualTo(medicineBatches2);

        medicineBatches2 = getMedicineBatchesSample2();
        assertThat(medicineBatches1).isNotEqualTo(medicineBatches2);
    }

    @Test
    void purchasesTest() {
        MedicineBatches medicineBatches = getMedicineBatchesRandomSampleGenerator();
        Purchases purchasesBack = getPurchasesRandomSampleGenerator();

        medicineBatches.setPurchases(purchasesBack);
        assertThat(medicineBatches.getPurchases()).isEqualTo(purchasesBack);

        medicineBatches.purchases(null);
        assertThat(medicineBatches.getPurchases()).isNull();
    }

    @Test
    void medicinesTest() {
        MedicineBatches medicineBatches = getMedicineBatchesRandomSampleGenerator();
        Medicines medicinesBack = getMedicinesRandomSampleGenerator();

        medicineBatches.setMedicines(medicinesBack);
        assertThat(medicineBatches.getMedicines()).isEqualTo(medicinesBack);

        medicineBatches.medicines(null);
        assertThat(medicineBatches.getMedicines()).isNull();
    }
}
