# The Exomiser - Core Library Changelog

## 11.0.0 ????
API breaking changes:
- Removed unused ```VariantSerialiser```
- Moved ```ChromosomalRegionIndex``` from ```analysis.util``` package to ```model```
- Changed ```HiPhiveOptions.DEFAULT``` to ```HiPhiveOptions.defaults()``` to match style with the rest of the framework
- Deleted redundant ```MvStoreUtil.generateAlleleKey()``` method in favour of ```AlleleProtoAdaptor.toAlleleKey()```
- Split ```VariantEffectPathogenicityScore.SPLICING_SCORE``` into ```SPLICE_DONOR_ACCEPTOR_SCORE``` and ```SPLICE_REGION_SCORE```
- Removed unused ```VariantEvaluation.getNumberOfIndividuals()``` and ```VariantEvaluation.Builder.numIndividuals()```
- ```InheritanceModeAnnotator``` now requires an Exomiser ```Pedigree``` as input and no longer takes a Jannovar ```de.charite.compbio.jannovar.pedigree.Pedigree``` 
- Changed ```SampleIdentifier``` default identifier from 'Sample' to 'sample' to fit existing internal implementation details
- Replaced ```Analysis.AnalysisBuilder.pedPath(pedPath)``` and ```Analysis.getPedPath()``` with ```Analysis.AnalysisBuilder.pedigree(pedigree)``` and ```Analysis.getPedigree()```
- Replaced ```AnalysisBuilder.pedPath(pedPath)```  with ```AnalysisBuilder.pedigree(pedigree)```
- Removed obsolete ```PedigreeFactory``` - this functionality has been split amongst the new Pedigree API classes
- Removed ```AnalysisMode.SPARSE``` this was confusing and unused. Unless you need to debug a script, we advise using ```AnalysisMode.PASS_ONLY```
- Replaced OutputSettings interface with the concrete implementation
- Removed OutputSettings.outputPassVariantsOnly() with OutputSettings.outputContributingVariantsOnly(). This still has the default value of ```false```

New APIs:
- Added new jannovar package and faster data serialisation format handled by the ```JannovarDataProtoSerialiser``` and ```JannovarProtoConverter```.
- Added new native ```Pedigree``` class for representing pedigrees.
- Added new ```PedFiles``` class for reading PED files into a ```Pedigree``` object.
- Added new ```PedigreeSampleValidator``` to check the pedigree, proband and VCF samples are consistent with each other.
- Added ```SampleIdentifier.defaultSample()``` for use with unspecified single-sample VCF files.
- Added ```InheritanceModeOptions.getMaxFreq()``` method for retrieving the maximum frequency of all the defined inheritance modes.
- Added new no-args ```AnalysisBuilder.addFrequencyFilter()``` which uses maximum value from ```InheritanceModeOptions```
- Added ```Pedigree``` support to ```AnalysisBuilder```
- Added new ```VariantEvaluation.getSampleGenotypes()``` method to map sample names to genotype for that allele
- Added new utility constructors to ```SampleGenotype``` _e.g._ ```SampleGenotype.het()``` , ```SampleGenotype.homRef()```

Other changes:
- Added support for REMM and CADD in ```AlleleProtoAdaptor```
- Added check to remove alleles not called as ALT in proband
- ```SampleGenotypes``` now calculated for all variants in te ```VariantFactory```
- Added support ```frequencyFilter: {}``` to ```AnalysisParser```
- Updated HTML output to display current SO terms for variant types/consequence
- Various code clean-up changes
- Changed dependency management to use spring-boot-dependencies rather than deprecated Spring Platform
- Updated Spring Boot to version 2.0.4

## 10.1.0 2018-05-09
- Added new simple ```BedFiles``` class for reading in ```ChromosomalRegion``` from an external file. 
- Added support for filtering multiple intervals in the ```IntervalFilter``` 
- Added support for parsing multiple intervals in the ```AnalysisParser```
- Added new ```OutputOption.JSON```
- Added new JsonResultsWriter - JSON results format should be considered as being in a 'beta' state and may or may not change slightly in the future.
- Added support for ClinVar annotations 
- Added ClinVar annotations to ```HTML``` and ```JSON``` output options
- ```TSV_GENE``` and ```TSV_VARIANT``` output formats have been frozen as adding the new datasources will break the format. Use the JSON output for machines or HTML for humans. 
- Updated Spring platform to Brussels-SR9. This will be the final Exomiser release on the Brussels release train.

## 10.0.1 2018-03-20
- Updated HTSJDK library to fix ```TribbleException``` being thrown when trying to parse bgzipped VCF files

## 10.0.0 2018-03-07
API breaking changes:
- Removed previously deprecated ```Settings``` and ```SettingsParser``` classes - this was only used by the cli which was also removed.
- Removed unused ```PrioritiserSettings``` and ```PrioritiserSettingsImpl``` classes - these were only used by the ```SettingsParser```
- Removed unused ```PrioritiserFactory.makePrioritiser(PrioritiserSettings settings)``` method - this was only used by the ```SettingsParser```
- Removed unused ```PrioritiserFactory.getHpoIdsForDiseaseId(String diseaseId)``` method. This duplicated/called ```PriorityService.getHpoIdsForDiseaseId(String diseaseId)```
- Renamed ```VariantTypePathogenicityScore``` to ```VariantEffectPathogenicityScore```
- Method names of ```Inheritable``` have changed from ```InheritanceModes``` to ```CompatibleInheritanceModes``` to better describe their function.
- Replaced ```SampleNameChecker``` with new ```SampleIdentifierUtil``` 
- Changed signature of ```InheritanceModeAnalyser``` to require an ```InheritanceModeAnnotator```. This is now using Exomiser and Jannovar-native calls to analyse inheritance modes instead of the Jannovar mendel-bridge.
- Changed ```GeneScorer.scoreGene()``` signature from ```Consumer<Gene>``` to ```Function<Gene, List<GeneScore>>``` to allow scoring of multiple inheritance modes in one run.
- Changed ```Analysis``` and ```AnalysisBuilder``` method ```modeOfInheritance``` to ```inheritanceModes(InheritanceModeOptions inheritanceModeOptions)```
- Removed unused methods on ```AnalysisResults```
- Renamed ```OMIMPriority``` to ```OmimPriority```
- Renamed ```OMIMPriorityResult``` to ```OmimPriorityResult```
- Changed ```OmimPriorityResult``` constructor to require ```Map<ModeOfInheritance, Double> scoresByMode```, ```getScoresByMode()``` and ```getScoreForMode(modeOfInheritance)``` methods 
- Changed ```DataMatrix``` from a concrete class to an interface
- Changed ```ResultsWriter``` signatures to require a ```ModeOfInheritance``` to write results out for.
- Changed ```ResultsWriterUtils``` now requires a specific ```ModeOfInheritance``` 

New APIs:
- Added new ```AlleleCall``` class to represent allele calls for alleles from the VCF file
- Added new ```GeneScore``` class for holding results from the ```GeneScorer```
- Added new ```SampleIdentifier``` class
- Added new ```SampleGenotype``` class to represent VCF GenotypeCalls for a sample on a particular allele.
- ```GeneIdentifier``` now implements ```Comparable``` and has a static ```compare(geneIdentifier1, geneIdentifier2)``` method
- ```Gene``` now contains ```GeneScore``` having been scored by a ```GeneScorer```
- ```VariantEvaluation``` now has methods to determine its compatibility and whether or not it contributes to the overall score under a particular ```ModeOfInheritance```
- Added new ```SampleIdentifierUtil``` to replace deleted ```SampleNameChecker```
- Added new ```InheritanceModeAnnotator``` and ```InheritanceModeOptions```
- Added new ```VariantContextSampleGenotypeConverter``` to create ```SampleGenotype``` from a ```VariantContext```
- Added new ```DataMatrixUtil```, ```InMemoryDataMatrix```, ```OffHeapDataMatrix```, ```StubDataMatrix``` implementations
- Added new methods on ```DataMatrixIO``` to facilitate loading new ```DataMatrix``` objects from disk.
- Added new ```AnalysisResultsWriter``` to handle writing out results instead of having to manually specify writers and inheritance modes 

Other changes:
- Demoted most logging from ```info``` to ```debug```
- Removed Spring control of Thymeleaf from ```ThymeleafConfig``` and ```HtmlResultsWriter``` so this no longer interferes with web templates

## 9.0.1 2018-01-15
- Updated the Jannovar library to 0.24 which now enables filtering for mitochondrial inheritance modes.

## 9.0.0 2017-12-12
In addition to the user-facing changes listed on the cli, the core has received extensive refactoring and changes.
- Maven groupId changed from root ```org.monarchinitiative``` to more specific ```org.monarchinitiative.exomiser```.
- New ```AlleleProto``` protobuf class used to store allele data in the new MVStore.
- Replaced ```DefaultPathogenicityDao``` and ```DefaultFrequencyDao``` implementations with ```MvStoreProto``` implementations.
- Classes in the ```genome``` package are no longer under direct Spring control as the ```@Component``` and ```@Autowired``` annotations have been removed to enable user-defined genome assemblies on a per-analysis basis.
- ```genome``` package classes are now configured explicitly in the ```exomiser-spring-boot-autoconfigure``` module.
- New ```GenomeAssembly``` enum
- New ```GenomeAnalysisServiceProvider``` class
- New ```GenomeAnalysisService``` interface - a facade for providing simplified access to the genome module.
- New ```VcfFiles``` utility class for providing access to VCF files with the HTSJDK
- New ```VariantAnnotator``` interface
- New ```JannovarVariantAnnotator``` and ```JannovarAnnotationService``` classes
- ```VariantFactoryImpl``` now takes a ```VariantAnnotator``` as a constructor argument.
- ```VariantDataService``` getRegulatoryFeatures() and getTopologicalDomains() split out into new ```GenomeDataService```
- Deprecated ```Settings``` class - this will be removed in the next major version.
- Updated classes in ```analysis``` package to enable analyses with user-defined genome assemblies.

## 8.0.0 2017-08-08
In addition to the user-facing changes listed on the cli, the core has received extensive refactoring and changes.
- Namespace changed from ```de.charite.compbio``` to ```org.monarchinitiative```. 
- Package layout has been changed to be more modular. New packages include ```genome``` and ```phenotype```.
- ```phenotype``` package is independent of the others and contains the new ```PhenodigmModelScorer```.
- Many classes are now immutable value objects, for example the ```Frequency```, ```FrequencyData``` and ```RsId``` classes. These use static ```of()``` constructors.
- Builders are now used extensively and are exposed using the static ```Class.builder()``` method.
- Prioritisers have been extensively refactored and test coverage has been much improved from zero.
- ```Prioritiser``` interface signature change.
- ```Exomiser``` class now has static ```getAnalysisBuilder()``` exposing a fluent API for building and running an analysis.
- New ```GeneSymbol``` class for storing mappings between HGNC and the UCSC/ENSEMBL/REFSEQ gene identifiers.
- New ```TranscriptAnnotation``` class for storing transcript annotations. This provides a much-improved memory footprint.
- New ```AllelePosition``` class for storing POS, REF and ALT and also providing basic variant normalisation/trimming.
- New ```TabixDataSource``` interface to abstract the ```TabixReader``` allowing simpler testing and other benefits. 
 
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
