# The Exomiser - Core Library Changelog

## 7.0.0 2015-10-01
Now requires Java 8 or higher to run.
- API changes:
    - New analysis package contains all high-level concepts for analysing exome/genome data
    - Main Exomiser entry point now accepts an Analysis instead of a SampleData and Settings
    - ExomiserSettings has been renamed to simply Settings and moved to the analysis package, to use these they should
    be converted by the SettingsParser and the resulting Analysis used in the Exomiser. These will run the Exomiser in
    the original exome-analysis algorithm, but this is not suitable to genome analysis.
    - An Analysis can be specified either programmatically, or via YAML and read by the AnalysisParser
    - An Analysis can run in FULL, SPARSE or a new PASS_ONLY mode. The latter is much more memory efficient as it will
    only keep those variants/genes which passed all the required filters.

## 6.0.0 2015-01-12
- API changes:
    - Package tidy-up - all packages are now use their maven package name as the root package for that project.
    - PhenixPriority now dies immediately and with an informative message if no HPO terms are supplied.
    - Added NONE PriorityType for when you really don't want to run any prioritiser.
    - Re-named the ExomiserMouse and ExomiserAllSpecies prioritisers to their published Phive and HiPhive names.
    - Removed unused List<Priority> requirement from writers.
    - TSV output now comes in TSV_GENE and TSV_VARIANT flavours.
    - Removed unused getBefore and getAfter methods from Priority interface.
    - Removed getConnection and setConnection from Priority interface as these were only used by some prioritisers.
    - Prioritisers requiring database access now use a DataSource rather than a direct connection.
    - Added getRowIndexForGene(entrezGeneId) and getColumnMatrixForGene(entrezGeneId) methods to DataMatrix.
    - Removed getRowToEntrezIdIndex from DataMatrix.
    - Refactored ExomeWalkerPriority and ExomiserAllSpeciesPriority to use new DataMatrix methods.

## 5.2.0 2014-12-18
- New style HTML output

## 5.1.0 2014-12-12
- Added ability for the VariantEvaluation to report whether the Variant it is associated with has been annotated by
Jannovar.
- VCF output format will now indicate which, if any variants have not been annotated by Jannovar for whatever reason.
- VariantEvaluation can now report a FilterStatus to indicate whether it has passed, failed or is unfiltered.
- Further under the hood clean-ups and improved test coverage - now at ~30%

## 5.0.1 2014-11-14
- Changed Jannovar to version 0.9 to fix a null pointer caused by inability to translate certain variants.

## 5.0.0 2014-11-14
- Focused on improving test coverage of the Factory and DAO packages in particular.
- API changes:
    - FrequencyDao and PathogenicityDao are now interfaces implemented by DefaultFrequencyDao and DefaultPathogenicityDao
    - New PedigreeFactory split out of SampleDataFactory
    - GeneFactory is no longer a static class
    - VariantEvaluationDataFactory renamed to VariantVariantEvaluationDataService
    - Removed unused constructors from SampleData
    - Added getEntrezGeneID method to VariantEvaluation to make API more consistent and lessen direct dependency on
    Jannovar Variant in the rest of the code.
    - Removed unused PhredScore class
    - FilterFactory now returns more specific Filter types - VariantFilter and GeneFilter from the relevant methods
