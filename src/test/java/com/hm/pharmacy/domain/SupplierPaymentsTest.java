package com.hm.pharmacy.domain;

import static com.hm.pharmacy.domain.PurchasesTestSamples.*;
import static com.hm.pharmacy.domain.SupplierPaymentsTestSamples.*;
import static com.hm.pharmacy.domain.SuppliersTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.hm.pharmacy.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SupplierPaymentsTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SupplierPayments.class);
        SupplierPayments supplierPayments1 = getSupplierPaymentsSample1();
        SupplierPayments supplierPayments2 = new SupplierPayments();
        assertThat(supplierPayments1).isNotEqualTo(supplierPayments2);

        supplierPayments2.setId(supplierPayments1.getId());
        assertThat(supplierPayments1).isEqualTo(supplierPayments2);

        supplierPayments2 = getSupplierPaymentsSample2();
        assertThat(supplierPayments1).isNotEqualTo(supplierPayments2);
    }

    @Test
    void suppliersTest() {
        SupplierPayments supplierPayments = getSupplierPaymentsRandomSampleGenerator();
        Suppliers suppliersBack = getSuppliersRandomSampleGenerator();

        supplierPayments.setSuppliers(suppliersBack);
        assertThat(supplierPayments.getSuppliers()).isEqualTo(suppliersBack);

        supplierPayments.suppliers(null);
        assertThat(supplierPayments.getSuppliers()).isNull();
    }

    @Test
    void purchasesTest() {
        SupplierPayments supplierPayments = getSupplierPaymentsRandomSampleGenerator();
        Purchases purchasesBack = getPurchasesRandomSampleGenerator();

        supplierPayments.setPurchases(purchasesBack);
        assertThat(supplierPayments.getPurchases()).isEqualTo(purchasesBack);

        supplierPayments.purchases(null);
        assertThat(supplierPayments.getPurchases()).isNull();
    }
}
