package org.monarchinitiative.exomiser.cli.commands;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "annotate", description = "Annotates variants, either singly or from a file", hidden = true)
public final class AnnotateCommand implements ExomiserCommand {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help and exit" )
    boolean help;

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1", heading = "Input options%n")
    public InputOption inputOption;

    @CommandLine.Option(names = "--assembly", converter = GenomeAssemblyConverter.class, required = true, description = "Genome assembly of the input variant or VCF. (hg19 or hg38)")
    public GenomeAssembly genomeAssembly;

    public static class InputOption {

        @CommandLine.Option(names = {"-v", "--variant"}, description = "Genomic variant to annotate. Variants should be formatted in " +
                                                                       "'Broad format' e.g. 10-12345-T-G. Structural variants (>50 bases) require an end and a VCF SV type e.g. " +
                                                                       "1-12345-13456-N-DEL. Chromosomes should be of the form [1-22,X,Y,M]. Coordinates should use 1-based " +
                                                                       "intervals (i.e. 1-start, fully-closed) on the positive-strand, as in VCF. The genome assembly these " +
                                                                       "coordinates are annotated against will be that of the provided transcripts.%n" +
                                                                       "GRCh37 examples:%n" +
                                                                       "  1-818046-T-C%n  2-265023-C-CA%n  3-361464-TA-T%n  10-123256215-T-G%n" +
                                                                       "GRCh38 examples:%n" +
                                                                       "  1-65568-A-C%n  2-265023-C-CT%n  3-319781-TA-T%n  10-121496701-T-G",
                converter = VariantCoordinatesConverter.class)
        public VariantCoordinates variant;

        @CommandLine.Option(names = "--vcf", description = "Path of the VCF file to be annotated", required = true)
        public Path vcfPath;

        @Override
        public String toString() {
            return "InputOption{" +
                   "variant=" + variant +
                   ", vcfPath=" + vcfPath +
                   '}';
        }
    }

    @Override
    public boolean validate() {
        return inputOption != null && genomeAssembly != null;
    }

    @Override
    public String toString() {
        return "AnnotateCommand{" +
                inputOption +
               ", genomeAssembly=" + genomeAssembly +
               '}';
    }

    record VariantCoordinatesConverter() implements CommandLine.ITypeConverter<VariantCoordinates> {

        @Override
        public VariantCoordinates convert(String value) throws Exception {
            String[] input = value.split("[-:]"); // TODO: allow SPDI with a --variant-format option?
            if (input.length == 4) {
                // 1-122334-A-C
                int start = Integer.parseInt(input[1]);
                String ref = input[2];
                int end = start + ref.length() - 1;
                return new VariantCoordinates(input[0], start, end, ref, input[3]);
            } else if (input.length == 5) {
                // TODO: 10-123256215-123256220-A-DEL test all SV types- INS will need a length, how about BND/TRA, should angle brackets be required on input?
                int start = Integer.parseInt(input[1]);
                int end = Integer.parseInt(input[2]);
                String ref = input[3];
                return new VariantCoordinates(input[0], start, end, ref, input[4]);
            }
            throw new IllegalArgumentException();
        }
    }

    public record VariantCoordinates(String chr, int start, int end, String ref, String alt) {

        public GenomicVariant toGenomicVariant(GenomicAssembly genomicAssembly) {
            Contig contig = genomicAssembly.contigByName(chr);
            Coordinates coordinates = Coordinates.oneBased(start, end);
            return switch (alt) {
                case "DEL", "INS", "INV", "CNV", "DUP", "DEL_ME", "INS_ME", "BND", "TRA" ->
                        GenomicVariant.of(contig, Strand.POSITIVE, coordinates, ref, "<" + alt + ">", start - end);
                default -> GenomicVariant.of(contig, Strand.POSITIVE, coordinates, ref, alt);
            };
        }

        @Override
        public String toString() {
            return switch (alt) {
                case "DEL", "INS", "INV", "CNV", "DUP", "DEL_ME", "INS_ME", "BND", "TRA" ->
                        chr + '-' + start + '-' + end + '-' + ref + '-' + alt;
                default -> chr + '-' + start + '-' + ref + '-' + alt;
            };
        }
    }
}
