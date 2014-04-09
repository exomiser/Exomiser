package de.charite.compbio.exomiser.io.html;

import java.io.Writer;
import java.io.IOException; 
import java.util.List;
import java.util.Iterator;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
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
 * @version 0.21 (22 November, 2013)
 */
public class HTMLWriterBOQA extends HTMLWriter {

    /** 
     * Set the file handle for writing 
     */
    public HTMLWriterBOQA(Writer writer)  {
	super(writer);
    }
    
    /**
     * Create a BufferedWriter from the file pointed to by fname
     * @param fname Name and path of file to be written to.
     */
    public HTMLWriterBOQA(String fname) throws ExomizerInitializationException {
	super(fname);
    }

     /**
     * This function outputs a  header with CSS
     */
    @Override public void writeHTMLHeaderAndCSS() throws IOException {
	this.out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Strict//EN\">\n" +
		  "<html>\n" +
		  "<head>\n" + 
		  "<title>The Exomizer - A Tool to Annotate and Prioritize Whole-Exome Sequencing Data</title>\n"+
		  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n");
	writeCSS(); /* this function is in superclass.*/
	this.out.write("</head>\n<body>");
    }


      /**
     * This function is designed to be used together with the apache tomcat server for the ExomeWalker.
     * It assumes that there is a CSS file located at css/walker.css, and additionally writes out the
     * title of the HTML page and some metadata. Note that after this function is called,
     * other functions may be called to add JavaScript or CSS, and then 
     * the function {@link #closeHeader} must be called to write the closing tags for
     * head and the opening tag for body.
     */

    public void writeWalkerHeader() throws IOException {
	out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Strict//EN\">\n" +
		  "<html>\n" +
		  "<head>\n" + 
		  "<title>Exome Walker - Random Walk Prioritization of  Whole-Exome Sequencing Data</title>\n"+
		  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n"+
		  "<meta content=\"Charit&eacute; Universit&auml;tsmedizin Berlin\" name=\"description\" />\n"+
		  "<meta content=\"CBB Web Team\" name=\"author\" />\n"+
		  "<meta content=\"en-GB\" http-equiv=\"Content-Language\" />\n"+
		  "<meta content=\"_top\" http-equiv=\"Window-target\" />\n"+
		  "<meta content=\"http://www.unspam.com/noemailcollection/\" name=\"no-email-collection\" />\n"+
		  "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/walker.css\" media=\"screen\" />\n");
    }


     /** 
     * write the closing tags for the HTML header for ExomeWalker.
     * Note that this function also writes the opening "<body>" tag, which 
     * calls the {@code toggle_visibility('tbl1','lnk1')} function to 
     * initialize the toggled tables with the gene data.
     */
    @Override public void closeHeader() throws IOException {
	out.write("</head>\n<body onload=\"toggle_visibility('tbl1','lnk1')\">\n");
    }


     /**
     * This function writes the Javascript function that is responsible for toggling the tables
     * with data about the individual candidate genes.
     */
    public  void writeWalkerJavaScript() throws IOException {
	out.write("<script type=\"text/javascript\">\n"+
		  "function toggle_visibility(tbid,lnkid)\n"+
		  "{\n"+
		  " var obj = document.getElementsByTagName(\"table\");\n"+
		  " for(i=0;i<obj.length;i++)\n"+
		  " {\n"+
		  "  if(obj[i].id && obj[i].id != tbid)\n"+
		  "  {\n"+
		  "   document.getElementById(obj[i].id).style.display = \"yes\";\n"+
		  "   x = obj[i].id.substring(3);\n"+
		  "   document.getElementById(\"lnk\"+x).value = \"[+]\";\n"+
		  "  }\n"+
		  " }\n"+
		  " if(document.all)\n"+
		  " {\n"+
		  "  document.getElementById(tbid).style.display = "+
		  "   document.getElementById(tbid).style.display == \"block\" ? \"none\" : \"block\";}\n"+
		  " else{\n"+
		  "   document.getElementById(tbid).style.display = "+
		  "  document.getElementById(tbid).style.display == \"table\" ? \"none\" : \"table\";\n"+
		  "}\n"+
		  " document.getElementById(lnkid).value = document.getElementById(lnkid).value == \"[-]\" ? \"[+]\" : \"[-]\";\n"+
		  "}\n"+
		  "</script>\n");
    }


      /**
     * Write the main output with the table of prioritized genes.
     * @param pedigree An object representing the 1..n people in the pedigree
     * @param geneList List of genes, assumed to be sorted in prioritized order.
     */
    @Override public void writeHTMLBody(Pedigree pedigree, List<Gene> geneList) 
    	throws IOException
    {
	HTMLTable table = new HTMLTable(pedigree);
	
	table.writePedigreeTable(this.out);
	this.out.write("<hr/>\n");
	table.writeTableHeader(this.out);
	int n=0;
	Iterator<Gene> it = geneList.iterator();
	while (it.hasNext()) {
	    Gene g = it.next();
	    n++; 
	    table.writeGeneTable(g,this.out,n);
	}
	table.writeTableFooter(this.out);
    }

}