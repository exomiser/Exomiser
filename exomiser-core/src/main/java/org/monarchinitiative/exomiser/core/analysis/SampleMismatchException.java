package org.monarchinitiative.exomiser.core.analysis;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SampleMismatchException extends RuntimeException {
    public SampleMismatchException(String message) {
        super(message);
    }
}
