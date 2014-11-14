# The Exomiser - Core Library Changelog

## 5.0.0 2014-11-14
- Focused on improving test coverage of the Factory and DAO packages in particular.
- API changes:
    - FrequencyDao and PathogenicityDao are now interfaces implemented by DefaultFrequencyDao and DefaultPathogenicityDao
    - New PedigreeFactory split out of SampleDataFactory
    - GeneFactory is no longer a static class
    - VariantEvaluationDataFactory renamed to VariantVariantEvaluationDataService
    - Removed unused constructors from SampleData
    - Added getEntrezGeneID method to VariantEvaluation to make API more consistent and lessen direct dependency on Jannovar Variant in the rest of the code.
    - Removed unused PhredScore class
    - FilterFactory now returns more specific Filter types - VariantFilter and GeneFilter from the relevant methods

