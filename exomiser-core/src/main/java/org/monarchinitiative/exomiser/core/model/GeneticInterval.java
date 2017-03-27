/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * A simple genetic interval defined as "The spatial continuous physical entity
 * which contains ordered genomic sets(DNA, RNA, Allele, Marker,etc.) between
 * and including two points (Nucleic Acid Base Residue) that have a liner
 * primary sequence structure. It is either a proper part of an chromosome or a
 * RNA molecule or an artificial genetic interval."
 *
 * @author Jules Jacobsen
 */
public class GeneticInterval implements ChromosomalRegion {

    private static final Logger logger = LoggerFactory.getLogger(GeneticInterval.class);

    private final int chromosome;
    private final int start;
    private final int end;

    public GeneticInterval(int chromosome, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException(String.format("Start %d position defined as occurring after end position %d. Please check your positions", start, end));
        }
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns a new GeneticInterval from the parsed string. Strings are to be
     * of the format: <li>chr1:123-456 chrY:1234-1220 chr19:345-567</li>
     *
     * @param refDict
     * @param interval
     * @return
     */
    public static GeneticInterval parseString(ReferenceDictionary refDict, String interval) {

        String intervalPattern = "chr(1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|X|Y|M):[0-9]+-[0-9]+";
        if (!Pattern.matches(intervalPattern, interval)) {
            throw new IllegalArgumentException(String.format("Genetic interval %s does not match expected pattern %s", interval, intervalPattern));
        }

        String[] intervalSections = interval.split(":");
        int localChr = refDict.getContigNameToID().get(intervalSections[0]);

        String positions = intervalSections[1];
        String[] startEnd = positions.split("-");

        int localStart = Integer.parseInt(startEnd[0]);
        int localEnd = Integer.parseInt(startEnd[1]);

        return new GeneticInterval(localChr, localStart, localEnd);
    }

    @Override
    public int getChromosome() {
        return chromosome;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.chromosome;
        hash = 97 * hash + this.start;
        hash = 97 * hash + this.end;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeneticInterval other = (GeneticInterval) obj;
        if (this.chromosome != other.chromosome) {
            return false;
        }
        if (this.start != other.start) {
            return false;
        }
        return this.end == other.end;
    }

    @Override
    public String toString() {
        return String.format("chr%d:%d-%d", chromosome, start, end);
    }
}
