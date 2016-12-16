package com.capside.realtimedemo.consumer;

/*
    Derived from: https://github.com/capside/aws-kinesis-zombies
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
