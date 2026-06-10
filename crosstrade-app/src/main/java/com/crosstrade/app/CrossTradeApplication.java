package com.crosstrade.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.crosstrade"})
public class CrossTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrossTradeApplication.class, args);
    }
}