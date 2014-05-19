package de.charite.compbio.exomiser.io.html;

import java.io.Writer;
import java.io.IOException; 
import java.util.List;
import java.util.Map;

import jannovar.common.Constants;
import jannovar.pedigree.Pedigree;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Triage;
import de.charite.compbio.exomiser.priority.RelevanceScore;




/**
 * This class is responsible for creating the tables used by the 
 * Exomiser to display the summary, the pedigree data, and to show prioritized variants.
 * The class basically creates an HTML table based on information in the 
 * {@link exomizer.exome.Gene Gene} objects and the {@link jannovar.exome.Variant} objects
 * contained in the  {@link exomizer.exome.Gene Gene} objects.
 * <P>
 * Note that the appearance of the Table is controlled by CSS code that is contained in the 
 * class {@link exomizer.io.html.HTMLWriter HTMLWriter}.
 * <P>
 * The methods of this class use {@code java.io.Writer} instead of {@code java.io.BufferedWriter} because
 * this allows client code to call the methods with either {@code java.io.BufferedWriter} or
 * {@code java.io.StringWriter}
 * <P>
 * Subclasses have been written for the CRE-Server and ExomeWalker
 * @see exomizer.io.html.HTMLTableCRE
 * @see exomizer.io.html.HTMLTableWalker
 * @author Peter N Robinson
 * @version 0.48 (2 February, 2014)
 */
public class HTMLTable {
    /** The Pedigree corresponding to the sample(s) being analysed. */
    private Pedigree pedigree=null;
   
    /**
     * These are the CSS class ids for the two types of table row for the prioritization table. See 
     * {@link exomizer.io.html.HTMLWriter HTMLWriter} for the CSS code. Essentially, we alternate between
     * d0 and d1 for genes, showing alternate white and grey rows. Note that this is not used
     * by the ExomeWalker tables, which instead use toggling.
     */
    private String[] rowclass = {"d0","d1"};
    /** Index of the current rowclass. Initialize to 1. */
    private int rowclassIdx=1;
    
    /**
     * The constructor 
     * @param ped A representation of the pedigree of the family being analysed (or single-sample).
     */
    public HTMLTable(Pedigree ped) {
	this.pedigree = ped;
    }
    
    /**
     * This function writes out a table representing the PED file of the
     * family being analysed (if a multisample VCF file is being analysed)
     * or the name of the sample (for a single-sample VCF file). 
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
     * @param out An open file handle (can come from the command line or server versions of Exomiser).
     */
    public void writePedigreeTable(Writer out)  throws IOException {
	int n = this.pedigree.getNumberOfIndividualsInPedigree();
	if (n==1){
	    String sampleName = this.pedigree.getSingleSampleName();
	    out.write("<table class=\"pedigree\">\n");
	    out.write(String.format("<tr><td>Sample name: %s</td></tr>\n",sampleName));
	    out.write("</table>\n");
	} else { /* multiple samples */
	    out.write("<h2>Analyzed samples</h2>\n");
	    out.write("<p>affected: red, parent of affected: light blue, unaffected: white</p>\n");
	    out.write("<table class=\"pedigree\">\n");
	    for (int i=0;i<n;++i) {
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
		     out.write("<td id=\"b\">"+ id + "</td>");
		} else {
		    out.write("<td id=\"w\">"+ id + "</td>");
		}
		out.write("<td>" + fathID + "</td><td>" + mothID + "</td><td>" + 
			  sex + "</td><td>" + disease + "</td></tr>\n");
	    }
	    out.write("</table>\n");
	    out.write("<br/>\n");
	}
    }


    /**
     * This function dynamically creates a table header based on the indices passed to the
     * constructor. Note that the zero-th cell in the table is always coming from the
     * {@link jannovar.exome.Variant} object, which contains annotation information that comes directly from the 
     * VCF file (gene name, name of mutation on trascript level).
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableHeader(Writer out) throws IOException  {
	out.write("<a name=\"Prioritization\">\n"+
		  "<h2>Prioritized Variant List</h2>\n"+
		  "</a>\n");
	out.write("<table class=\"priority\">\n");
	out.write("<tr>\n");
	out.write("<th style=\"width:50%\">Variant</th>");
	out.write("<th style=\"width:20%\">Variant analysis</th>");
	out.write("<th style=\"width:30%\">Phenotypic analysis</th>");
	out.write("</tr>\n");
    }

    /**
     * Writes a string with HTML code for the Table footer
     * @param out IO stream to write the HTML file to.
     */
    public void writeTableFooter(Writer out) throws IOException  {
	out.write("</table>\n");
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
	String currentCSSclass = this.rowclass[rowclassIdx];
	rowclassIdx = (rowclassIdx + 1)%2;
	currentCSSclass = this.rowclass[rowclassIdx];
	writeVariantAndGeneRow(gen,currentCSSclass, out);
	int N = gen.getNumberOfVariants();
	for (int i=1;i<N;++i) {
	    VariantEvaluation ve = gen.getNthVariant(i);
	    writeVariant(ve,currentCSSclass, out);
	}
    }


      /**
     * @return a String with all of the affected transcripts, meant for
     * display on html page.
     */
    protected String getTranscriptListForToolTip(VariantEvaluation varev) {
	List<String> lst = varev.getAnnotationListWithAnnotationClass();
	String gsymbol = varev.getGeneSymbol();
	StringBuilder sb = new StringBuilder();
	String chrvar = varev.getChromosomalVariant();
	sb.append("<b>" + chrvar + " (<i>"+gsymbol+"</i>)</b><br/>\n");
	for (String s : lst) {
	    sb.append(s + "<br/>\n");
	}
	return sb.toString();
    }



     /**
     * The ExomeWalker results display has one toggle-table for each gene that
     * has survived filtering. This version of the function has a A map of interactions with seed genes. 
     * This class does noting with it, but subclasses can display these interactions.
     * @param gen The gene to be displayed
     * @param out File handle to write to
     * @param n A running number for the current gene.
     * @param interactionLst A list of interactions with seed genes. 
     */
    public void writeGeneTable(Gene gen, Writer out, int n, List<String> interactionLst) throws IOException {

	writeGeneTable(gen,out,n);

    }


    /**
     * The ExomeWalker results display has one toggle-table for each gene that
     * has survived filtering.
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
	// Each variant now has exactly the same number of Triage objects with the results of filtering.
	Map<FilterType,Triage> triageMap = ve.getTriageMap();
	Map<FilterType,RelevanceScore> relevanceMap = gen.getRelevanceMap();
	Triage freq = triageMap.get(FilterType.FREQUENCY_FILTER);
	Triage path = triageMap.get(FilterType.PATHOGENICITY_FILTER);
	// Write the variant score associated with the first (zero-th) variant
	writeVariantScoreCell(path,freq,out);
	RelevanceScore phenodigm = relevanceMap.get(FilterType.PHENODIGM_FILTER);
	RelevanceScore gwanderer = relevanceMap.get(FilterType.GENEWANDERER_FILTER);
        RelevanceScore pwanderer = relevanceMap.get(FilterType.PHENOWANDERER_FILTER);
        RelevanceScore dpwanderer = relevanceMap.get(FilterType.DYNAMIC_PHENOWANDERER_FILTER);
	RelevanceScore mim = relevanceMap.get(FilterType.OMIM_FILTER);
	RelevanceScore resnik = relevanceMap.get(FilterType.HPO_FILTER);
	RelevanceScore phmizer = relevanceMap.get(FilterType.PHENOMIZER_FILTER);
	/** Span over all rows with variants for this gene. */
	out.write(String.format("<td rowspan=\"%d\">",n_variants)); 
        System.out.println("OMIM:"+mim+",PWANDERER:"+pwanderer);
	if (phenodigm == null && mim == null && gwanderer == null && phmizer==null) {
	    out.write(".");
	}
	if (phmizer != null) 
	    out.write(phmizer.getHTMLCode());
	if  (phenodigm != null)
	    out.write(phenodigm.getHTMLCode());
	if  (gwanderer != null)
	    out.write(gwanderer.getHTMLCode());
        if  (pwanderer != null)
	    out.write(pwanderer.getHTMLCode());
        if  (dpwanderer != null)
	    out.write(dpwanderer.getHTMLCode());
	if (mim != null)
	    out.write(mim.getHTMLCode());
	if (resnik != null)
	    out.write(resnik.getHTMLCode());
    	out.write("</td></tr>\n");
	/* WHen we get here, we have finished writing the row for the first (zero-th) variant associated
	   with this gene. Now we will write the remaining lines. Remember that since the first row has
	   as its last column a multi-row column that spans all of the other rows, the remaining rows do
	   not need to write information for the last column. */
	for (int i=1;i<n_variants;++i) {
	    VariantEvaluation vev = gen.getNthVariant(i);
	    out.write("<tr>\n");
	    outputVariant(vev,out);
	    // Each variant now has exactly the same number of Triage objects with the results of filtering.
	    Map<FilterType,Triage> triageMp = vev.getTriageMap();
	    Triage frq = triageMp.get(FilterType.FREQUENCY_FILTER);
	    Triage pth = triageMp.get(FilterType.PATHOGENICITY_FILTER);
	    writeVariantScoreCell(pth,frq,out);
	    out.write("</tr>\n");
	}
	out.write("</table>\n"); /* this ends the toggle table (with id = tblx, where x is an integer) */
	out.write("</tr>\n</table>\n"); /* This ends the overall table that included the toggle table */


    }




    /**
     * This writes the <b>first</b> row for a given {@link exomizer.exome.Gene Gene}. 
     * Note that the last cell of the row will span all
     * of the rows for this gene (using the HTML {@code rowspan} attribute), and the current function is
     * thus used only for the first row for some gene. All of the other variants for a gene are output using the
     * function {@link #writeVariant}.
     */
    protected void writeVariantAndGeneRow(Gene gen, String css, Writer out) throws IOException {
	VariantEvaluation ve = gen.getNthVariant(0);
	int n_variants = gen.getNumberOfVariants();
	if (n_variants == 0) { return; /* this should never happen */ }
	String entrez = getEntrezURL(gen);
	double priorityScore = gen.getPriorityScore();
	double filterScore = gen.getFilterScore();
	double combined = gen.getCombinedScore();
	String scoreString = String.format("&nbsp;&nbsp;gene relevance score: %.3f&nbsp;&nbsp;variant score %.3f&nbsp;&nbsp;total score: %.3f",
					   priorityScore,filterScore,combined);
	out.write(String.format("<tr class=\"%s\"><td colspan=\"4\" style=\"border:solid 2px\">"+
				"<b><i>%s</i></b>&nbsp;&nbsp;%s&nbsp;&nbsp;%s</td></tr>\n",
				css,gen.getGeneSymbol(),entrez,scoreString));
	out.write(String.format("<tr class=\"%s\">\n",css));
	outputVariant(ve,out);
	
	// Each variant now has exactly the same number of Triage objects with the results of filtering.
	Map<FilterType,Triage> triageMap = ve.getTriageMap();
	Map<FilterType,RelevanceScore> relevanceMap = gen.getRelevanceMap();
	Triage freq = triageMap.get(FilterType.FREQUENCY_FILTER);
	Triage path = triageMap.get(FilterType.PATHOGENICITY_FILTER);
	writeVariantScoreCell(path,freq,out);
	


	RelevanceScore phenodigm = relevanceMap.get(FilterType.PHENODIGM_FILTER);
	RelevanceScore zfin_phenodigm = relevanceMap.get(FilterType.ZFIN_PHENODIGM_FILTER);
	RelevanceScore mim = relevanceMap.get(FilterType.OMIM_FILTER);
        RelevanceScore pwanderer = relevanceMap.get(FilterType.PHENOWANDERER_FILTER);
        RelevanceScore dpwanderer = relevanceMap.get(FilterType.DYNAMIC_PHENOWANDERER_FILTER);
	/** Span over all rows with variants for this gene. */
	out.write(String.format("<td rowspan=\"%d\">",n_variants)); 
	if (phenodigm == null && mim == null && zfin_phenodigm == null && pwanderer == null) {
	    out.write(".");
	}
	if  (phenodigm != null)
	    out.write(phenodigm.getHTMLCode() );
	if  (zfin_phenodigm != null)
	    out.write(zfin_phenodigm.getHTMLCode() );
        if (pwanderer != null)
	    out.write(pwanderer.getHTMLCode());
        if (dpwanderer != null)
	    out.write(dpwanderer.getHTMLCode());
	if (mim != null)
	    out.write(mim.getHTMLCode());
    	out.write("</td></tr>\n");
    }

    /**
     * This function writes an HTML row for a single variant that is not the
     * first variant to be show for the current gene (the first variant is
     * written by the function {@link #writeVariantAndGeneRow}).
     * @param var The evaluation object for the variant
     * @param css Either d0 or d1, the CSS classes to make the alterating rows have alternating colors.
     * @param out A file handle to write to
     */
    protected void writeVariant(VariantEvaluation var, String css, Writer out) throws IOException {
	
	out.write(String.format("<tr class=\"%s\">\n",css));
	outputVariant(var,out);
	// Each variant now has exactly the same number of Triage objects with the results of filtering.
	Map<FilterType,Triage> triageMap = var.getTriageMap();
	Triage freq = triageMap.get(FilterType.FREQUENCY_FILTER);
	Triage path = triageMap.get(FilterType.PATHOGENICITY_FILTER);
	writeVariantScoreCell(path,freq,out);
	out.write("</tr>\n");
    }


    /**
     * Generate a URL and HTML link for it to display +/- 5 nucleotides around the position of the mutation
     * in the UCSC browser.
     * @param varev The variant to be displayed in the UCSC browser
     */
    public String getUCSCBrowswerURL(VariantEvaluation varev) {
	int OFFSET = 5;
	String chrom = varev.getChromosomeAsString();
	int start = varev.getVariantStartPosition() - OFFSET;
	int end = varev.getVariantEndPosition() + OFFSET;
	String symbol = varev.getGeneSymbol();
	String url = String.format("http://genome.ucsc.edu/cgi-bin/hgTracks?org=Human&db=hg19&position=%s:%d-%d",
				   chrom,start,end);
	String anchor = String.format("<a href=\"%s\" target=\"_new%s\">View in UCSC Browser</a>",url,symbol);
	return anchor;
    }

    /**
     * Write the entire contents of the cell containing the variant score.
     * @param path An object containing the pathogenicity predictions
     * @param freq An object containing the frequency evaluation
     * @param out A file handle to the HTML file being written
     */
    protected void writeVariantScoreCell(Triage path,Triage freq, Writer out) throws IOException
    {
	out.write("<td>");
	out.write("<i>Pathogenicity:</i>\n");

	if (path==null)
	    out.write("<ul><li>n/a</li></ul>\n");
	else
	    out.write(path.getHTMLCode());
	out.write("<i>Frequency</i>\n");
	if (freq==null)
	    out.write("<ul><li>n/a</li></ul>\n");
	else
	    out.write(freq.getHTMLCode());
	out.write("</td>\n");
    }
	
       
    /** 
     * This function is responsible for creating the HTML elements displayed as the leftmost cell
     * of the prioritization table for an individual variant. It basically creates links to UCSC,
     * EntrezGene, and shows the annotations for each affected transcript.
     * <P>
     * This function writes the entire table cell (td element) for the variant
     * @param ve the Variant to be formated.
     * @param out Buffer to write the HTML code to.
     */
    public void outputVariant(VariantEvaluation ve, Writer out) throws IOException
    {
	String ucsc = getUCSCBrowswerURL(ve);
	String chromVariant = ve.getChromosomalVariant();
	
	out.write("<td>");  
	/* Output the gene symbol */
	out.write("<b>" + ve.getGeneSymbol() + "</b></br>\n");
	/* Output the variant (chromosomal notation) */
	out.write( chromVariant + "<br/>\n");
	/* Output the genotype */
	List<String> genotypeList = ve.getGenotypeList();
	String gtype = null;
	if (genotypeList.size() == 1) {
	    out.write( String.format(" [<b>%s</b>] </br>",genotypeList.get(0)));
	} else {
	    outputFormatedMultisampleGenotypeList(genotypeList,out);
	    out.write("<br/>\n");
	}
	/* Output the UCSC URL */
	out.write( ucsc + "<br/>\n" ); 
	/* Output the various transcript annotations */
	formatMultipleTranscripts(ve,out);
	/* Output the variant quality */
	Triage qual = ve.getTriageMap().get(FilterType.QUALITY_FILTER);
	if (qual!=null)
	    out.write(qual.getHTMLCode() + "\n");
	out.write("</td>\n");
    }



    /**
     * This function formats the multipe-sample genotype list using
     * "div" definitions found in the class
     * {@link exomizer.io.html.HTMLWriter HTMLWriter} such that affecteds,
     * parents of affecteds, and unaffecteds are show with different colors.
     * @param lst List of genotypes for the pedigree represented as strings.
     * @param out File handle to write to.
     */
    protected void outputFormatedMultisampleGenotypeList(List<String> lst, Writer out) 
	throws IOException {
	out.write("Genotype: <table class=\"gtype\"><tr>\n");
	for (int i=0;i<lst.size();++i) {
	    if (this.pedigree.isNthPersonAffected(i)){
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
     * @param g The {@link exomizer.exome.Gene Gene} object for which we want the NCBI Entrez Gene ID
     * @return An HTML link to the URL at NCBI corresponding to the Entrez Gene
     */
    protected String getEntrezURL(Gene g) {
	int entrezID = g.getEntrezGeneID();
	if (entrezID ==  Constants.UNINITIALIZED_INT) {
	    return g.getGeneSymbol();
	} else {
	    String url = String.format("<a href=\"http://www.ncbi.nlm.nih.gov/gene/%d\"><i>%s</i></a>",
				       entrezID,g.getGeneSymbol());
	    return url;
	}
    }


    /**
     * This method breaks a string of variant annotations into
     * one-to-a line annotations. This should be refactored such that
     * the annotations themselves make this.
     * @param varev The current variant object.
     * @param out File handle to write HTML page
     */
    protected void formatMultipleTranscripts(VariantEvaluation varev, Writer out) throws IOException
    {

	/* First thing is to get the position of the variant and its surrounding on the
	   chromosome so that we can create a URL for the UCSC browser to show it. */
	int chromPos = varev.getVariantStartPosition();
	String chrom = varev.getChromosomeAsString();
	int x = chromPos-9;
	int y = chromPos +10;
	String positionString =  positionString = String.format("%s:%d-%d",chrom,x,y);
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
		formatUCSCTranscript(id, annotation,null, positionString, out); 
	    } else {
		String symbol = B[2];
		formatUCSCTranscript(id, annotation,symbol, positionString, out);
	    }   
	    out.write("</li>\n");
	}
	out.write("</ul>\n");
    }

    /**
     * This function is responsible for writing the line with the annotation for an individual transcript.
     * This function expects to get a gene symbol for variants that affect more than one gene. In this case, the
     * gene symbol is shown in parentheses after the main annotation. For variants that affect only one gene,
     * the function expects that this argument is null (in this case, the gene symbol is not appended to the
     * main annotation).
     * @param id An accession number such as uc010wkp.2
     * @param annotation the corresponding variant annotation
     * @param genesymbol This function expects to get a gene symbol for variants that affect more than one gene
     * @param posString Position range (20 nt) around the mutation. Used to make URL for UCSC browser.
     * @param out file handle to write to
     */
    private void formatUCSCTranscript(String id, String annotation, String genesymbol, String posString, Writer out) throws IOException
    {
	String url = null;
	if (! id.startsWith("uc") ) {
	    /* Not a knownGenes id, e.g., uc010wkp.2 */
	    url = id; /* just the id, not a real URL */
	} else {
	    url = String.format("<a href=\"http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg19&position=%s\">%s</a>",
				   posString,id);
	}
	
	String A[] = annotation.split(":");
	if (A.length == 1) {
	    out.write (String.format("%s: %s",url, A[0]));
	} else	if (A.length == 2) {
	    out.write(String.format("%s: %s (%s)", url, A[0], A[1]));
	} else if (A.length == 3) {
	    out.write( String.format("%s: %s (%s; %s)",url,A[1],A[2],A[0]) );
	} else { /* there are at least four fields now */
	    out.write( String.format("%s: %s (%s; %s; %s)",url,A[1],A[2],A[0], A[4]) );
	}
	if (genesymbol != null) {
	    out.write (" (" + genesymbol + ")" );
	}
    }


    /**
     * This function gets a list of Strings representing information about a mutation derived from 
     * a {@link  exomizer.filter.Triage} object (passed into this function from {@link exomizer.Exomizer}).
     * @return HTML code for an unordered list representing information about a variant.
     */
    private String  format_triage_list(List<String> L) {
	if (L==null) return ".";
	if (L.size()==1) return L.get(0);
	StringBuilder sb = new StringBuilder();
	sb.append("<ul>");
	for (String s : L) {
	    sb.append("<li>" + s + "</li>");
	}
	sb.append("</ul>\n");
	return sb.toString();
    }

}