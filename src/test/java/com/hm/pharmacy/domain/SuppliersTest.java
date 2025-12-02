package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.PurchasesTestSamples.*;
import static com.hm.pharmacy.domain.SupplierPaymentsTestSamples.*;
import static com.hm.pharmacy.domain.SuppliersTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SuppliersTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Suppliers.class);
        Suppliers suppliers1 = getSuppliersSample1();
        Suppliers suppliers2 = new Suppliers();
        assertThat(suppliers1).isNotEqualTo(suppliers2);

        suppliers2.setId(suppliers1.getId());
        assertThat(suppliers1).isEqualTo(suppliers2);

        suppliers2 = getSuppliersSample2();
        assertThat(suppliers1).isNotEqualTo(suppliers2);
    }

    @Test
    void purchasesTest() {
        Suppliers suppliers = getSuppliersRandomSampleGenerator();
        Purchases purchasesBack = getPurchasesRandomSampleGenerator();

        suppliers.addPurchases(purchasesBack);
        assertThat(suppliers.getPurchases()).containsOnly(purchasesBack);
        assertThat(purchasesBack.getSuppliers()).isEqualTo(suppliers);

        suppliers.removePurchases(purchasesBack);
        assertThat(suppliers.getPurchases()).doesNotContain(purchasesBack);
        assertThat(purchasesBack.getSuppliers()).isNull();

        suppliers.purchases(new HashSet<>(Set.of(purchasesBack)));
        assertThat(suppliers.getPurchases()).containsOnly(purchasesBack);
        assertThat(purchasesBack.getSuppliers()).isEqualTo(suppliers);

        suppliers.setPurchases(new HashSet<>());
        assertThat(suppliers.getPurchases()).doesNotContain(purchasesBack);
        assertThat(purchasesBack.getSuppliers()).isNull();
    }

    @Test
    void supplierPaymentsTest() {
        Suppliers suppliers = getSuppliersRandomSampleGenerator();
        SupplierPayments supplierPaymentsBack = getSupplierPaymentsRandomSampleGenerator();

        suppliers.addSupplierPayments(supplierPaymentsBack);
        assertThat(suppliers.getSupplierPayments()).containsOnly(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getSuppliers()).isEqualTo(suppliers);

        suppliers.removeSupplierPayments(supplierPaymentsBack);
        assertThat(suppliers.getSupplierPayments()).doesNotContain(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getSuppliers()).isNull();

        suppliers.supplierPayments(new HashSet<>(Set.of(supplierPaymentsBack)));
        assertThat(suppliers.getSupplierPayments()).containsOnly(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getSuppliers()).isEqualTo(suppliers);

        suppliers.setSupplierPayments(new HashSet<>());
        assertThat(suppliers.getSupplierPayments()).doesNotContain(supplierPaymentsBack);
        assertThat(supplierPaymentsBack.getSuppliers()).isNull();
    }
}
