/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeBuildTest {

    @Test
    public void testDefaultBuild() {
        assertThat(GenomeBuild.defaultBuild(), equalTo(GenomeBuild.HG19));
    }

    @Test
    public void testNamesIncludeHg19() {
        assertThat(GenomeBuild.fromValue("hg19"), equalTo(GenomeBuild.HG19));
    }

    @Test
    public void testNamesIncludeHg37() {
        assertThat(GenomeBuild.fromValue("hg37"), equalTo(GenomeBuild.HG19));
    }

    @Test
    public void testNamesIncludeHg38() {
        assertThat(GenomeBuild.fromValue("hg38"), equalTo(GenomeBuild.HG38));
    }

    @Test
    public void testCanUseGrchNomenclature() {
        assertThat(GenomeBuild.fromValue("GRCh37"), equalTo(GenomeBuild.HG19));
        assertThat(GenomeBuild.fromValue("GRCh38"), equalTo(GenomeBuild.HG38));
    }

    @Test(expected = GenomeBuild.InvalidGenomeAssemblyException.class)
    public void testEmptyNameThrowsException() throws Exception {
        GenomeBuild.fromValue("");
    }

    @Test(expected = NullPointerException.class)
    public void testNullNameThrowsException() throws Exception {
        GenomeBuild.fromValue(null);
    }

    @Test(expected = GenomeBuild.InvalidGenomeAssemblyException.class)
    public void testUnrecognisedNameThrowsException() throws Exception {
        GenomeBuild.fromValue("unrecognised build number");
    }

    @Test
    public void testToString() {
        assertThat(GenomeBuild.HG19.toString(), equalTo("hg19"));
        assertThat(GenomeBuild.HG38.toString(), equalTo("hg38"));
    }
}