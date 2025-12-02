package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PaymentsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Payments getPaymentsSample1() {
        return new Payments().id(1L).paymentMethod("paymentMethod1").paymentStatus("paymentStatus1");
    }

    public static Payments getPaymentsSample2() {
        return new Payments().id(2L).paymentMethod("paymentMethod2").paymentStatus("paymentStatus2");
    }

    public static Payments getPaymentsRandomSampleGenerator() {
        return new Payments()
            .id(longCount.incrementAndGet())
            .paymentMethod(UUID.randomUUID().toString())
            .paymentStatus(UUID.randomUUID().toString());
    }
}
