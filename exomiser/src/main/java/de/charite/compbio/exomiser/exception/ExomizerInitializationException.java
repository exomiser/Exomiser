package de.charite.compbio.exomiser.exception;


/**
 * Exception that occurs during initialization of the Exomizer, for
 * instance while parsing input files.
 * @author Peter Robinson
 * @version 0.02 (6 December,2013)
 */
public class ExomizerInitializationException extends ExomizerException {

    public static final long serialVersionUID = 1L;

    public ExomizerInitializationException() {
	super("Unknown exception during initialization of Exomizer");
    }

    public ExomizerInitializationException(String msg) {
	super(msg);
    }
}
