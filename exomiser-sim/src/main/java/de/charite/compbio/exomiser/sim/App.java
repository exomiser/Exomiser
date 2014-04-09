/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.sim;

import de.charite.compbio.exomiser.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * App class for Running the Exome Simulator from the command line.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class App {
    
    @Autowired
    private static ExomeSimulator exomeSimulator;
            
    public static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String args[]) {
        if (args.length < 2) {
            usage();
        }
        String outname = args[0];
        String type = args[1];

        if (!type.equals("ALL") && !type.equals("EA") && !type.equals("AA")) {
            usage();
        }

        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        exomeSimulator = context.getBean(ExomeSimulator.class);
        exomeSimulator.outputExome(type, outname);
    }
    
    public static void usage() {
        logger.error("Usage: java -jar ExomeSimulator.jar name-of-outfile type");
        logger.error("type: one of ALL,EA,AA(ESP project datatype)");
        System.exit(1);
    }
  
}
