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

package org.monarchinitiative.exomiser.data.phenotype.processors.model.gene;

import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class GeneModel implements OutputLine {

    private static final Logger logger = LoggerFactory.getLogger(GeneModel.class);

    private final String modelId;
    private final String geneId;
    private final String geneSymbol;
    private final List<String> modelPhenotypeIds;

    public GeneModel(String modelId, String geneId, String geneSymbol, List<String> modelPhenotypeIds) {
        this.modelId = modelId;
        this.geneId = geneId;
        this.geneSymbol = geneSymbol;
        this.modelPhenotypeIds = modelPhenotypeIds;
    }

    public String getModelId() {
        return modelId;
    }

    public String getGeneId() {
        return geneId;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public List<String> getModelPhenotypeIds() {
        return modelPhenotypeIds;
    }

    @Override
    public String toOutputLine() {
        // the max length for the hp/mp/zp_id column is 3000 - check the size before writing out in order to avoid the
        // data import from failing.
        List<String> trimmedPhenotypeIds = allowMaxSize(3000, modelPhenotypeIds);
        return geneId + "|" + geneSymbol + "|" + modelId + "|" + String.join(",", trimmedPhenotypeIds);
    }

    private List<String> allowMaxSize(int maxColumnSize, List<String> modelPhenotypeIds) {
        if (modelPhenotypeIds.isEmpty()) {
            return modelPhenotypeIds;
        }
        // ontology identifiers should all follow the same pattern and length e.g. ZP:0000023, ZP:0000025, HP:0000001
        int identifierLength = modelPhenotypeIds.get(0).length();
        int maxAllowableIdentifiers = maxColumnSize / (identifierLength + 1);
        if (modelPhenotypeIds.size() > maxAllowableIdentifiers) {
            logger.warn("Truncating {}-{} phenotypes from {} to {} in order to fit max column size of {}", modelId, geneSymbol, modelPhenotypeIds.size(), maxAllowableIdentifiers, maxColumnSize);
            return modelPhenotypeIds.stream().limit(maxAllowableIdentifiers).collect(Collectors.toList());
        }
        return modelPhenotypeIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneModel)) return false;
        GeneModel geneModel = (GeneModel) o;
        return modelId.equals(geneModel.modelId) &&
                geneId.equals(geneModel.geneId) &&
                geneSymbol.equals(geneModel.geneSymbol) &&
                modelPhenotypeIds.equals(geneModel.modelPhenotypeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(geneId, geneSymbol, modelId, modelPhenotypeIds);
    }

    @Override
    public String toString() {
        return "GeneModel{" +
                "geneId='" + geneId + '\'' +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", modelId=" + modelId +
                ", modelPhenotypeIds=" + modelPhenotypeIds +
                '}';
    }
}
