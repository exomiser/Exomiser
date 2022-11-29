package org.monarchinitiative.exomiser.cli.command;


import org.monarchinitiative.exomiser.cli.VariantReanalyser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;


@Command(name = "diff-reanalysis",
        description = "Compares two Exomiser variants.tsv analysis files and prints significant changes to STDOUT.")
public final class DiffReanalysisCommand implements ExomiserCommand, Callable<Integer> {

    @Option(names = {"--original"}, description = "Original variants.tsv results file", required = true)
    public Path originalPath;
    @Option(names = {"--latest"}, description = "Reanalysed variants.tsv results file", required = true)
    public Path latestPath;

    @Override
    public String toString() {
        return "DiffReanalysisCommand{" +
                "originalPath=" + originalPath +
                ", latestPath=" + latestPath +
                '}';
    }

    @Override
    public Integer call() {
        return new VariantReanalyser(originalPath, latestPath, null).call();
    }
}
