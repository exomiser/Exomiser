/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.monarchinitiative.svart.assembly.SequenceRole.ASSEMBLED_MOLECULE;

/**
 * Genome reference assembly version - hg19/hg38.
 */
public enum GenomeAssembly {

    UNKNOWN("na", "na", "na", GenomicAssembly.of("", "", "", "", "", "", "", List.of())),

    // GRCh37.p13:
    // https://www.ncbi.nlm.nih.gov/grc/human/data?asm=GRCh37.p13
    // Source: https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.13
    // For gory details, see:
    // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.25_GRCh37.p13/GCF_000001405.25_GRCh37.p13_assembly_report.txt
    HG19("hg19", "GRCh37", "GRCh37.p13", GenomicAssemblies.GRCh37p13()),

    // GRCh38.p13:
    // https://www.ncbi.nlm.nih.gov/grc/human/data?asm=GRCh38.p13
    // https://www.ncbi.nlm.nih.gov/assembly/GCF_000001405.39
    // For gory details, see:
    // ftp://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.39_GRCh38.p13/GCF_000001405.39_GRCh38.p13_assembly_report.txt
    // grep 'assembled-molecule'
    HG38("hg38", "GRCh38", "GRCh38.p13", GenomicAssemblies.GRCh38p13());

    /**
     * Returns the {@link GenomeAssembly} of the provided {@link Contig}. Note that the mitochondrial contig is identical
     * in both assemblies so this function will always return 'HG19' in this case.
     *
     * @param contig
     * @return The {@link GenomeAssembly} of the {@link Contig}
     */
    public static GenomeAssembly assemblyOfContig(Contig contig) {
        if (HG19.containsContig(contig)) {
            return HG19;
        }
        if (HG38.containsContig(contig)) {
            return HG38;
        }
        return UNKNOWN;
    }

    private final String value;
    private final String grcValue;

    private final String name;
    private final GenomicAssembly genomicAssembly;
    private final List<Contig> contigs;

    GenomeAssembly(String value, String grcValue, String name, GenomicAssembly genomicAssembly) {
        this.value = value;
        this.grcValue = grcValue;
        this.name = name;
        this.genomicAssembly = genomicAssembly;
        this.contigs = genomicAssembly.contigs().stream()
                // only want 1-25, X, Y, MT
                .filter(contig -> contig.sequenceRole() == ASSEMBLED_MOLECULE)
                .collect(Collectors.toUnmodifiableList());
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

    public GenomicAssembly genomicAssembly() {
        return genomicAssembly;
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
        return getContigById(chr).refSeqAccession();
    }

    public String getGenBankAccession(int chr) {
        return getContigById(chr).genBankAccession();
    }

    public List<Contig> contigs() {
        return contigs;
    }

    public String getName() {
        return genomicAssembly.name();
    }

    public String refSeqAccession() {
        return genomicAssembly.refSeqAccession();
    }

    public boolean containsContig(Contig contig) {
        return genomicAssembly.containsContig(contig);
    }

    public Contig getContigById(int contigId) {
        return genomicAssembly.contigById(contigId);
    }

    public Contig getContigByName(String contigName) {
        return genomicAssembly.contigByName(contigName);
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