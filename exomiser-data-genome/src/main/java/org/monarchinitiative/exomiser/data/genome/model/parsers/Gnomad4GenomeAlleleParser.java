package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gnomad4GenomeAlleleParser extends Gnomad3GenomeAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(Gnomad4GenomeAlleleParser.class);

    public Gnomad4GenomeAlleleParser() {
        super(GnomadPopulationKey.GNOMAD_V4_GENOMES, Set.of(".", "PASS", "RF", "InbreedingCoeff", "LCR", "SEGDUP"));
    }

}
