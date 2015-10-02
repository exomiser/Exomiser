/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.*;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.dao.RemmDao;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.ArrayList;
import java.util.EnumSet;
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
        else if (pathogenicitySources.contains(PathogenicitySource.REMM) && isRegulatoryNonCodingVariant(variantEffect)) {
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
    
    private static final Set<VariantEffect> nonRegulatoryNonCodingVariantEffects = EnumSet.of(VariantEffect.STOP_LOST, VariantEffect.STOP_RETAINED_VARIANT,
            VariantEffect.STOP_GAINED, VariantEffect.START_LOST, VariantEffect.SYNONYMOUS_VARIANT, VariantEffect.SPLICE_REGION_VARIANT, VariantEffect.SPLICE_ACCEPTOR_VARIANT,
            VariantEffect.SPLICE_DONOR_VARIANT, VariantEffect.FRAMESHIFT_ELONGATION, VariantEffect.FRAMESHIFT_TRUNCATION, VariantEffect.FRAMESHIFT_VARIANT, VariantEffect.MNV,
            VariantEffect.FEATURE_TRUNCATION, VariantEffect.DISRUPTIVE_INFRAME_DELETION, VariantEffect.DISRUPTIVE_INFRAME_INSERTION, VariantEffect.INFRAME_DELETION, VariantEffect.INFRAME_INSERTION,
            VariantEffect.INTERNAL_FEATURE_ELONGATION, VariantEffect.COMPLEX_SUBSTITUTION);
            
    private boolean isRegulatoryNonCodingVariant(VariantEffect variantEffect) {
        return !nonRegulatoryNonCodingVariantEffects.contains(variantEffect);
    }
    
    @Override
    public VariantEffect getVariantRegulatoryFeatureData(Variant variant) {
        if (variant.getVariantEffect() == VariantEffect.INTERGENIC_VARIANT || variant.getVariantEffect() == VariantEffect.UPSTREAM_GENE_VARIANT) {
            return regulatoryFeatureDao.getRegulatoryFeatureData(variant);
        }
        return variant.getVariantEffect();
    }

}
