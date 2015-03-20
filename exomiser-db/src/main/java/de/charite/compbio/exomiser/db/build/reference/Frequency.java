package de.charite.compbio.exomiser.db.build.reference;

import jannovar.common.Constants;
import java.util.Objects;

/**
 * This class is meant to be used for parsing dbSNP data and ESP data to get
 * information about the population frequency of variants. Objects of this class
 * will be created by the parsers {@link exomizer.io.dbSNP2FrequencyParser dbSNP2FrequencyParser}
 * as well as {@link exomizer.io.ESP2FrequencyParser ESP2FrequencyParser} during
 * parsing, and matching variants will be combined. Finally, objects of this
 * class know how to write themselves as a line of the postgres Dump File. <P>
 * The frequencies (from dbSNP and ESP) are both stored as percentages. Note
 * that the files downloaded from ESP expressed the MAF (minor allele frequency)
 * as a percentage, whereas the files from dbSNP use a proportion. The code in
 * {@link exomizer.io.dbSNP2FrequencyParser dbSNP2FrequencyParser} therefore
 * converts the data in dbSNP to percentages for uniformity's sake. <P> Note
 * that this class implements {@code Comparable} because it is intended to be
 * used as an element of a {@code TreeSet} in the class
 * {@link exomizer.io.dbSNP2FrequencyParser dbSNP2FrequencyParser} in order to
 * sort and search these objects while creating a dump file for postgreSQL.
 *
 * @author Peter Robinson
 * @version 0.08 (29 June, 2013)
 */
public class Frequency implements Comparable<Frequency> {

    /**
     * Byte representation of the chromosome
     */
    private final byte chromosome;
    /**
     * Start position of the variant on the chromosome
     */
    private final int pos;
    /**
     * Sequence (one or more nucleotides) of the reference
     */
    private final String ref;
    /**
     * Sequence (one or more nucleotides) of the alt (variant) sequence
     */
    private final String alt;
    /**
     * Integer representation of the rsId
     */
    private int rsId;
    /**
     * Float representation of dbSNP minor allele frequency (from 1000G phase I
     * via dbSNP), expressed as a percentage
     */
    private float dbSNPmaf;
    /**
     * Float representation of the ESP minor allele frequency for European
     * Americans, expressed as a percentage *
     */
    private float espEA;
    /**
     * Float representation of the ESP minor allele frequency for African
     * Americans, expressed as a percentage *
     */
    private float espAA;
    /**
     * Float representation of the ESP minor allele frequency for all comers,
     * expressed as a percentage *
     */
    private float espAll;
    private float exACAfr;
    private float exACAmr;
    private float exACEas;
    private float exACFin;
    private float exACNfe;
    private float exACOth;
    private float exACSas;

    public Frequency(byte chromosome, int position, String ref, String alt, int rsId) {
        this.chromosome = chromosome;
        this.pos = position;
        this.ref = ref;
        this.alt = alt;
        this.rsId = rsId;
    }

    public byte getChromosome() {
        return chromosome;
    }

    public int getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public int getRsId() {
        return rsId;
    }

    public float getESPFrequencyAA() {
        return espAA;
    }

    public float getESPFrequencyEA() {
        return espEA;
    }

    public float getESPFrequencyAll() {
        return espAll;
    }
    
    public float getExACFrequencyAfr() {
        return exACAfr;
    }
    
    public float getExACFrequencyAmr() {
        return exACAmr;
    }
    
    public float getExACFrequencyEas() {
        return exACEas;
    }
    
    public float getExACFrequencyFin() {
        return exACFin;
    }
    
    public float getExACFrequencyNfe() {
        return exACNfe;
    }
    
    public float getExACFrequencyOth() {
        return exACOth;
    }
    
    public float getExACFrequencySas() {
        return exACSas;
    }
    
    
    public void setRsId(int rsId) {
        this.rsId = rsId;
    }

    /**
     * Sets the frequency (expressed as percent) in dbSNP data of the current
     * variant. Note that client code is expected to transform propoprtions,
     * i.e. value \in [0,1] into percentages, i.e., value \in [0,100] before
     * calling this method.
     *
     * @param maf The minor allele frequency, expressed as a percentage.
     */
    public void setDbSnpGmaf(float maf) {
        this.dbSNPmaf = maf;
    }

    /**
     * Sets the frequency (expressed as percent) in the ESP data of the current
     * variant. Note that the ESP MAF is expressed in percentage in the original
     * files, and thus, the parameter is a value \in [0,100]
     *
     * @param f The minor allele frequency of this variant as found in the ESP
     * data.
     */
    public void setESPFrequencyEA(float f) {
        this.espEA = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ESP
     * data.
     */
    public void setESPFrequencyAA(float f) {
        this.espAA = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ESP
     * data.
     */
    public void setESPFrequencyAll(float f) {
        this.espAll = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ExAC
     * data.
     */
    public void setExACFrequencyAfr(float f) {
        this.exACAfr = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ExAC
     * data.
     */
    public void setExACFrequencyAmr(float f) {
        this.exACAmr = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ExAC
     * data.
     */
    public void setExACFrequencyEas(float f) {
        this.exACEas = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ExAC
     * data.
     */
    public void setExACFrequencyFin(float f) {
        this.exACFin = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ExAC
     * data.
     */
    public void setExACFrequencyNfe(float f) {
        this.exACNfe = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ExAC
     * data.
     */
    public void setExACFrequencyOth(float f) {
        this.exACOth = f;
    }

    /**
     * @param f The minor allele frequency of this variant as found in the ExAC
     * data.
     */
    public void setExACFrequencySas(float f) {
        this.exACSas = f;
    }

    /**
     * This frequency object has four frequency estimations,
     * {@link #dbSNPmaf}, {@link #espEA}, {@link #espAA}, and {@link #espAll}.
     * This method returns the maximum frequency amongst these four values.
     */
    public float getMaximumFrequency() {
        return Math.max(Math.max(this.dbSNPmaf, this.espAll), Math.max(this.espEA, this.espAA));
    }

    /**
     * This method is used to create a single line of the file we will import
     * into the postgreSQL database (table: frequency). The structure of the
     * line is <P> chromosome | position | ref | alt | rsid | dbSNPmaf |
     * ESPmafEA | ESPmafAA | ESPmafAll; <P> Note that the rsId is printed as an
     * integer and that client code will need to add the "rs" and transform it
     * into a String.
     */
    public String getDumpLine() {
        String s = String.format("%d|%d|%s|%s|%d|%f|%f|%f|%f|%f|%f|%f|%f|%f|%f|%f%n", this.chromosome, this.pos, this.ref, this.alt, this.rsId, this.dbSNPmaf, this.espEA,
                this.espAA, this.espAll, this.exACAfr, this.exACAmr, this.exACEas, this.exACFin, this.exACNfe, this.exACOth, this.exACSas);
        return s;
    }

    /**
     * This method is implemented for the {@code Comparable} interface. We sort
     * frequency objects based on <OL> <LI>Chromosome <LI>Position <LI>Reference
     * sequence <LI>Alt sequence </OL>
     */
    @Override
    public int compareTo(Frequency f) {
        if (this.chromosome != f.chromosome) {
            return (this.chromosome - f.chromosome);
        }
        if (this.pos != f.pos) {
            return (this.pos - f.pos);
        }
        if (!this.ref.equals(f.ref)) {
            return this.ref.compareTo(f.ref);
        }
        return this.alt.compareTo(f.alt);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.chromosome;
        hash = 37 * hash + this.pos;
        hash = 37 * hash + Objects.hashCode(this.ref);
        hash = 37 * hash + Objects.hashCode(this.alt);
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
        final Frequency other = (Frequency) obj;
        if (this.chromosome != other.chromosome) {
            return false;
        }
        if (this.pos != other.pos) {
            return false;
        }
        if (!Objects.equals(this.ref, other.ref)) {
            return false;
        }
        return Objects.equals(this.alt, other.alt);
    }

    /**
     * This method checks of the frequency object other ias located at the same
     * position as the current object. The method is intended to help avoid
     * putting duplicate entries into the database.
     */
    public boolean isIdenticalSNP(Frequency other) {
        if (this.chromosome != other.chromosome) {
            return false;
        }
        if (this.pos != other.pos) {
            return false;
        }
        if (!this.ref.equals(other.ref)) {
            return false;
        }
        if (!this.alt.equals(other.alt)) {
            return false;
        }
        return true;
    }

    /**
     * In a few cases, dbSNP contains multiple entries for some given SNP. This
     * method is used to reset an entry to the values of some other entry in
     * these cases (see the method checkVariantForExomalLocationAndOutput from
     * the class dbSNP2FrequencyParser).
     */
    public void resetFrequencyValues(Frequency other) {
        this.dbSNPmaf = other.dbSNPmaf;
        this.espEA = other.espEA;
        this.espAA = other.espAA;
        this.espAll = other.espAll;
        this.rsId = other.rsId;

    }

    @Override
    public String toString() {
        return "Frequency{" + "chromosome=" + chromosome + ", pos=" + pos + ", ref=" + ref + ", alt=" + alt + ", rsID=" + rsId + ", dbSNPmaf=" + dbSNPmaf + ", espEA=" + espEA + ", espAA=" + espAA + ", espAll=" + espAll + '}';
    }
}