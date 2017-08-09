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

package org.monarchinitiative.exomiser.core.prioritisers;

/**
 * Bayes Similarity in HPO
 *
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @author Max Schubach <max.schubach@bihealth.de>
 * @author Jules Jacobsen
 * @version 0.01 (August, 2017).
 */
public class BOQAPriorityResult extends AbstractPriorityResult {


    public BOQAPriorityResult(int geneId, String geneSymbol, double propability) {
        super(PriorityType.BOQA_PRIORITY, geneId, geneSymbol, propability);
    }

    /**
     */
    @Override
    public String getHTMLCode() {
        return String.format("<dl><dt>BOQA semantic similarity score: %.2f </dt></dl>", this.score);
    }

}
