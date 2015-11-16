/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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


package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.*;
import de.charite.compbio.exomiser.core.model.RegulatoryFeature;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.TopologicalDomain;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class VariantDataServiceImpl implements VariantDataService {

    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceImpl.class);

    @Autowired
    private FrequencyDao frequencyDao;
    @Autowired
    private PathogenicityDao pathogenicityDao;
    @Resource(name = "caddDao")
    private CaddDao caddDao;
    @Resource(name = "remmDao")
    private RemmDao remmDao;
    @Autowired
    private RegulatoryFeatureDao regulatoryFeatureDao;
    @Autowired
    private TadDao tadDao;

    private static final PathogenicityData NO_PATH_DATA = new PathogenicityData();

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        FrequencyData allFrequencyData = frequencyDao.getFrequencyData(variant);
        return frequencyDataFromSpecifiedSources(allFrequencyData, frequencySources);
    }

    protected FrequencyData frequencyDataFromSpecifiedSources(FrequencyData allFrequencyData, Set<FrequencySource> frequencySources) {
        Set<Frequency> wanted = allFrequencyData.getKnownFrequencies().stream()
                .filter(frequency -> frequencySources.contains(frequency.getSource()))
                .collect(toSet());
        return new FrequencyData(allFrequencyData.getRsId(), wanted);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        //OK, this is a bit stupid, but if no sources are defined we're not going to bother checking for data
        if (pathogenicitySources.isEmpty()) {
            return NO_PATH_DATA;
        }
        //TODO: ideally we'd have some sort of compact, high-performance document store for this sort of data rather than several different datasources to query and ship.
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

    protected PathogenicityData pathDataFromSpecifiedDataSources(List<PathogenicityScore> allPathScores, Set<PathogenicitySource> pathogenicitySources) {
        Set<PathogenicityScore> wanted = allPathScores.stream()
                .filter(pathogenicity -> pathogenicitySources.contains(pathogenicity.getSource()))
                .collect(toSet());
        return new PathogenicityData(wanted);
    }

    @Override
    public List<RegulatoryFeature> getRegulatoryFeatures() {
        return regulatoryFeatureDao.getRegulatoryFeatures();
    }

    @Override
    public List<TopologicalDomain> getTopologicallyAssociatedDomains() {
        return tadDao.getAllTads();
    }

}
