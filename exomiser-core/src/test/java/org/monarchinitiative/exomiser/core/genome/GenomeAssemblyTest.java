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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.svart.Contig;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.genome.GenomeAssembly.HG19;
import static org.monarchinitiative.exomiser.core.genome.GenomeAssembly.HG38;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeAssemblyTest {

    @Test
    public void testDefaultBuild() {
        assertThat(GenomeAssembly.defaultBuild(), equalTo(HG19));
    }

    @Test
    public void testNamesIncludeHg19() {
        assertThat(GenomeAssembly.parseAssembly("hg19"), equalTo(HG19));
    }

    @Test
    public void testNamesIncludeHg37() {
        assertThat(GenomeAssembly.parseAssembly("hg37"), equalTo(HG19));
    }

    @Test
    public void testNamesIncludeHg38() {
        assertThat(GenomeAssembly.parseAssembly("hg38"), equalTo(HG38));
    }

    @Test
    public void testCanUseGrchNomenclature() {
        assertThat(GenomeAssembly.parseAssembly("GRCh37"), equalTo(HG19));
        assertThat(GenomeAssembly.parseAssembly("GRCh38"), equalTo(HG38));
    }

    @Test
    public void testEmptyNameThrowsException() {
        assertThrows(GenomeAssembly.InvalidGenomeAssemblyException.class, () -> GenomeAssembly.parseAssembly(""));
    }

    @Test
    public void testNullNameThrowsException() {
        assertThrows(NullPointerException.class, () -> GenomeAssembly.parseAssembly(null));
    }

    @Test
    public void testUnrecognisedNameThrowsException() {
        assertThrows(GenomeAssembly.InvalidGenomeAssemblyException.class, () -> GenomeAssembly.parseAssembly("unrecognised build number"));
    }

    @Test
    public void testToString() {
        assertThat(HG19.toString(), equalTo("hg19"));
        assertThat(HG38.toString(), equalTo("hg38"));
    }

    @Test
    void testGetReferenceIdUnknown() {
        assertThat(HG19.getRefSeqAccession(-1), equalTo(""));
        assertThat(HG38.getRefSeqAccession(-1), equalTo(""));

        assertThat(HG19.getRefSeqAccession(999), equalTo(""));
        assertThat(HG38.getRefSeqAccession(999), equalTo(""));
    }

    @Test
    void testGetReferenceIdUnplaced() {
        assertThat(HG19.getRefSeqAccession(0), equalTo(""));
        assertThat(HG38.getRefSeqAccession(0), equalTo(""));
    }

    @Test
    void testGetReferenceIdChr1() {
        assertThat(HG19.getRefSeqAccession(1), equalTo("NC_000001.10"));
        assertThat(HG38.getRefSeqAccession(1), equalTo("NC_000001.11"));
    }

    @Test
    void testGetReferenceIdChrX() {
        assertThat(HG19.getRefSeqAccession(23), equalTo("NC_000023.10"));
        assertThat(HG38.getRefSeqAccession(23), equalTo("NC_000023.11"));
    }

    @Test
    void testGetReferenceIdChrM() {
        assertThat(HG19.getRefSeqAccession(25), equalTo("NC_012920.1"));
        assertThat(HG38.getRefSeqAccession(25), equalTo("NC_012920.1"));
    }

    @Test
    void testGetChromosomeId() {
        assertThat(HG19.getContigById(1).id(), equalTo(1));
        assertThat(HG38.getContigById(1).id(), equalTo(1));

        assertThat(HG19.getContigById(24).id(), equalTo(24));
        assertThat(HG38.getContigById(24).id(), equalTo(24));
    }

    @Test
    void testGetChromosomeName() {
        assertThat(HG19.getContigById(1).name(), equalTo("1"));
        assertThat(HG38.getContigById(1).name(), equalTo("1"));

        assertThat(HG19.getContigById(24).name(), equalTo("Y"));
        assertThat(HG38.getContigById(24).name(), equalTo("Y"));
    }

    @Test
    void testGetChromosomeAccession() {
        assertThat(HG19.getContigById(1).refSeqAccession(), equalTo("NC_000001.10"));
        assertThat(HG38.getContigById(1).refSeqAccession(), equalTo("NC_000001.11"));

        assertThat(HG19.getContigById(24).refSeqAccession(), equalTo("NC_000024.9"));
        assertThat(HG38.getContigById(24).refSeqAccession(), equalTo("NC_000024.10"));
    }

    @Test
    void getContigByNameUnrecognisedReturnsUnknown() {
        Contig wibble = HG19.getContigByName("Wibble!");
        assertThat(wibble, equalTo(Contig.unknown()));
    }

    @Test
    void getContigByName() {
        Contig hg19Chr1 = HG19.getContigByName("1");
        assertThat(hg19Chr1.id(), equalTo(1));
        assertThat(hg19Chr1.name(), equalTo("1"));
        assertThat(hg19Chr1.length(), equalTo(249250621));
        assertThat(hg19Chr1.refSeqAccession(), equalTo("NC_000001.10"));

        Contig hg19chrM = HG19.getContigByName("MT");
        assertThat(hg19chrM.id(), equalTo(25));
        assertThat(hg19chrM.name(), equalTo("MT"));
        assertThat(hg19chrM.length(), equalTo(16569));
        assertThat(hg19chrM.refSeqAccession(), equalTo("NC_012920.1"));

        Contig hg38Chr1 = HG38.getContigByName("1");
        assertThat(hg38Chr1.id(), equalTo(1));
        assertThat(hg38Chr1.name(), equalTo("1"));
        assertThat(hg38Chr1.length(), equalTo(248956422));
        assertThat(hg38Chr1.refSeqAccession(), equalTo("NC_000001.11"));

        Contig hg38chrM = HG19.getContigByName("MT");
        assertThat(hg38chrM.id(), equalTo(25));
        assertThat(hg38chrM.name(), equalTo("MT"));
        assertThat(hg38chrM.length(), equalTo(16569));
        assertThat(hg38chrM.refSeqAccession(), equalTo("NC_012920.1"));
    }

    @Test
    void getContigById() {
        Contig hg19Chr1 = HG19.getContigById(1);
        assertThat(hg19Chr1.id(), equalTo(1));
        assertThat(hg19Chr1.name(), equalTo("1"));
        assertThat(hg19Chr1.length(), equalTo(249250621));
        assertThat(hg19Chr1.refSeqAccession(), equalTo("NC_000001.10"));

        Contig hg19chrM = HG19.getContigById(25);
        assertThat(hg19chrM.id(), equalTo(25));
        assertThat(hg19chrM.name(), equalTo("MT"));
        assertThat(hg19chrM.length(), equalTo(16569));
        assertThat(hg19chrM.refSeqAccession(), equalTo("NC_012920.1"));

        Contig hg38Chr1 = HG38.getContigById(1);
        assertThat(hg38Chr1.id(), equalTo(1));
        assertThat(hg38Chr1.name(), equalTo("1"));
        assertThat(hg38Chr1.length(), equalTo(248956422));
        assertThat(hg38Chr1.refSeqAccession(), equalTo("NC_000001.11"));

        Contig hg38chrM = HG19.getContigById(25);
        assertThat(hg38chrM.id(), equalTo(25));
        assertThat(hg38chrM.name(), equalTo("MT"));
        assertThat(hg38chrM.length(), equalTo(16569));
        assertThat(hg38chrM.refSeqAccession(), equalTo("NC_012920.1"));
    }

    @Test
    void onlyContainsAssembledMolecules() {
        List<Integer> expected = Stream.iterate(1, i -> i + 1).limit(25).collect(toList());
        assertThat(HG19.contigs().stream().map(Contig::id).collect(toList()), equalTo(expected));
        assertThat(HG38.contigs().stream().map(Contig::id).collect(toList()), equalTo(expected));
    }

    @Test
    public void testAssemblyOfHg19Contig() {
        Contig hg19chr1 = HG19.getContigById(1);
        Contig hg38chr1 = HG38.getContigById(1);
        assertThat(HG19.containsContig(hg19chr1), equalTo(true));
        assertThat(HG19.containsContig(hg38chr1), equalTo(false));
        assertThat(HG38.containsContig(hg19chr1), equalTo(false));
        assertThat(HG38.containsContig(hg38chr1), equalTo(true));
    }

    @Test
    public void testAssemblyOfHg37Contig() {
        Contig chr1 = HG19.getContigById(1);
        assertThat(GenomeAssembly.assemblyOfContig(chr1), equalTo(HG19));
    }

    @Test
    public void testAssemblyOfHg38Contig() {
        Contig chr1 = HG38.getContigById(1);
        assertThat(GenomeAssembly.assemblyOfContig(chr1), equalTo(HG38));
    }

    @Test
    public void testAssemblyOfMtContig() {
        Contig chrM = HG38.getContigById(25);
        assertThat(GenomeAssembly.assemblyOfContig(chrM), equalTo(HG19));
    }
}