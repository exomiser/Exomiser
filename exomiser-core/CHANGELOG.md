# The Exomiser - Core Library Changelog

## 14.0.2 2024-09-20

- Fix for issue #571. This is a bug-fix release to prevent erroneous assignment of `PVS1` to recessive-compatible variants in LOF-tolerant genes.

## 14.0.1 2024-09-03

- Fix for Issue #565. This is a patch release to prevent a possible ArrayIndexOutOfBoundsException being thrown when outputting the variants TSV file. There are no other changes.

## 14.0.0 2024-02-29

This release **requires data version >= 2402** and **Java version >= 17** (the previous LTS release).

- Added new GeneBlacklistFilter [#457](https://github.com/exomiser/Exomiser/issues/457)
- Enabled independent update of ClinVar data [#501](https://github.com/exomiser/Exomiser/issues/501)
- Add new ClinVar conflicting evidence counts in HTML output [#535](https://github.com/exomiser/Exomiser/issues/535)
- Added PS1, PM1, PM5 categories to ACMG assignments
- Updated gene constraints to use gnomad v4.0 data
- TSV genes, TSV variants and VCF outputs will only write to a single file where the possible modes of inheritances are now shown together rather than split across separate files.
- Altered reporting of InheritanceModeFilter to state that the number shown refers to variants rather than genes.
- Added new `ClinVarDao` and `ClinVarWhiteListReader` to take advantage of the independently upgradeable ClinVar data files.
- The `VariantWhiteList` is now dynamically loaded from the ClinVar data provided in the clinvar.mv.db file
- `VariantDataServiceImpl` now requires a `ClinVarDao`
- Fix for issue [#531](https://github.com/exomiser/Exomiser/issues/531) where the `priorityScoreFilter` and `regulatoryFeatureFilter` pass/fail counts were not displayed in the HTML.
- Fix for issue [#534](https://github.com/exomiser/Exomiser/issues/534) where variant frequency and/or pathogenicity annotations are missing in certain run configurations.

New APIs:
- New `AnalysisDurationFormatter`
- New `FilterResultsCounter`
- New `FilterResultCount` data class
- New `AnalysisResults.filterResultCounts` field
- New `FilterRunner.filterCounts()` and `FilterRunner.logFilterResult()` methods
- New `Filterable.failedFilter()` method
- New `AlleleData` class to encapsulate building AlleleProto.Frequency and AlleleProto.PathogenicityScore instances
- Added new `ClinVarDao` and `ClinVarWhiteListReader` to take advantage of the independently upgradeable ClinVar data files.
- `Frequency` can either be constructed from a percentage frequency or a set of AC, AN, HOM counts. 
- Added `AlleleProto.AlleleKey alleleKey()` method to `Variant` to memoise
- Add PathogenicitySource `ALPHA_MISSENSE`, `EVE`, `SPLICE_AI`
- Add new `Frequency`, `FrequencySource`, `PathogenicityScore`, `PathogenicitySource`, `VariantEffect` and `ClinVar.ReviewStatus` to proto schema.

API breaking changes:
- `PathogenicityData` and `FrequencyData` now follow a 'record' rather than 'java bean' pattern for field accessors e.g. `PathogenicityData.clinVarData()` rather than `PathogenicityData.getClinVarData()` 
- Deleted deprecated `TsvGeneAllMoiResultsWriter`, `TsvVariantAllMoiResultsWriter` and `VcfAllMoiResultsWriter` classes
- Delete PathogenicitySource `M_CAP`, `MPC`, `PRIMATE_AI`
- Alter ESP FrequencySource long forms to short e.g. `ESP_AFRICAN_AMERICAN` to `ESP_AA`
- TSV output column `CLINVAR_ALLELE_ID` has been changed to `CLINVAR_VARIANT_ID` to allow easier reference to ClinVar variants.


Other changes:
- Updated Spring Boot to version 3.2.3

## 13.3.0 2023-10-17

- Updated Jannovar version to 0.41 to fix incorrect MT codon table usage [#521](https://github.com/exomiser/Exomiser/issues/521)
- Downgraded PM2 - PM2_Supporting for variants lacking frequency information [#502](https://github.com/exomiser/Exomiser/issues/502).
- Updated Acgs2020Classifier and Acmg2015Classifier to allow for PVS1 and PM2_Supporting to be sufficient to trigger LIKELY_PATHOGENIC
- Updated AcmgEvidence to fit a Bayesian points-based system [#514](https://github.com/exomiser/Exomiser/issues/514)
- Removed ASJ, FIN, OTH ExAC and gnomAD populations from presets and examples [#513](https://github.com/exomiser/Exomiser/issues/513).
- Fix for regression causing `<INV>` variants to be incorrectly down-ranked 
- Fix for issue [#486](https://github.com/exomiser/Exomiser/issues/486) where VCF output includes whitespace in INFO field.
- Logs will now display elapsed time correctly if an analysis runs over an hour (!).
- Updated exomiser-phenotype-data to take annotations from phenotype.hpoa [#351](https://github.com/exomiser/Exomiser/issues/351), [#373](https://github.com/exomiser/Exomiser/issues/373), [#379](https://github.com/exomiser/Exomiser/issues/379)
- Updated application.properties and ResourceConfigurationProperties to remove unused fields.
- Updated DiseaseInheritanceCacheReader and DiseasePhenotypeReader to parse phenotype.hpoa file
- Updated DiseaseResourceConfig to use hpoa resource

New APIs:

- New `Acmg2020PointsBasedClassifier` class
- New `FrequencyData.size()` method
- New `FrequencyData.isEmpty()` method
- New `FrequencyData.containsFrequencySource()` method
- New `FrequencyData.getMaxFreqForPopulation()` method
- New `FrequencySource.NON_FOUNDER_POPS` method

## 13.2.1 2023-06-30

- Fix for bug where all `<INS>` structural variants were given a maximal variant score of 1.0 regardless of their position on a transcript.
- Added partial implementation of [SVanna scoring](https://genomemedicine.biomedcentral.com/articles/10.1186/s13073-022-01046-6/tables/1) for coding and splice site symbolic variants. 
- Fix for issue #481 where TSV and VCF results files would contain no data when the analysis `inheritanceModes` was empty. 

**IMPORTANT!** *This will be the last major release to run on Java 11. Subsequent major releases (i.e. 14+) will require Java 17.*

## 13.2.0 2023-02-28

- Fixed excessive CPU usage and application hang after variant prioritisation with large number of results
- Fixed issue [#478](https://github.com/exomiser/Exomiser/issues/478) where gene.tsv output files are empty when running a phenotype only prioritisation.
- Fixed broken links to OMIM in the phenotypic similarity section of the HTML output [#465](https://github.com/exomiser/Exomiser/issues/465)
- Added gene symbol as HTML id tag in gene panel HTML results [#422](https://github.com/exomiser/Exomiser/pull/422)
- Fixed broken build due to missing sonumina repository and related artefacts [#460](https://github.com/exomiser/Exomiser/issues/460)


API breaking changes:

- None


New APIs:

- New `OutputSettings.getOutputDirectory()`
- New `OutputSettings.getOutputFileName()`


Deprecated methods:

- `OutputSettings.getOutputPrefix()` deprecated in favour of new `OutputSettings.getOutputDirectory()` and `OutputSettings.getOutputFileName()` methods


Other changes:

- Update Spring boot 2.6.9 -> 2.7.7


## 13.1.0 2022-07-29

The three new features for this release is the automated ACMG classification of small sequence variants, calculating
p-values for the combined scores and providing new and more interpretable TSV and VCF output files.

- Added new automated ACMG annotations for top-scoring variants in known disease-causing genes.
- Added new combined score p-value
- Added new TSV_GENE, TSV_VARIANT and VCF output files containing ranked genes/variants for all the assessed modes of
  inheritance. Note that __these new file formats will supersede the existing individual MOI-specific TSV/VCF files which
  will be removed in the next major release__. See the [online documentation](https://exomiser.readthedocs.io/en/latest/result_interpretation.html) for details.
- New update online documentation! See https://exomiser.readthedocs.io/en/latest/

API breaking changes:

- None

New APIs:

- New `Analysis.getMainPrioritiser()`
- New `AnalysisStep.isGenePrioritiser()`
- New `Gene.getAssociatedDiseases()` method
- New `Gene.getCompatibleGeneScores()`
- New `Gene.pValue()` and `GeneScore.pValue()` methods
- New `CombinedScorePvalueCalculator` class
- New `acmg` package
- New `GeneScore.getAcmgAssignments()`
- New `GeneScore.Builder.acmgAssignments()` and `pValue()` setters
- New `TranscriptAnnotation.rank()`, `getRankTotal()` and `getRankType()` for intron/exon numbering of variant position.
- New `ExomiserConfigReporter` class for logging startup configuration
- New `OutputSettings.applyOutputSettings()`
- New `GeneScoreRanker` to help with new output writers
- New `TsvGeneAllMoiResultsWriter`
- New `TsvVariantAllMoiResultsWriter`
- New `VcfAllMoiResultsWriter`

 
Other changes:
- Updated Spring Boot to version 2.6.9
- Added automated docker build for CLI and web
- Update HtmlResultsWriter to detect transcript data source
- Fix broken StringDB links in HTML output


## 13.0.0 2021-09-21

This release is primarily focussed on enabling simultaneous prioritisation of structural and non-structural variation
with as consistent an API as possible for both types of variation. It also introduces a new API for specifying richer
information about a `Sample` based on the v1 GA4GH [Phenopacket](https://phenopacket-schema.readthedocs.io/en/1.0.0/)

This release requires data version >= 2109 and Java version >= 11 (Java 17 recommended).

API breaking changes:

- New target Java version set to 11
- `Exomiser.run()` now requires `Sample` and `Analysis` arguments
- `AnalysisRunner` interface now requires `Sample` and `Analysis` arguments
- `Analysis` fields `vcfPath`, `pedigree`, `probandSampleName` and `genomeAssembly` moved to new `Sample` class
- `PedigreeSampleValidator` moved from `util` into new `sample` package
- Replaced `SampleIdentifierUtil` with `SampleIdentifiers` class
- Replaced `SampleIdentifier` with `SampleData`
- `Variant` now extends `org.monarchinitiative.svart.Variant` - see https://github.com/exomiser/svart/ for details
- Deprecated `VariantCoordinates` - replaced by `org.monarchinitiative.svart.Variant`
- `VariantEvaluation.getSampleGenotypes()` now returns a `SampleGenotypes` class
- Changed `VariantAnnotation` from implementing `Variant` to implementing new `VariantAnnotations` interface
- Updated variant coordinates `getChromosome()`, `chromosomeName()`, `getPosition()`, `getRef()`, `getAlt()` to
  use `Svart` `contigId()`, `contigName()`, `start()`, `end()`, `ref()` and `alt()` signatures
- Replaced `RsId` with `String` type in `FrequencyData` constructors and return from `hasDbSnpRsID()` method
- Replaced `Contig` class with new `Contigs` class
- `VariantAnnotator` interface changed to `List<VariantAnnotation> annotate(@Nullable Variant variant)`
- `VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes()` method now returns a `SampleGenotypes` object
- `VariantFactory` now a `@FunctionalInterface` with a `createVariantEvaluations()`
- `VariantFactoryImpl` now requires `VariantAnnotator` and `VcfReader` input arguments
- `VcfCodecs` now requires `List` rather than `Set` inputs

New APIs:

- New protobuf schemas for `Job`, `Analysis`, `Sample`, `OutputOptions`
- New `Exomiser.run(JobProto.Job job)` entry point
- New `FluentAnalysisBuilder` interface implemented by `AnalysisBuilder` and new `AnalysisProtoBuilder` for consistent
  API between proto and domain classes
- New `AnalysisGroup` class extracted from `AbstractAnalysisRunner`
- New `Sample` class to encapsulate data about the sample, such as `Age` and `Sex`
- New `Age` class
- New `Phenopacket...` classes for reading and converting sample data from v1 phenopackets
- New Proto converter classes
- New `SampleIdentifiers` class
- New `SampleData` class to contain `sampleIdentifier`, `SampleGenotype` and `CopyNumber`
- New `SampleGenotypes` class to handle
- New `CopyNumber` class for handling copy number variation data from VCF
- New `AbstractVariant` class
- New `VariantAnnotations` interface
- New `AlleleCall.parseAlleleCall()` method
- New `Pedigree` `justProband(String id, Individual.Sex sex))` and `anscestorsOf(Pedigree.Individual individual)`
  methods
- New `SvFrequencyDao`, `SvPathogenicityDao` and `SvDaoUtil` classes
- New `VariantWhiteListLoader` class
- New `JannovarAnnotationService.annotateSvGenomeVariant()` method
- New `JannovarSmallVariantAnnotator` class
- New `JannovarStructuralVariantAnnotator` class
- New `TranscriptModelUtil` class
- New `VcfReader` interface with `VcfFileReader` and `NoOpVcfReader` implementations
- New `VariantContextConverter` class for converting `VariantContext` objects into `Variant`

Other changes:

- Updated Spring Boot to version 2.5.3
- Updated Jannovar to version 0.30
- Updated HTSJDK to version 2.24.1
- `AnalysisResults` now hold references to original `Sample` and `Analysis` objects
- `GenomeAnalysisService` can now return a `VariantAnnotator` object
- `GenomeAssembly` now wraps two `GenomicAssembly` objects
- Added `ClinVarData` `starRating()` and `isSecondaryAssociationRiskFactorOrOther()` methods
- Added DBVAR, DECIPHER, DGV, GNOMAD_SV and GONL SV `FrequencySource`
- Updated `VariantEffectPathogenicityScore` to become `final` and added default inversion score
- Numerous small changes to improve performance.

## 12.1.0 2019-09-25

- The JSON output now shows the id of the variantEvaluation taken from the VCF file.

New APIs:

- Added `VariantEvaluation.getId()` and `VariantEvaluation.Builder.id()` methods to store VCF id field contents.

## 12.0.0 2019-02-28

Its Rare-Disease Day 2019! Although we're not officially releasing this on Feb 28, we're code-complete and undergoing
final performance and quality tests, so it's as good as released. This was unplanned, and therefore proves that the
universe can have a sense of humour/appropriateness.

API breaking changes:

- Removed FREQUENCY_SOURCE_MAP from FrequencySource
- Changed `Frequency`, `RsId` and `PathogenicityScore` static `valueOf()` constructor to `of()`
- Removed deprecated `IntervalFilter.getGeneticInterval()`
- Changed visibility of `PhenodigmMatchRawScore` from public to package private and made immutable
- Changed visibility of `CrossSpeciesPhenotypeMatcher` from public to package private and added static `of()`
  constructor
- Replaced redundant `Default*DaoMvStoreProto` classes with new `AllelePropertiesDaoMvStore`
- Added `OntologyService` as constructor argument to `AnalysisFactory`, `AnalysisParser` and `AnalysisBuilder`
- Replaced `BasePathogenicityScore.compareTo()` method with default `PathogenicityScore.compareTo()`
- `GeneticInterval` no longer accepts `ReferenceDictionary` as a constructor argument

New APIs:

- Added CADD and REMM to data-genome `AlleleProperty`
- Moved `JannovarDataSourceLoader` from autoconfigure to core module
- Added `AllelePosition.isSymbolic()` method
- Added `Variant.isCodingVariant()` method
- Added `AnalysisBuilder.addIntervalFilter(Collection<ChromosomalRegion> chromosomalRegions)` method
- Added new non-public `FilterStats` class for more accurate filtering statistics
- Added new `AllelePropertiesDao` interface
- Added new `AllelePropertiesDaoMvStore` implementation
- Added new `AllelePropertiesDaoAdapter` to fix issue of Spring cache proxy not being able to intercept internal calls
- Added new `HpoIdChecker` class to return current HPO id/terms for an input id/term
- Added new `HumanPhenotypeOntologyDao.getIdToPhenotypeTerms()` method
- Added new `OntologyService.getCurrentHpoIds()` method
- Added new `SampleGenotype.isEmpty()` method
- Added new experimental `VcfCodecs` class for de/serialising VCF lines
- Added new `JannovarDataProtoSerialiser.loadProto()` method for loading intermediate `JannovarProto.JannovarData`
- Added new `VariantWhiteList` and `InMemoryVariantWhiteList` implementation
- Added new `VariantEvaluation.isWhiteListed()` method and relevant builder methods
- Added new `JannovarDataFactory` for a simple programmatic API to build `JannovarData` objects
- Added new `TranscriptSource` enum
- Added new `PathogenicityScore.of()` static factory constructor
- Added new `PathogenicityScore.getRawScore()` method
- Added default `PathogenicityScore.compareTo()` method
- Added new static `PathogenicityScore.compare()` method
- Added new `ScaledPathogenicityScore` class
- Added new `MpcScore` class
- Add new `Contig` class for converting contig names to integer-based id

Other changes:

- Updated Spring Boot to version 2.1.3
- Updated Jannovar to version 0.28
- Updated HTSJDK to version 2.18.2
- Refactored `FrequencyData` to use array-based backing for 5-10% memory usage improvement and lower GC especially when
  nearing max memory
- Refactored `AnalysisParser` to utilise `AnalysisBuilder` directly reducing code duplication
- Refactored `AnalysisRunner` classes to to utilise new `FilterStats` class
- Refactored `QueryPhenotypeMatch` to store and return input queryPhenotypeMatches argument
- Refactored `VariantDataServiceImpl` to use new AllelePropertiesDao
- Refactored `VariantDataServiceImpl` for better readability and performance
- Added check for obsolete HPO id input in `AnalysisBuilder.hpoIds()`
- Re-enabled `PhenixPrioritiser` in `AnalysisParser`
- Refactored `VariantEvaluation.getSampleGenotypeString()` implementation to use `SampleGenotype` instead
  of `VariantContext`
- Refactored `VariantEffectCounter` internals with `VariantEvaluation` calls in place of `VariantContext`
- Enabled flagging of variants on a whitelist and bypassing of `FrequencyFilter` and `VariantEffectFilter`
- Changed `DefaultDiseaseDao` to only return diseases marked as having known disease-gene association or
  copy-number/structural causes
- Added range check to `BasePathogenicityScore` constructor
- Updated `CaddScore` and `SiftScore` to extend `ScaledPathogenicityScore`
- Updated `CaddDao` to use CADD phred scaled score directly
- Replaced production use of `ReferenceDictionary` from `HG19RefDictBuilder` with `Contig`
- Added new `PathogenicitySource` sources - `M_CAP, MPC, MVP, PRIMATE_AI`. Be aware that these may not be free for
  commercial use.

## 11.0.0 2018-09-21
API breaking changes:

- Removed unused `VariantSerialiser`
- Moved `ChromosomalRegionIndex` from `analysis.util` package to `model`
- Changed `HiPhiveOptions.DEFAULT` to `HiPhiveOptions.defaults()` to match style with the rest of the framework
- Deleted redundant `MvStoreUtil.generateAlleleKey()` method in favour of `AlleleProtoAdaptor.toAlleleKey()`
- Split `VariantEffectPathogenicityScore.SPLICING_SCORE` into `SPLICE_DONOR_ACCEPTOR_SCORE` and `SPLICE_REGION_SCORE`
- Removed unused `VariantEvaluation.getNumberOfIndividuals()` and `VariantEvaluation.Builder.numIndividuals()`
- `InheritanceModeAnnotator` now requires an Exomiser `Pedigree` as input and no longer takes a
  Jannovar `de.charite.compbio.jannovar.pedigree.Pedigree`
- Changed `SampleIdentifier` default identifier from 'Sample' to 'sample' to fit existing internal implementation
  details
- Replaced `Analysis.AnalysisBuilder.pedPath(pedPath)` and `Analysis.getPedPath()`
  with `Analysis.AnalysisBuilder.pedigree(pedigree)` and `Analysis.getPedigree()`
- Replaced `AnalysisBuilder.pedPath(pedPath)`  with `AnalysisBuilder.pedigree(pedigree)`
- Removed obsolete `PedigreeFactory` - this functionality has been split amongst the new Pedigree API classes
- Removed `AnalysisMode.SPARSE` this was confusing and unused. Unless you need to debug a script, we advise
  using `AnalysisMode.PASS_ONLY`
- Replaced OutputSettings interface with the concrete implementation
- Replaced `OutputSettings.outputPassVariantsOnly()` with `OutputSettings.outputContributingVariantsOnly()`. This still
  has the default value of `false`

New APIs:

- Added new jannovar package and faster data serialisation format handled by the `JannovarDataProtoSerialiser`
  and `JannovarProtoConverter`.
- Added new native `Pedigree` class for representing pedigrees.
- Added new `PedFiles` class for reading PED files into a `Pedigree` object.
- Added new `PedigreeSampleValidator` to check the pedigree, proband and VCF samples are consistent with each other.
- Added `SampleIdentifier.defaultSample()` for use with unspecified single-sample VCF files.
- Added `InheritanceModeOptions.getMaxFreq()` method for retrieving the maximum frequency of all the defined inheritance
  modes.
- Added new no-args `AnalysisBuilder.addFrequencyFilter()` which uses maximum value from `InheritanceModeOptions`
- Added `Pedigree` support to `AnalysisBuilder`
- Added new `VariantEvaluation.getSampleGenotypes()` method to map sample names to genotype for that allele
- Added new utility constructors to `SampleGenotype` _e.g._ `SampleGenotype.het()` , `SampleGenotype.homRef()`

Other changes:

- Added support for REMM and CADD in `AlleleProtoAdaptor`
- Added check to remove alleles not called as ALT in proband
- `SampleGenotypes` now calculated for all variants in te `VariantFactory`
- Added support for `frequencyFilter: {}` to `AnalysisParser`
- Updated HTML output to display current SO terms for variant types/consequence
- Various code clean-up changes
- Changed dependency management to use spring-boot-dependencies rather than deprecated Spring Platform
- Updated Spring Boot to version 2.0.4

## 10.1.0 2018-05-09

- Added new simple `BedFiles` class for reading in `ChromosomalRegion` from an external file.
- Added support for filtering multiple intervals in the `IntervalFilter`
- Added support for parsing multiple intervals in the `AnalysisParser`
- Added new `OutputOption.JSON`
- Added new JsonResultsWriter - JSON results format should be considered as being in a 'beta' state and may or may not
  change slightly in the future.
- Added support for ClinVar annotations
- Added ClinVar annotations to `HTML` and `JSON` output options
- `TSV_GENE` and `TSV_VARIANT` output formats have been frozen as adding the new datasources will break the format. Use
  the JSON output for machines or HTML for humans.
- Updated Spring platform to Brussels-SR9. This will be the final Exomiser release on the Brussels release train.

## 10.0.1 2018-03-20

- Updated HTSJDK library to fix `TribbleException` being thrown when trying to parse bgzipped VCF files

## 10.0.0 2018-03-07
API breaking changes:

- Removed previously deprecated `Settings` and `SettingsParser` classes - this was only used by the cli which was also
  removed.
- Removed unused `PrioritiserSettings` and `PrioritiserSettingsImpl` classes - these were only used by
  the `SettingsParser`
- Removed unused `PrioritiserFactory.makePrioritiser(PrioritiserSettings settings)` method - this was only used by
  the `SettingsParser`
- Removed unused `PrioritiserFactory.getHpoIdsForDiseaseId(String diseaseId)` method. This
  duplicated/called `PriorityService.getHpoIdsForDiseaseId(String diseaseId)`
- Renamed `VariantTypePathogenicityScore` to `VariantEffectPathogenicityScore`
- Method names of `Inheritable` have changed from `InheritanceModes` to `CompatibleInheritanceModes` to better describe
  their function.
- Replaced `SampleNameChecker` with new `SampleIdentifierUtil`
- Changed signature of `InheritanceModeAnalyser` to require an `InheritanceModeAnnotator`. This is now using Exomiser
  and Jannovar-native calls to analyse inheritance modes instead of the Jannovar mendel-bridge.
- Changed `GeneScorer.scoreGene()` signature from `Consumer<Gene>` to `Function<Gene, List<GeneScore>>` to allow scoring
  of multiple inheritance modes in one run.
- Changed `Analysis` and `AnalysisBuilder` method `modeOfInheritance`
  to `inheritanceModes(InheritanceModeOptions inheritanceModeOptions)`
- Removed unused methods on `AnalysisResults`
- Renamed `OMIMPriority` to `OmimPriority`
- Renamed `OMIMPriorityResult` to `OmimPriorityResult`
- Changed `OmimPriorityResult` constructor to require `Map<ModeOfInheritance, Double> scoresByMode`, `getScoresByMode()`
  and `getScoreForMode(modeOfInheritance)` methods
- Changed `DataMatrix` from a concrete class to an interface
- Changed `ResultsWriter` signatures to require a `ModeOfInheritance` to write results out for.
- Changed `ResultsWriterUtils` now requires a specific `ModeOfInheritance`

New APIs:

- Added new `AlleleCall` class to represent allele calls for alleles from the VCF file
- Added new `GeneScore` class for holding results from the `GeneScorer`
- Added new `SampleIdentifier` class
- Added new `SampleGenotype` class to represent VCF GenotypeCalls for a sample on a particular allele.
- `GeneIdentifier` now implements `Comparable` and has a static `compare(geneIdentifier1, geneIdentifier2)` method
- `Gene` now contains `GeneScore` having been scored by a `GeneScorer`
- `VariantEvaluation` now has methods to determine its compatibility and whether or not it contributes to the overall
  score under a particular `ModeOfInheritance`
- Added new `SampleIdentifierUtil` to replace deleted `SampleNameChecker`
- Added new `InheritanceModeAnnotator` and `InheritanceModeOptions`
- Added new `VariantContextSampleGenotypeConverter` to create `SampleGenotype` from a `VariantContext`
- Added new `DataMatrixUtil`, `InMemoryDataMatrix`, `OffHeapDataMatrix`, `StubDataMatrix` implementations
- Added new methods on `DataMatrixIO` to facilitate loading new `DataMatrix` objects from disk.
- Added new `AnalysisResultsWriter` to handle writing out results instead of having to manually specify writers and
  inheritance modes

Other changes:

- Demoted most logging from `info` to `debug`
- Removed Spring control of Thymeleaf from `ThymeleafConfig` and `HtmlResultsWriter` so this no longer interferes with
  web templates

## 9.0.1 2018-01-15
- Updated the Jannovar library to 0.24 which now enables filtering for mitochondrial inheritance modes.

## 9.0.0 2017-12-12
In addition to the user-facing changes listed on the cli, the core has received extensive refactoring and changes.

- Maven groupId changed from root `org.monarchinitiative` to more specific `org.monarchinitiative.exomiser`.
- New `AlleleProto` protobuf class used to store allele data in the new MVStore.
- Replaced `DefaultPathogenicityDao` and `DefaultFrequencyDao` implementations with `MvStoreProto` implementations.
- Classes in the `genome` package are no longer under direct Spring control as the `@Component` and `@Autowired`
  annotations have been removed to enable user-defined genome assemblies on a per-analysis basis.
- `genome` package classes are now configured explicitly in the `exomiser-spring-boot-autoconfigure` module.
- New `GenomeAssembly` enum
- New `GenomeAnalysisServiceProvider` class
- New `GenomeAnalysisService` interface - a facade for providing simplified access to the genome module.
- New `VcfFiles` utility class for providing access to VCF files with the HTSJDK
- New `VariantAnnotator` interface
- New `JannovarVariantAnnotator` and `JannovarAnnotationService` classes
- `VariantFactoryImpl` now takes a `VariantAnnotator` as a constructor argument.
- `VariantDataService` getRegulatoryFeatures() and getTopologicalDomains() split out into new `GenomeDataService`
- Deprecated `Settings` class - this will be removed in the next major version.
- Updated classes in `analysis` package to enable analyses with user-defined genome assemblies.

## 8.0.0 2017-08-08
In addition to the user-facing changes listed on the cli, the core has received extensive refactoring and changes.

- Namespace changed from `de.charite.compbio` to `org.monarchinitiative`.
- Package layout has been changed to be more modular. New packages include `genome` and `phenotype`.
- `phenotype` package is independent of the others and contains the new `PhenodigmModelScorer`.
- Many classes are now immutable value objects, for example the `Frequency`, `FrequencyData` and `RsId` classes. These
  use static `of()` constructors.
- Builders are now used extensively and are exposed using the static `Class.builder()` method.
- Prioritisers have been extensively refactored and test coverage has been much improved from zero.
- `Prioritiser` interface signature change.
- `Exomiser` class now has static `getAnalysisBuilder()` exposing a fluent API for building and running an analysis.
- New `GeneSymbol` class for storing mappings between HGNC and the UCSC/ENSEMBL/REFSEQ gene identifiers.
- New `TranscriptAnnotation` class for storing transcript annotations. This provides a much-improved memory footprint.
- New `AllelePosition` class for storing POS, REF and ALT and also providing basic variant normalisation/trimming.
- New `TabixDataSource` interface to abstract the `TabixReader` allowing simpler testing and other benefits.

 
## 7.2.3 2016-11-02 
- Partial bug-fix for multi-sample VCF files where the proband sample is not the first sample in the genotypes section leading to occasional scores of 0 for the exomiser_gene_variant_score in cases where the variants are heterozygous and consistent with autosomal recessive.

*IMPORTANT!* As a workaround for this issue ensure the proband sample is the first sample in the VCF file. This will be properly fixed in the next major release.

## 7.2.2 2016-07-01 
- Fix for issue when using OmimPrioritiser with UNDEFINED inheritance mode which led to gene phenotype scores being halved.
- Fix for VCF output multiple allele line duplications. VCF output will now have alternate alleles written out on the same line if they were originally like that in the input VCF. The variant scores will be concatenated to correspond with the alleles. VCFs containing alleles split onto seperate lines in the input file will continue to have them like this in the output file.

## 7.2.1 2016-01-05
- Fix for incorrect inheritance mode calculations where the variant chromosome number is prefixed with 'chr' in VCF file.

## 7.2.0 2015-11-25
- Enabled TAD code in AbstractAnalysisRunner
- Added isNonCodingVariant() method to Variant interface.
- Deprecated VariantAnnotator and VariantFactory constructor which used this.
- Added new constructor for VariantFactory which takes a JannovarData object.
- Substantial tidy-up of test helper code with help of new TestFactory, GeneTranscripModelBuilder and VariantContextBuilder classes.

## 7.1.0 2015-10-21
- Added new ChromosomalRegion interface implemented by TopologicalDomain and RegulatoryRegion classes.
- Added new ChromosomalRegionIndex class for providing extremely fast lookups of variants in ChromosomalRegions.
- Removed RegulatoryFilterDataProvider - this functionality is now in the AbstractAnalysisRunner.

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
    - and a LOT more under the hood changes and clean-ups.

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
