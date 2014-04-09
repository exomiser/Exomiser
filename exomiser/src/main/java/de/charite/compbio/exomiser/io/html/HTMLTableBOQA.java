package de.charite.compbio.exomiser.io.html;

import java.io.Writer;
import java.io.IOException; 
import java.util.Map;

import jannovar.pedigree.Pedigree;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.ITriage;
import de.charite.compbio.exomiser.priority.IRelevanceScore;



/**
 * This class is responsible for creating the tables used by the 
 * Exomiser to display the summary, the pedigree data, and to show prioritized variants.
 * The class basically creates an HTML table based on information in the 
 * {@link exomizer.exome.Gene Gene} objects and the {@link jannovar.exome.Variant} objects
 * contained in the  {@link exomizer.exome.Gene Gene} objects.
 * <P> r
 * Note that the appearance of the Table is controlled by CSS code that is contained in the 
 * class {@link exomizer.io.html.HTMLWriter HTMLWriter}.
 * <P>
 * The methods of this class use {@code java.io.Writer} instead of {@code java.io.BufferedWriter} because
 * this allows client code to call the methods with either {@code java.io.BufferedWriter} or
 * {@code java.io.StringWriter}
 * <P>
 * Methods have been added to write "toggle" tables for the ExomeWalker server. They rely on java script
 * code to do the toggling. The Java script gets written by methods in the class 
 * {@link exomizer.io.html.HTMLWriter HTMLWriter}.
 * @author Peter N Robinson
 * @version 0.41 (18 November, 2013)
 */
public class HTMLTableBOQA extends HTMLTable {

    /**
     * The constructor merely passes the
     * {@link jannovar.pedigree.Pedigree Pedigree} object to the superclass.
     * @param ped A representation of the pedigree of the family being analysed (or single-sample).
     */
    public HTMLTableBOQA(Pedigree ped) {
	super(ped);
	System.out.println("HTMLTableBOQA CTOR");
    }


     /**
     * The ExomeWalker results display has one toggle-table for each gene that
     * has survived filtering.
     * @param gen The gene to be displayed
     * @param out File handle to write to
     * @param n A running number for the current gene.
     */
    @Override public void writeGeneTable(Gene gen, Writer out, int n) throws IOException {
	int n_variants = gen.getNumberOfVariants(); /* Number of variants associated with this gene */
	if (n_variants == 0) { 
	    return; /* this should never happen */ 
	}
	out.write("<table width=\"1024px\" border=\"0\" align=\"center\" cellpadding=\"2\" cellspacing=\"0\">\n");
	out.write(" <tr bgcolor=\"#4682B4\" height=\"25\">\n");
	out.write("   <td bgcolor=\"#4682B4\" align=\"left\" width=\"5%\">\n");
	String onclick = String.format("<input id=\"lnk%d\" type=\"button\" value=\"[+]\" onclick=\"toggle_visibility('tbl%d','lnk%d');\">",
				       n,n,n);
	out.write( onclick + "\n"+  "</td>\n");
	String entrez = getEntrezURL(gen);
	String tdWithGeneName = String.format("<td width=\"25%%\"><font size=\"3\" face=\"tahoma\" color=\"#FAFAFA\">\n"+
					      "<strong>%d) %s</strong></font></td>", n,entrez);
	out.write(tdWithGeneName); /* This is the first cell (td) of the table with the gene entry */

	double priorityScore = gen.getPriorityScore();
	double filterScore = gen.getFilterScore();
	String combined = String.format("%.3f",gen.getCombinedScore());
	String scoreString = String.format("&nbsp;&nbsp;Gene relevance: <strong>%.3f</strong>&nbsp;&nbsp;"+
					   "Variant score <strong>%.3f</strong>&nbsp;&nbsp;",
					   priorityScore,filterScore);
	out.write("  <td width=\"45%\"><font size=\"3\" face=\"tahoma\" color=\"#FAFAFA\">" + scoreString + "</td>\n");
	out.write("  <td width=\"25%\"><font size=\"3\" face=\"tahoma\" color=\"#FAFAFA\">Total: <strong>" + combined +
		  "    </strong></font></td>\n"+
		  "</tr>\n");
	/* At this point, we have just finished printing the first row of the table for some gene.
	   This row is always shown. The remaining stuff is toggle-able. */

	String tableID = String.format("tbl%d",n);
	out.write(String.format("<table width=\"1024px\" border=\"0\" align=\"center\" cellpadding=\"2\" cellspacing=\"0\" id=\"%s\">",
				tableID));
	/** Now write the first row. It has a multirow column at the end for the gene priority score. */
	out.write("<tr>\n");	  
	//The following function outputs the left-most table of transcript-based variants etc.
	VariantEvaluation ve = gen.getNthVariant(0); /* Get the first (zero-th) variant associated with this gene */
	outputVariant(ve,out);
	// Each variant now has exactly the same number of ITriage objects with the results of filtering.
	Map<FilterType,ITriage> triageMap = ve.getTriageMap();
	Map<FilterType,IRelevanceScore> relevanceMap = gen.getRelevanceMap();
	ITriage freq = triageMap.get(FilterType.FREQUENCY_FILTER);
	ITriage path = triageMap.get(FilterType.PATHOGENICITY_FILTER);
	// Write the variant score associated with the first (zero-th) variant
	writeVariantScoreCell(path,freq,out);
	IRelevanceScore boqa = relevanceMap.get(FilterType.BOQA_FILTER);
	IRelevanceScore mim = relevanceMap.get(FilterType.OMIM_FILTER);
	/** Span over all rows with variants for this gene. */
	out.write(String.format("<td rowspan=\"%d\">",n_variants)); 
	if (boqa == null && mim == null) {
	    out.write(".");
	}
	if  (boqa != null)
	    out.write(boqa.getHTMLCode() );
	if (mim != null)
	    out.write(mim.getHTMLCode());
    	out.write("</td></tr>\n");
	/* WHen we get here, we have finished writing the row for the first (zero-th) variant associated
	   with this gene. Now we will write the remaining lines. Remember that since the first row has
	   as its last column a multi-row column that spans all of the other rows, the remaining rows do
	   not need to write information for the last column. */
	for (int i=1;i<n_variants;++i) {
	    VariantEvaluation vev = gen.getNthVariant(i);
	    out.write("<tr>\n");
	    outputVariant(vev,out);
	    // Each variant now has exactly the same number of ITriage objects with the results of filtering.
	    Map<FilterType,ITriage> triageMp = vev.getTriageMap();
	    ITriage frq = triageMp.get(FilterType.FREQUENCY_FILTER);
	    ITriage pth = triageMp.get(FilterType.PATHOGENICITY_FILTER);
	    writeVariantScoreCell(pth,frq,out);
	    out.write("</tr>\n");
	}
	out.write("</table>\n"); /* this ends the toggle table (with id = tblx, where x is an integer) */
	out.write("</tr>\n</table>\n"); /* This ends the overall table that included the toggle table */


    }







}