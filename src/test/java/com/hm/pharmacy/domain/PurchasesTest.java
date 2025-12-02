package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.MedicineBatchesTestSamples.*;
import static com.hm.pharmacy.domain.PurchaseItemsTestSamples.*;
import static com.hm.pharmacy.domain.PurchasesTestSamples.*;
import static com.hm.pharmacy.domain.SupplierPaymentsTestSamples.*;
import static com.hm.pharmacy.domain.SuppliersTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PurchasesTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Purchases.class);
        Purchases purchases1 = getPurchasesSample1();
        Purchases purchases2 = new Purchases();
        assertThat(purchases1).isNotEqualTo(purchases2);

        purchases2.setId(purchases1.getId());
        assertThat(purchases1).isEqualTo(purchases2);

        purchases2 = getPurchasesSample2();
        assertThat(purchases1).isNotEqualTo(purchases2);
    }

    @Test
    void purchaseItemsTest() {
        Purchases purchases = getPurchasesRandomSampleGenerator();
        PurchaseItems purchaseItemsBack = getPurchaseItemsRandomSampleGenerator();

        purchases.addPurchaseItems(purchaseItemsBack);
        assertThat(purchases.getPurchaseItems()).containsOnly(purchaseItemsBack);
        assertThat(purchaseItemsBack.getPurchases()).isEqualTo(purchases);

        purchases.removePurchaseItems(purchaseItemsBack);
        assertThat(purchases.getPurchaseItems()).doesNotContain(purchaseItemsBack);
        assertThat(purchaseItemsBack.getPurchases()).isNull();

        purchases.purchaseItems(new HashSet<>(Set.of(purchaseItemsBack)));
        assertThat(purchases.getPurchaseItems()).containsOnly(purchaseItemsBack);
        assertThat(purchaseItemsBack.getPurchases()).isEqualTo(purchases);

        purchases.setPurchaseItems(new HashSet<>());
        assertThat(purchases.getPurchaseItems()).doesNotContain(purchaseItemsBack);
        assertThat(purchaseItemsBack.getPurchases()).isNull();
    }

    @Test
    void medicineBatchesTest() {
        Purchases purchases = getPurchasesRandomSampleGenerator();
        MedicineBatches medicineBatchesBack = getMedicineBatchesRandomSampleGenerator();

        purchases.addMedicineBatches(medicineBatchesBack);
        assertThat(purchases.getMedicineBatches()).containsOnly(medicineBatchesBack);
        assertThat(medicineBatchesBack.getPurchases()).isEqualTo(purchases);

        purchases.removeMedicineBatches(medicineBatchesBack);
        assertThat(purchases.getMedicineBatches()).doesNotContain(medicineBatchesBack);
        assertThat(medicineBatchesBack.getPurchases()).isNull();

        purchases.medicineBatches(new HashSet<>(Set.of(medicineBatchesBack)));
        assertThat(purchases.getMedicineBatches()).containsOnly(medicineBatchesBack);
        assertThat(medicineBatchesBack.getPurchases()).isEqualTo(purchases);

        purchases.setMedicineBatches(new HashSet<>());
        assertThat(purchases.getMedicineBatches()).doesNotContain(medicineBatchesBack);
        assertThat(medicineBatchesBack.getPurchases()).isNull();
    }

    @Test
    void supplierPaymentsTest() {
        Purchases purchases = getPurchasesRandomSampleGenerator();
        SupplierPayments supplierPaymentsBack = getSupplierPaymentsRandomSampleGenerator();

        purchases.addSupplierPayments(supplierPaymentsBack);
        assertThat(purchases.getSupplierPayments()).containsOnly(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getPurchases()).isEqualTo(purchases);

        purchases.removeSupplierPayments(supplierPaymentsBack);
        assertThat(purchases.getSupplierPayments()).doesNotContain(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getPurchases()).isNull();

        purchases.supplierPayments(new HashSet<>(Set.of(supplierPaymentsBack)));
        assertThat(purchases.getSupplierPayments()).containsOnly(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getPurchases()).isEqualTo(purchases);

        purchases.setSupplierPayments(new HashSet<>());
        assertThat(purchases.getSupplierPayments()).doesNotContain(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getPurchases()).isNull();
    }

    @Test
    void suppliersTest() {
        Purchases purchases = getPurchasesRandomSampleGenerator();
        Suppliers suppliersBack = getSuppliersRandomSampleGenerator();

        purchases.setSuppliers(suppliersBack);
        assertThat(purchases.getSuppliers()).isEqualTo(suppliersBack);

        purchases.suppliers(null);
        assertThat(purchases.getSuppliers()).isNull();
    }
}
