package de.charite.compbio.exomiser.db.parsers;

/**
 * Command line functions from apache
 */
import de.charite.compbio.exomiser.db.resources.Resource;
import de.charite.compbio.exomiser.db.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is intended to parseResource the information about the OMIM phenotypic
 series and to enter it into the SQL database exomizer that will be used for
 the larger exomizer project. Note that this program parses the output of the
 perl scripts that are located in the "extraStuff" folder of the ExomeWalker
 project; see the README file there for information on how to get all of that
 parsed. These scripts are currently not very pretty, since the phenoseries
 information is delivered as an HTML page with subtly inconsistent formatting,
 it requires some "hacks" to parseResource with a perl script.
 <P>
 * This program is thus part of the ExomeWalker subproject of the Exomizer.
 * <P>
 * The format of the pheno2gene.txt file is as follows:
 * <PRE>
 * Warburg micro syndrome|600118|10p12.1|Warburg micro syndrome 3|3|614222|RAB18, WARBM3|602207|22931|RAB18
 * </PRE> We are interested in the following fields.
 * <ol>
 * <li> Warburg micro syndrome: name of the phenoseries
 * <li> 600118: MIM Id of the main entry of the phenoseries, we will use it as a
 * PRIMARY KEY
 * <li> 10p12.1: Cytoband of the specific disease entry
 * <li> Warburg micro syndrome 3: Name of the specific disease entry
 * <li> 3: OMIM class of the specific disease entry (3 means gene has been
 * mapped)
 * <li> 614222: MIM Id (phenotype) of the specific disease entry
 * <li> RAB18, WARBM3: gene symbol and synonyms of the specific disease entry
 * <li> 602207: MIM Id (gene) of the specific disease entry
 * <li> 22931: Entrez Gene Id of the specific disease entry's gene
 * <li> Gene symbol of the Entrez Gene entry (should match one of the items in
 * field 7).
 * </ol>
 *
 * @author Peter Robinson
 * @version 0.06 (2 January, 2014)
 *
 */
public class PhenoSeriesParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(PhenoSeriesParser.class);


    public PhenoSeriesParser() {
    }

    /**
     * ResourceParser for the pheno2gene.txt file which produces the phenoseries.pg dump file,
 assuming you want to call them that.
     * 
     * Note that OMIM has some entries such as 61206
     * <ul>
     * <li>Frontotemporal lobar degeneration, TARDBP-related
     * <li>Amyotrophic lateral sclerosis 10, with or without FTD
     * </ul>
     * Format:
     * <pre>
     * Agammaglobulinemia|601495|9q34.11|Agammaglobulinemia 5|3|613506|LRRC8A, KIAA1437, AGM5|608360|56262|LRRC8A
     * Agammaglobulinemia|601495|10q24.1|Agammaglobulinemia 4|3|613502|BLNK, SLP65, AGM4|604515|29760|BLNK
     * Agammaglobulinemia ...
     * </pre> These entries have the same OMIM phenoID and the same OMIM gene,
     * and are in a sense duplicates, more or less two versions of the same
     * disease. To keep things simple, we take only one of these entries, and we
     * discard the rest.
     * @param resource
     * @param inDir
     * @param outDir
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {
        //you might notice that the code here is pretty similar to that in the 
        //Omim2GeneParser because it is parsing the same file, but handling the data slightly differently.
        //Sorry. This is clunky and a cardinal sin in direct violation of DRY. 
        //But done to fit the parseResource() paradigm. Peter did it better before-hand 
        //(i.e. one class only), but it produced two different tables. Given this is static data we're parsing
        //and it's likely to be depricated at some point this is hopefully not too evil.
        
        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        ResourceOperationStatus status;
        
        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset());
                BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
        
            Set<String> uniqueSeriesIds = new HashSet<>();
            Map<String, Phenoseries> phenoseriesMap = new HashMap<>();

            String line;
            Phenoseries ps;
            
            while((line = reader.readLine()) != null) {
              logger.debug(line);
                String[] fields = line.split("\\|");
        //        "INSERT INTO omim2gene(mimDiseaseID, mimDiseaseName,cytoBand,mimGeneID,entrezGeneID,geneSymbol,seriesID) "+
                final int expectedFields = 10;
                if (fields.length != expectedFields) {
                    logger.error("Expected {} fields but got {} for line {}", expectedFields, fields.length, line);
                    continue;
                }
                
		String seriesName = fields[0];
		String seriesId = fields[1];
                String cytoBand = fields[2];
                String mimDiseaseName = fields[3];
		String mimDiseaseId = fields[5];
		String mimGeneId = fields[7];
		String entrezGeneId = fields[8];
		String geneSymbol = fields[9];
                
                String uniqueSeriesId = String.format("%s-%s", seriesId, mimDiseaseId);
                
                if (entrezGeneId.equals("?")) {
                    logger.debug("No Entrez gene mapped for phenoseries: {} MIM gene: {} location: {} disease: {} {}", seriesId,  mimGeneId, cytoBand, mimDiseaseId , mimDiseaseName); // No gene for this entry
                } else if (uniqueSeriesIds.contains(uniqueSeriesId)) {
                    //is this a bug with the original data?
                    logger.debug("seriesId-diseaseId {} has already been mapped, skipping phenoseries: {} MIM gene: {} location: {} disease: {} {}", uniqueSeriesId, seriesId,  mimGeneId, cytoBand, mimDiseaseId , mimDiseaseName);
                } else {
                    uniqueSeriesIds.add(uniqueSeriesId);
                    logger.debug(String.format("%s|%s|%s|%s|%s|%s|%s%n", mimDiseaseId , mimDiseaseName, cytoBand, mimGeneId, entrezGeneId, geneSymbol, seriesId));
                } 
                
                if (phenoseriesMap.containsKey(seriesId)) {
                    ps = phenoseriesMap.get(seriesId);
                    ps.addEntrezGene(entrezGeneId);
                } else {
                    ps = new Phenoseries(seriesId, seriesName, entrezGeneId);
                    phenoseriesMap.put(seriesId, ps);
                }
            }
            //now we should have a nicely populated map of phenoseries, time to write them to file
            for (Phenoseries phenoseries : phenoseriesMap.values()) {
                logger.debug("Writing series: {} {} {}", phenoseries.getSeriesID(), phenoseries.getSeriesName(), phenoseries.getGeneCount());
                writer.write(String.format("%s|%s|%s%n", phenoseries.getSeriesID(), phenoseries.getSeriesName(), phenoseries.getGeneCount()));
            }
            status = ResourceOperationStatus.SUCCESS;
        }  catch (FileNotFoundException ex) {
            logger.error("{}", ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error("{}", ex);
            status = ResourceOperationStatus.FAILURE;
        }
        resource.setParseStatus(status);
        logger.info("{}", status);
    }


    /**
     * An inner class to help record the phenoseries before we add them to the
     * database.
     */
    class Phenoseries {

        private String seriesID = null;
        private String seriesName = null;
        Set<String> geneIDset = null;

        Phenoseries(String id, String name, String entrezgene) {
            this.geneIDset = new HashSet<>();
            this.geneIDset.add(entrezgene);
            this.seriesID = id;
            this.seriesName = name;
        }

        public void addEntrezGene(String entrezgene) {
            this.geneIDset.add(entrezgene);
        }

        public String getSeriesID() {
            return this.seriesID;
        }

        public String getSeriesName() {
            return this.seriesName;
        }

        public int getGeneCount() {
            return this.geneIDset.size();
        }
    }
}
