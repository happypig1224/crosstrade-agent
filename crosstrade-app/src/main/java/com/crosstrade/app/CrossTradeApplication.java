package com.crosstrade.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.crosstrade"})
@EnableJdbcRepositories(basePackages = {"com.crosstrade.common.repository"})
public class CrossTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrossTradeApplication.class, args);
    }
}