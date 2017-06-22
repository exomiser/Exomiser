package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Component
public class LocalFrequencyDao implements FrequencyDao {

    private final Logger logger = LoggerFactory.getLogger(LocalFrequencyDao.class);

    private final TabixReader tabixReader;

    @Autowired
    public LocalFrequencyDao(TabixReader localFrequencyTabixReader) {
        this.tabixReader = localFrequencyTabixReader;
    }

    @Cacheable(value = "local", key = "#variant.hgvsGenome")
    @Override
    public FrequencyData getFrequencyData(Variant variant) {
        return processResults(variant);
    }

    FrequencyData processResults(Variant variant) {
        String chromosome = variant.getChromosomeName();
        String ref = dotIfEmpty(variant.getRef());
        String alt = dotIfEmpty(variant.getAlt());
        int start = variant.getPosition();

        if (isIndel(ref, alt)) {
            if (alt.equals(".")) {
                start -= 1;
            }
            return getIndelPositionFrequencyData(chromosome, start, ref, alt);
        }

        return getPositionFrequencyData(chromosome, start, ref, alt);
    }

    private boolean isIndel(String ref, String alt) {
        return ref.equals(".") || alt.equals(".");
    }


    private String dotIfEmpty(String allele) {
        if ("-".equals(allele) || allele.isEmpty()) {
            return ".";
        }
        return allele;
    }

    private FrequencyData getPositionFrequencyData(String chromosome, int start, String ref, String alt) {
        //Local frequency file defined as tab-delimited lines in 'VCF-lite' format:
        //chr   pos ref alt freq(%)
        //1 12345   A   T   23.0  (an A->T SNP on chr1 at position 12345 with frequency of 23.0%)
        //1 12345   A   TG   0.01  (an A->TG insertion on chr1 at position 12345 with frequency of 0.01%)
        //note in the usual VCF format these would be on a single line
        //1 12345   AT   G   0.02  (an AT->G deletion on chr1 at position 12345 with frequency of 0.02%)
        //1 12345   T   .   0.03  (an T->. monomorphic site (no alt allele) on chr1 at position 12345 with frequency of 0.03%)
        try {
            TabixReader.Iterator results = tabixReader.query(chromosome + ":" + start + "-" + start);
            String line;
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                String refField = elements[2];
                String altField = elements[3];
                if (refField.equals(ref) && altField.equals(alt)) {
                    return parseLocalFrequency(elements[4]);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read from local frequency tabix file {}", tabixReader.getSource(), e);
        }
        return FrequencyData.empty();
    }


    private FrequencyData getIndelPositionFrequencyData(String chromosome, int start, String ref, String alt) {
        try {
            TabixReader.Iterator results = tabixReader.query(chromosome + ":" + start + "-" + start);
            String line;
            while ((line = results.next()) != null) {
                String[] elements = line.split("\t");
                String refField = dotIfEmpty(elements[2].substring(1));
                String altField = dotIfEmpty(elements[3].substring(1));
                if (refField.equals(ref) && altField.equals(alt)) {
                    return parseLocalFrequency(elements[4]);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read from Indel tabix file {}", tabixReader.getSource(), e);
        }
        return FrequencyData.empty();
    }

    private FrequencyData parseLocalFrequency(String frequencyInPercentField) {
        float value = Float.parseFloat(frequencyInPercentField);
        Frequency localFreq = Frequency.valueOf(value, FrequencySource.LOCAL);
        return FrequencyData.of(null, localFreq);
    }
}
