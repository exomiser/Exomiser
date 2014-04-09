package de.charite.compbio.exomiser.exception;



/**
 * Exception that occurs during parsing of VCF files.
 * @author Peter Robinson 
 * @version August 22,2012
 */
public class ExomizerSQLException extends ExomizerException {

    public static final long serialVersionUID = 1L;

    public ExomizerSQLException() {
	super("Unknown SQL exception");
    }

    public ExomizerSQLException(String msg) {
	super(msg);
    }

}