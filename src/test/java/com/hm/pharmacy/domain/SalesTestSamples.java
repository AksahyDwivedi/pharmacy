package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SalesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Sales getSalesSample1() {
        return new Sales().id(1L).invoiceNumber("invoiceNumber1");
    }

    public static Sales getSalesSample2() {
        return new Sales().id(2L).invoiceNumber("invoiceNumber2");
    }

    public static Sales getSalesRandomSampleGenerator() {
        return new Sales().id(longCount.incrementAndGet()).invoiceNumber(UUID.randomUUID().toString());
    }
}
