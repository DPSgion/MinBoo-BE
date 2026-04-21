package com.phobo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PhoBoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhoBoApplication.class, args);
    }

}
