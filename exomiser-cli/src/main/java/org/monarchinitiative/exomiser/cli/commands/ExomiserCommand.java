package org.monarchinitiative.exomiser.cli.commands;

/**
 * Marker interface for Exomiser Commands
 */
public sealed interface ExomiserCommand permits AnalyseCommand, AnnotateCommand, BatchCommand {

    /**
     * The validate function should be used to indicate whether the command is in a valid state to be run. This should
     * only check that the correct command parameters have been provided. It is intended for this method to be used as a
     * check to catch states which the CLI parser is unable to validate.
     *
     * @return true if the command is valid.
     */
    boolean validate();
}
