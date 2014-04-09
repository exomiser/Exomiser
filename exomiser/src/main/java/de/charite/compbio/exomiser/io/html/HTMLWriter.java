package de.charite.compbio.exomiser.io.html;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException; 
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.filter.IFilter;
import de.charite.compbio.exomiser.priority.IPriority;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.reference.Network;

import jannovar.common.VariantType;
import jannovar.exome.VariantTypeCounter;
import jannovar.pedigree.Pedigree;

/**
 * This class is responsible for creating the framework of the
 * HTML page that is created by the Exomiser for each VCF analysis.
 * It writes out the CSS elements and the constant parts of the HTML
 * page.
 * <P>
 * The class writes to a {@code java.io.Writer} so that we can use either
 * a BufferedWriter or a StringWriter. This allows the class to be used either by
 * the cmmand-line version of the Exomiser or by apache tomcat versions that pass in 
 * an open file handle.
 * @author Peter Robinson
 * @version 0.37 (18 February, 2014)
 */
public class HTMLWriter {
    /** File handle to write output. */
    protected Writer out=null;
    /** maximum number of genes that will be shown (default 100). This parameter can be set to 10,20,50,All on
     * the PhenIX and ExomeWalker servers. */
    protected int n_genes=25000;
    /** The name of the VCF file used for the analysis. */
    protected String vcfBasename=null;
    /** 
     * Some of the prioritization methods start from seed genes. They are lisated here.
     * For example, ExomeWalker starts off with Entrez Gene genes to do the random
     * walk analysis.
     */
    protected List<String> seedGeneList=null;
    /**
     * This object can display network-type information about genes. It can be
     * used to represent relationships between candidate genes (e.g., STRING interactions).
     */
    protected Network network=null;
    /** List of Human Gene Mutation Database hits (provided by Exomizer class) */
    private List<String> hgmdLst=null;
    /**
     * The disease gene family that is the basis of the genes in the
     * list of seed genes (see {@link #seedGeneList}).
     */
    protected String diseaseGeneFamilyName=null;
    /**
     * A map that will contain the version information about the resources used
     * to build the database, e.g., "dbSNP" (key) "version 2" (value).
     */
    protected Map<String,String> versionMap=null;
    /**
     * @param lst A list of HGMD entries (mutations) that have been found in the database.
     */
    public void addHGMDHits(List<String> lst) {
	this.hgmdLst = lst;
    }

    /** 
     * @param writer A file handle or stringwriter (for the web server).
     */
    public HTMLWriter(Writer writer)  {
	this.out=writer;
    }
    /** 
     * @param writer A file handle or stringwriter (for the web server).
     * @param basename The basename of the VCF file.
     */
    public HTMLWriter(Writer writer, String basename) {
	this(writer);
	this.vcfBasename = basename;
    }

    /**
     * This constructor opens a new file handle for writing.
     * @param fname The name of the output file that will be created
     */
    public HTMLWriter(String fname) throws ExomizerInitializationException {
	try{
	    FileWriter fstream = new FileWriter(fname);
	    this.out = new BufferedWriter(fstream);
	}  catch (IOException e){
	    String s = String.format("Error initializing HTMLWriter: %s", e.getMessage());
	    throw new ExomizerInitializationException(s);
	}
    }
    
    /**
     * This constructor opens a new file handle for writing.
     * @param fname The name of the output file that will be created
     * @param basename The basename of the VCF file.
     */
    public HTMLWriter(String fname, String basename) throws ExomizerInitializationException {
	this(fname);
	this.vcfBasename = basename;
    }
    /**
     * Add a list of HPO terms used to prioritze genes. Intended to
     * allow the terms to be displayed on the HTML page, this function 
     * does not actually do analysis. Note that the method needs to 
     * be implemented by subclasses, here it is a no-op.*/
    public void addHPOList(List<String> lst) {
	/*  no-op */
    }

    /**
     * @param mp A map with key: resource name, value: version
     */
    public void addVersionInfo(Map<String,String> mp) {
	this.versionMap = mp;
    }

    /**
     * @param net A network of interactions between seed and candidate genes.
     */
    public void setNetwork(Network net) {
	this.network = net;
    }
    
    /**
     * Output a Table with the distribution of VariantTypes.
     * @param vtc An object from the Jannovar library with counts of all variant types
     * @param sampleNames Names of the (multiple) samples in the VCF file
     */
    public void writeVariantDistributionTable(VariantTypeCounter vtc, List<String> sampleNames)
	throws ExomizerException 
    {
	try {
	    out.write("<table>\n");
	    out.write("<thead><tr align=\"left\">\n");
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
		List<Integer> cts = vtc.getTypeSpecificCounts(vt);
		String vtstr = vt.toDisplayString();
		out.write(String.format("<tr><td>%s</td>", vtstr));
		for (int k=0;k<ncol;++k) {
		    out.write(String.format("<td>%d</td>",cts.get(k)));
		}
		out.write("</tr>\n");
	    }
	    out.write("</tbody>\n</table><p>&nbsp;</p>\n");
	} catch (Exception e) {
	    String s = String.format("Error writing variant distribtion table: %s",e.getMessage());
	    throw new ExomizerException(s);
	}
    }

    /**
     * Output a Table with the distribution of VariantTypes.
     * @param vtc An object from the Jannovar library with counts of all variant types
     * @param sampleName Name of the (single) sample in the VCF file
     */
    public void writeVariantDistributionTable(VariantTypeCounter vtc, String sampleName) 
	throws ExomizerException
    {
	List<String> lst = new ArrayList<String>();
	lst.add(sampleName);
	writeVariantDistributionTable(vtc,lst);
    }

    /**
     * Output a Table with the distribution of VariantTypes.
     * @param vtc An object from the Jannovar library with counts of all variant types
     * @param sampleNames Names of the (multiple) samples in the VCF file
     */
    public String writeSangerVariantDistributionTable(VariantTypeCounter vtc, List<String> sampleNames)
            throws ExomizerException
    {
        try {
	    StringBuffer row = new StringBuffer();
            row.append("<table class=\"zebra-table\">\n");
            row.append("<thead><tr>\n");
            row.append("<th>Variant Type</th>");
            int ncol = sampleNames.size();
            for (int i=0;i<ncol;i++) {
                row.append(String.format("<th>%s</th>",sampleNames.get(i)));
            }
            row.append("</tr></thead>\n");
            row.append("<tbody>\n");
            // Now iterator over all of the variant types.                                                                                               
            Iterator<VariantType> iter = vtc.getVariantTypeIterator();
            while (iter.hasNext()) {
                VariantType vt = iter.next();
                List<Integer> cts = vtc.getTypeSpecificCounts(vt);
                String vtstr = vt.toDisplayString();
                row.append(String.format("<tr><td>%s</td>", vtstr));
                for (int k=0;k<ncol;++k) {
                    row.append(String.format("<td>%d</td>",cts.get(k)));
                }
                row.append("</tr>\n");
            }
            row.append("</tbody>\n</table><p>&nbsp;</p>\n");
	    return row.toString();
        } catch (Exception e) {
            String s = String.format("Error writing variant distribtion table: %s",e.getMessage());
            throw new ExomizerException(s);
        }
    }

    /**                
     * Output a Table with the distribution of VariantTypes.
     * @param vtc An object from the Jannovar library with counts of all variant types
     * @param sampleName Name of the (single) sample in the VCF file
     */
    public String writeSangerVariantDistributionTable(VariantTypeCounter vtc, String sampleName)
        throws ExomizerException
    {
        List<String> lst = new ArrayList<String>();
        lst.add(sampleName);
        return writeSangerVariantDistributionTable(vtc,lst);
    }


     /**
     * Output a Table with the distribution of VariantTypes.
     */
    public void writeVariantDistributionTable(VariantTypeCounter vtc) 
	throws ExomizerException
    {
	List<String> lst = new ArrayList<String>();
	lst.add("?name");
	writeVariantDistributionTable(vtc,lst);
    }


    /**
     * Close the file handle
     */
    public void finish() {
	try {
	    this.out.close();
	} catch (IOException e) {
	    System.err.println("[ERROR] Problem encountered while closing HTML file handle: " + e.getMessage());
	}
    }


    /** For some types of output, we need to know the numbers of genes to decide
     * how to create the output.
     * @param n number of genes that have been prioritized.
     */
    public void setNumberOfGenes(int n) {
	this.n_genes = n;
    }

    /**
     * This function outputs an HTML header together with CSS code and a top-menu with links to
     * the divers sections of the page.
     */
    public void writeHTMLHeaderAndCSS() throws IOException {
	openHeader();
	writeCSS();
	closeHeader();
	writeTopMenu();
    }

    /** Write the very beginning of the HTML file, with the DOCTYPE
     * declaration and the title. After this segment, if desired, one should
     * write CSS using the function {@link #writeCSS} in this class
     * or another function (e.g., from a tomcat server).
     */
    public void openHeader() throws IOException {
	out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Strict//EN\">\n" +
		  "<html>\n" +
		  "<head>\n" + 
		  "<title>The Exomizer - A Tool to Annotate and Prioritize Whole-Exome Sequencing Data</title>\n"+
		  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n");
    }

    /**
     * Write the closing tags for the HTML head and write the opening tag for the
     * HTML body.
     */
    public void closeHeader() throws IOException {
	out.write("</head>\n<body>");
    }

    /**
     * Output the cascading style sheet (CSS) code for the HTML webpage
     */
    protected void writeCSS() throws IOException {
	out.write("<style type=\"text/css\">\n");

	out.write("body {\n"+  
		  "font-family: sans-serif;   \n"+ 
		  "color: #000000;    \n"+
		  "background-color: #FFFFFF;  \n"+
		  "margin: 0; \n"+
		 "padding: 0; \n"+
		  "} \n");
	out.write("h1 {  \n"+
		  "color: #336699; \n"+
		  "} \n"+
		  "h2,h3,h4 {  \n"+
		  "text-align: left; \n"+
		  "}\n"+
		  "h2 { \n"+
		  "color: #336699;\n"+
		  "}\n");
	out.write("a {\n"+
		  "color: #336699;\n"+
		  "}\n");
	out.write("p { \n"+
		  "text-align: justify;\n"+
		  "line-height: 120%;\n"+
		  "padding-left: 2em;\n"+
		  "}\n"+
		  "p.small { \n"+
		  "font-size: 80%;\n"+
		  "}\n");

	out.write("table.summary { margin-left: auto;margin-right: auto;width: 80%;text-align:left;}\n"+
		  ".summary th {text-align: center; background-color: #336699;color: #FFFFFF;padding: 0.4em;font-size: 100%}\n"+
		  ".summary td{ text-align: left;background-color: #DDDDDD;color: #000000;padding: 0.4em;font-size: 75%}\n");


	out.write("table.priority {margin-left: auto;margin-right: auto;width: 90%;text-align:left; "+
		  "border-collapse: collapse; border-width: 2px; border-spacing: 2px; border-style: solid;"+
		  "border-color: black; background-color: white}\n"+
		  ".priority th {padding: 0.4em; border-width: 1px;padding: 1px;border-style: inset;"+
		  "border-color: gray;background-color: white;-moz-border-radius: ; }\n"+
		  ".priority td {color:black;background-color:#FFFFFF; border-width: 1px;word-break:break-all;"+
		  "padding: 1px;border-style: inset;border-color: gray;-moz-border-radius: ;}\n"+
		  ".priority td.g {border:2px dotted ##0070C0;padding 10px;}\n"+
		  ".priority tr.d0 td { background-color: #FFFFFF; color: black;}\n"+
		  ".priority tr.d1 td {	background-color: #DDDDDD; color: black;}\n");

	out.write("table#qy {margin-left: auto;margin-right: auto;text-align:left; font-size:75%;"+
		  "border-collapse: collapse; border-width: 1px; border-spacing: 1px; border-style: solid;"+
		  "border-color: black; background-color: white}\n");

	/* Style ztable that is used for the pedigree and other purposes */
	out.write("table.ztable {"+
		  "border: 1px solid #DFDFDF;"+
		  //"background-color: #F9F9F9;"+
		  "margin-left: auto;margin-right: auto;"+
		  "width: 80%;"+
		  "text-align: left;"+
		  "-moz-border-radius: 3px;"+
		  "-webkit-border-radius: 3px;"+
		  "border-radius: 3px;"+
		  "font-family: Arial,\"Bitstream Vera Sans\",Helvetica,Verdana,sans-serif;"+
		  "color: #333;"+
		  "}\n");
	out.write("table.ztable td,  th {"+
		  "border-top-color: white;"+
		  "border-bottom: 1px solid #DFDFDF;"+
		  "color: #555;"+
		  "}\n");
	out.write("table.ztable th {"+
		  "text-shadow: rgba(255, 255, 255, 0.796875) 0px 1px 0px;"+
		  "font-family: Georgia,\"Times New Roman\",\"Bitstream Charter\",Times,serif;"+
		  "font-weight: bold;"+
		  "padding: 7px 7px 8px;"+
		  "text-align: left;"+
		  "line-height: 1.3em;"+
		  "font-size: 14px;"+
		  "}\n");
	out.write("table.ztable td {"+
		  "font-size: 12px;"+
		  "padding: 4px 7px 2px;"+
		  "vertical-align: top;"+
		  "}\n");
	/* classes to make table cells a certain color */
	out.write("td.w { background-color: #FFFFFF; }\n"); /* white, for unaffected */
	out.write("td.lb { background-color: #A9F5F2; }\n"); /* light blue, for unaffected parent */
	out.write("td.b { background-color: #424242; color: red; }\n"); /* black background, white text color */
	out.write("div.w { background-color: #FFFFFF; display: inline-block; }\n"); /* white, for unaffected */
	out.write("div.lb { background-color: #A9F5F2;display: inline-block; }\n"); /* light blue, for unaffected parent */
	out.write("div.b { background-color: #424242; color: red;display: inline-block; }\n"); /* black background, white text color */

	/* Style for the header */
	out.write("#header { \n"+
		  "border-bottom: 1px;\n"+
		  "margin-bottom: 0;\n"+
		  "height: 100px;\n"+
		  "padding: 0;\n"+
		  "margin-top: 0;\n"+
		  "background-color: #336699;\n"+
		  "color: #FFFFFF;\n"+
		  "width: 100%;\n"+
		  "font-size: 250%;\n"+
		  "text-align: center;\n"+
		  "padding: 0.3em;\n"+
		  "}\n");

	out.write("#topmenu { \n"+
		  "margin-left: auto;\n"+
		  "margin-right: auto;\n"+
		  "background-color: #DDDDDD;\n"+
		  "color: #336699;\n"+
		  "width: 100%;\n"+
		  "font-size: 120%;\n"+
		  "text-align: center;\n"+
		  "padding: 0.3em;\n"+
		  "}\n");
	out.write("#topmenu a { \n"+
		  "text-decoration: none;\n"+
		  "padding: 0.3em;\n"+
		  "}\n");
	out.write("#topmenu a:hover { \n"+
		  "background-color: #336699;\n"+
		  "color: #DDDDDD;\n"+
		  "}\n");
	out.write("#main { \n"+
		  "padding: 0px; \n"+
		  "margin-left: 20px; \n"+
		  "border-left: 1px; \n"+
		  "margin-right: 20px; \n"+
		  "border-bottom: 10px; \n"+
		  "} \n");

	out.write("hr {"+
		  "border: 0;"+
		  "width: 80%;"+
		  "color: #f00;"+
		  "background-color: #ADD8E6;"+
		  "height: 5px;"+
		  "}\n");

	out.write("#footer { \n"+
		   "border-bottom: 1px;\n"+
		  "border-top: 1px; \n"+
		  "margin-bottom: 0;\n"+
		  "height: 20px;\n"+
		  "padding: 0;\n"+
		  "margin-top: 0;\n"+
		  "background-color: #336699;\n"+
		  "color: #FFFFFF;\n"+
		  "width: 100%;\n"+
		  "font-size: 100%;\n"+
		  "text-align: center;\n"+
		  "padding: 0.3em;\n"+
		  "} \n");

	out.write("#footer p { \n"+
		  "color: white; \n"+
		  "font-size: 85%; \n"+
		  "border: 0; \n"+
		  "margin: 0; \n"+
		  "padding: 0.2em; \n"+
		  "padding-left: 2em; \n"+
		  "} \n");

	out.write("#footer a {  \n"+
		  "color: white; \n"+
		  "} \n");

	out.write("</style>\n");

    }



    /**
     * This function writes out the title, the top menu, and then it opens the div element for main that
     * will be closed by the function {@link #writeHTMLFooter}.
     */
    public void  writeTopMenu() throws IOException
    {
	out.write("<div id=\"header\">\nThe Exomizer: Annotate and Filter Variants\n</div>\n");
	out.write("<div id=\"topmenu\">\n");
	out.write("<a href=\"#Filtering\">Filtering summary</a> |\n" +
		  "<a href=\"#Distribution\">Variant type distribution</a> |\n"+
		  "<a href=\"#Prioritization\">Prioritized Variant List</a> |\n"+
		  "<a href=\"#About\">About</a>\n");
	out.write("</div>\n");
	out.write("<div id=\"main\">\n");

    }
    
    /**
     * Writes the final paragraph right before the footer of the Exomizer output page
     */
    public void writeAbout() throws IOException
    {
	out.write("<h2><a name=\"About\">About</a></h2>\n");
	out.write("<p>The Exomizer is a Java program that functionally annotates variants from whole-exome " +
		  "sequencing data starting from a VCF file (version 4). The functional annotation code is " +
		  "based on <a href=\"https://github.com/charite/jannovar/\">Jannovar</a> and uses " +
		  "<a href=\"http://genome.ucsc.edu/\">UCSC</a> KnownGene transcript definitions and "+
		  "hg19 genomic coordinates</p>\n");
	out.write("<p>Variants are prioritized according to user-defined criteria on variant frequency, pathogenicity,"+
		  " quality, inheritance pattern, and model organism phenotype data. Predicted pathogenicity data "+
		  " was extracted from the <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/21520341\">dbNSFP</a> resource.");
	out.write("<P>Developed by the Computational Biology and Bioinformatics group at the " +
		  "<a href=\"http://genetik.charite.de/\">Institute for Medical Genetics and Human Genetics</a> of the "+
		  "<a href=\"www.charite.de\">Charit&eacute; - Universit&auml;tsmedizin Berlin</a> and the Mouse " +
		  " Informatics Group at the <a href=\"http://www.sanger.ac.uk/\">Sanger Institute</a>.</P>\n");

    }

    /**
     * @param msg A list of error and status messages.
     */
    public void writeStatusMessage(List<String> msg)  throws IOException
    {
	out.write("<ul>\n");
	for (String s: msg) {
	    out.write(String.format("<li>%s</li>\n",s));
	}
	out.write("</ul>\n");
    }


    /**
     * This should be the last function called to write the Exomizer HTML page.
     * It first closes the div section for the MAIN part of the HTML page and then
     * writes out a footer bar, and closes the HTML element.
     */
    public void writeHTMLFooter() throws IOException
    {
	out.write("</div>\n<!-- END_MAIN -->\n");
	out.write("<div id=\"footer\">\n" +
		  "<p>Problems, suggestions, or comments? " +
		  "Please <a href=\"mailto:peter.robinson@charite.de\">let us know</a></p>\n"+
		  "</div>\n"+
		  "</body>\n"+
		  "</html>\n");
    }


    /**
     * Print information on the filters chosen by the user
     * and a summary of the filtering results.
     * @param filterList List of the filters chosen by the user.
     * @param priorityList List of prioritizers chosen by the user.
     */
    public void writeHTMLFilterSummary(List<IFilter> filterList, 
				       List<IPriority> priorityList) 
	throws IOException
    {
	// The following code gets information on the filters chosen by the use
	// and prints out a summary of the filtering results.
	HTMLFilterSummary filtersum = new HTMLFilterSummary();
	for (IFilter f : filterList) {
	    FilterType fl = f.getFilterTypeConstant();
	    // Get data for row in the filter table.
	    String name =  f.getFilterName();
	    List<String> descript = f.getMessages();
	    int before = f.getBefore();
	    int after  = f.getAfter();
	    if (descript.size()==1) {
		filtersum.addRow(name, descript.get(0),before,after);
	    } else {
		filtersum.addRow(name,descript,before,after);
	    }
	}
	
	/* Similar as above, but for the prioritizers. */
		/* Similar as above, but for the prioritizers. */
	for (IPriority p : priorityList) {
	    if (p.display_in_HTML()) {
		String name =  p.getPriorityName();
		String h = p.getHTMLCode();
		int before = p.getBefore();
		int after  = p.getAfter();
		filtersum.addRow(name,h,before,after);
	    }
	}
	
	filtersum.writeTable(this.out);
    }


    /**
     * Write the main output with the table of prioritized genes.
     * @param pedigree An object representing the 1..n people in the pedigree
     * @param geneList List of genes, assumed to be sorted in prioritized order.
     */
    public void writeHTMLBody(Pedigree pedigree, List<Gene> geneList) 
    	throws IOException
    {
	HTMLTable table = new HTMLTable(pedigree);
	table.writePedigreeTable(this.out);
	this.out.write("<hr/>\n");
	table.writeTableHeader(this.out);
	Iterator<Gene> it = geneList.iterator();
	while (it.hasNext()) {
	    Gene g = it.next();
	    table.writeTableRow(g,this.out);
	}
	table.writeTableFooter(this.out);
    }


       /**
     * Writes a textbox with the results of the search for variants that
     * are contained in the HGMD Pro database.
     */
    public void writeHGMDBox() throws IOException {
	out.write("<div class=\"boxcontainer\">\n"+
		  "<article class=\"box\">\n"+
		  "<a name=\"hgmd\">\n"+
		  "<h3>Human Gene Mutation Database</h3>\n");
	if (this.hgmdLst.size()>0) {
	    out.write("<ul>\n");
	    Iterator<String> it = this.hgmdLst.iterator();
	    while(it.hasNext()) {
		String h = it.next();
		out.write("<li>" + h + "</li>\n");
	    }
	    out.write("</ul>\n");
	    out.write("<p style=\"font-size:60%;\">All HGMD mutations are shown that affect a nucleotide with a mutation in the VCF file."+
		      " Users should note that occasionally a mutation in this list may have a different mutated "+
		      " nucleotide than the variant in the VCF file. Such mutations are shown here on the "+
		      " assumption that it may be useful to know, but as always caution and professional experience are "+
		      " required for their interpretation.</p>");
	} else {
	    out.write("<p>No published mutations identified in HGMD</p>");
	}
	out.write("</article>\n"+
		  "</div>\n");

    }


    public void writeVersionInfo(String... itemlist) {

    }


    public void setSeedGeneURLs(List<String> urls) {
	this.seedGeneList=urls;
    }

    public void setDiseaseGeneFamilyName(String name) {
	this.diseaseGeneFamilyName=name;
    }




}
/* eof */
