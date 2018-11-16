/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.genome.dao.AllelePropertiesDao;
import org.monarchinitiative.exomiser.core.genome.dao.FrequencyDao;
import org.monarchinitiative.exomiser.core.genome.dao.PathogenicityDao;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of the VariantDataService. This is a
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantDataServiceImpl implements VariantDataService {

    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImpl.class);

    private final AllelePropertiesDao allelePropertiesDao;

    private final FrequencyDao localFrequencyDao;

    private final PathogenicityDao caddDao;
    private final PathogenicityDao remmDao;
    private final PathogenicityDao testPathScoreDao;

    private VariantDataServiceImpl(Builder builder) {
        this.allelePropertiesDao = builder.allelePropertiesDao;
        this.localFrequencyDao = builder.localFrequencyDao;

        this.caddDao = builder.caddDao;
        this.remmDao = builder.remmDao;
        this.testPathScoreDao = builder.testPathScoreDao;
    }

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        List<Frequency> allFrequencies = new ArrayList<>();

        FrequencyData defaultFrequencyData = allelePropertiesDao.getFrequencyData(variant);
        List<Frequency> defaultFrequencies = defaultFrequencyData.getKnownFrequencies();
        for (Frequency frequency : defaultFrequencies) {
            if (frequencySources.contains(frequency.getSource())) {
                allFrequencies.add(frequency);
            }
        }

        if (frequencySources.contains(FrequencySource.LOCAL)) {
            FrequencyData localFrequencyData = localFrequencyDao.getFrequencyData(variant);
            allFrequencies.addAll(localFrequencyData.getKnownFrequencies());
        }

        return FrequencyData.of(defaultFrequencyData.getRsId(), allFrequencies);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        //OK, this is a bit stupid, but if no sources are defined we're not going to bother checking for data
        if (pathogenicitySources.isEmpty()) {
            return PathogenicityData.empty();
        }

        List<PathogenicityDao> daosToQuery = new ArrayList<>();
        // Prior to version 10.1.0 this would only look-up MISSENSE variants, but this would miss out scores for stop/start
        // gain/loss an other possible SNV scores from the bundled pathogenicity databases as well as any ClinVar annotations.
        // This could be run alongside the frequencies as they are all stored in the same datastore
        VariantEffect variantEffect = variant.getVariantEffect();
        // we're going to deliberately ignore synonymous variants from dbNSFP as these shouldn't be there
        // e.g. ?assembly=hg37&chr=1&start=158581087&ref=G&alt=A has a MutationTaster score of 1
        if (variantEffect != VariantEffect.SYNONYMOUS_VARIANT && variant.isCodingVariant()) {
            daosToQuery.add(allelePropertiesDao);
        }
        else if (pathogenicitySources.contains(PathogenicitySource.REMM) && variant.isNonCodingVariant()) {
            //REMM is trained on non-coding regulatory bits of the genome, this outperforms CADD for non-coding variants
            // We're never going to find any ClinVar data like this, but the data will be available when the frequency
            // data was retrieved with the AllelePropertiesDao
            daosToQuery.add(remmDao);
        }
        
        //CADD does all of it although is not as good as REMM for the non-coding regions.
        if (pathogenicitySources.contains(PathogenicitySource.CADD)) {
            daosToQuery.add(caddDao);
        }

        if (pathogenicitySources.contains(PathogenicitySource.TEST)) {
            daosToQuery.add(testPathScoreDao);
        }

        List<PathogenicityData> pathData = daosToQuery.parallelStream()
                .map(pathDao -> pathDao.getPathogenicityData(variant))
                .collect(Collectors.toList());

        return collectPathogenicityData(pathogenicitySources, pathData);
    }

    private PathogenicityData collectPathogenicityData(Set<PathogenicitySource> pathogenicitySources, List<PathogenicityData> pathData) {
        List<PathogenicityScore> allPathScores = new ArrayList<>();
        ClinVarData clinVarData = ClinVarData.empty();

        for (PathogenicityData pathogenicityData : pathData) {
            if (pathogenicityData.hasClinVarData()) {
                // will only happen with the default
                clinVarData = pathogenicityData.getClinVarData();
            }
            for (PathogenicityScore score : pathogenicityData.getPredictedPathogenicityScores()) {
                if (pathogenicitySources.contains(score.getSource())) {
                    allPathScores.add(score);
                }
            }
        }

        return PathogenicityData.of(clinVarData, allPathScores);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private AllelePropertiesDao allelePropertiesDao;

        private FrequencyDao localFrequencyDao;

        private PathogenicityDao caddDao;
        private PathogenicityDao remmDao;
        private PathogenicityDao testPathScoreDao;

        public Builder allelePropertiesDao(AllelePropertiesDao allelePropertiesDao) {
            this.allelePropertiesDao = allelePropertiesDao;
            return this;
        }

        public Builder localFrequencyDao(FrequencyDao localFrequencyDao) {
            this.localFrequencyDao = localFrequencyDao;
            return this;
        }

        public Builder caddDao(PathogenicityDao caddDao) {
            this.caddDao = caddDao;
            return this;
        }

        public Builder remmDao(PathogenicityDao remmDao) {
            this.remmDao = remmDao;
            return this;
        }

        public Builder testPathScoreDao(PathogenicityDao testPathScoreDao) {
            this.testPathScoreDao = testPathScoreDao;
            return this;
        }

        public VariantDataServiceImpl build() {
            return new VariantDataServiceImpl(this);
        }
    }

}
