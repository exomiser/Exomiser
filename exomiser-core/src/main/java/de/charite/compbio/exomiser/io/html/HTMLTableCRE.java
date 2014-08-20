package de.charite.compbio.exomiser.io.html;

import java.io.Writer;
import java.io.IOException; 
import java.util.List;
import java.util.Map;
import jannovar.pedigree.Pedigree;

import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.priority.PriorityScore;


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
 * @version 0.48 (25 January, 2014)
 */
public class HTMLTableCRE extends HTMLTable {
     /** A counter to keep track of what gene row we are writing. This is initialized when the header is written
     * an incremented for each row.*/
    private int currentRow;

    /**
     * The constructor merely passes the
     * {@link jannovar.pedigree.Pedigree Pedigree} object to the superclass.
     * @param ped A representation of the pedigree of the family being analysed (or single-sample).
     */
    public HTMLTableCRE(Pedigree ped) {
	super(ped);
    }


     /** This method extracts the fields from the current {@link jannovar.exome.Variant} object that have been 
     * indicated in the array idx with the corresponding fields. 
     * <P>
     * This method keeps track of the current gene and uses alternating colors for rows with variants from
     * different genes. The corresponding CSS code is to be found in the class 
     * {@link exomizer.io.html.HTMLWriter HTMLWriter}.
     * @param gen The {@link exomizer.exome.Gene Gene} object that is to be displayed in the current Table row.
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableRow(Gene gen, Writer out) throws IOException {
	currentRow++;
	writeVariantAndGeneRow(gen,out); /* Writes the gene info and first variant */
	int N = gen.getNumberOfVariants();
	/* write the other variants in this gene, if any */
	for (int i=1;i<N;++i) {
	    VariantEvaluation ve = gen.getNthVariant(i);
	    out.write("<tr><td colspan=\"2\"><hr class=\"grad\"></td></tr>\n");
	    out.write("<tr>");
	    writeVariant(ve, out);
	    out.write("</tr>\n");
	}
    }

    /**
     * This function writes an HTML row for a single variant that is not the
     * first variant to be show for the current gene (the first variant is
     * written by the function {@link #writeVariantAndGeneRow}).
     *
     * @param var The evaluation object for the variant
     * @param out A file handle to write to
     */
    protected void writeVariant(VariantEvaluation var,  Writer out) throws IOException {
	String ucsc = getUCSCBrowserURL(var);

	out.write("<td>");  
	/* Output the variant (chromosomal notation) */
	String rep = getCRERepresentativeVariant(var);
	out.write("<b>" + rep  + "</b>&nbsp;&nbsp;&nbsp;("+var.getChromosomalVariant()+")</br>\n");
	out.write("<b>" + var.getVariantType() + "</b></br>\n");
	/* Output the genotype */
	List<String> genotypeList = var.getGenotypeList();
	if (genotypeList.size() == 1) {
	    out.write( String.format("Genotype: [<b>%s</b>] <br/>",genotypeList.get(0)));
	} else {
	    outputFormatedMultisampleGenotypeList(genotypeList,out);
	    out.write("<br/>\n");
	}
	/* Output the UCSC URL */
	out.write( ucsc + "<br/>\n" );
	if (var.passedFilter(FilterType.QUALITY_FILTER)) {
	    List<String> lst = qualityResultWriter.getFilterResultList(var);
	    out.write("Phred variant quality: " + lst.get(0) + "<br/>\n");
	}
	String tooltip = getTranscriptListForToolTip(var);

	out.write ("<br/><p class=\"h\">Affected transcripts<span>" + tooltip + "</span></p> <br/>\n");
	/* Output the variant quality */
	List<String> mutList = var.getMutationReferenceList();
	for (String mut : mutList) {
	    out.write(mut + "<br/>\n");
	}
	out.write("</td>\n");
	writeVariantCREScoreCell(var,out);

    } 

    /**
     * Create an HTML link to Mutation taster
     * http://www.mutationtaster.org/cgi-bin/MT_dev/MT_ChrPos.cgi?chromosome=5&position=175811101&ref=C&alt=CGT
     */
    private String mutationTasterLink(VariantEvaluation var) {
	String mt = null;
	mt = String.format("http://www.mutationtaster.org/cgi-bin/MT_dev/MT_ChrPos.cgi?chromosome=%s&position=%d&ref=%s&alt=%s",
			   var.getChromosomeAsString(), 
			   var.getPosition(),
			   var.getRef(),
			   var.getAlt());
	String a = String.format("<a href=\"%s\" class=\"button glow\" target=\"_new%d\">Analyse variant with MutationTaster2</a>",mt,var.getPosition());
	return a;
    }


     /**
     * Write the entire contents of the cell containing the variant score.
     * @param path An object containing the pathogenicity predictions
     * @param freq An object containing the frequency evaluation
     * @param out A file handle to the HTML file being written
     */
    protected void writeVariantCREScoreCell(VariantEvaluation var, Writer out) throws IOException
    {
	out.write("<td>");
	out.write("<i>Pathogenicity:</i><br/>\n");

	if (var.passedFilter(FilterType.PATHOGENICITY_FILTER)) {
            out.write(pathResultsWriter.getFilterResultSummary(var));
        } else {
            out.write("n/a<br/>\n");
        }
	out.write("<i>Frequency</i><br/>\n");
	if (var.passedFilter(FilterType.FREQUENCY_FILTER)) {
            out.write(frequencyResultWriter.getFilterResultSummary(var));
        } else {
            out.write("n/a<br/>\n");
        }
	out.write("Variant read depth: " + var.getVariantReadDepth() + "<br/>\n");
	if (var.isSNV()) {
	    String mt = mutationTasterLink(var);
	    out.write( mt );
	}
	out.write("</td>\n");
	
    }
	

    


    private String getCRERepresentativeVariant(VariantEvaluation v) {
	String ann = v.getRepresentativeAnnotation();
	// ann is now e.g.,  CLDN16(uc010hze.3:exon1:c.164delG:p.R55fs)
	String A[] = ann.split(":");
	if (A.length != 4)
	    return ann;
	else {
	    String prot = A[3];
	    int i = prot.indexOf(")");
	    if (i>0)
		prot = prot.substring(0,i);
	    return String.format("%s, %s", A[2],prot);
	}
    }


     /**
     * This writes the <b>first</b> row for a given {@link exomizer.exome.Gene Gene}. 
     * Note that the last cell of the row will span all
     * of the rows for this gene (using the HTML {@code rowspan} attribute), and the current function is
     * thus used only for the first row for some gene. All of the other variants for a gene are output using the
     * function {@link #writeVariant}.
     */
    protected void writeVariantAndGeneRow(Gene gen, Writer out) throws IOException {
	VariantEvaluation ve = gen.getNthVariant(0);
	int n_variants = gen.getNumberOfVariants();
	if (n_variants == 0) { return; /* this should never happen */ }
	String entrez = getEntrezURL(gen);
	double priorityScore = gen.getPriorityScore();
	double filterScore = gen.getFilterScore();
	double combined = gen.getCombinedScore();
	String scoreString = String.format("&nbsp;&nbsp;Gene relevance score: %.3f&nbsp;&nbsp;variant score %.3f"+
					   "&nbsp;&nbsp;total score: %.3f",
					   priorityScore,filterScore,combined);
	//scoreString = String.format("%s --- has %d",scoreString,n_variants);
	out.write(String.format("<tr><td colspan=\"3\" style=\"border:solid 2px\">"+
				"<b>%d) %s</b>&nbsp;&nbsp;%s</td></tr>\n",
				currentRow,gen.getGeneSymbol(),scoreString));
	out.write(String.format("<tr>\n"));
	writeVariant(ve,out);
	
	// Each variant now has exactly the same number of FilterScore objects with the results of filtering.
	//HashMap<FilterType,FilterScore> triageMap = ve.getTriageMap();
	
	//Triage freq = triageMap.get(FilterType.FREQUENCY_FILTER);
	//Triage path = triageMap.get(FilterType.PATHOGENICITY_FILTER);
	//writeVariantScoreCell(path,freq,out);
	

	Map<PriorityType, PriorityScore> relevanceMap= gen.getPriorityScoreMap();
	PriorityScore phenomizer = relevanceMap.get(PriorityType.PHENOMIZER_PRIORITY);
	PriorityScore mim = relevanceMap.get(PriorityType.OMIM_PRIORITY);
	/** Span over all rows with variants for this gene. */
	out.write(String.format("<td rowspan=\"%d\" valign=\"top\">",n_variants)); 
	if (phenomizer == null && mim == null) {
	    out.write(".");
	}
	if  (phenomizer != null)
	    out.write(phenomizer.getHTMLCode() );
	if (mim != null)
	    out.write(mim.getHTMLCode());
    	out.write("</td></tr>\n");
    }



    /**
     * This function dynamically creates a table header based on the indices passed to the
     * constructor. Note that the zero-th cell in the table is always coming from the
     * {@link jannovar.exome.Variant} object, which contains annotation information that comes directly from the 
     * VCF file (gene name, name of mutation on trascript level).
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableHeader(Writer out) throws IOException  {
	out.write("<a name=\"priority\">\n"+
		  "</a>\n");
	out.write("<table class=\"priority\">\n");
	out.write("<tr>\n");
	out.write("<th style=\"width:50%\">Variant</th>");
	out.write("<th style=\"width:20%\">Variant analysis</th>");
	out.write("<th style=\"width:30%\">Phenotypic analysis</th>");
	out.write("</tr>\n");
	this.currentRow = 0;
    }
}