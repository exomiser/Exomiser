package de.charite.compbio.exomiser.core.filters;


/**
 * Filter Variants on the basis of Thousand Genomes and 5000 Exome project data.
 * The FrequencyFilterResult is created by the FrequencyFilter, one for each tested
 variant. The FrequencyFilterResult object can be used to ask whether the variant
 passes the filter, in this case whether it is rarer than then threshold in
 both the Thousand Genomes data and the Exome Server Project data. If no
 information is available for either of these, the filter is not applied (ergo
 the Variant does not fail the filter).
 <P>
 * Note that the frequency data for Variants is expressed in percentage (not
 * proportion).
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.05 (9 January,2013).
 */
public class FrequencyFilterResult extends GenericFilterResult {

    private static final FilterType FILTER_TYPE = FilterType.FREQUENCY_FILTER;
    
    public FrequencyFilterResult(float score, FilterResultStatus resultStatus) {
        super(FILTER_TYPE, score, resultStatus);
    }

    /**
     * This method returns a numerical value that is closer to one, the rarer
     * the variant is. If a variant is not entered in any of the four data
     * sources, it returns one (highest score). Otherwise, it identifies the
     * maximum MAF in any of the databases, and returns a score that depends on
     * the MAF. Note that the frequency is expressed as a percentage.
     *
     * @return return a float representation of the filter result [0..1]. If the
     * result is boolean, return 0.0 for false and 1.0 for true
     */
    @Override
    public float getScore() {
        return super.getScore();
    }   
}
