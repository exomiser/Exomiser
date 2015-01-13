package de.charite.compbio.exomiser.db.build.parsers;

import de.charite.compbio.exomiser.db.build.resources.Resource;
import de.charite.compbio.exomiser.db.build.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class EntrezParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(EntrezParser.class);
    private final HashMap<String, List<Integer>> ensembl2EntrezGene;

    /**
     * @param ensembl2EntrezGene
     */
    public EntrezParser(HashMap<String, List<Integer>> ensembl2EntrezGene) {
        this.ensembl2EntrezGene = ensembl2EntrezGene;
    }

    /**
     * This function does the actual work of parsing the Entrez data.
     *
     * @param resource Resource containing the information about
     * @param inDir Directory path to string file.
     * @param outDir Directory where output file is to be written
     * @return the ResourceOperationStatus
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);

        HashMap<Integer, String> entrez2sym = new HashMap<>();
        ResourceOperationStatus status;
        
        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset());
                BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split("\t");
                if (split.length != 3) {
                    continue;
                }
                String ens = split[0];
                Integer entrez = null;
                if (split[1].equals("")) {
                    continue;
                }
                try {
                    entrez = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    logger.error("Malformed line: {} (could not parse entrez gene field: '{}')", line, split[1]);
                }

                if (split[2] == null || split[2].isEmpty()) {
                    logger.warn("Could not extract symbol, skipping line: {}", line);
                }
                String symbol = split[2];
                entrez2sym.put(entrez, symbol);
                if (this.ensembl2EntrezGene.containsKey(ens)) {
                    List<Integer> test = this.ensembl2EntrezGene.get(ens);
                    if (!test.contains(entrez)) {
                        test.add(entrez);
                    }
                } else {
                    List<Integer> lst = new ArrayList<>();
                    lst.add(entrez);
                    this.ensembl2EntrezGene.put(ens, lst);
                }
            }

            Iterator<Integer> it = entrez2sym.keySet().iterator();
            while (it.hasNext()) {
                Integer id = it.next();
                if (id == null) {
                    continue;
                }
                String sym = entrez2sym.get(id);
                writer.write(String.format("%s|%s", id, sym));
                writer.newLine();
            }
            writer.close();
            reader.close();
            status = ResourceOperationStatus.SUCCESS;
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FAILURE;
        }
        resource.setParseStatus(status);
        logger.info("{}", status);
    }
}
