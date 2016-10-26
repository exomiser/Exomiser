/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.monarchinitiative.exomiser.cli;

import org.monarchinitiative.exomiser.cli.config.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Locale;


/**
 * Main class for calling off the command line in the Exomiser package.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private AnnotationConfigApplicationContext applicationContext;

    public static void main(String[] args) {
        Main main = new Main();
        main.run(args);
    }

    private void run(String[] args) {
        setUpApplicationContext();
        showSplash();
        runExomiser(args);
    }

    //Get Spring started - this contains the configuration of the application
    private AnnotationConfigApplicationContext setUpApplicationContext() {
        Locale.setDefault(Locale.UK);
        logger.info("Locale set to {}", Locale.getDefault());
        //this is set here so that Spring can load the properties file from the jarFilePath
        System.setProperty("jarFilePath", jarFilePath().toString());
        applicationContext = new AnnotationConfigApplicationContext(MainConfig.class);
        return applicationContext;
    }

    private Path jarFilePath() {
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        try {
            return Paths.get(codeSource.getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
            logger.error("Unable to find jar file", ex);
            throw new RuntimeException("Unable to find jar file", ex);
        }
    }

    private void showSplash() {
        String splash = (String) applicationContext.getBean("banner");
        System.out.println(splash);
    }

    private void runExomiser(String[] args) {
        ExomiserCommandLineRunner exomiserCliRunner = applicationContext.getBean(ExomiserCommandLineRunner.class);
        exomiserCliRunner.run(args);
        logger.info("Exomising finished - Bye!");
    }

}
