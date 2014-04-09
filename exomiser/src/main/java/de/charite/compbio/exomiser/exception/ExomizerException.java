package de.charite.compbio.exomiser.exception;



/**
 * A small Exception hierarchy for the Exomizer. Subclasses of this
 * class are used to catch various problems from the various parts of the
 * program, some of which will cause the program to terminate and some will
 * note. In any case, we want to terminate gracefully so that it is possible
 * to print an HTML error message for users if say there are major problems with
 * the input format.
 * @author Peter Robinson
 * @version 0.02 (December 2,2012)
 */
public class ExomizerException extends Exception {

    private String mistake=null;
    
    public static final long serialVersionUID = 1L;

    public ExomizerException() {
	super();
    }

    public ExomizerException(String msg) {
	super(msg);
	this.mistake = msg;
    }

    public String getError()
    {
	return mistake;
    }

    /**
     * Print a summary of the error
     */
    public String toString() {
	return getError();
    }
}