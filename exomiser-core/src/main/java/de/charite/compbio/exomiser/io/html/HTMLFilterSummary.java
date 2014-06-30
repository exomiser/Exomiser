package de.charite.compbio.exomiser.io.html;


import java.util.List;
import java.util.ArrayList;
import java.io.Writer;
import java.io.IOException; 


/**
 * This class is designed to create a nice-looking table that will
 * provide a nice summary of the filtering results. There is one row
 * for each filter chosen by the user, displaying the parameters used
 * as well as the numbers of variants before and after the filter was
 * applied.
 * <P>
 * The class writes to a {@code java.io.Writer} so that we can use either
 * a BufferedWriter or a StringWriter.
 * <P>
 * See also {@link exomizer.io.html.HTMLWriter HTMLWriter} for the CSS code used to style
 * the table.
 * @author Peter Robinson 
 * @version 0.09 (2 January, 2014)
 */
public class HTMLFilterSummary {
    /** Each item in this list holds the summary of one exome filter operation. */
    private List<HTMLFilterSummaryRow> row_list = null;

    public HTMLFilterSummary() {
	this.row_list = new ArrayList<HTMLFilterSummaryRow>();
    }
    /** Add a row of data containing a simple string description of the filter parameters. */
    public void addRow(String name, String Description, int before, int after) {
	HTMLFilterSummaryRow row = new HTMLFilterSummaryRow(name,Description,before, after);
	this.row_list.add(row);
    }
    /** Add a row of data containing a description list of the filter parameters. */
    public void addRow(String name, List<String> list, int before, int after) {
	HTMLFilterSummaryRow row = new HTMLFilterSummaryRow(name,list,before, after);
	this.row_list.add(row);
    }

    public void writeTable(Writer out) throws IOException {
	if (this.row_list.isEmpty()) {
	    writeErrorParagraph(out);
	    return;
	}
       	out.write("<table>\n"+
		  "<tr>\n"+
		  "<th>Filter</th>");
	out.write("<th>Parameters</th>");
	out.write("<th>Variants before filtering</th>");
	out.write("<th>Variants after filtering</th>");
	out.write("</tr>\n");
	for (HTMLFilterSummaryRow r : this.row_list) {
	    String s = r.getHTMLCodeForRow();
	    out.write(s + "\n");
	}
	out.write("</table>\n<br/>\n");
    }


     public void writeTableWithCSS(Writer out) throws IOException {
	if (this.row_list.isEmpty()) {
	    writeErrorParagraph(out);
	    return;
	}
       	out.write("<table class=\"summary\">\n"+
		  "<thead><tr>\n"+
		  "<th>Filter</th>");
	out.write("<th>Parameters</th>");
	out.write("<th>Variants before filtering</th>");
	out.write("<th>Variants after filtering</th>");
	out.write("</tr></thead>\n");
	out.write("<tbody>\n");
	for (HTMLFilterSummaryRow r : this.row_list) {
	    String s = r.getHTMLCodeForRow();
	    out.write(s + "\n");
	}
	out.write("</tbody></table>\n<br/>\n");
    }


    /** This should never happen, but if the main program does not initialized this object
     * correctly and there are now filtering parameters, then this error message will be 
     * shown.
     */
    public void writeErrorParagraph(Writer out) throws IOException {
	out.write("<P>Exomizer Error: Filter parameters not recorded.</P>\n");
    }

    /**
     * This internal class encapsulates the HTML code needed to produce a row in the HTML table that
     * is usd to display a summary of the filtering results.
     */
    private class HTMLFilterSummaryRow {
	/** Number of variants before filter applied. */
	private int n_before;
	/** Number of variants after filter applied. */
	private int n_after;
	/** Name of filter */
	private String filterName=null;
	/** Description of filtering as simple String */
	private String description=null;
	/** Description of filtering as list (HTML <UL> element) */
	private List<String> descriptionList=null;
	/** Construct a row with a simple description. */
	public HTMLFilterSummaryRow(String name, String Description, int before, int after) {
	    this.filterName = name;
	    this.description = Description;
	    this.n_before = before;
	    this.n_after = after;
	}
	/** Construct a row with a  description list. */
	public HTMLFilterSummaryRow(String name, List<String> list, int before, int after) {
	    this.filterName = name;
	    this.descriptionList = list;
	    this.n_before = before;
	    this.n_after = after;
	}

	/**
	 * This function creates a row for the table <b>Summary of Exome Filtering</b>.
	 */
	public String getHTMLCodeForRow() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("<tr>");
	    sb.append("<td>" + filterName + "</td>");
	    if (description != null) {
		sb.append("<td>&#8226; " + this.description + "</td>");
	    } else {
		sb.append("<td>");
		for (String s : descriptionList) {
		    sb.append("&#8226; "+ s + "<br/><br/>\n");
		}
		sb.append("</td>");
	    }
	    sb.append(String.format("<td>%d</td>",n_before));
	    sb.append(String.format("<td>%d</td>",n_after));
	    sb.append("</tr>");
	    return sb.toString();
	} 
    }
    /* ---- end of inner class HTMLFilterSummaryRow ----*/

}
