/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.data.genome.model.parsers.sv;

import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.data.genome.model.SvFrequency;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;
import org.monarchinitiative.svart.VariantType;

import java.util.List;


public class DgvSvFreqParser implements Parser<SvFrequency> {

    @Override
    public List<SvFrequency> parseLine(String line) {
        if (line.startsWith("variantaccession")) {
            // header
            // variantaccession	chr	start	end	varianttype	variantsubtype	reference	pubmedid	method	platform	mergedvariants	supportingvariants	mergedorsample	frequency	samplesize	observedgains	observedlosses	cohortdescription	genes	samples
            return List.of();
        }
        String[] tokens = line.split("\t");
        String accession = tokens[0];
        int chr = Contigs.parseId(tokens[1]);
        int start = Integer.parseInt(tokens[2]);
        int end = Integer.parseInt(tokens[3]);
        // n.b. gain+loss sites are multiallelic so need to be assigned based on the observed gains/losses below
        VariantType variantType = parseType(tokens[5]);
        String dbVarId = parseDbVarId(tokens[11]);
        int an = Integer.parseInt(tokens[14]);
        int gains = zeroIfEmpty(tokens[15]);
        int losses = zeroIfEmpty(tokens[16]);
        if (gains != 0 && losses != 0) {
            return List.of(
                    new SvFrequency(chr, start, end, 0, VariantType.DUP, accession, "DGV", accession, gains, an),
                    new SvFrequency(chr, start, end, 0, VariantType.DEL, accession, "DGV", accession, losses, an)
            );
        }
        if (gains != 0) {
            return List.of(new SvFrequency(chr, start, end, 0, variantType, dbVarId, "DGV", accession, gains, an));
        }
        return List.of(new SvFrequency(chr, start, end, 0, variantType, dbVarId, "DGV", accession, losses, an));
    }

    private int zeroIfEmpty(String token) {
        return token.isEmpty() ? 0 : Integer.parseInt(token);
    }

    private String parseDbVarId(String token) {
        if (!token.isEmpty()) {
            String[] ids = token.split(",");
            return ids[0];
        }
        return "";
    }

    private VariantType parseType(String dgvType) {
        switch (dgvType) {
            // deletions
            case "deletion":
                return VariantType.DEL;
            case "loss":
                return VariantType.DEL;
            case "mobile element deletion":
                return VariantType.DEL_ME;
            case "alu deletion":
                return VariantType.DEL_ME_ALU;
            case "herv deletion":
                return VariantType.DEL_ME_HERV;
            case "line 1 deletion":
                return VariantType.DEL_ME_LINE1;
            case "sva deletion":
                return VariantType.DEL_ME_SVA;
            // insertions
            case "insertion":
                return VariantType.INS;
            case "novel sequence insertion":
                return VariantType.INS;
            case "mobile element insertion":
                return VariantType.INS_ME;
            case "alu insertion":
                return VariantType.INS_ME_ALU;
            case "herv insertion":
                return VariantType.INS_ME_HERV;
            case "line 1 insertion":
                return VariantType.INS_ME_LINE1;
            case "sva insertion":
                return VariantType.INS_ME_SVA;
            // duplications
            case "duplication":
                return VariantType.DUP;
            case "gain":
                return VariantType.DUP;
            case "tandem duplication":
                return VariantType.DUP_TANDEM;
            // inversion
            case "inversion":
                return VariantType.INV;
            // complex types
            case "gain+loss":
                return VariantType.CNV_COMPLEX;
            case "complex":
                return VariantType.CNV_COMPLEX;
            case "sequence alteration":
                return VariantType.CNV_COMPLEX;
            default:
                return VariantType.CNV;
        }
    }
}
