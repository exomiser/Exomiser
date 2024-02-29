/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.monarchinitiative.exomiser.core.genome.dao.ClinVarWhiteListReader;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Specialised AlleleIndexer for producing the ClinVar whitelist
 *
 * @deprecated Replaced by the {@link ClinVarWhiteListReader} which will
 * read and filter the entire ClinVar database on the fly.
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Deprecated
public class ClinVarWhiteListFileAlleleIndexer extends AbstractIndexer<Allele> {

    private static final Logger logger = LoggerFactory.getLogger(ClinVarWhiteListFileAlleleIndexer.class);

    private final BufferedWriter bufferedWriter;
    private final Set<Allele> blacklist;
    private final AtomicLong count = new AtomicLong(0);


    public ClinVarWhiteListFileAlleleIndexer(BufferedWriter bufferedWriter, Set<Allele> blacklist) {
        this.bufferedWriter = bufferedWriter;
        this.blacklist = blacklist;
    }

    @Override
    public void write(Allele allele) {
        ClinVarData clinVarData = allele.getClinVarData();
        if (!clinVarData.isSecondaryAssociationRiskFactorOrOther() && isPathOrLikelyPath(clinVarData) && clinVarData.starRating() >= 1 && notOnBlackList(allele)) {
            StringJoiner stringJoiner = new StringJoiner("\t");
            stringJoiner.add(Integer.toString(allele.getChr()));
            stringJoiner.add(Integer.toString(allele.getPos()));
            stringJoiner.add(allele.getRef());
            stringJoiner.add(allele.getAlt());
            stringJoiner.merge(createClinVarInfo(clinVarData));

            try {
                bufferedWriter.write(stringJoiner.toString());
                bufferedWriter.newLine();
                // when writing out to the BlockCompressedOutputStream don't use flush()
                // this is necessary if only writing to an uncompressed test file.
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to write to ClinVar whitelist index file", ex);
            }
            count.incrementAndGet();
        }
    }

    private boolean notOnBlackList(Allele allele) {
        return !blacklist.contains(allele);
    }

    //  CLNREVSTAT
    //  Zero starts - ignore these:
    //   no_assertion_criteria_provided,
    //   no_assertion_provided,
    //   no_interpretation_for_the_single_variant,
    //  One star:
    //   1* criteria_provided,_conflicting_interpretations,
    //   1* criteria_provided,_single_submitter,
    //  Keep These:
    //   2* criteria_provided,_multiple_submitters,_no_conflicts,
    //   3* reviewed_by_expert_panel
    //   4* practice_guideline,

    private boolean isPathOrLikelyPath(ClinVarData clinVarData) {
        return switch (clinVarData.getPrimaryInterpretation()) {
            case PATHOGENIC, PATHOGENIC_OR_LIKELY_PATHOGENIC, LIKELY_PATHOGENIC -> true;
            default -> false;
        };
    }

    private StringJoiner createClinVarInfo(ClinVarData clinVarData) {
        StringJoiner stringJoiner = new StringJoiner(";");
        stringJoiner.add("VARIATIONID=" + clinVarData.getVariationId());
        stringJoiner.add("CLNSIG=" + clinVarData.getPrimaryInterpretation());
        stringJoiner.add("CLNREVSTAT=" + clinVarData.getReviewStatus());
        stringJoiner.add("STARS=" + clinVarData.starRating());
        return stringJoiner;
    }

    @Override
    public long count() {
        return count.get();
    }

    @Override
    public void close() {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close ClinVar whitelist index file", e);
        }
    }
}
