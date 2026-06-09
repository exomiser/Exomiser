package org.monarchinitiative.exomiser.cli.commands;

import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "pheno", hidden = true)
public final class PhenotypeCommand implements ExomiserCommand {

    @CommandLine.Option(names = {"--phenopacket"}, description = "Phenopacket file")
    public Path phenopacketPath;

    @Override
    public boolean validate() {
        return true;
    }
}
