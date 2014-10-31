/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.factories;

import jannovar.exception.JannovarException;
import jannovar.io.SerializationManager;
import jannovar.reference.Chromosome;
import jannovar.reference.TranscriptModel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles de-serialising of known genes files produced from UCSC or Ensemble data.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ChromosomeMapFactory {
    
    public static final Logger logger = LoggerFactory.getLogger(ChromosomeMapFactory.class);
    
    /**
     * Jannovar makes a serialized file that represents a
     * HashMap<String, TranscriptModel> containing each and every
     * {@link jannovar.reference.TranscriptModel TranscriptModel} object. This
     * method both deserializes this file and also adds each TranscriptModel to
     * the corresponding IntervalTree of the
     * {@link jannovar.reference.Chromosome Chromosome} object. When we are
     * done, the {@link exomizer.Exomizer#chromosomeMap} contains Chromosome
     * objects for chromosomes 1-22,X,Y, and M, each of which contains the
     * TranscriptModel objects for each of the genes located on those
     * chromosomes.
     */
    public static Map<Byte,Chromosome> deserializeKnownGeneData(Path serealizedKnownGenePath) {
        logger.info("DESERIALISING KNOWN GENES FILE: {}", serealizedKnownGenePath);
        ArrayList<TranscriptModel> kgList = null;
        SerializationManager manager = new SerializationManager();
        try {
            kgList = manager.deserializeKnownGeneList(serealizedKnownGenePath.toString());
        } catch (JannovarException je) {
            String message = String.format("Unable to deserialize the known gene definition file: %s", serealizedKnownGenePath);
            logger.error(message);
            throw new RuntimeException(message, je);
        }
        logger.info("DONE DESERIALISING KNOWN GENES");
        return Chromosome.constructChromosomeMapWithIntervalTree(kgList);
    }
 
}
