/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeAssemblyTest {

    @Test
    public void testDefaultBuild() {
        assertThat(GenomeAssembly.defaultBuild(), equalTo(GenomeAssembly.HG19));
    }

    @Test
    public void testNamesIncludeHg19() {
        assertThat(GenomeAssembly.fromValue("hg19"), equalTo(GenomeAssembly.HG19));
    }

    @Test
    public void testNamesIncludeHg37() {
        assertThat(GenomeAssembly.fromValue("hg37"), equalTo(GenomeAssembly.HG19));
    }

    @Test
    public void testNamesIncludeHg38() {
        assertThat(GenomeAssembly.fromValue("hg38"), equalTo(GenomeAssembly.HG38));
    }

    @Test
    public void testCanUseGrchNomenclature() {
        assertThat(GenomeAssembly.fromValue("GRCh37"), equalTo(GenomeAssembly.HG19));
        assertThat(GenomeAssembly.fromValue("GRCh38"), equalTo(GenomeAssembly.HG38));
    }

    @Test
    public void testEmptyNameThrowsException() {
        assertThrows(GenomeAssembly.InvalidGenomeAssemblyException.class, () -> GenomeAssembly.fromValue(""));
    }

    @Test
    public void testNullNameThrowsException() {
        assertThrows(NullPointerException.class, () -> GenomeAssembly.fromValue(null));
    }

    @Test
    public void testUnrecognisedNameThrowsException() {
        assertThrows(GenomeAssembly.InvalidGenomeAssemblyException.class, () -> GenomeAssembly.fromValue("unrecognised build number"));
    }

    @Test
    public void testToString() {
        assertThat(GenomeAssembly.HG19.toString(), equalTo("hg19"));
        assertThat(GenomeAssembly.HG38.toString(), equalTo("hg38"));
    }

    @Test
    void testGetReferenceIdUnknown() {
        assertThat(GenomeAssembly.HG19.getReferenceAccession(-1), equalTo("UNKNOWN"));
        assertThat(GenomeAssembly.HG38.getReferenceAccession(-1), equalTo("UNKNOWN"));

        assertThat(GenomeAssembly.HG19.getReferenceAccession(26), equalTo("UNKNOWN"));
        assertThat(GenomeAssembly.HG38.getReferenceAccession(26), equalTo("UNKNOWN"));
    }

    @Test
    void testGetReferenceIdUnplaced() {
        assertThat(GenomeAssembly.HG19.getReferenceAccession(0), equalTo("UNKNOWN"));
        assertThat(GenomeAssembly.HG38.getReferenceAccession(0), equalTo("UNKNOWN"));
    }

    @Test
    void testGetReferenceIdChr1() {
        assertThat(GenomeAssembly.HG19.getReferenceAccession(1), equalTo("NC_000001.10"));
        assertThat(GenomeAssembly.HG38.getReferenceAccession(1), equalTo("NC_000001.11"));
    }

    @Test
    void testGetReferenceIdChrX() {
        assertThat(GenomeAssembly.HG19.getReferenceAccession(23), equalTo("NC_000023.10"));
        assertThat(GenomeAssembly.HG38.getReferenceAccession(23), equalTo("NC_000023.11"));
    }

    @Test
    void testGetReferenceIdChrM() {
        assertThat(GenomeAssembly.HG19.getReferenceAccession(25), equalTo("NC_012920.1"));
        assertThat(GenomeAssembly.HG38.getReferenceAccession(25), equalTo("NC_012920.1"));
    }

    @Test
    void testGetChromosomeId() {
        assertThat(GenomeAssembly.Hg19.CHR_1.getId(), equalTo(1));
        assertThat(GenomeAssembly.Hg38.CHR_1.getId(), equalTo(1));

        assertThat(GenomeAssembly.Hg19.CHR_Y.getId(), equalTo(24));
        assertThat(GenomeAssembly.Hg38.CHR_Y.getId(), equalTo(24));
    }

    @Test
    void testGetChromosomeName() {
        assertThat(GenomeAssembly.Hg19.CHR_1.getName(), equalTo("1"));
        assertThat(GenomeAssembly.Hg38.CHR_1.getName(), equalTo("1"));

        assertThat(GenomeAssembly.Hg19.CHR_Y.getName(), equalTo("Y"));
        assertThat(GenomeAssembly.Hg38.CHR_Y.getName(), equalTo("Y"));
    }

    @Test
    void testGetChromosomeAccession() {
        assertThat(GenomeAssembly.Hg19.CHR_1.getAccession(), equalTo("NC_000001.10"));
        assertThat(GenomeAssembly.Hg38.CHR_1.getAccession(), equalTo("NC_000001.11"));

        assertThat(GenomeAssembly.Hg19.CHR_Y.getAccession(), equalTo("NC_000024.9"));
        assertThat(GenomeAssembly.Hg38.CHR_Y.getAccession(), equalTo("NC_000024.10"));
    }
}