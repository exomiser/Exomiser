/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhivePriorityResultTest {
    @Test
    public void testIsEquals() throws Exception {
        PhivePriorityResult result = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, "MGI:95523", "Fgfr2");
        PhivePriorityResult other = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, "MGI:95523", "Fgfr2");
        assertThat(result, equalTo(other));
    }

    @Test
    public void testNotEquals() throws Exception {
        PhivePriorityResult result = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, "MGI:95523", "Fgfr3");
        PhivePriorityResult other = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, "MGI:95523", "Fgfr2");
        assertThat(result, not(equalTo(other)));
    }

    @Test
    public void testToString() throws Exception {
        PhivePriorityResult result = new PhivePriorityResult(2263, "FGFR2", 0.827862024307251, "MGI:95523", "Fgfr2");
        assertThat(result.toString(), equalTo("PhivePriorityResult{geneId=2263, geneSymbol='FGFR2', score=0.827862024307251, mgiId='MGI:95523', mgiGeneSymbol='Fgfr2'}"));
    }

}