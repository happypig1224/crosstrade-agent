package com.crosstrade;

import java.util.UUID;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class Test {
    @org.junit.jupiter.api.Test
    public void test() {
        System.out.println(UUID.randomUUID().toString().replace("-", ""));
    }
}
