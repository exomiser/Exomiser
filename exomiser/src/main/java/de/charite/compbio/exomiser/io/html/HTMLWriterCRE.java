package de.charite.compbio.exomiser.io.html;

import java.io.Writer;
import java.io.IOException; 
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;

import jannovar.common.VariantType;
import jannovar.exome.VariantTypeCounter;
import jannovar.pedigree.Pedigree;

/**
 * This class is responsible for creating the framework of the
 * HTML page that is created by the Exomiser for each VCF BOQA analysis.
 * It writes out the CSS elements and the constant parts of the HTML
 * page.
 * <P>
 * The class writes to a {@code java.io.Writer} so that we can use either
 * a BufferedWriter or a StringWriter. This allows the class to be used either by
 * the cmmand-line version of the Exomiser or by apache tomcat versions that pass in 
 * an open file handle.
 * <p>
 * The class now offers a series of methods with "Walker" in their name that offer
 * toggle functionality that we are using for the ExomeWalker server.
 * @author Peter Robinson
 * @version 0.36 (25 January, 2014)
 */
public class HTMLWriterCRE extends HTMLWriter {
    /** List of HPO URLs in form of an HTML anchor. */
    private List<String> hpoLst=null;
   

    /** 
     * @param writer File handle to write the HTML output page to
     */
    public HTMLWriterCRE(Writer writer)  {
	super(writer);
    }

    /** 
     * @param writer File handle to write the HTML output page to
     * @param basename The basename of the VCF file with which we performed the analysis.
     */
    public HTMLWriterCRE(Writer writer,String basename)  {
	super(writer,basename);
    }
    
    /**
     * Create a BufferedWriter from the file pointed to by fname
     * @param fname Name and path of file to be written to.
     */
    public HTMLWriterCRE(String fname) throws ExomizerInitializationException {
	super(fname);
    }
     /**
     * Create a BufferedWriter from the file pointed to by fname
     * @param fname Name and path of file to be written to.
     * @param basename The basename of the VCF file with which we performed the analysis.
     */
    public HTMLWriterCRE(String fname,String basename) throws ExomizerInitializationException {
	super(fname,basename);
    }




    /** Write the very beginning of the HTML file, with the DOCTYPE
     * declaration and the title. After this segment, if desired, one should
     * write CSS using the function {@link #writeCSS} in this class
     * or another function (e.g., from a tomcat server).
     */
    @Override public void openHeader() throws IOException {
	out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Strict//EN\"\n"+
		   "\"http://www.w3.org/TR/html4/strict.dtd\">\n" +
		  "<html lang=\"en\">\n" +
		  "<head>\n" + 
		  "<meta name=\"description\" "+ 
		  "content=\"PhenIX: Variant prioritization by phenotypic similarity\">\n"+
		  "<meta name=\"keywords\" "+
		  "content=\"PhenIX, variant prioritization, exome, human phenotype ontology\">\n"+	
		  "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" >\n"+
		  "<title>PhenIX: Phenotypic Interpretation of eXomes</title>\n");
     }


      /**
     * This function writes out the title, the top menu, and then it opens the div element for main that
     * will be closed by the function {@link #writeHTMLFooter}.
     */
    public void  writeTopMenu() throws IOException
    {
	out.write("<div class=\"container\">\n"+  /* Div stack height 1 */
		  "<h2 class=\"alt\">\n"+
		  "<div style=\"font-size:32px;\">\n"+ /* Div stack height 2 */
		  "<a href=\"http://compbio.charite.de/PhenIX/\">"+
		  "<img src=\"img/pheniX.png\" alt=\"PhenIX\" width=\"350px\" style=\"border:none;\"/></a> \n"+
		  "</div>\n"+ /* Div stack height 1 */
		  "</h2>\n"+
		  "<hr>\n");
	out.write("<div class=\"container\">\n"+ /* Div stack height 2 */
		  "<div class=\"grid_L alpha colborder\">\n"+/* Div stack height 3 */
		  "<h6>PhenIX: Phenotypic Interpretation of eXomes</h6>\n"+
		  "<p>PhenIX is designed to help professionals identify\n"+
		  "disease-causing mutations in Next-Generation-Sequencing (NGS) diagnostics. It currently\n"+
		  "is implemented to search for variants in a comprehensive list of genes known to be\n"+
		  "associated with Mendelian diseases, and will work best with NGS datasets that have been \n"+
		  "generated with exomes or comprehensive gene-panel approaches that enrich these genes. It will filter the variants\n"+
		  "identified on the basis of rarity, predicted pathogenicity and clinical similarity with \n"+
		  "the clinical manifestations of the individual being investigated with the corresponding\n"+
		  "Mendelian disease entities. The clinical similarity is calculated using semantic similarity \n"+
		  "using the <a href=\"http://www.human-phenotype-ontology.org\">Human Phenotype Ontology (HPO)</a>\n"+ 
		  "and an adaptation of the <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/19800049\" >Phenomizer</a> \n"+
		  "algorithm. Note that PhenIX by design will only prioritize known disease genes and is not intended to \n"+
		  "search for novel disease genes</p>\n"+
		  "</div>\n"); /* Div stack height 2 */
        out.write("<div class=\"grid_S omega\">\n"+/* Div stack height 3 */
		  "<h6>Results</h6>\n"+
		  "<p><ul>\n"+
		  "<li><a href=\"#priority\" >Top 20 ranked candidate genes.</a></li>   \n"+
		  //"<li><a href=\"#hgmd\">Human Gene Mutation Database.</a></li>   \n"+
		  "<li><a href=\"#filter\" >Exome Filtering Results.</a></li>  \n"+ 
		  "<li><a href=\"#distribution\" >Overall variant distribution.</a></li>   \n"+  
		  "<li><a href=\"#about\">About PhenIX.</a></li>   \n"+   
		  "</ul><p>&nbsp;</p>\n"+
		  "<span>Begin new analysis</span>\n"+
		  "</div>\n"+/* Div stack height 2 */
		  "</div>\n"+/* Div stack height 1 */
		  "<hr>\n"+
		  "<hr class=\"space\">\n");
    }


     /**
     * Output the cascading style sheet (CSS) code for the HTML webpage
     */
    @Override public void writeCSS() throws IOException {
	out.write("<style type=\"text/css\">\n");
	out.write("html {font-size:100.01%;}\n"+
		  "body {\n"+
		  "font-size:75%;"+
		  "color:#222;"+
		  "background:#fff;"+
		  "font-family:\"Helvetica Neue\", Arial, Helvetica, "+
		  "sans-serif;"+
		  "min-width: 950px;"+
		  "}\n");
	out.write("h1, h2, h3, h4, h5, h6 {font-weight:normal;color:#111;}\n"+
		  "h1 {font-size:3em;line-height:1;margin-bottom:0.5em;}\n"+
		  "h2 {font-size:2em;margin-bottom:0.75em;}\n"+
		  "h3 {font-size:1.5em;line-height:1;margin-bottom:1em;}\n"+
		  "h6 {font-size:1em;font-weight:bold;}\n");
	out.write(".container {\n"+
		  "margin-left: auto;\n"+
		  "margin-right: auto;\n"+
		  "margin:0 auto;\n"+
		  "width: 950px;\n"+
		  "}\n");
	out.write(".container .grid_S {\n"+
		  "width: 250px;\n"+
		  "}\n");
	/* large (L) grid, about 2/3 of page width */
	out.write(".container .grid_L {\n"+
		  "width: 500px;\n"+
		  "}\n");
	out.write(".grid_S,\n"+
		  ".grid_L {\n"+
		  "display: inline;\n"+
		  "float: left;\n"+
		  "margin-left: 10px;\n"+
		  "margin-right: 10px;\n"+
		  "position: relative;\n"+
		  "}\n");
	/* Alpha is used for the first grid-block in a row, and
	   Omega is used for the last grid-block in a row */
	out.write(".alpha {\n"+
		  "margin-left: 0;\n"+
		  "}\n"+
		  ".omega {\n"+
		  "margin-right: 0;\n"+
		  "}\n");
	out.write(".omega span{position:absolute;bottom:1;right:1;}\n");
	out.write(".clearfix:after, .container:after {content:\"\0020\"\n"+
		  ";display:block;height:0;clear:both;visibility:hidden;overflow:hidden;}\n"+
		  ".clearfix, .container {display:block;}\n"+
		  ".clear {clear:both;}\n"+
		  ".code {\n"+
		  "border-style: dashed;\n"+
		  "border-width: 1px;\n"+
		  "padding:10px;\n"+
		  "background-color: #D6D6D6;\n"+
		  "font-family: monospace;\n"+
		  "font-size: 1em;\n"+
		  "margin: 10px; \n"+
		  "}\n");
	out.write(".boxcontainer\n"+
		  "{\n"+
		  "border-top: 0px solid #666;\n"+
		  "position: relative;\n"+
		  "padding: 5px 0;\n"+
		  "}\n");
	out.write(".colborder {padding-right:24px;margin-right:25px;border-right:1px solid #ddd;}\n"+
		  "hr.space {background:#fff;color:#fff;visibility:hidden;}\n"+
		  ".alt{\n"+
		  "color: #333\n"+
		  "font-family: \"Century Schoolbook\", Georgia, Times, serif;\n"+
		  "font-style: italic;\n"+
		  "font-weight: normal;\n"+
		  "margin: .2em 0 .4em 0;\n"+
		  "letter-spacing: -2px;\n"+
		  "}\n");
	/* The following is the box with rounded corners. */
	out.write(".box\n"+
		  "{\n"+
		  "background-color: #FFFFFF;\n"+
		  "border: 1px solid #666;\n"+
		  "border-radius: 5px;\n"+
		  "box-shadow: 5px 5px 5px #ccc;\n"+
		  "moz-border-radius: 5px;\n"+
		  "moz-box-shadow: 5px 5px 5px #ccc;\n"+
		  "padding: 29px;\n"+
		  "webkit-border-radius: 5px;\n"+
		  "webkit-box-shadow: 5px 5px 5px #ccc;\n"+
		  "}\n");
	/* The following is for the pedigree table */
	out.write("table.pedigree {\n"+
		  "margin-left: auto;\n"+
		  "margin-right: auto;\n"+
		  "width: 60%;\n"+
		  "text-align:left;\n"+ 
		  "border-collapse: collapse;\n"+
		  "border-width: 0px;\n"+
		  "border-spacing: 2px;\n"+
		  "border-style: solid;\n"+
		  "border-color: black;\n"+
		  "background-color: white\n"+
		  "}\n");
	/* The following makes tool tips */
	out.write("span{\n"+
		  "background: none repeat scroll 0 0 #F8F8F8;\n"+
		  "border: 5px solid #ffce38;\n"+
		  "background:#ffce38; \n"+
		  "color: black;\n"+
		  "font-size: 12px;\n"+
		  "height: 100%;\n"+
		  "letter-spacing: 1px;\n"+
		  "line-height: 20px;\n"+
		  "margin: 0 auto;\n"+
		  "position: relative;\n"+
		  "text-align: center;\n"+
		  "display:none;\n"+
		  "padding:0 20px;\n"+
		  "}\n");
	out.write("p.h{\n"+
		  "margin:1px;\n"+
		  "float:left;\n"+
		  "position:relative;\n"+
		  "cursor:pointer;\n"+
		  "border-style: dotted;\n"+
		  "border-width: 1px;\n"+
		  "padding:10px;\n"+
		  "background-color: #E0E0E0;\n"+
		  "margin: 10px; \n"+
		  "}\n");
	out.write("p.h:hover span{\n"+
		  "display:block;\n"+
		  "left: 0; top:0;\n"+
		  "margin: 20px 0 0;\n"+
		  "}\n");
	/* A gradient hr separator  */ 
	out.write("hr.grad {\n"+
		  "border: 0;\n"+
		  "height: 1px;\n"+
		  "background: #333;\n"+ 
		  "background-image: -webkit-linear-gradient(left, #ccc, #333, #ccc);\n"+
		  "background-image: -moz-linear-gradient(left, #ccc, #333, #ccc);\n"+
		  "background-image: -ms-linear-gradient(left, #ccc, #333, #ccc);\n"+
		  "background-image: -o-linear-gradient(left, #ccc, #333, #ccc);\n"+
		  "}\n");
	/* The following causes really long strings (e.g., long deletions) to
	   break and thereby avoid messing up the table format */
	out.write("table {\n"+
		  "table-layout: fixed;\n"+
		  "width: 100%;\n"+
		  "}\n");
	out.write("table td, table th {\n"+
		  "word-wrap: break-word;\n"+   /* Internet Explorer 5.5+, Chrome, Firefox 3+ */
		  "overflow-wrap: break-word;\n"+ /* CSS3 standard: Chrome & Opera 2013+ */
		   "white-space: -moz-pre-wrap;\n"+ /* Firefox 1.0-2.0 */
		  "white-space: -pre-wrap;\n"+  /* Opera 4-6 */
		  "white-space: -o-pre-wrap;\n"+/* Opera 7 */
		  "white-space: pre-wrap;\n"+ /* CSS3 */
		  "}\n");
	out.write(".summary {\n"+
		  "align: left;\n"+
		  "vertical-align:top;\n"+
		  "padding: 10px;\n"+
		  "border:1px solid green;}\n");
	out.write(".summary thead tr   { background-color: #ccc; }\n");
	out.write(".summary td   { vertical-align:top; border-bottom: 1px solid #000;}\n");
	out.write(".summary ul li {margin-top: 2px;}\n");
	/* The following makes the glowing box around the MutationTaster2 link */
	out.write(".button {\n"+
		  "margin: .4em;\n"+
		  "padding: 1em;\n"+
		  "cursor: pointer;\n"+
		  "background: #e1e1e1;\n"+
		  "text-decoration: none;\n"+
		  "color: black;\n"+
		  "/* Prevent highlight colour when element is tapped */\n"+
		  "-webkit-tap-highlight-color: rgba(0, 0, 0, 0);\n"+
		  "}\n");
	out.write(".glow {\n"+
		  "display: inline-block;\n"+
		  "-webkit-transition-duration: 0.3s;\n"+
		  "transition-duration: 0.3s;\n"+
		  "-webkit-transition-property: box-shadow;\n"+
		  "transition-property: box-shadow;\n"+
		  "-webkit-transform: translateZ(0);\n"+
		  "-ms-transform: translateZ(0);\n"+
		  "transform: translateZ(0);\n"+
		  "box-shadow: 0 0 1px rgba(0, 0, 0, 0);\n"+
		  "}\n");
	out.write(".glow:hover {\n"+
		  "box-shadow: 0 0 8px rgba(152, 96, 35, 0.6);\n"+
		  "}\n");
	out.write(".glow1 {\n"+
		  "display: inline-block;\n"+
		  "-webkit-transition-duration: 0.3s;\n"+
		  "transition-duration: 0.3s;\n"+
		  "-webkit-transition-property: box-shadow;\n"+
		  "transition-property: box-shadow;\n"+
		  "-webkit-transform: translateZ(0);\n"+
		  "-ms-transform: translateZ(0);\n"+
		  "transform: translateZ(0);\n"+
		  "box-shadow: 0 0 1px rgba(0, 0, 0, 0);\n"+
		  "}\n");
	out.write(".glow1:hover {\n"+
		  "box-shadow: 0 0 8px rgba(28,134,238, 0.6);\n"+
		  "}\n");
	out.write(".glow2 {\n"+
		  "display: inline-block;\n"+
		  "-webkit-transition-duration: 0.3s;\n"+
		  "transition-duration: 0.3s;\n"+
		  "-webkit-transition-property: box-shadow;\n"+
		  "transition-property: box-shadow;\n"+
		  "-webkit-transform: translateZ(0);\n"+
		  "-ms-transform: translateZ(0);\n"+
		  "transform: translateZ(0);\n"+
		  "box-shadow: 0 0 1px rgba(0, 0, 0, 0);\n"+
		  "}\n");
	out.write(".glow2:hover {\n"+
		  "box-shadow: 0 0 8px rgba(84,255,159, 0.6);\n"+
		  "}\n");
	out.write("</style>\n");
    }


    


    /** 
     * This writes a paragraph with a list of HPO terms that the user submitted for prioritisation.
     * The list is shown in form of links to our website, e.g., 
     * http://www.human-phenotype-ontology.org/hpoweb/showterm?id=HP:0000118
     */
    private void writeHPOTerms() throws IOException {
	out.write("<p>Human Phenotype Ontology terms Used to Describe the Clinical Profile</p>\n");
	out.write("<ul>\n");
	Iterator<String> it = this.hpoLst.iterator();
	while(it.hasNext()) {
	    String h = it.next();
	    out.write("<li>" + h + "</li>\n");
	}
	out.write("</ul>\n");

    }

 

    /**
     * @param lst A listof HPO terms entered by the user for the phenotype-driven search.
     */
     public void addHPOList(List<String> lst) {
	 this.hpoLst = lst;
    }

  

  



      /**
     * Write the main output with the table of prioritized genes.
     * Note that this function outputs only the top
     * {@link exomizer.io.html.HTMLWriter#n_genes n_genes} genes.
     * @param pedigree An object representing the 1..n people in the pedigree
     * @param geneList List of genes, assumed to be sorted in prioritized order.
     */
    @Override public void writeHTMLBody(Pedigree pedigree, List<Gene> geneList) 
    	throws IOException
    {
	HTMLTable table = new HTMLTableCRE(pedigree);
	//table.writePedigreeTable(this.out);
	//this.out.write("<hr/>\n");
	int genecount = Math.min(this.n_genes,geneList.size());

	out.write("<div class=\"boxcontainer\">\n<article class=\"box\">\n");
	out.write("<div style=\"position: absolute; top: 10px; right: 10px;text-align:right;\">\n");
	java.util.Date dt = new java.util.Date();
	out.write("Analysis performed on " + dt.toString() );
	if (this.vcfBasename != null) {
	    out.write(" with " + this.vcfBasename);
	}
	out.write("</div>\n");
	writeHPOTerms();
	out.write("<a name=\"priority\">\n"+
		  "<h3>Top " + genecount + " ranked candidate genes</h3>\n");
	


	table.writeTableHeader(this.out);



	for (int k=0;k<genecount;++k) {
	    Gene g = geneList.get(k);
	    table.writeTableRow(g,this.out); // writeGeneTable(g,out, k+1); //
	}
	table.writeTableFooter(this.out);
	/* Close the boxcontainer div */
	out.write("</article>\n"+
		  "</div>\n");

    }


     /**
     * Print information on the filters chosen by the user
     * and a summary of the filtering results.
     * @param filterList List of the filters chosen by the user.
     * @param priorityList List of prioritizers chosen by the user.
     */
    @Override public void writeHTMLFilterSummary(List<Filter> filterList, 
				       List<Priority> priorityList) 
	throws IOException
    {
	// The following code gets information on the filters chosen by the use
	// and prints out a summary of the filtering results.
	HTMLFilterSummary filtersum = new HTMLFilterSummary();
	for (Filter f : filterList) {
	    FilterType fl = f.getFilterType();
	    // Get data for row in the filter table.
	    String name =  f.getFilterName();
	    List<String> descript = f.getMessages();
	    int before = f.getBefore();
	    int after  = f.getAfter();
	    filtersum.addRow(name,descript,before,after);
	}
	for (Priority p : priorityList) {
	    if (p.displayInHTML()) {
		String name =  p.getPriorityName();
		System.out.println("Calling for " + name);
		List<String> descript = p.getMessages();
		int before = p.getBefore();
		int after  = p.getAfter();
		filtersum.addRow(name,descript,before,after);
	    }
	}

	out.write("<div class=\"boxcontainer\">\n"+
		  "<article class=\"box\">\n"+
		  "<a name=\"filter\"/>\n"+
		  "<h2>Exome Filtering Results</h2>\n");
	filtersum.writeTableWithCSS(this.out);
	/* Close the boxcontainer div */
	out.write("</article>\n"+
		  "</div>\n");
    }


    



     /**
     * Output a Table with the distribution of VariantTypes.
     * @param vtc An object from the Jannovar library with counts of all variant types
     * @param sampleNames Names of the (multiple) samples in the VCF file
     */
     @Override  public void writeVariantDistributionTable(VariantTypeCounter vtc, List<String> sampleNames)
	throws ExomizerException 
    {
	try {
	    out.write("<div class=\"boxcontainer\">\n"+
		      "<article class=\"box\">\n"+
		      "<a name=\"distribution\">\n"+
		      "<h2>Distribution of variants</h2>\n");
	    //vtc.writeSummaryTable(sampleNames,out);
	    out.write("<table class=\"summary\">\n");
	    out.write("<thead><tr align=\"left\" style=\"border-bottom: 1px solid black;\">\n");
	    out.write("<th>Variant Type</th>");
	    int ncol = sampleNames.size();
	    for (int i=0;i<ncol;i++) {
		out.write(String.format("<th>%s</th>",sampleNames.get(i)));
	    }
	    out.write("</tr></thead>\n");
	    out.write("<tbody>\n");
	    // Now iterator over all of the variant types.
	    Iterator<VariantType> iter = vtc.getVariantTypeIterator();
	    while (iter.hasNext()) {
		VariantType vt = iter.next();
		ArrayList<Integer> cts = vtc.getTypeSpecificCounts(vt);
		String vtstr = vt.toDisplayString();
		out.write(String.format("<tr><td>%s</td>", vtstr));
		for (int k=0;k<ncol;++k) {
		    out.write(String.format("<td>%d</td>",cts.get(k)));
		}
		out.write("</tr>\n");
	    }
	    out.write("</tbody>\n</table><p>&nbsp;</p>\n");
	    out.write("</article>\n"+
		      "</div>\n");
	} catch (Exception e) {
	    String s = String.format("Error writing variant distribtion table: %s",e.getMessage());
	    throw new ExomizerException(s);
	}
    }



    /**
     * This prints a list of the version of the dataresources used for the website.
     */
    private void printVersionInfo() throws IOException
    {
	if (this.versionMap==null || this.versionMap.size()==0)
	    return;
	out.write("<h6>Version Information</h6>\n");
	out.write("<ul>\n");
	Iterator<String> it = this.versionMap.keySet().iterator();
	while (it.hasNext()) {
	    String k = it.next();
	    String v = this.versionMap.get(k);
	    if (k.equals("STRING"))
		continue;
	    out.write(String.format("<li>%s: %s</li>\n",k,v));
	}
	out.write("</ul>\n");
    }


    
    /**
     * Writes the final paragraph right before the footer of the Exomizer output page
     */
    @Override public void writeAbout() throws IOException
    {
	out.write("<h2><a name=\"about\">About</a></h2>\n");
	out.write("<p>PhenIX is a Java program that functionally annotates variants from whole-exome " +
		  "sequencing data starting from a VCF file (version 4). The functional annotation code is " +
		  "based on <a href=\"https://github.com/charite/jannovar/\">Jannovar</a> and uses " +
		  "<a href=\"http://genome.ucsc.edu/\">UCSC</a> KnownGene transcript definitions and "+
		  "hg19 genomic coordinates</p>\n");
	out.write("<p>Variants are prioritized according to variant frequency, pathogenicity,"+
		  " quality, inheritance pattern. Predicted pathogenicity data "+
		  " was extracted from the <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/21520341\">dbNSFP</a> resource.</p>");
	out.write("<p>Following variant analysis, the candidate genes are ranked according to "+
		  "their phenotypic similarity with Mendelian diseases with known disease genes. "+
		  "Therefore, PhenIX is intended to identify " +
		  "mutations in known disease genes that can be clinically interpreted. It "+
		  "is not intended for exome sequencing projects that aim to identify "+
		  "novel disease genes.</p>\n");
	printVersionInfo();
	out.write("<p>Developed by the Computational Biology and Bioinformatics group at the " +
		  "<a href=\"http://genetik.charite.de/\">Institute for Medical Genetics and Human Genetics</a> of the "+
		  "<a href=\"www.charite.de\">Charit&eacute; - Universit&auml;tsmedizin Berlin</a>"+
		  " and the Mouse " +
		  " Informatics Group at the <a href=\"http://www.sanger.ac.uk/\">Sanger Institute</a>.</p>\n");

    }



}
/* eof */
