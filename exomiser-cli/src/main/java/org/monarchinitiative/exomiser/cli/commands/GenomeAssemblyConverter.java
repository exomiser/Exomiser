package org.monarchinitiative.exomiser.cli.commands;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import picocli.CommandLine;

public class GenomeAssemblyConverter implements CommandLine.ITypeConverter<GenomeAssembly> {
    @Override
    public GenomeAssembly convert(String value) throws Exception {
        return GenomeAssembly.parseAssembly(value);
    }
}
