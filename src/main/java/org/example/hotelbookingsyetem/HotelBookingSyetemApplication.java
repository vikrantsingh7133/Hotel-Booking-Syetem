package org.example.hotelbookingsyetem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelBookingSyetemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelBookingSyetemApplication.class, args);
    }

}
