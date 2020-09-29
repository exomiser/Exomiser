/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.genome;

import org.monarchinitiative.exomiser.core.model.Chromosome;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * genome reference assembly version - hg19/hg38.
 */
public enum GenomeAssembly {

    // GRCh37.p13:
    // https://www.ncbi.nlm.nih.gov/grc/human/data?asm=GRCh37.p13
    // Source: https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.13
    // For gory details, see:
    // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.25_GRCh37.p13/GCF_000001405.25_GRCh37.p13_assembly_report.txt
    HG19("hg19", "GRCh37", "GRCh37.p13", "GCF_000001405.25", List.of(
            Chromosome.unknown(),
            Chromosome.of(1, "1", 249250621, "NC_000001.10", "CM000663.1"),
            Chromosome.of(2, "2", 243199373, "NC_000002.11", "CM000664.1"),
            Chromosome.of(3, "3", 198022430, "NC_000003.11", "CM000665.1"),
            Chromosome.of(4, "4", 191154276, "NC_000004.11", "CM000666.1"),
            Chromosome.of(5, "5", 180915260, "NC_000005.9", "CM000667.1"),
            Chromosome.of(6, "6", 171115067, "NC_000006.11", "CM000668.1"),
            Chromosome.of(7, "7", 159138663, "NC_000007.13", "CM000669.1"),
            Chromosome.of(8, "8", 146364022, "NC_000008.10", "CM000670.1"),
            Chromosome.of(9, "9", 141213431, "NC_000009.11", "CM000671.1"),
            Chromosome.of(10, "10", 135534747, "NC_000010.10", "CM000672.1"),
            Chromosome.of(11, "11", 135006516, "NC_000011.9", "CM000673.1"),
            Chromosome.of(12, "12", 133851895, "NC_000012.11", "CM000674.1"),
            Chromosome.of(13, "13", 115169878, "NC_000013.10", "CM000675.1"),
            Chromosome.of(14, "14", 107349540, "NC_000014.8", "CM000676.1"),
            Chromosome.of(15, "15", 102531392, "NC_000015.9", "CM000677.1"),
            Chromosome.of(16, "16", 90354753, "NC_000016.9", "CM000678.1"),
            Chromosome.of(17, "17", 81195210, "NC_000017.10", "CM000679.1"),
            Chromosome.of(18, "18", 78077248, "NC_000018.9", "CM000680.1"),
            Chromosome.of(19, "19", 59128983, "NC_000019.9", "CM000681.1"),
            Chromosome.of(20, "20", 63025520, "NC_000020.10", "CM000682.1"),
            Chromosome.of(21, "21", 48129895, "NC_000021.8", "CM000683.1"),
            Chromosome.of(22, "22", 51304566, "NC_000022.10", "CM000684.1"),
            Chromosome.of(23, "X", 155270560, "NC_000023.10", "CM000685.1"),
            Chromosome.of(24, "Y", 59373566, "NC_000024.9", "CM000686.1"),
            Chromosome.of(25, "MT", 16569, "NC_012920.1", "J01415.2"))
    ),

    // GRCh38.p13:
    // https://www.ncbi.nlm.nih.gov/grc/human/data?asm=GRCh38.p13
    // https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.39
    // For gory details, see:
    // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.39_GRCh38.p13/GCF_000001405.39_GRCh38.p13_assembly_report.txt
    // grep 'assembled-molecule'
    HG38("hg38", "GRCh38", "GRCh38.p13", "GCF_000001405.39", List.of(
            Chromosome.unknown(),
            Chromosome.of(1, "1", 248956422, "NC_000001.11", "CM000663.2"),
            Chromosome.of(2, "2", 242193529, "NC_000002.12", "CM000664.2"),
            Chromosome.of(3, "3", 198295559, "NC_000003.12", "CM000665.2"),
            Chromosome.of(4, "4", 190214555, "NC_000004.12", "CM000666.2"),
            Chromosome.of(5, "5", 181538259, "NC_000005.10", "CM000667.2"),
            Chromosome.of(6, "6", 170805979, "NC_000006.12", "CM000668.2"),
            Chromosome.of(7, "7", 159345973, "NC_000007.14", "CM000669.2"),
            Chromosome.of(8, "8", 145138636, "NC_000008.11", "CM000670.2"),
            Chromosome.of(9, "9", 138394717, "NC_000009.12", "CM000671.2"),
            Chromosome.of(10, "10", 133797422, "NC_000010.11", "CM000672.2"),
            Chromosome.of(11, "11", 135086622, "NC_000011.10", "CM000673.2"),
            Chromosome.of(12, "12", 133275309, "NC_000012.12", "CM000674.2"),
            Chromosome.of(13, "13", 114364328, "NC_000013.11", "CM000675.2"),
            Chromosome.of(14, "14", 107043718, "NC_000014.9", "CM000676.2"),
            Chromosome.of(15, "15", 101991189, "NC_000015.10", "CM000677.2"),
            Chromosome.of(16, "16", 90338345, "NC_000016.10", "CM000678.2"),
            Chromosome.of(17, "17", 83257441, "NC_000017.11", "CM000679.2"),
            Chromosome.of(18, "18", 80373285, "NC_000018.10", "CM000680.2"),
            Chromosome.of(19, "19", 58617616, "NC_000019.10", "CM000681.2"),
            Chromosome.of(20, "20", 64444167, "NC_000020.11", "CM000682.2"),
            Chromosome.of(21, "21", 46709983, "NC_000021.9", "CM000683.2"),
            Chromosome.of(22, "22", 50818468, "NC_000022.11", "CM000684.2"),
            Chromosome.of(23, "X", 156040895, "NC_000023.11", "CM000685.2"),
            Chromosome.of(24, "Y", 57227415, "NC_000024.10", "CM000686.2"),
            Chromosome.of(25, "MT", 16569, "NC_012920.1", "J01415.2"))
    );

    private final String value;
    private final String grcValue;

    private final String name;
    private final String refSeqAccession;
    private final List<Chromosome> chromosomes;
    private final int numChromsomes;
    private final Map<String, Chromosome> chromosomesByName;


    GenomeAssembly(String value, String grcValue, String name, String refSeqAccession, List<Chromosome> chromosomes) {
        this.value = value;
        this.grcValue = grcValue;
        this.name = name;
        this.refSeqAccession = refSeqAccession;
        this.chromosomes = chromosomes;
        this.numChromsomes = this.chromosomes.size() - 1;
        this.chromosomesByName = chromosomes.stream().collect(toMap(Chromosome::getName, Function.identity()));
    }

    public static GenomeAssembly defaultBuild() {
        return GenomeAssembly.HG19;
    }

    public static GenomeAssembly parseAssembly(String value) {
        Objects.requireNonNull(value, "Genome build cannot be null");
        switch (value.toLowerCase()) {
            case "hg19":
            case "hg37":
            case "grch37":
                return HG19;
            case "hg38":
            case "grch38":
                return HG38;
            default:
                throw new InvalidGenomeAssemblyException(String.format("'%s' is not a valid/supported genome assembly.", value));
        }
    }

    @Override
    public String toString() {
        return value;
    }

    public String toGrcString() {
        return grcValue;
    }

    // https://www.ncbi.nlm.nih.gov/genome/?term=txid9606[orgn]
    // Returns the RefSeq id for the given chromosome number for the assembly.
    public String getRefSeqAccession(int chr) {
        return getContigById(chr).getRefSeqAccession();
    }

    public String getGenBankAccession(int chr) {
        return getContigById(chr).getGenBankAccession();
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public String getName() {
        return name;
    }

    public String refSeqAccession() {
        return refSeqAccession;
    }

    public Chromosome getContigById(int contigId) {
        if (contigId < 0 || contigId > numChromsomes) {
            // Exomiser uses '0' to represent unplaced contigs, 23, 24, 25 for X, Y, MT
            return Chromosome.unknown();
        }
        return chromosomes.get(contigId);
    }

    public Chromosome getContigByName(String contigName) {
        return chromosomesByName.getOrDefault(contigName, Chromosome.unknown());
    }

    public static class InvalidGenomeAssemblyException extends RuntimeException {

        public InvalidGenomeAssemblyException() {
        }

        public InvalidGenomeAssemblyException(String message) {
            super(message);
        }

        public InvalidGenomeAssemblyException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidGenomeAssemblyException(Throwable cause) {
            super(cause);
        }

        public InvalidGenomeAssemblyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}