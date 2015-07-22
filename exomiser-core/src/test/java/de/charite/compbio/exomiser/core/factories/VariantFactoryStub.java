package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactoryStub extends VariantFactory {


    public VariantFactoryStub() {
        super(null);
    }

    @Override
    public List<VariantContext> createVariantContexts(Path vcfPath) {
        return Collections.emptyList();
    }

    @Override
    public List<VariantEvaluation> createVariantEvaluations(Path vcfPath) {
        return Collections.emptyList();
    }
}

