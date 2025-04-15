package org.monarchinitiative.exomiser.cli.commands;

import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "batch", description = "Runs a batch of analyses. This will avoid the initial application startup time for each sample if running singly," +
                                                   " will allow the JVM to fully optimise performance, and maximise the use of any caches which have been enabled." +
                                                   " In cases where hundreds or thousands of samples need running, it is recommended to run this command on batches of" +
                                                   " samples, in parallel jobs on several independent nodes.")
public final class BatchCommand implements ExomiserCommand {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help and exit" )
    boolean help;

    @CommandLine.Parameters(arity = "1", description = "Path to cli batch file. This should be in plain text file with the cli input for an analysis on each line.")
    Path batchFilePath;

    @CommandLine.Option(names = "--dry-run", description = "Runs initial checks on the commands specified in the batch file to ensure the file paths are valid and the sample names are correctly linked to any provided pedigree.")
    boolean dryRun;

    @Override
    public boolean validate() {
        // nothing to validate, or should this perform the dry-run?
        return true;
    }

    @Override
    public String toString() {
        return "BatchCommand{" +
               "batchFilePath=" + batchFilePath +
               ", dryRun=" + dryRun +
               '}';
    }
}
