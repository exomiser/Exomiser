/*
 * Copyright (C) 2014 jj8
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.model;

import de.charite.compbio.exomiser.core.filter.FilterReport;
import java.util.List;

/**
 *
 * @author Jules Jacobsen {jules.jacobsen@sanger.ac.uk}
 */
public class GeneResult {
    private Integer geneId;
    private String geneSymbol;
    
    private float combinedScore;
    private float  filterScore;
    private float phenotypeScore;
    private List<FilterReport> phenotypicAnalysisReports;
    //TODO: maybe....
//                    passedVariants
//                    failedVariants
//                            pathScores - passed ones only
//                                    frequency scores - passed ones only
                                    
    
}
