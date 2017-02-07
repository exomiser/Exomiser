package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.monarchinitiative.exomiser.core.model.Model;
import org.monarchinitiative.exomiser.core.model.ModelPhenotypeMatch;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@FunctionalInterface
public interface ModelScorer {

    ModelPhenotypeMatch scoreModel(Model model);

}
