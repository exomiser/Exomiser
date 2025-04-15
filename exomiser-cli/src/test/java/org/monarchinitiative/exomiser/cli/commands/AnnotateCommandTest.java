package org.monarchinitiative.exomiser.cli.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AnnotateCommandTest {

    AnnotateCommand instance;
    CommandLine commandLine;

    @BeforeEach
    void setup() {
        instance = new AnnotateCommand();
        commandLine = new CommandLine(instance)
                .setCaseInsensitiveEnumValuesAllowed(true);
    }

    @Test
    void smallVariantOption() {
        commandLine.parseArgs("-v", "1-12345-A-C", "--assembly", "hg19");
        assertThat(instance.inputOption.variant, equalTo(new AnnotateCommand.VariantCoordinates("1", 12345, 12345, "A", "C")));
    }

    @Test
    void structuralVariantOption() {
        commandLine.parseArgs("-v", "1-11111-22222-A-<DEL>", "--assembly", "hg19");
        assertThat(instance.inputOption.variant, equalTo(new AnnotateCommand.VariantCoordinates("1", 11111, 22222, "A", "<DEL>")));
    }

    @Test
    void vcfOption() {
        commandLine.parseArgs("--vcf", "variants.vcf", "--assembly", "hg38");
        assertThat(instance.inputOption.vcfPath, equalTo(Path.of("variants.vcf")));
    }

    @Test
    void inputOptionsAreMutuallyExclusive() {
        assertThrows(CommandLine.MutuallyExclusiveArgsException.class, () -> commandLine.parseArgs("--vcf", "variants.vcf", "-v", "1-12345-A-C", "--assembly", "hg19"));
    }

}