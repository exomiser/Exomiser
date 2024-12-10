package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;

public class SpliceAiAlleleParser implements AlleleParser {
    @Override
    public List<Allele> parseLine(String line) {
        if (line.startsWith("#")) {
            // comment line.
            return List.of();
        }
        var allele = parseAllele(line);
        return List.of(allele);
    }

    private Allele parseAllele(String line) {
        String[] fields = line.split("\t");
        if (fields.length != 5) {
            throw new IllegalStateException("Expected 5 fields but found " + fields.length + " in line " + line);
        }
        int chr = Contigs.parseId(fields[0]); // 1..22,X,Y,MT
        int start = Integer.parseInt(fields[1]);
        String ref = fields[2];
        String alt = fields[3];
        float score = Float.parseFloat(fields[4]);
        Allele allele = new Allele(chr, start, ref, alt);
        allele.addPathogenicityScore(AlleleProto.PathogenicityScore.newBuilder().setPathogenicitySource(AlleleProto.PathogenicitySource.SPLICE_AI).setScore(score).build());
        return allele;
    }
}
