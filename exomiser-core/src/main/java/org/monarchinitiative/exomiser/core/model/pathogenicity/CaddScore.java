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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

/**
 * CADD info - see {@link http://cadd.gs.washington.edu/info}
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CaddScore extends BasePathogenicityScore {

    // According to https://cadd.gs.washington.edu/info a good cutoff to use is the PHRED scaled scores of
    // 10-20 which equates to 90-99% most deleterious or 13-20 (95-99%). These are scaled to 0.90 - 0.99. The M-CAP authors
    // (http://bejerano.stanford.edu/mcap/) suggest this is too permissive, although their recommended thresholds
    // don't appear to match what was actually suggested.
    public static CaddScore of(float score) {
        return new CaddScore(score);
    }
        
    private CaddScore(float score) {
        super(PathogenicitySource.CADD, score);
    }
    
}
