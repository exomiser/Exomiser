package de.charite.compbio.exomiser.io.html;

import java.io.Writer;
import java.io.IOException; 
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import de.charite.compbio.exomiser.priority.PriorityScore;
import jannovar.pedigree.Pedigree;

import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.priority.PriorityType;


/**
 * This class is responsible for creating the tables used by the 
 * Exomiser to display the Clinically Relevant Exome Server webpage.
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
 * The HTML page is designed to show 20 (or if the users asks for it, all) top candidate genes
 * together with a summary of the overall results.
 * @author Peter N Robinson
 * @version 0.45 (2 February, 2014)
 */
public class HTMLTableWalker extends HTMLTable {

    /**
     * The constructor merely passes the
     * {@link jannovar.pedigree.Pedigree Pedigree} object to the superclass.
     * @param ped A representation of the pedigree of the family being analysed (or single-sample).
     */
    public HTMLTableWalker(Pedigree ped) {
	super(ped);
    }


    

    /**
     * This function dynamically creates a table header based on the indices passed to the
     * constructor. Note that the zero-th cell in the table is always coming from the
     * {@link jannovar.exome.Variant} object, which contains annotation information that comes directly from the 
     * VCF file (gene name, name of mutation on trascript level).
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableHeader(Writer out) throws IOException  {
	out.write("<table class=\"priority\">\n");
	out.write("<tr>\n");
	out.write("<th style=\"width:50%\">Variant</th>");
	out.write("<th style=\"width:20%\">Variant analysis</th>");
	out.write("<th style=\"width:30%\">Phenotypic analysis</th>");
	out.write("</tr>\n");
    }

    /**
     * @return A string intended to display information about diseases associated with the
     * gene on the ExomeWalker HTML page
     */
    private String getOmimText(Gene gene) {
	Map<PriorityType, PriorityScore> relevanceScoreMap = gene.getPriorityScoreMap();
	PriorityScore mim = relevanceScoreMap.get(PriorityType.OMIM_PRIORITY);
	if (mim == null) {
	    return "No known human Mendelian disease";
	} else {
	    StringBuilder sb = new StringBuilder();
	    sb.append(String.format("Diseases associated with %s:<br/>\n",gene.getGeneSymbol()));
            List<String> lst = mim.getFilterResultList();
	    for (String a : lst) {
		sb.append(String.format("%s<br/>\n",a));
	    }
	    return sb.toString();
	}
    }


     @Override public void writeGeneTable(Gene gene, Writer out, int n, 
					  List<String> interactionList) throws IOException {
	 int n_variants = gene.getNumberOfVariants(); /* Number of variants associated with this gene */
	 String entrez = getEntrezURL(gene);
	 double priorityScore = gene.getPriorityScore();
	 double filterScore = gene.getFilterScore();
	 double combined = gene.getCombinedScore();
	 String scoreString = String.format("Random walk score: %.3f<br/>variant score %.3f&nbsp;&nbsp;<br/>total score: %.3f",
					    priorityScore,filterScore,combined);

	String omim = getOmimText(gene);
	if (n_variants == 0) { 
	    return; /* this should never happen */ 
	}
	out.write("<table class=\"bordered\">\n"+
		  "<thead>\n"+
		  "<tr>\n");
	out.write("<th colspan=\"2\">" + n + ") <i>" + entrez + "</th></tr>\n"+
		  "</thead>\n");
	out.write("<tbody>\n");
	out.write("<tr><td>"+ omim +"</td><td>" + scoreString + "</td></tr>\n");
	if (interactionList != null && interactionList.size()>0) {
	    StringBuilder sb =new StringBuilder();
	    boolean notfirst=false;
	    for (String s : interactionList) {
		if (notfirst)
		    sb.append(", ");
		else
		    notfirst=true;
		sb.append("<i>"+s+"</i>");
	    }
	    out.write("<tr><td colspan=\"2\">Interactions with seed genes: " + sb.toString() + "</td></tr>\n");
	} else {
	    out.write("<tr><td colspan=\"2\">No first or second-degree interactions with seed genes</td></tr>\n");
	}
	Iterator<VariantEvaluation> iter = gene.getVariantEvaluationIterator();
	while(iter.hasNext()) {
	    VariantEvaluation varev = iter.next();
	    String chrvar = varev.getChromosomalVariant();
	    String annot = varev.getRepresentativeAnnotation(); 
	    int n_annot = varev.getNumberOfAffectedTranscripts(); 
	    String ucscLink = getUCSCBrowserURL(varev);
	    String pathogenicity = getPathogenicityForToolTip(varev);
	    out.write(String.format("<tr><td>%s<br/> %s<br/> %s</td>",chrvar,annot,pathogenicity));
	    out.write(String.format("<td>%s",ucscLink));
	    if (n_annot>1) {
		String tooltip = getTranscriptListForToolTip(varev);
		out.write ("<br/><p class=\"h\">See all transcripts <span>" + tooltip + "</span></p></td></tr>\n");
	    } else {
		out.write("</td></tr>\n");
	    }
   	}
	out.write("</tbody></table>\n");

    }

     /**
     * The ExomeWalker results display has one toggle-table for each gene that
     * has survived filtering.
     * @param gene The gene to be displayed
     * @param out File handle to write to
     * @param n A running number for the current gene.
     */
    @Override 
    public void writeGeneTable(Gene gene, Writer out, int n) throws IOException {
	int n_variants = gene.getNumberOfVariants(); /* Number of variants associated with this gene */
	double filterScore = gene.getFilterScore();
	double combined = gene.getCombinedScore();
	double priorityScore = gene.getPriorityScore();
	String scoreString = String.format("Random walk score: %.3f<br/>variant score %.3f&nbsp;&nbsp;<br/>total score: %.3f",
					   priorityScore,filterScore,combined);
	String entrez = getEntrezURL(gene);
	String omim = getOmimText(gene);
	if (n_variants == 0) { 
	    return; /* this should never happen */ 
	}
	out.write("<table class=\"bordered\">\n"+
		  "<thead>\n"+
		  "<tr>\n");
	out.write("<th colspan=\"2\">" + n + ") <i>" + entrez + "</th></tr>\n"+
		  "</thead>\n");
	out.write("<tbody>\n");
	out.write("<tr><td>"+ omim +"</td><td>" + scoreString + "</td></tr>\n");	  

	Iterator<VariantEvaluation> iter = gene.getVariantEvaluationIterator();
	while(iter.hasNext()) {
	    VariantEvaluation varev = iter.next();
	    String chrvar = varev.getChromosomalVariant();
	    String annot = varev.getRepresentativeAnnotation(); 
	    int n_annot = varev.getNumberOfAffectedTranscripts(); 
	    String ucscLink = getUCSCBrowserURL(varev);
	    String pathogenicity = getPathogenicityForToolTip(varev);
	    out.write(String.format("<tr><td>%s<br/> %s<br/> %s</td>",chrvar,annot,pathogenicity));
	    out.write(String.format("<td>%s",ucscLink));
	    if (n_annot>1) {
		String tooltip = getTranscriptListForToolTip(varev);
		out.write ("<br/><p class=\"h\">See all transcripts <span>" + tooltip + "</span></p></td></tr>\n");
	    } else {
		out.write("</td></tr>\n");
	    }
   	}
	out.write("</tbody></table>\n");

    }



    private String getPathogenicityForToolTip(VariantEvaluation varev) {
	StringBuilder sb = new StringBuilder();
	sb.append(String.format("<p class=\"h\">Pathogenicity score: %.2f<span>", varev.getVariantScore())); 
	if (varev.passedFilter(FilterType.PATHOGENICITY_FILTER)) {
	    sb.append(pathResultsWriter.getFilterResultSummary(varev));
	}
	if (varev.passedFilter(FilterType.FREQUENCY_FILTER)) {
	    sb.append(frequencyResultWriter.getFilterResultSummary(varev));
	}
	sb.append("</span></p>\n");
	return sb.toString();
    }



  

    /**
     * We get a string linke this HLA-A(uc003nok.3:exon2:c.28G>T:p.G10W) 
     * and want to use uc003nok.3 as the basis of a URL and HTML anchor tag.
     */
    private String getUCSCBrowswerURLForKnownGene(String annot) {
	int i = annot.indexOf("(");
	if (i<0) return annot;
	int j = annot.indexOf(")",i);
	if (j<0) return annot;
	String kg = annot.substring(i+1,j);
	String url = String.format("http://genome.ucsc.edu/cgi-bin/hgGene?db=hg19&hgg_gene=%s",kg);
	String anchor = String.format("<a href=\"%s\" target=\"_new%s\">View %s in UCSC Browser</a>",url,kg,kg);
	return anchor;
    }
}