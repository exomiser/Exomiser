package org.monarchinitiative.exomiser.db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@SpringBootApplication
public class BuildDatabaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(BuildDatabaseApplication.class, args).close();
    }
}
