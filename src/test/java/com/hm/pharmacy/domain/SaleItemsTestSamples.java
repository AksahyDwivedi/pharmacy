package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SaleItemsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static SaleItems getSaleItemsSample1() {
        return new SaleItems().id(1L).quantity(1);
    }

    public static SaleItems getSaleItemsSample2() {
        return new SaleItems().id(2L).quantity(2);
    }

    public static SaleItems getSaleItemsRandomSampleGenerator() {
        return new SaleItems().id(longCount.incrementAndGet()).quantity(intCount.incrementAndGet());
    }
}
