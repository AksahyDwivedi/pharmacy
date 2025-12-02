package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MedicineBatchesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static MedicineBatches getMedicineBatchesSample1() {
        return new MedicineBatches().id(1L).batchNumber("batchNumber1").quantity(1);
    }

    public static MedicineBatches getMedicineBatchesSample2() {
        return new MedicineBatches().id(2L).batchNumber("batchNumber2").quantity(2);
    }

    public static MedicineBatches getMedicineBatchesRandomSampleGenerator() {
        return new MedicineBatches()
            .id(longCount.incrementAndGet())
            .batchNumber(UUID.randomUUID().toString())
            .quantity(intCount.incrementAndGet());
    }
}
