/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisParser {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisParser.class);

    public Analysis parse(String analysisDoc) {
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> map = (Map) yaml.load(readFile(analysisDoc));
        for (String key : map.keySet()) {
            Map<String, String> subValues = map.get(key);
            System.out.println(key);
            for (String subValueKey : subValues.keySet()) {
                System.out.println(String.format("\t%s = %s", subValueKey, subValues.get(subValueKey)));
            }
        }
        System.out.println(yaml.dump(map));

        return null;
    }

    private FileReader readFile(String analysisDoc) {
        try {
            return new FileReader(analysisDoc);
        } catch (FileNotFoundException ex) {
            throw new AnalysisFileNotFoundException("Unable to find analysis file: " + ex.getMessage());
        }
    }

    protected static class AnalysisFileNotFoundException extends RuntimeException {

        AnalysisFileNotFoundException(String message) {
            super(message);
        }
    }
}
