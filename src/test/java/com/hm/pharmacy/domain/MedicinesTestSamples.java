package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MedicinesTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Medicines getMedicinesSample1() {
        return new Medicines().id(1L).name("name1").manufacturer("manufacturer1").category("category1").stock(1);
    }

    public static Medicines getMedicinesSample2() {
        return new Medicines().id(2L).name("name2").manufacturer("manufacturer2").category("category2").stock(2);
    }

    public static Medicines getMedicinesRandomSampleGenerator() {
        return new Medicines()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .manufacturer(UUID.randomUUID().toString())
            .category(UUID.randomUUID().toString())
            .stock(intCount.incrementAndGet());
    }
}
