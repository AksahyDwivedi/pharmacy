package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class SuppliersTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Suppliers getSuppliersSample1() {
        return new Suppliers().id(1L).name("name1").contactPerson("contactPerson1").phone("phone1").email("email1");
    }

    public static Suppliers getSuppliersSample2() {
        return new Suppliers().id(2L).name("name2").contactPerson("contactPerson2").phone("phone2").email("email2");
    }

    public static Suppliers getSuppliersRandomSampleGenerator() {
        return new Suppliers()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .contactPerson(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString());
    }
}
