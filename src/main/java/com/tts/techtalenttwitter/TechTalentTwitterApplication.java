package com.tts.techtalenttwitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class TechTalentTwitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechTalentTwitterApplication.class, args);
    }
}
