package org.monarchinitiative.exomiser.core.analysis;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SimpleAnalysisRunnerTest.class, SparseAnalysisRunnerTest.class, PassOnlyAnalysisRunnerTest.class})
public class AnalysisRunnerSuite {
}
