package org.monarchinitiative.exomiser.cli.command;


public sealed interface ExomiserCommand permits DiffReanalysisCommand, AnalysisCommand, BatchCommand, ExomiserCommands {

}
