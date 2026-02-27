package org.monarchinitiative.exomiser.cli.commands.batch;

import java.util.List;

public record BatchFileValidationResults(int checkedCount, int errorCount, List<SampleValidationError> sampleValidationErrors) {
}
