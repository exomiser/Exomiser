package de.charite.compbio.exomiser.web;

import de.charite.compbio.exomiser.autoconfigure.EnableExomiser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@EnableExomiser
@ComponentScan(basePackages = {"de.charite.compbio.exomiser.core", "de.charite.compbio.exomiser.web"})
@SpringBootApplication
public class ExomiserWebApp {

    public static void main(String[] args) {
        SpringApplication.run(ExomiserWebApp.class, args);
    }

}
