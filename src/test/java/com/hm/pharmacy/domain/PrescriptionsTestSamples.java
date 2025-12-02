package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PrescriptionsTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Prescriptions getPrescriptionsSample1() {
        return new Prescriptions().id(1L).doctorName("doctorName1");
    }

    public static Prescriptions getPrescriptionsSample2() {
        return new Prescriptions().id(2L).doctorName("doctorName2");
    }

    public static Prescriptions getPrescriptionsRandomSampleGenerator() {
        return new Prescriptions().id(longCount.incrementAndGet()).doctorName(UUID.randomUUID().toString());
    }
}
