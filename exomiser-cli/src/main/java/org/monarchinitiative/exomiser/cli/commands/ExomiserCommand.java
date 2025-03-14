package org.monarchinitiative.exomiser.cli.commands;

public sealed interface ExomiserCommand permits AnalyseCommand, AnnotateCommand, BatchCommand {
    // marker interface for exomiser Commands

    boolean validate();
}
