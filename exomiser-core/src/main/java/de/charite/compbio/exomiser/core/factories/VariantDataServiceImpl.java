/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.CaddDao;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.NcdsDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.dao.RegulatoryFeatureDao;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    @Autowired
    private CaddDao caddDao;
    @Lazy
    @Autowired
    private NcdsDao ncdsDao;
    @Autowired
    private RegulatoryFeatureDao regulatoryFeatureDao;

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        FrequencyData allFrequencyData = frequencyDao.getFrequencyData(variant);
        return frequencyDataWithSpecifiedFrequencies(allFrequencyData, frequencySources);
    }

    protected FrequencyData frequencyDataWithSpecifiedFrequencies(FrequencyData allFrequencyData, Set<FrequencySource> frequencySources) {
        Set<Frequency> wanted = allFrequencyData.getKnownFrequencies().stream()
                .filter(frequency -> frequencySources.contains(frequency.getSource()))
                .collect(toSet());
        return new FrequencyData(allFrequencyData.getRsId(), wanted);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        return pathogenicityDao.getPathogenicityData(variant);
    }
    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        if (variant.getVariantEffect() == VariantEffect.INTERGENIC_VARIANT || variant.getVariantEffect() == VariantEffect.UPSTREAM_GENE_VARIANT) {
            return regulatoryFeatureDao.getRegulatoryFeatureData(variant);
        }
        return variant.getVariantEffect();
    }

//    @Override
//    public PathogenicityData getVariantCaddData(Variant variant) {
//        return caddDao.getPathogenicityData(variant);
//    }
//
//    @Override
//    public PathogenicityData getVariantNcdsData(Variant variant) {
//        return ncdsDao.getPathogenicityData(variant);
//    }

}
