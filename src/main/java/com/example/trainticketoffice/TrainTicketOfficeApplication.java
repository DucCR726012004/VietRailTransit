package com.example.trainticketoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TrainTicketOfficeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainTicketOfficeApplication.class, args);
    }
}