package de.charite.compbio.exomiser.io.html;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.priority.PriorityScore;
import de.charite.compbio.exomiser.priority.PriorityType;
import jannovar.common.Constants;
import jannovar.pedigree.Pedigree;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class is responsible for creating the tables used by the Exomiser to
 * display the summary, the pedigree data, and to show prioritized variants. The
 * class basically creates an HTML table based on information in the
 * {@link exomizer.exome.Gene Gene} objects and the
 * {@link jannovar.exome.Variant} objects contained in the
 * {@link exomizer.exome.Gene Gene} objects.
 * <P>
 * Note that the appearance of the Table is controlled by CSS code that is
 * contained in the class {@link exomizer.io.html.HTMLWriter HTMLWriter}.
 * <P>
 * The methods of this class use {@code java.io.Writer} instead of
 * {@code java.io.BufferedWriter} because this allows client code to call the
 * methods with either {@code java.io.BufferedWriter} or
 * {@code java.io.StringWriter}
 * <P>
 * Subclasses have been written for the CRE-Server and ExomeWalker
 *
 * @see exomizer.io.html.HTMLTableCRE
 * @see exomizer.io.html.HTMLTableWalker
 * @author Peter N Robinson
 * @version 0.48 (2 February, 2014)
 */
public class HTMLTable {

    /**
     * The Pedigree corresponding to the sample(s) being analysed.
     */
    private Pedigree pedigree = null;

    protected final FilterResultWriter pathResultsWriter = new PathogenicityFilterResultWriter();
    protected final FilterResultWriter frequencyResultWriter = new FrequencyFilterResultWriter();
    protected final FilterResultWriter qualityResultWriter = new QualityFilterResultWriter();

    /**
     * These are the CSS class ids for the two types of table row for the
     * prioritization table. See {@link exomizer.io.html.HTMLWriter HTMLWriter}
     * for the CSS code. Essentially, we alternate between d0 and d1 for genes,
     * showing alternate white and grey rows. Note that this is not used by the
     * ExomeWalker tables, which instead use toggling.
     */
    private String[] rowclass = {"d0", "d1"};
    /**
     * Index of the current rowclass. Initialize to 1.
     */
    private int rowclassIdx = 1;

    /**
     * The constructor
     *
     * @param ped A representation of the pedigree of the family being analysed
     * (or single-sample).
     */
    public HTMLTable(Pedigree ped) {
        this.pedigree = ped;
    }

    /**
     * This function writes out a table representing the PED file of the family
     * being analysed (if a multisample VCF file is being analysed) or the name
     * of the sample (for a single-sample VCF file).
     * <P>
     * For multisample VCF files, a color code is used to mark the following
     * kinds of samples (individuals):
     * <ul>
     * <li>Unaffected parent: white</li>
     * <li>Affected (whether parent or not): dark grey</li>
     * <li>Unaffected sibling: light blue</li>
     * </ul>
     * The same color code will be used for showing the genotypes of the
     * individual variants, which hopefully will help in their interpretation.
     *
     * @param out An open file handle (can come from the command line or server
     * versions of Exomiser).
     */
    public void writePedigreeTable(Writer out) throws IOException {
        int n = this.pedigree.getNumberOfIndividualsInPedigree();
        if (n == 1) {
            String sampleName = this.pedigree.getSingleSampleName();
            out.write("<table class=\"pedigree\">\n");
            out.write(String.format("<tr><td>Sample name: %s</td></tr>\n", sampleName));
            out.write("</table>\n");
        } else { /* multiple samples */

            out.write("<h2>Analyzed samples</h2>\n");
            out.write("<p>affected: red, parent of affected: light blue, unaffected: white</p>\n");
            out.write("<table class=\"pedigree\">\n");
            for (int i = 0; i < n; ++i) {
                List<String> lst = this.pedigree.getPEDFileDatForNthPerson(i);
                String fam = lst.get(0);
                String id = lst.get(1);
                String fathID = lst.get(2);
                String mothID = lst.get(3);
                String sex = lst.get(4);
                String disease = lst.get(5);
                out.write("<tr><td>" + fam + "</td>");
                if (this.pedigree.isNthPersonAffected(i)) {
                    out.write("<td id=\"g\">" + id + "</td>");
                } else if (this.pedigree.isNthPersonParentOfAffected(i)) {
                    out.write("<td id=\"b\">" + id + "</td>");
                } else {
                    out.write("<td id=\"w\">" + id + "</td>");
                }
                out.write("<td>" + fathID + "</td><td>" + mothID + "</td><td>"
                        + sex + "</td><td>" + disease + "</td></tr>\n");
            }
            out.write("</table>\n");
            out.write("<br/>\n");
        }
    }

    /**
     * This function dynamically creates a table header based on the indices
     * passed to the constructor. Note that the zero-th cell in the table is
     * always coming from the {@link jannovar.exome.Variant} object, which
     * contains annotation information that comes directly from the VCF file
     * (gene name, name of mutation on trascript level).
     *
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableHeader(Writer out) throws IOException {
        out.write("<a name=\"Prioritization\">\n"
                + "<h2>Prioritized Variant List</h2>\n"
                + "</a>\n");
        out.write("<table class=\"priority\">\n");
        out.write("<tr>\n");
        out.write("<th style=\"width:50%\">Variant</th>");
        out.write("<th style=\"width:20%\">Variant analysis</th>");
        out.write("<th style=\"width:30%\">Phenotypic analysis</th>");
        out.write("</tr>\n");
    }

    /**
     * Writes a string with HTML code for the Table footer
     *
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableFooter(Writer out) throws IOException {
        out.write("</table>\n");
    }

    /**
     * This method extracts the fields from the current
     * {@link jannovar.exome.Variant} object that have been indicated in the
     * array idx with the corresponding fields.
     * <P>
     * This method keeps track of the current gene and uses alternating colors
     * for rows with variants from different genes. The corresponding CSS code
     * is to be found in the class
     * {@link exomizer.io.html.HTMLWriter HTMLWriter}.
     *
     * @param gen The {@link exomizer.exome.Gene Gene} object that is to be
     * displayed in the current Table row.
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableRow(Gene gene, Writer out) throws IOException {
        String currentCSSclass = this.rowclass[rowclassIdx];
        rowclassIdx = (rowclassIdx + 1) % 2;
        currentCSSclass = this.rowclass[rowclassIdx];

        StringBuilder stringbuilder = new StringBuilder();

        appendGeneScoreRow(stringbuilder, gene, currentCSSclass);

//	int N = gen.getNumberOfVariants();
//	for (int i=1;i<N;++i) {
//	    VariantEvaluation ve = gen.getNthVariant(i);
//	    writeVariant(ve,currentCSSclass, out);
//	}
        List<VariantEvaluation> passedFilters = new ArrayList<>();

        for (VariantEvaluation ve : gene.getVariantList()) {
            if (ve.passesFilters()) {
                passedFilters.add(ve);
            }
        }

        if (passedFilters.size() > 1) {
            VariantEvaluation firstVariant = passedFilters.get(0);
            stringbuilder.append(String.format("<tr class=\"%s\">\n", currentCSSclass));
            appendVariantResultCell(stringbuilder, firstVariant);
            appendPrioritiserResultCell(stringbuilder, gene);
            stringbuilder.append("</tr>\n");

            //then append the rest in a new row each, this way the prioritiser result will span all the rows
            for (int i = 1; i < passedFilters.size(); i++) {
                stringbuilder.append(String.format("<tr class=\"%s\">\n", currentCSSclass));
                appendVariantResultCell(stringbuilder, passedFilters.get(i));
                stringbuilder.append("</tr>\n");

            }
        }
        
        if (passedFilters.size() == 1) {
            stringbuilder.append(String.format("<tr class=\"%s\">\n", currentCSSclass));
            appendVariantResultCell(stringbuilder, passedFilters.get(0));
            appendPrioritiserResultCell(stringbuilder, gene);
            stringbuilder.append("</tr>\n");

        }


        out.write(stringbuilder.toString());
    }

    /**
     * @return a String with all of the affected transcripts, meant for display
     * on html page.
     */
    protected String getTranscriptListForToolTip(VariantEvaluation varev) {
        List<String> lst = varev.getAnnotationListWithAnnotationClass();
        String gsymbol = varev.getGeneSymbol();
        StringBuilder sb = new StringBuilder();
        String chrvar = varev.getChromosomalVariant();
        sb.append("<b>" + chrvar + " (<i>" + gsymbol + "</i>)</b><br/>\n");
        for (String s : lst) {
            sb.append(s + "<br/>\n");
        }
        return sb.toString();
    }

    /**
     * The ExomeWalker results display has one toggle-table for each gene that
     * has survived filtering. This version of the function has a A map of
     * interactions with seed genes. This class does noting with it, but
     * subclasses can display these interactions.
     *
     * @param gen The gene to be displayed
     * @param out File handle to write to
     * @param n A running number for the current gene.
     * @param interactionLst A list of interactions with seed genes.
     */
    public void writeGeneTable(Gene gen, Writer out, int n, List<String> interactionLst) throws IOException {

        writeGeneTable(gen, out, n);

    }

    /**
     * The ExomeWalker results display has one toggle-table for each gene that
     * has survived filtering.
     *
     * @param gen The gene to be displayed
     * @param out File handle to write to
     * @param n A running number for the current gene.
     */
    public void writeGeneTable(Gene gen, Writer out, int n) throws IOException {
        int n_variants = gen.getNumberOfVariants(); /* Number of variants associated with this gene */

        if (n_variants == 0) {
            return; /* this should never happen */

        }
        out.write("<table width=\"1024px\" border=\"0\" align=\"center\" cellpadding=\"2\" cellspacing=\"0\">\n");
        out.write(" <tr bgcolor=\"#4682B4\" height=\"25\">\n");
        out.write("   <td bgcolor=\"#4682B4\" align=\"left\" width=\"5%\">\n");
        String onclick = String.format("<input id=\"lnk%d\" type=\"button\" value=\"[+]\" onclick=\"toggle_visibility('tbl%d','lnk%d');\">",
                n, n, n);
        out.write(onclick + "\n" + "</td>\n");
        String entrez = getEntrezURL(gen);
        String tdWithGeneName = String.format("<td width=\"25%%\"><font size=\"3\" face=\"tahoma\" color=\"#FAFAFA\">\n"
                + "<strong>%d) %s</strong></font></td>", n, entrez);
        out.write(tdWithGeneName); /* This is the first cell (td) of the table with the gene entry */

        double priorityScore = gen.getPriorityScore();
        double filterScore = gen.getFilterScore();
        String combined = String.format("%.3f", gen.getCombinedScore());
        String scoreString = String.format("&nbsp;&nbsp;Gene relevance: <strong>%.3f</strong>&nbsp;&nbsp;"
                + "Variant score <strong>%.3f</strong>&nbsp;&nbsp;",
                priorityScore, filterScore);
        out.write("  <td width=\"45%\"><font size=\"3\" face=\"tahoma\" color=\"#FAFAFA\">" + scoreString + "</td>\n");
        out.write("  <td width=\"25%\"><font size=\"3\" face=\"tahoma\" color=\"#FAFAFA\">Total: <strong>" + combined
                + "    </strong></font></td>\n"
                + "</tr>\n");
        /* At this point, we have just finished printing the first row of the table for some gene.
         This row is always shown. The remaining stuff is toggle-able. */

        String tableID = String.format("tbl%d", n);
        out.write(String.format("<table width=\"1024px\" border=\"0\" align=\"center\" cellpadding=\"2\" cellspacing=\"0\" id=\"%s\">",
                tableID));
        /**
         * Now write the first row. It has a multirow column at the end for the
         * gene priority score.
         */
        out.write("<tr>\n");
        //The following function outputs the left-most table of transcript-based variants etc.
        VariantEvaluation ve = gen.getNthVariant(0); /* Get the first (zero-th) variant associated with this gene */

        outputVariant(ve, out);
        // Write the variant score associated with the first (zero-th) variant
        writeVariantScoreCell(ve, out);
        Map<PriorityType, PriorityScore> relevanceMap = gen.getPriorityScoreMap();
//	PriorityScore phenodigm = relevanceMap.get(PriorityType.PHENODIGM_MGI_PRIORITY);
//	PriorityScore gwanderer = relevanceMap.get(PriorityType.GENEWANDERER_PRIORITY);
//        PriorityScore dpwanderer = relevanceMap.get(PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY);
//	PriorityScore mim = relevanceMap.get(PriorityType.OMIM_PRIORITY);
//	PriorityScore resnik = relevanceMap.get(PriorityType.UBERPHENO_PRIORITY);
//	PriorityScore phmizer = relevanceMap.get(PriorityType.PHENOMIZER_PRIORITY);
        /**
         * Span over all rows with variants for this gene.
         */
        out.write(String.format("<td rowspan=\"%d\">", n_variants));

        for (Entry<PriorityType, PriorityScore> entry : relevanceMap.entrySet()) {
            out.write(entry.getValue().getHTMLCode());
        }

//	if (phenodigm == null && mim == null && gwanderer == null && phmizer==null) {
//	    out.write(".");
//	}
//	if (phmizer != null) 
//	    out.write(phmizer.getHTMLCode());
//	if  (phenodigm != null)
//	    out.write(phenodigm.getHTMLCode());
//	if  (gwanderer != null)
//	    out.write(gwanderer.getHTMLCode());
//        if  (dpwanderer != null)
//	    out.write(dpwanderer.getHTMLCode());
//	if (mim != null)
//	    out.write(mim.getHTMLCode());
//	if (resnik != null)
//	    out.write(resnik.getHTMLCode());
        out.write("</td></tr>\n");
        /* When we get here, we have finished writing the row for the first (zero-th) variant associated
         with this gene. Now we will write the remaining lines. Remember that since the first row has
         as its last column a multi-row column that spans all of the other rows, the remaining rows do
         not need to write information for the last column. */
        for (int i = 1; i < n_variants; ++i) {
            VariantEvaluation vev = gen.getNthVariant(i);
            out.write("<tr>\n");
            outputVariant(vev, out);
            // Each variant now has exactly the same number of Triage objects with the results of filtering.
            writeVariantScoreCell(vev, out);
            out.write("</tr>\n");
        }
        out.write("</table>\n"); /* this ends the toggle table (with id = tblx, where x is an integer) */

        out.write("</tr>\n</table>\n"); /* This ends the overall table that included the toggle table */


    }

    protected StringBuilder appendGeneScoreRow(StringBuilder stringBuilder, Gene gene, String css) {

        String entrez = getEntrezURL(gene);
        double priorityScore = gene.getPriorityScore();
        double filterScore = gene.getFilterScore();
        double combined = gene.getCombinedScore();
        String scoreString = String.format("&nbsp;&nbsp;gene relevance score: %.3f&nbsp;&nbsp;variant score %.3f&nbsp;&nbsp;total score: %.3f",
                priorityScore, filterScore, combined);
        stringBuilder.append(String.format("<tr class=\"%s\"><td colspan=\"4\" style=\"border:solid 2px\">"
                + "<b><i>%s</i></b>&nbsp;&nbsp;%s&nbsp;&nbsp;%s</td></tr>\n",
                css, gene.getGeneSymbol(), entrez, scoreString));

        return stringBuilder;
    }

    /**
     * This function writes an HTML cell for a single variant.
     *
     * @param var The evaluation object for the variant
     * @param stringBuilder
     * @return a stringBuilder containing the variant cell data
     */
    protected StringBuilder appendVariantResultCell(StringBuilder stringBuilder, VariantEvaluation var) {

        appendVariantCell(stringBuilder, var);
        appendVariantScoreCell(stringBuilder, var);

        return stringBuilder;
    }

    /**
     * This function is responsible for creating the HTML elements displayed as
     * the leftmost cell of the prioritization table for an individual variant.
     * It basically creates links to UCSC, EntrezGene, and shows the annotations
     * for each affected transcript.
     * <P>
     * This function writes the entire table cell (td element) for the variant
     *
     * @param stringBuilder
     * @param ve the Variant to be formated.
     * @return
     */
    public StringBuilder appendVariantCell(StringBuilder stringBuilder, VariantEvaluation ve) {

        String ucsc = getUCSCBrowserURL(ve);
        String chromVariant = ve.getChromosomalVariant();

        stringBuilder.append("<td>");
        /* Output the gene symbol */
        stringBuilder.append("<b>" + ve.getGeneSymbol() + "</b></br>\n");
        /* Output the variant (chromosomal notation) */
        stringBuilder.append(chromVariant + "<br/>\n");
        /* Output the genotype */
        List<String> genotypeList = ve.getGenotypeList();
        if (genotypeList.size() == 1) {
            stringBuilder.append(String.format(" [<b>%s</b>] </br>", genotypeList.get(0)));
        } else {
            appendFormatedMultisampleGenotypeList(stringBuilder, genotypeList);
            stringBuilder.append("<br/>\n");
        }
        /* Output the UCSC URL */
        stringBuilder.append(ucsc + "<br/>\n");
        /* OuVariantScorehe various transcript annotations */
        appendMultipleTranscripts(stringBuilder, ve);
        /* Output the variant quality */
        if (ve.passedFilter(FilterType.QUALITY_FILTER)) {
            stringBuilder.append(qualityResultWriter.getHTMLCode(ve) + "\n");
        }
        stringBuilder.append("</td>\n");

        return stringBuilder;

    }

    /**
     * Returns a String of the cell containing the variant score.
     *
     * @param stringBuilder
     * @param variantEvaluation
     * @return
     */
    protected StringBuilder appendVariantScoreCell(StringBuilder stringBuilder, VariantEvaluation variantEvaluation) {

        stringBuilder.append("<td>");
        stringBuilder.append("<i>Pathogenicity:</i>\n");

        if (variantEvaluation.passedFilter(FilterType.PATHOGENICITY_FILTER)) {
            stringBuilder.append(pathResultsWriter.getHTMLCode(variantEvaluation));
        } else {
            stringBuilder.append("<ul><li>n/a</li></ul>\n");
        }
        stringBuilder.append("<i>Frequency</i>\n");
        if (variantEvaluation.passedFilter(FilterType.FREQUENCY_FILTER)) {
            stringBuilder.append(frequencyResultWriter.getHTMLCode(variantEvaluation));
        } else {
            stringBuilder.append("<ul><li>n/a</li></ul>\n");
        }
        stringBuilder.append("</td>\n");

        return stringBuilder;
    }

    protected StringBuilder appendPrioritiserResultCell(StringBuilder stringBuilder, Gene gene) {

        int numPassedVariants = 0;
        for (VariantEvaluation varEval : gene.getVariantList()) {
            if (varEval.passesFilters()) {
                numPassedVariants++;
            }
        }
        if (numPassedVariants == 0) {
            return stringBuilder; /* this should never happen */

        }

        /**
         * Span over all rows with variants for this gene.
         */
        stringBuilder.append(String.format("<td rowspan=\"%d\">", numPassedVariants));
        Map<PriorityType, PriorityScore> relevanceMap = gene.getPriorityScoreMap();

        if (relevanceMap.isEmpty()) {
            stringBuilder.append(".");
        } else {
            for (Entry<PriorityType, PriorityScore> entryMap : relevanceMap.entrySet()) {
                stringBuilder.append(entryMap.getValue().getHTMLCode());
            }
        }

        stringBuilder.append("</td>\n");

        return stringBuilder;
    }

    /**
     * This writes the <b>first</b> row for a given
     * {@link exomizer.exome.Gene Gene}. Note that the last cell of the row will
     * span all of the rows for this gene (using the HTML {@code rowspan}
     * attribute), and the current function is thus used only for the first row
     * for some gene. All of the other variants for a gene are output using the
     * function {@link #writeVariant}.
     */
//    protected void writeVariantAndGeneRow(Gene gen, String css, Writer out) throws IOException {
//        VariantEvaluation ve = gen.getNthVariant(0);
//        //this is a hack as the first variant is shown regardless of whether it passed any filters or not. 
//        //FIX THIS!!! (use Thymeleaf....)
//        int numPassedVariants = 0;
//        for (VariantEvaluation varEval : gen.getVariantList()) {
//            if (varEval.passesFilters()) {
//                numPassedVariants++;
//            }
//        }
//        if (numPassedVariants == 0) {
//            return; /* this should never happen */
//
//        }
//        String entrez = getEntrezURL(gen);
//        double priorityScore = gen.getPriorityScore();
//        double filterScore = gen.getFilterScore();
//        double combined = gen.getCombinedScore();
//        String scoreString = String.format("&nbsp;&nbsp;gene relevance score: %.3f&nbsp;&nbsp;variant score %.3f&nbsp;&nbsp;total score: %.3f",
//                priorityScore, filterScore, combined);
//        out.write(String.format("<tr class=\"%s\"><td colspan=\"4\" style=\"border:solid 2px\">"
//                + "<b><i>%s</i></b>&nbsp;&nbsp;%s&nbsp;&nbsp;%s</td></tr>\n",
//                css, gen.getGeneSymbol(), entrez, scoreString));
//        out.write(String.format("<tr class=\"%s\">\n", css));
//        outputVariant(ve, out);
//
//        // Each variant now has exactly the same number of Triage objects with the results of filtering.
//        writeVariantScoreCell(ve, out);
//
//        Map<PriorityType, PriorityScore> relevanceMap = gen.getPriorityScoreMap();
//
//        PriorityScore phenodigm = relevanceMap.get(PriorityType.PHENODIGM_MGI_PRIORITY);
//        PriorityScore mim = relevanceMap.get(PriorityType.OMIM_PRIORITY);
//        PriorityScore dpwanderer = relevanceMap.get(PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY);
//        /**
//         * Span over all rows with variants for this gene.
//         */
//        out.write(String.format("<td rowspan=\"%d\">", numPassedVariants));
//        if (phenodigm == null && mim == null) {
//            out.write(".");
//        }
//        if (phenodigm != null) {
//            out.write(phenodigm.getHTMLCode());
//        }
//        if (dpwanderer != null) {
//            out.write(dpwanderer.getHTMLCode());
//        }
//        if (mim != null) {
//            out.write(mim.getHTMLCode());
//        }
//        out.write("</td></tr>\n");
//    }
    /**
     * This function writes an HTML row for a single variant that is not the
     * first variant to be show for the current gene (the first variant is
     * written by the function {@link #writeVariantAndGeneRow}).
     *
     * @param var The evaluation object for the variant
     * @param css Either d0 or d1, the CSS classes to make the alterating rows
     * have alternating colors.
     * @param out A file handle to write to
     */
//    protected void writeVariant(VariantEvaluation var, String css, Writer out) throws IOException {
//
//        out.write(String.format("<tr class=\"%s\">\n", css));
//        outputVariant(var, out);
//        writeVariantScoreCell(var, out);
//        out.write("</tr>\n");
//    }
    /**
     * Generate a URL and HTML link for it to display +/- 5 nucleotides around
     * the position of the mutation in the UCSC browser.
     *
     * @param varev The variant to be displayed in the UCSC browser
     */
    public String getUCSCBrowserURL(VariantEvaluation varev) {
        int OFFSET = 5;
        String chrom = varev.getChromosomeAsString();
        int start = varev.getVariantStartPosition() - OFFSET;
        int end = varev.getVariantEndPosition() + OFFSET;
        String symbol = varev.getGeneSymbol();
        String url = String.format("http://genome.ucsc.edu/cgi-bin/hgTracks?org=Human&db=hg19&position=%s:%d-%d",
                chrom, start, end);
        String anchor = String.format("<a href=\"%s\" target=\"_new%s\">View in UCSC Browser</a>", url, symbol);
        return anchor;
    }

    /**
     * Write the entire contents of the cell containing the variant score.
     *
     * @param variantEvaluation
     * @param out handle to the HTML file being written
     * @throws java.io.IOException
     */
    protected void writeVariantScoreCell(VariantEvaluation variantEvaluation, Writer out) throws IOException {
        out.write("<td>");
        out.write("<i>Pathogenicity:</i>\n");

        if (variantEvaluation.passedFilter(FilterType.PATHOGENICITY_FILTER)) {
            out.write(pathResultsWriter.getHTMLCode(variantEvaluation));
        } else {
            out.write("<ul><li>n/a</li></ul>\n");
        }
        out.write("<i>Frequency</i>\n");
        if (variantEvaluation.passedFilter(FilterType.FREQUENCY_FILTER)) {
            out.write(frequencyResultWriter.getHTMLCode(variantEvaluation));
        } else {
            out.write("<ul><li>n/a</li></ul>\n");
        }
        out.write("</td>\n");
    }

    /**
     * This function is responsible for creating the HTML elements displayed as
     * the leftmost cell of the prioritization table for an individual variant.
     * It basically creates links to UCSC, EntrezGene, and shows the annotations
     * for each affected transcript.
     * <P>
     * This function writes the entire table cell (td element) for the variant
     *
     * @param ve the Variant to be formated.
     * @param out Buffer to write the HTML code to.
     */
    public void outputVariant(VariantEvaluation ve, Writer out) throws IOException {
        String ucsc = getUCSCBrowserURL(ve);
        String chromVariant = ve.getChromosomalVariant();

        out.write("<td>");
        /* Output the gene symbol */
        out.write("<b>" + ve.getGeneSymbol() + "</b></br>\n");
        /* Output the variant (chromosomal notation) */
        out.write(chromVariant + "<br/>\n");
        /* Output the genotype */
        List<String> genotypeList = ve.getGenotypeList();
        String gtype = null;
        if (genotypeList.size() == 1) {
            out.write(String.format(" [<b>%s</b>] </br>", genotypeList.get(0)));
        } else {
            outputFormatedMultisampleGenotypeList(genotypeList, out);
            out.write("<br/>\n");
        }
        /* Output the UCSC URL */
        out.write(ucsc + "<br/>\n");
        /* OuVariantScorehe various transcript annotations */
        formatMultipleTranscripts(ve, out);
        /* Output the variant quality */
        if (ve.passedFilter(FilterType.QUALITY_FILTER)) {
            out.write(qualityResultWriter.getHTMLCode(ve) + "\n");
        }
        out.write("</td>\n");
    }

    /**
     * This function formats the multiple-sample genotype list using "div"
     * definitions found in the class
     * {@link exomizer.io.html.HTMLWriter HTMLWriter} such that affecteds,
     * parents of affecteds, and unaffecteds are show with different colors.
     *
     * @param lst List of genotypes for the pedigree represented as strings.
     * @param out File handle to write to.
     */
    protected void outputFormatedMultisampleGenotypeList(List<String> lst, Writer out)
            throws IOException {
        out.write("Genotype: <table class=\"gtype\"><tr>\n");
        for (int i = 0; i < lst.size(); ++i) {
            if (this.pedigree.isNthPersonAffected(i)) {
                out.write("<td id=\"g\">" + lst.get(i) + "</td>");
            } else if (this.pedigree.isNthPersonParentOfAffected(i)) {
                out.write("<td id=\"b\">" + lst.get(i) + "</td>");
            } else {
                out.write("<td id=\"w\">" + lst.get(i) + "</td>");
            }
        }
        out.write("</tr></table>\n");
    }

    /**
     * @param g The {@link exomizer.exome.Gene Gene} object for which we want
     * the NCBI Entrez Gene ID
     * @return An HTML link to the URL at NCBI corresponding to the Entrez Gene
     */
    protected String getEntrezURL(Gene g) {
        int entrezID = g.getEntrezGeneID();
        if (entrezID == Constants.UNINITIALIZED_INT) {
            return g.getGeneSymbol();
        } else {
            String url = String.format("<a href=\"http://www.ncbi.nlm.nih.gov/gene/%d\"><i>%s</i></a>",
                    entrezID, g.getGeneSymbol());
            return url;
        }
    }

    /**
     * This method breaks a string of variant annotations into one-to-a line
     * annotations. This should be refactored such that the annotations
     * themselves make this.
     *
     * @param varev The current variant object.
     * @param out File handle to write HTML page
     */
    protected void formatMultipleTranscripts(VariantEvaluation varev, Writer out) throws IOException {

        /* First thing is to get the position of the variant and its surrounding on the
         chromosome so that we can create a URL for the UCSC browser to show it. */
        int chromPos = varev.getVariantStartPosition();
        String chrom = varev.getChromosomeAsString();
        int x = chromPos - 9;
        int y = chromPos + 10;
        String positionString = positionString = String.format("%s:%d-%d", chrom, x, y);
        List<String> annList = varev.getAnnotationList();
        out.write("<ul>\n");
        for (String s : annList) {
            out.write("<li>");
            /* We expect a string such as "uc010wkp.2|remaining annotation" */
            String B[] = s.split("\\|");
            if (B.length < 2 || B.length > 3) {
                out.write(s);
                continue; /* some error occured, just print out the String and go on */

            }
            String id = B[0];
            String annotation = B[1];
            if (B.length == 2) {
                formatUCSCTranscript(id, annotation, null, positionString, out);
            } else {
                String symbol = B[2];
                formatUCSCTranscript(id, annotation, symbol, positionString, out);
            }
            out.write("</li>\n");
        }
        out.write("</ul>\n");
    }

    /**
     * This method breaks a string of variant annotations into one-to-a line
     * annotations. This should be refactored such that the annotations
     * themselves make this.
     *
     * @param stringBuilder to which data is appended
     * @param varev The current variant object.
     * @return the original stringBuilder containing the new data
     */
    protected StringBuilder appendMultipleTranscripts(StringBuilder stringBuilder, VariantEvaluation varev) {

        /* First thing is to get the position of the variant and its surrounding on the
         chromosome so that we can create a URL for the UCSC browser to show it. */
        int chromPos = varev.getVariantStartPosition();
        String chrom = varev.getChromosomeAsString();
        int x = chromPos - 9;
        int y = chromPos + 10;
        String positionString = positionString = String.format("%s:%d-%d", chrom, x, y);
        List<String> annList = varev.getAnnotationList();
        stringBuilder.append("<ul>\n");
        for (String s : annList) {
            stringBuilder.append("<li>");
            /* We expect a string such as "uc010wkp.2|remaining annotation" */
            String B[] = s.split("\\|");
            if (B.length < 2 || B.length > 3) {
                stringBuilder.append(s);
                continue; /* some error occured, just print out the String and go on */

            }
            String id = B[0];
            String annotation = B[1];
            if (B.length == 2) {
                appendUCSCTranscript(stringBuilder, id, annotation, null, positionString);
            } else {
                String symbol = B[2];
                appendUCSCTranscript(stringBuilder, id, annotation, symbol, positionString);
            }
            stringBuilder.append("</li>\n");
        }
        stringBuilder.append("</ul>\n");

        return stringBuilder;
    }

    /**
     * This function is responsible for writing the line with the annotation for
     * an individual transcript. This function expects to get a gene symbol for
     * variants that affect more than one gene. In this case, the gene symbol is
     * shown in parentheses after the main annotation. For variants that affect
     * only one gene, the function expects that this argument is null (in this
     * case, the gene symbol is not appended to the main annotation).
     *
     * @param id An accession number such as uc010wkp.2
     * @param annotation the corresponding variant annotation
     * @param genesymbol This function expects to get a gene symbol for variants
     * that affect more than one gene
     * @param posString Position range (20 nt) around the mutation. Used to make
     * URL for UCSC browser.
     * @param out file handle to write to
     */
    private void formatUCSCTranscript(String id, String annotation, String genesymbol, String posString, Writer out) throws IOException {
        String url = null;
        if (!id.startsWith("uc")) {
            /* Not a knownGenes id, e.g., uc010wkp.2 */
            url = id; /* just the id, not a real URL */

        } else {
            url = String.format("<a href=\"http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=%s\">%s</a>",
                    posString, id);
        }

        String A[] = annotation.split(":");
        if (A.length == 1) {
            out.write(String.format("%s: %s", url, A[0]));
        } else if (A.length == 2) {
            out.write(String.format("%s: %s (%s)", url, A[0], A[1]));
        } else if (A.length == 3) {
            out.write(String.format("%s: %s (%s; %s)", url, A[1], A[2], A[0]));
        } else { /* there are at least four fields now */

            out.write(String.format("%s: %s (%s; %s; %s)", url, A[1], A[2], A[0], A[4]));
        }
        if (genesymbol != null) {
            out.write(" (" + genesymbol + ")");
        }
    }

    private StringBuilder appendUCSCTranscript(StringBuilder stringbuilder, String id, String annotation, String genesymbol, String posString) {
        String url = null;
        if (!id.startsWith("uc")) {
            /* Not a knownGenes id, e.g., uc010wkp.2 */
            stringbuilder.append(id); /* just the id, not a real URL */

        } else {
            stringbuilder.append(String.format("<a href=\"http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=%s\">%s</a>", posString, id));
        }

        String A[] = annotation.split(":");
        if (A.length == 1) {
            stringbuilder.append(String.format("%s: %s", url, A[0]));
        } else if (A.length == 2) {
            stringbuilder.append(String.format("%s: %s (%s)", url, A[0], A[1]));
        } else if (A.length == 3) {
            stringbuilder.append(String.format("%s: %s (%s; %s)", url, A[1], A[2], A[0]));
        } else { /* there are at least four fields now */

            stringbuilder.append(String.format("%s: %s (%s; %s; %s)", url, A[1], A[2], A[0], A[4]));
        }
        if (genesymbol != null) {
            stringbuilder.append(String.format(" (%s)", genesymbol));
        }
        return stringbuilder;
    }

    /**
     * This function gets a list of Strings representing information about a
     * mutation derived from a {@link  exomizer.filter.Triage} object (passed
     * into this function from {@link exomizer.Exomizer}).
     *
     * @return HTML code for an unordered list representing information about a
     * variant.
     */
    private String format_triage_list(List<String> L) {
        if (L == null) {
            return ".";
        }
        if (L.size() == 1) {
            return L.get(0);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (String s : L) {
            sb.append("<li>" + s + "</li>");
        }
        sb.append("</ul>\n");
        return sb.toString();
    }

    private StringBuilder appendFormatedMultisampleGenotypeList(StringBuilder stringBuilder, List<String> genotypeList) {
        stringBuilder.append("Genotype: <table class=\"gtype\"><tr>\n");
        for (int i = 0; i < genotypeList.size(); ++i) {
            if (this.pedigree.isNthPersonAffected(i)) {
                stringBuilder.append("<td id=\"g\">" + genotypeList.get(i) + "</td>");
            } else if (this.pedigree.isNthPersonParentOfAffected(i)) {
                stringBuilder.append("<td id=\"b\">" + genotypeList.get(i) + "</td>");
            } else {
                stringBuilder.append("<td id=\"w\">" + genotypeList.get(i) + "</td>");
            }
        }
        stringBuilder.append("</tr></table>\n");
        return stringBuilder;

    }

}
