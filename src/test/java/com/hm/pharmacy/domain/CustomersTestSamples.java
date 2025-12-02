package com.hm.pharmacy.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CustomersTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Customers getCustomersSample1() {
        return new Customers().id(1L).name("name1").phone("phone1").email("email1");
    }

    public static Customers getCustomersSample2() {
        return new Customers().id(2L).name("name2").phone("phone2").email("email2");
    }

    public static Customers getCustomersRandomSampleGenerator() {
        return new Customers()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .phone(UUID.randomUUID().toString())
            .email(UUID.randomUUID().toString());
    }
}
