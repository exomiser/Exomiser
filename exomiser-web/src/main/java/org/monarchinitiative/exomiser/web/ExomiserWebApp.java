package org.monarchinitiative.exomiser.web;

import org.monarchinitiative.exomiser.autoconfigure.EnableExomiser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@EnableExomiser
@SpringBootApplication
public class ExomiserWebApp {

    public static void main(String[] args) {
        SpringApplication.run(ExomiserWebApp.class, args);
    }

}
