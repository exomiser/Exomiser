# The Exomiser - Core Library Changelog

## 6.0.0
- API changes:
    - Package tidy-up, which triggered the full version bump. 

## 5.2.0 2014-12-18
- New style HTML output

## 5.1.0 2014-12-12
- Added ability for the VariantEvaluation to report whether the Variant it is associated with has been annotated by Jannovar.
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
    - Added getEntrezGeneID method to VariantEvaluation to make API more consistent and lessen direct dependency on Jannovar Variant in the rest of the code.
    - Removed unused PhredScore class
    - FilterFactory now returns more specific Filter types - VariantFilter and GeneFilter from the relevant methods

