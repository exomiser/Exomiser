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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.genome.dao.CaddDao;
import org.monarchinitiative.exomiser.core.genome.dao.FrequencyDao;
import org.monarchinitiative.exomiser.core.genome.dao.PathogenicityDao;
import org.monarchinitiative.exomiser.core.genome.dao.RemmDao;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Default implementation of the VariantDataService. This is a
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantDataServiceImpl implements VariantDataService {

    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImpl.class);

    private FrequencyDao defaultFrequencyDao;
    private FrequencyDao localFrequencyDao;

    private PathogenicityDao pathogenicityDao;
    private CaddDao caddDao;
    private RemmDao remmDao;

    private VariantDataServiceImpl(Builder builder) {
        this.defaultFrequencyDao = builder.defaultFrequencyDao;
        this.localFrequencyDao = builder.localFrequencyDao;

        this.pathogenicityDao = builder.pathogenicityDao;
        this.caddDao = builder.caddDao;
        this.remmDao = builder.remmDao;
    }

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        List<Frequency> allFrequencies = new ArrayList<>();
        FrequencyData allFrequencyData = defaultFrequencyDao.getFrequencyData(variant);
        allFrequencies.addAll(allFrequencyData.getKnownFrequencies());

        if (frequencySources.contains(FrequencySource.LOCAL)) {
            FrequencyData localFrequencyData = localFrequencyDao.getFrequencyData(variant);
            allFrequencies.addAll(localFrequencyData.getKnownFrequencies());
        }

        return frequencyDataFromSpecifiedSources(allFrequencyData.getRsId(), allFrequencies, frequencySources);
    }

    protected static FrequencyData frequencyDataFromSpecifiedSources(RsId rsid, List<Frequency> allFrequencies, Set<FrequencySource> frequencySources) {
        Set<Frequency> wanted = allFrequencies.stream()
                .filter(frequency -> frequencySources.contains(frequency.getSource()))
                .collect(toSet());
        if (rsid.isEmpty() && wanted.isEmpty()) {
            return FrequencyData.empty();
        }
        return FrequencyData.of(rsid, wanted);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        //OK, this is a bit stupid, but if no sources are defined we're not going to bother checking for data
        if (pathogenicitySources.isEmpty()) {
            return PathogenicityData.empty();
        }

        List<PathogenicityScore> allPathScores = new ArrayList<>();
        final VariantEffect variantEffect = variant.getVariantEffect();
        //Polyphen, Mutation Taster and SIFT are all trained on missense variants - this is what is contained in the original variant table, but we shouldn't know that.
        if (variantEffect == VariantEffect.MISSENSE_VARIANT) {
            PathogenicityData missenseScores = pathogenicityDao.getPathogenicityData(variant);
            allPathScores.addAll(missenseScores.getPredictedPathogenicityScores());
        }
        else if (pathogenicitySources.contains(PathogenicitySource.REMM) && variant.isNonCodingVariant()) {
            //REMM is trained on non-coding regulatory bits of the genome, this outperforms CADD for non-coding variants
            PathogenicityData nonCodingScore = remmDao.getPathogenicityData(variant);
            allPathScores.addAll(nonCodingScore.getPredictedPathogenicityScores());
        }
        
        //CADD does all of it although is not as good as REMM for the non-coding regions.
        if (pathogenicitySources.contains(PathogenicitySource.CADD)) {
            PathogenicityData caddScore = caddDao.getPathogenicityData(variant);
            allPathScores.addAll(caddScore.getPredictedPathogenicityScores());
        }

        return pathDataFromSpecifiedDataSources(allPathScores, pathogenicitySources);
    }

    protected static PathogenicityData pathDataFromSpecifiedDataSources(List<PathogenicityScore> allPathScores, Set<PathogenicitySource> pathogenicitySources) {
        Set<PathogenicityScore> wanted = allPathScores.stream()
                .filter(pathogenicity -> pathogenicitySources.contains(pathogenicity.getSource()))
                .collect(toSet());
        if (wanted.isEmpty()) {
            return PathogenicityData.empty();
        }
        return PathogenicityData.of(wanted);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private FrequencyDao defaultFrequencyDao;
        private FrequencyDao localFrequencyDao;

        private PathogenicityDao pathogenicityDao;
        private CaddDao caddDao;
        private RemmDao remmDao;

        public Builder defaultFrequencyDao(FrequencyDao defaultFrequencyDao) {
            this.defaultFrequencyDao = defaultFrequencyDao;
            return this;
        }

        public Builder localFrequencyDao(FrequencyDao localFrequencyDao) {
            this.localFrequencyDao = localFrequencyDao;
            return this;
        }

        public Builder pathogenicityDao(PathogenicityDao pathogenicityDao) {
            this.pathogenicityDao = pathogenicityDao;
            return this;
        }

        public Builder caddDao(CaddDao caddDao) {
            this.caddDao = caddDao;
            return this;
        }

        public Builder remmDao(RemmDao remmDao) {
            this.remmDao = remmDao;
            return this;
        }

        public VariantDataServiceImpl build() {
            return new VariantDataServiceImpl(this);
        }
    }

}
