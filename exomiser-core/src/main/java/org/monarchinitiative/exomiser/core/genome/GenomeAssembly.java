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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * genome reference assembly version - hg19/hg38.
 */
public enum GenomeAssembly {

    //TODO: there is a circular dependency between GenomeAssembly and Chromosomes
    HG19("hg19", "GRCh37", Hg19.values()),
    HG38("hg38", "GRCh38", Hg38.values());

    private final String value;
    private final String grcValue;

    private final List<Chromosome> chromosomes;
    private final int numChromsomes;

    GenomeAssembly(String value, String grcValue, Chromosome[] chromosomes) {
        this.value = value;
        this.grcValue = grcValue;
        this.chromosomes = Arrays.stream(chromosomes)
                .sorted(Comparator.comparingInt(Chromosome::getId))
                .collect(Collectors.toList());
        this.numChromsomes = this.chromosomes.size() - 1;
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
    public String getReferenceAccession(int chr) {
        if (chr < 0 || chr > numChromsomes) {
            // Exomiser uses '0' to represent unplaced contigs, 23, 24, 25 for X, Y, MT
            return chromosomes.get(0).getAccession();
        }
        return chromosomes.get(chr).getAccession();
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
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

    // GRCh37.p13:
    // https://www.ncbi.nlm.nih.gov/grc/human/data?asm=GRCh37.p13
    // Source: https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.13
    // For gory details, see:
    // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.25_GRCh37.p13/GCF_000001405.25_GRCh37.p13_assembly_report.txt
    protected enum Hg19 implements Chromosome {

        UNKNOWN(0, ".", "UNKNOWN"),
        CHR_1(1, "1", "NC_000001.10"),
        CHR_2(2, "2", "NC_000002.11"),
        CHR_3(3, "3", "NC_000003.11"),
        CHR_4(4, "4", "NC_000004.11"),
        CHR_5(5, "5", "NC_000005.9"),
        CHR_6(6, "6", "NC_000006.11"),
        CHR_7(7, "7", "NC_000007.13"),
        CHR_8(8, "8", "NC_000008.10"),
        CHR_9(9, "9", "NC_000009.11"),
        CHR_10(10, "10", "NC_000010.10"),
        CHR_11(11, "11", "NC_000011.9"),
        CHR_12(12, "12", "NC_000012.11"),
        CHR_13(13, "13", "NC_000013.10"),
        CHR_14(14, "14", "NC_000014.8"),
        CHR_15(15, "15", "NC_000015.9"),
        CHR_16(16, "16", "NC_000016.9"),
        CHR_17(17, "17", "NC_000017.10"),
        CHR_18(18, "18", "NC_000018.9"),
        CHR_19(19, "19", "NC_000019.9"),
        CHR_20(20, "20", "NC_000020.10"),
        CHR_21(21, "21", "NC_000021.8"),
        CHR_22(22, "22", "NC_000022.10"),
        CHR_X(23, "X", "NC_000023.10"),
        CHR_Y(24, "Y", "NC_000024.9"),
        CHR_MT(25, "MT", "NC_012920.1");

        private final int id;
        private final String name;
        private final String accession;

        Hg19(int id, String name, String accession) {
            this.id = id;
            this.name = name;
            this.accession = accession;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getAccession() {
            return accession;
        }

        public GenomeAssembly getGenomeAssembly() {
            return GenomeAssembly.HG19;
        }
    }

    // GRCh38.p13:
    // https://www.ncbi.nlm.nih.gov/grc/human/data?asm=GRCh38.p13
    // https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.39
    // For gory details, see:
    // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.39_GRCh38.p13/GCF_000001405.39_GRCh38.p13_assembly_report.txt
    protected enum Hg38 implements Chromosome {

        UNKNOWN(0, ".", "UNKNOWN"),
        CHR_1(1, "1", "NC_000001.11"),
        CHR_2(2, "2", "NC_000002.12"),
        CHR_3(3, "3", "NC_000003.12"),
        CHR_4(4, "4", "NC_000004.12"),
        CHR_5(5, "5", "NC_000005.10"),
        CHR_6(6, "6", "NC_000006.12"),
        CHR_7(7, "7", "NC_000007.14"),
        CHR_8(8, "8", "NC_000008.11"),
        CHR_9(9, "9", "NC_000009.12"),
        CHR_10(10, "10", "NC_000010.11"),
        CHR_11(11, "11", "NC_000011.10"),
        CHR_12(12, "12", "NC_000012.12"),
        CHR_13(13, "13", "NC_000013.11"),
        CHR_14(14, "14", "NC_000014.9"),
        CHR_15(15, "15", "NC_000015.10"),
        CHR_16(16, "16", "NC_000016.10"),
        CHR_17(17, "17", "NC_000017.11"),
        CHR_18(18, "18", "NC_000018.10"),
        CHR_19(19, "19", "NC_000019.10"),
        CHR_20(20, "20", "NC_000020.11"),
        CHR_21(21, "21", "NC_000021.9"),
        CHR_22(22, "22", "NC_000022.11"),
        CHR_X(23, "X", "NC_000023.11"),
        CHR_Y(24, "Y", "NC_000024.10"),
        CHR_MT(25, "MT", "NC_012920.1");

        private final int id;
        private final String name;
        private final String accession;

        Hg38(int id, String name, String accession) {
            this.id = id;
            this.name = name;
            this.accession = accession;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getAccession() {
            return accession;
        }

        public GenomeAssembly getGenomeAssembly() {
            return GenomeAssembly.HG38;
        }
    }
}