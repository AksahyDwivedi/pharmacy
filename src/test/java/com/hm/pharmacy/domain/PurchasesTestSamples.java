package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PurchasesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Purchases getPurchasesSample1() {
        return new Purchases().id(1L).invoiceNumber("invoiceNumber1");
    }

    public static Purchases getPurchasesSample2() {
        return new Purchases().id(2L).invoiceNumber("invoiceNumber2");
    }

    public static Purchases getPurchasesRandomSampleGenerator() {
        return new Purchases().id(longCount.incrementAndGet()).invoiceNumber(UUID.randomUUID().toString());
    }
}
