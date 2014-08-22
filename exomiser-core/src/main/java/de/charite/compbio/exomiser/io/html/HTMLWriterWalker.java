package de.charite.compbio.exomiser.io.html;

import de.charite.compbio.exomiser.core.filter.FilterReport;
import java.io.Writer;
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;

import jannovar.pedigree.Pedigree;
import jannovar.exome.VariantTypeCounter;
import jannovar.common.VariantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for creating the HTML page that is created by the
 * Exomiser for the ExomeWalker VCF analysis. It writes out the CSS elements and
 * the constant parts of the HTML page.
 * <P>
 * The class writes to a {@code java.io.Writer} so that we can use either a
 * BufferedWriter or a StringWriter. This allows the class to be used either by
 * the cmmand-line version of the Exomiser or by apache tomcat versions that
 * pass in an open file handle.
 * <p>
 * The class now offers a series of methods with "Walker" in their name that
 * offer toggle functionality that we are using for the ExomeWalker server.
 *
 * @author Peter Robinson
 * @version 0.29 (16 February, 2014)
 */
public class HTMLWriterWalker extends HTMLWriter {

    private final Logger logger = LoggerFactory.getLogger(HTMLWriterWalker.class);

    /**
     * Set the file handle for writing
     */
    public HTMLWriterWalker(Writer writer) {
        super(writer);
    }

    /**
     * Create a BufferedWriter from the file pointed to by fname
     *
     * @param fname Name and path of file to be written to.
     */
    public HTMLWriterWalker(String fname) {
        super(fname);
    }

    /**
     * This function is designed to be used together with the apache tomcat
     * server for the ExomeWalker. It assumes that there is a CSS file located
     * at css/walker.css, and additionally writes out the title of the HTML page
     * and some metadata. Note that after this function is called, other
     * functions may be called to add JavaScript or CSS, and then the function
     * {@link #closeHeader} must be called to write the closing tags for head
     * and the opening tag for body.
     */
    @Override
    public void writeHTMLHeaderAndCSS() throws IOException {
        out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Strict//EN\"\n"
                + "\"http://www.w3.org/TR/html4/strict.dtd\">\n"
                + "<html lang=\"en\">\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>Exome Walker - Random Walk Prioritization of  Whole-Exome Sequencing Data</title>\n"
                + "<meta charset=\"utf-8\" />"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n"
                + "<meta content=\"Charit&eacute; Universit&auml;tsmedizin Berlin\" name=\"description\" />\n"
                + "<meta content=\"CBB Web Team\" name=\"author\" />\n"
                + "<meta content=\"en-GB\" http-equiv=\"Content-Language\" />\n"
                + "<meta content=\"_top\" http-equiv=\"Window-target\" />\n"
                + "<meta content=\"http://www.unspam.com/noemailcollection/\" name=\"no-email-collection\" />\n"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/walker.css\" media=\"screen\" />\n");
        writeCSS();
        closeHeader(); // closes head and opens html body
        writeTopMenu();
    }

    /**
     * This function writes out the title, the top menu, and then it opens the
     * div element for main that will be closed by the function
     * {@link #writeHTMLFooter}.
     */
    public void writeTopMenu() throws IOException {
        out.write("<div class=\"container\">\n"
                + /* Div stack height 1 */ "<h2 class=\"alt\">\n"
                + "<img src=\"img/logo.png\" height=\"60\" alt=\"Exome Walker\"/>\n"
                + "</h2>\n"
                + "<h5>Prioritization of whole-exome data by random-walk analysis of protein-protein interactions</h5>\n"
                + "<hr>\n");
        out.write("<div class=\"grid_S alpha\">\n"
                +/* Div stack height 3 */ "<h6>Results</h6>\n"
                + "<p><ul>\n"
                + "<li><a href=\"#priority\" >Top ranked candidate genes.</a></li>   \n"
                + "<li><a href=\"#stats\" >Result statistics and quality control.</a></li>  \n"
                + "<li><a href=\"#distribution\" >Overall variant distribution.</a></li>   \n"
                + "<li><a href=\"#about\">About Exome Walker.</a></li>   \n"
                + "</ul><p>&nbsp;</p>\n"
                + "</div>\n");/* Div stack height 1 */

        out.write("<div class=\"grid_L omega colborder\">\n"
                +/* Div stack height 3 */ "<p><strong>EXOME WALKER</strong> is a computational method to prioritise a set of candidates "
                + "in exome sequencing projects that aim to identify novel Mendelian disease genes."
                + "Our approach involves filtering a "
                + "<a href=\"http://www.1000genomes.org/wiki/Analysis/vcf4.0\">Variant Call Format</a> (VCF) file "
                + "according to a number of user-definable criteria, for instance, off-target variants (those that "
                + "are not located within or close to protein-coding exons) are removed. Following this, genes "
                + "are prioritised according to a variant score (predicted pathogenicity, rarity, pattern of variants "
                + "compatible with the assumed mode of inheritance) and to their vicinity to other genes that belong "
                + "to the same disease-gene family within the protein protein interaction (PPI) network.</p>"
                + "</div>\n"
                + /* Div stack height 2 */ "</div>\n"
                +/* Div stack height 1 */ "<hr>\n"
                + "<hr class=\"space\">\n");
    }

    /**
     * Output the cascading style sheet (CSS) code for the HTML webpage
     */
    @Override
    public void writeCSS() throws IOException {
        out.write("<style type=\"text/css\">\n");
        out.write("html {font-size:100.01%;}\n"
                + "body {\n"
                + "font-size:80%;\n"
                + "line-height:130%;\n"
                + "color:black;\n"
                + "background:#FCFAF0;\n"
                + "font-family:\"Helvetica Neue\", Arial, Helvetica, sans-serif;\n"
                + "min-width: 950px;\n"
                + "}\n");
        out.write("h1, h2, h3, h4, h5, h6 {font-weight:normal;color:#111;}\n"
                + "h1 {font-size:3em;line-height:1;margin-bottom:0.5em;}\n"
                + "h2 {font-size:2em;margin-bottom:0.75em;}\n"
                + "h3 {font-size:1.5em;line-height:1;margin-bottom:1em;}\n"
                + "h6 {font-size:1em;font-weight:bold;}\n");
        out.write(".container {\n"
                + "margin-left: auto;\n"
                + "margin-right: auto;\n"
                + "margin:0 auto;\n"
                + "width: 950px;\n"
                + "}\n");
        out.write(".container .grid_S {\n"
                + "width: 300px;\n"
                + "}\n");
        /* large (L) grid, about 2/3 of page width */
        out.write(".container .grid_L {\n"
                + "width: 500px;\n"
                + "}\n");
        out.write(".grid_S,\n"
                + ".grid_L {\n"
                + "display: inline;\n"
                + "float: left;\n"
                + "margin-left: 10px;\n"
                + "margin-right: 10px;\n"
                + "}\n");
        /* Alpha is used for the first grid-block in a row, and
         Omega is used for the last grid-block in a row */
        out.write(".alpha {\n"
                + "margin-left: 0;\n"
                + "}\n"
                + ".omega {\n"
                + "margin-right: 0;\n"
                + "}\n");
        out.write(".clearfix:after, .container:after {content:\"\0020\"\n"
                + ";display:block;height:0;clear:both;visibility:hidden;overflow:hidden;}\n"
                + ".clearfix, .container {display:block;}\n"
                + ".clear {clear:both;}\n"
                + ".code {\n"
                + "border-style: dashed;\n"
                + "border-width: 1px;\n"
                + "padding:10px;\n"
                + "background-color: #D6D6D6;\n"
                + "font-family: monospace;\n"
                + "font-size: 1em;\n"
                + "margin: 10px; \n"
                + "}\n");
        out.write(".boxcontainer\n"
                + "{\n"
                + "border-top: 0px solid #666;\n"
                + "padding: 5px 0;\n"
                + "width: 90%;\n"
                + "margin-left:5%;\n"
                + "margin-right:5%\n"
                + "}\n");
        out.write(".colborder {padding-right:24px;margin-right:25px;border-right:1px solid #ddd;}\n"
                + "hr.space {background:#fff;color:#fff;visibility:hidden;}\n"
                + ".alt{\n"
                + "color: #333\n"
                + "font-family: \"Century Schoolbook\", Georgia, Times, serif;\n"
                + "font-style: italic;\n"
                + "font-weight: normal;\n"
                + "margin: .2em 0 .4em 0;\n"
                + "letter-spacing: -2px;\n"
                + "}\n");
        /* The following is the box with rounded corners. */
        out.write(".box\n"
                + "{\n"
                + "background-color: #FFFFFF;\n"
                + "border: 1px solid #666;\n"
                + "border-radius: 5px;\n"
                + "box-shadow: 5px 5px 5px #ccc;\n"
                + "moz-border-radius: 5px;\n"
                + "moz-box-shadow: 5px 5px 5px #ccc;\n"
                + "padding: 29px;\n"
                + "webkit-border-radius: 5px;\n"
                + "webkit-box-shadow: 5px 5px 5px #ccc;\n"
                + "}\n");
        /* the following makes the rounded boxes about the genes/variants */
        out.write("table {\n"
                + "*border-collapse: collapse; /* IE7 and lower */\n"
                + "border-spacing: 0;\n"
                + "width: 100%;\n"
                + "}\n");
        out.write(".bordered {\n"
                + "border: solid #ccc 1px;\n"
                + "-moz-border-radius: 6px;\n"
                + "-webkit-border-radius: 6px;\n"
                + "border-radius: 6px;\n"
                + "-webkit-box-shadow: 0 1px 1px #ccc;\n"
                + "-moz-box-shadow: 0 1px 1px #ccc; \n"
                + "box-shadow: 0 1px 1px #ccc; \n"
                + "}\n");
        out.write(".bordered tr:hover {\n"
                + "background: #fbf8e9;\n"
                + "-o-transition: all 0.1s ease-in-out;\n"
                + "-webkit-transition: all 0.1s ease-in-out;\n"
                + "-moz-transition: all 0.1s ease-in-out;\n"
                + "-ms-transition: all 0.1s ease-in-out;\n"
                + "transition: all 0.1s ease-in-out;\n"
                + "}\n");
        out.write(".bordered td, .bordered th {\n"
                + "border-left: 1px solid #ccc;\n"
                + "border-top: 1px solid #ccc;\n"
                + "padding: 10px;\n"
                + "text-align: left;\n"
                + "}\n");
        out.write(".bordered th {\n"
                + "background-color: #63AA9C;\n"
                + "background-image: -webkit-gradient(linear, left top, left bottom, from(#ebf3fc), to(#dce9f9));\n"
                + "background-image: -webkit-linear-gradient(top, #ebf3fc, #63AA9C);\n"
                + "background-image:    -moz-linear-gradient(top, #ebf3fc, #63AA9C);\n"
                + "background-image:     -ms-linear-gradient(top, #ebf3fc, #63AA9C);\n"
                + "background-image:      -o-linear-gradient(top, #ebf3fc, #63AA9C);\n"
                + "background-image:         linear-gradient(top, #ebf3fc, #63AA9C);\n"
                + "-webkit-box-shadow: 0 1px 0 rgba(255,255,255,.8) inset; \n"
                + "-moz-box-shadow:0 1px 0 rgba(255,255,255,.8) inset;  \n"
                + "box-shadow: 0 1px 0 rgba(255,255,255,.8) inset;\n"
                + "border-top: none;\n"
                + "text-shadow: 0 1px 0 rgba(255,255,255,.5); \n"
                + "}\n");
        out.write(".bordered td:first-child, .bordered th:first-child {\n"
                + "border-left: none;\n"
                + "}\n");
        out.write(".bordered th:first-child {\n"
                + "-moz-border-radius: 6px 0 0 0;\n"
                + "-webkit-border-radius: 6px 0 0 0;\n"
                + "border-radius: 6px 0 0 0;\n"
                + "}\n");
        out.write(".bordered th:last-child {\n"
                + "-moz-border-radius: 0 6px 0 0;\n"
                + "-webkit-border-radius: 0 6px 0 0;\n"
                + "border-radius: 0 6px 0 0;\n"
                + "}\n");
        out.write(".bordered th:only-child{\n"
                + "-moz-border-radius: 6px 6px 0 0;\n"
                + "-webkit-border-radius: 6px 6px 0 0;\n"
                + "border-radius: 6px 6px 0 0;\n"
                + "}\n");
        out.write(".bordered tr:last-child td:first-child {\n"
                + "-moz-border-radius: 0 0 0 6px;\n"
                + "-webkit-border-radius: 0 0 0 6px;\n"
                + "border-radius: 0 0 0 6px;\n"
                + "}");
        out.write(".bordered tr:last-child td:last-child {\n"
                + "-moz-border-radius: 0 0 6px 0;\n"
                + "-webkit-border-radius: 0 0 6px 0;\n"
                + "border-radius: 0 0 6px 0;\n"
                + "}\n");
        /* The following makes a colored table to show the score */
        out.write("table.score {\n"
                + "border-left-style:solid; \n"
                + "border-right-style:solid; \n"
                + "border-top-style:solid; \n"
                + "border-bottom-style:solid; \n"
                + "border-color:black; \n"
                + "border-width:1\n"
                + " }\n"
                + "table.score td {\n"
                + "font-weight:bold;\n"
                + "font-family:\"Helvetica Neue\", Arial, sans-serif;\n"
                + "color=#000000;\n"
                + "}\n");
        out.write("ul.omim {\n"
                + "list-style-type: none;\n"
                + "padding: 0;\n"
                + "margin-left: 0;\n"
                + "}\n");
        out.write("section.sep {\n"
                + "padding: 5px 0 5px 0;\n"
                + "overflow: hidden;\n"
                + "}\n");
        out.write("section.sep2 {\n"
                + "padding: 5px 0 5px 0;\n"
                + "overflow: hidden;\n"
                + "border-bottom: 2px solid #63AA9C;\n"
                + "}\n");
        /* The following makes tool tips */
        out.write("span{\n"
                + "background: none repeat scroll 0 0 #F8F8F8;\n"
                + "border: 5px solid 221E1D;\n"
                + "background:#E9633B; \n"
                + "color: #221E1D;\n"
                + "font-size: 13px;\n"
                + "height: 100%;\n"
                + "letter-spacing: 1px;\n"
                + "line-height: 30px;\n"
                + "margin: 0 auto;\n"
                + "position: relative;\n"
                + "text-align: center;\n"
                + "top: -80px;\n"
                + "left:-30px;\n"
                + "display:none;\n"
                + "padding:0 20px;\n"
                + "}\n");
        out.write("p.h{\n"
                + "margin:1px;\n"
                + "float:left;\n"
                + "position:relative;\n"
                + "cursor:pointer;\n"
                + "border-style: dotted;\n"
                + "border-width: 1px;\n"
                + "padding:10px;\n"
                + "background-color: #63AA9C;\n"
                + "font-family: monospace;\n"
                + "font-size: 1em;\n"
                + "margin: 10px; \n"
                + "}\n");
        out.write("p.h:hover span{\n"
                + "display:block;\n"
                + "left: 0; top:0;\n"
                + "margin: 20px 0 0;\n"
                + "}\n");
        out.write("</style>\n");
    }

    /**
     * Write the main output with the table of prioritized genes.
     *
     * @param pedigree An object representing the 1..n people in the pedigree
     * @param geneList List of genes, assumed to be sorted in prioritized order.
     */
    @Override
    public void writeHTMLBody(Pedigree pedigree, List<Gene> geneList)
            throws IOException {
        int genecount = Math.min(this.n_genes, geneList.size());
        out.write("<div class=\"boxcontainer\">\n"
                + "<article class=\"box\">\n"
                + "<a name=\"priority\"></a>\n"
                + "<h3>Top " + genecount + " ranked candidate genes</h3>\n");
        HTMLTable table = new HTMLTableWalker(pedigree);

        table.writePedigreeTable(this.out);
        this.out.write("<section class=\"sep\"></section>\n");
        writeEntrezSeedGenes();
        this.out.write("<section class=\"sep2\"></section>\n");
        this.out.write("<section class=\"sep\"></section>\n");

        for (int k = 0; k < genecount; ++k) {
            Gene g = geneList.get(k);
            int entrezid = g.getEntrezGeneID();
            List<String> interactantList = this.network.evalCandidateGene(entrezid);
            table.writeGeneTable(g, this.out, k + 1, interactantList);
            this.out.write("<section class=\"sep\"></section>\n");
        }
        table.writeTableFooter(this.out);
        out.write("</div>\n</div>\n</section>\n\n");
        /* Close the boxcontainer div */
        out.write("</article>\n"
                + "</div>\n");
    }

    /**
     * Print information on the filters chosen by the user and a summary of the
     * filtering results.
     *
     * @param filterList List of the filters chosen by the user.
     * @param priorityList List of prioritizers chosen by the user.
     */
    @Override
    public void writeHTMLFilterSummary(List<FilterReport> filterReports,
            List<Priority> priorityList)
            throws IOException {
	// The following code gets information on the filters chosen by the use
        // and prints out a summary of the filtering results.
        HTMLFilterSummary filtersum = new HTMLFilterSummary();
        for (FilterReport report : filterReports) {
            FilterType fl = report.getFilterType();
            // Get data for row in the filter table.
            String name = String.format("%s filter", report.getFilterType().toString());
            List<String> descript = report.getMessages();
            int before = report.getPassed();
            int after = report.getFailed();
            if (descript.size() == 1) {
                filtersum.addRow(name, descript.get(0), before, after);
            } else {
                filtersum.addRow(name, descript, before, after);
            }
        }

        /* Similar as above, but for the prioritizers. */
        for (Priority p : priorityList) {
            if (p.displayInHTML()) {
                String name = p.getPriorityName();
                String h = p.getHTMLCode();
                int before = p.getBefore();
                int after = p.getAfter();
                filtersum.addRow(name, h, before, after);
            }
        }

        out.write("<div class=\"boxcontainer\">\n"
                + "<article class=\"box\">\n"
                + "<a name=\"stats\"></a>\n"
                + "<h3>Result statistics and quality control</h3>\n");
        filtersum.writeTable(this.out);
        /* Close the boxcontainer div */
        out.write("</article>\n"
                + "</div>\n");
    }

    /**
     * Output a Table with the distribution of VariantTypes.
     *
     * @param vtc An object from the Jannovar library with counts of all variant
     * types
     * @param sampleNames Names of the (multiple) samples in the VCF file
     */
    @Override
    public void writeVariantDistributionTable(VariantTypeCounter vtc, List<String> sampleNames) {
        try {
            out.write("<div class=\"boxcontainer\">\n"
                    + "<article class=\"box\">\n"
                    + "<a name=\"distribution\"></a>\n"
                    + "<h2>Distribution of variants</h2>\n");
            out.write("<table id=\"variantDistribution\">\n");
            out.write("<thead><tr>\n");
            out.write("<th>Variant Type</th>");
            int ncol = sampleNames.size();
            for (int i = 0; i < ncol; i++) {
                out.write(String.format("<th>%s</th>", sampleNames.get(i)));
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
                for (int k = 0; k < ncol; ++k) {
                    out.write(String.format("<td>%d</td>", cts.get(k)));
                }
                out.write("</tr>\n");
            }
            out.write("</tbody>\n</table><p>&nbsp;</p>\n");
            out.write("</article>\n"
                    + "</div>\n");
        } catch (IOException e) {
            logger.error("Error writing variant distribution table", e);
        }
    }

    /**
     * Writes the final paragraph right before the footer of the Exomizer output
     * page
     */
    @Override
    public void writeAbout() throws IOException {
        out.write("<div class=\"boxcontainer\">\n"
                + "<article class=\"box\">\n"
                + "<h2><a name=\"About\"/>About</a></h2>\n");
        out.write("<p>ExomeWalker is a Java program that functionally annotates variants from whole-exome "
                + "sequencing data starting from a VCF file (version 4). The functional annotation code is "
                + "based on <a href=\"https://github.com/charite/jannovar/\">Jannovar</a> and uses "
                + "<a href=\"http://genome.ucsc.edu/\">UCSC</a> KnownGene transcript definitions and "
                + "hg19 genomic coordinates</p>\n");
        out.write("<p>Variants are prioritized according to user-defined criteria on variant frequency, pathogenicity,"
                + " quality, inheritance pattern, and protein-protein interaction data for random walk analysis "
                + " as described in <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/18371930\">K&ouml;hler et al. (2008)</a>.");
        printVersionInfo();
        out.write("<P>Developed by the Computational Biology and Bioinformatics group at the "
                + "<a href=\"http://genetik.charite.de/\">Institute for Medical Genetics and Human Genetics</a> of the "
                + "<a href=\"www.charite.de\">Charit&eacute; - Universit&auml;tsmedizin Berlin</a> and the Mouse "
                + " Informatics Group at the <a href=\"http://www.sanger.ac.uk/\">Sanger Institute</a>.</p>\n"
                + "</article></div>\n");

    }
    
    /**
     * This prints a list of the version of the dataresources used for the
     * website.
     */
    private void printVersionInfo() throws IOException {
        if (this.versionMap == null || this.versionMap.size() == 0) {
            return;
        }
        out.write("<h6>Version Information</h6>\n");
        out.write("<ul>\n");
        String v = null;
        String K[] = {"dbNSFP", "Exome Server Project", "dnSNP", "OMIM", "STRING"};
        for (String k : K) {
            v = this.versionMap.get(k);
            if (v != null) {
                out.write(String.format("<li>%s: %s</li>\n", k, v));
            }
        }
        out.write("</ul>\n");
    }
    
    private void writeEntrezSeedGenes() throws IOException {
        out.write("<h3>Random Walk Analysis</h3>\n"
                + "<p>Analysis was performed with the following seed genes from phenotypic series <i>"
                + this.diseaseGeneFamilyName + "</i>:</p>\n"
                + "<ul>\n");
        for (String url : this.seedGeneList) {
            out.write("<li>" + url + "</li>\n");
        }
        out.write("</ul>\n");

    }


}
