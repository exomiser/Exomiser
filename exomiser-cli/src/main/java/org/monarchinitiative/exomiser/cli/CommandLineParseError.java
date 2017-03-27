/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CommandLineParseError extends RuntimeException {

    public CommandLineParseError(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandLineParseError(String message) {
        super(message);
    }
        
}
