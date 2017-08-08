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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.db.reference;

/**
 * Class representing the chrom/ref/position/alt and calculated pathogenicity 
 * scores.
 *
 * @author Jules Jacobsen <jules.jacoobsen@sanger.ac.uk>
 */
public class VariantPathogenicity {

    //TODO: this class needs refactoring to split out the variant coordinates 
    //from the pathogenicity scores.
    private final int chromosome;
    private final int position;
    private final String ref;
    private final String alt;

    private final Float siftScore;
    private final Float polyphenScore;
    private final Float muttasterScore;
    private final Float caddRawRankScore;
    private final Float caddRawScore;

    /**
     * This class encapsulates the information in a single dbNSFP line. In some
     * cases, there are multiple lines for the same chromosomal position. We
     * choose the line that is predicted to be most pathogenic by using the
     * function {@link #maxPathogenicity maxPathogenicity()} of this class. The
     * function {@link #toDumpLine toDumpLine()} can then be called to get the
     * corresponding SQL dump line.
     *
     * @param chromosome An integer representation of the chromosome (23:X,
     * 24:Y, 25:M)
     * @param position Position of the variant on the chromosome
     * @param ref reference nucleotide
     * @param alt variant (alternate) nucleotide
     * @param siftScore SIFT score for the variant
     * @param polyphen2HVAR Polyphen2 score for the variant
     * @param muttasterScore Mutation Taster score for the variant
     * @param phyloP phyloP conservation score for the variant
     * @param caddRawRankScore
     * @param caddRawScore
     */
    public VariantPathogenicity(int chromosome, int position, String ref, String alt,
                                Float siftScore, Float polyphen2HVAR, Float muttasterScore,
                                Float caddRawRankScore, Float caddRawScore) {
        this.chromosome = chromosome;
        this.position = position;
        this.ref = ref;
        this.alt = alt;
        this.siftScore = siftScore;
        this.polyphenScore = polyphen2HVAR;
        this.muttasterScore = muttasterScore;
        this.caddRawRankScore = caddRawRankScore;
        this.caddRawScore = caddRawScore;
    }

    /**
     * This returns a line that will form part of the import file for
     * postgreSQL.
     *
     * @return
     */
    public String toDumpLine() {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s%n",
                chromosome, position, ref, alt,
                siftScore, polyphenScore, muttasterScore, caddRawRankScore, caddRawScore);
    }

    @Override
    public String toString() {
        return "VariantPathogenicity{" + "chromosome=" + chromosome +
                ", position=" + position + ", ref=" + ref + ", alt=" + alt +
                ", sift=" + siftScore + ", polyphen=" + polyphenScore +
                ", muttaster=" + muttasterScore + ", caddRawRank=" + caddRawRankScore + ", caddRaw=" + caddRawScore + '}';
    }
}
