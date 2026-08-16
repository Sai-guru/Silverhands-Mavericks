package io.bootify.silverhands;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SilverhandsApplication {

    public static void main(final String[] args) {
        SpringApplication.run(SilverhandsApplication.class, args);

        System.out.println("Silverhands application started successfully.");
    }

}
