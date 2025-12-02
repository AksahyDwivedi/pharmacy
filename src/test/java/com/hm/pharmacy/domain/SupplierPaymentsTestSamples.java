package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SupplierPaymentsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static SupplierPayments getSupplierPaymentsSample1() {
        return new SupplierPayments().id(1L).paymentMethod("paymentMethod1").paymentStatus("paymentStatus1");
    }

    public static SupplierPayments getSupplierPaymentsSample2() {
        return new SupplierPayments().id(2L).paymentMethod("paymentMethod2").paymentStatus("paymentStatus2");
    }

    public static SupplierPayments getSupplierPaymentsRandomSampleGenerator() {
        return new SupplierPayments()
            .id(longCount.incrementAndGet())
            .paymentMethod(UUID.randomUUID().toString())
            .paymentStatus(UUID.randomUUID().toString());
    }
}
