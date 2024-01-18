The Exomiser - A Tool to Annotate and Prioritize Exome Variants
===============================================================

[![GitHub release](https://img.shields.io/github/release/exomiser/Exomiser.svg)](https://github.com/exomiser/Exomiser/releases)
[![CircleCI](https://circleci.com/gh/exomiser/Exomiser/tree/development.svg?style=shield)](https://circleci.com/gh/exomiser/Exomiser/tree/development)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b518a9448b5b4889a40b3dc660ef585a)](https://www.codacy.com/app/monarch-initiative/Exomiser?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=exomiser/Exomiser&amp;utm_campaign=Badge_Grade)
[![Documentation](https://readthedocs.org/projects/exomiser/badge/?version=latest)](http://exomiser.readthedocs.io/en/latest)
#### Overview:

The Exomiser is a Java program that finds potential disease-causing variants from whole-exome or whole-genome sequencing data.

Starting from a [VCF](https://samtools.github.io/hts-specs/VCFv4.3.pdf) file and a set of phenotypes encoded using the [Human Phenotype Ontology](http://www.human-phenotype-ontology.org) (HPO) it will annotate, filter and prioritise likely causative variants. The program does this based on user-defined criteria such as a variant's predicted pathogenicity, frequency of occurrence in a population and also how closely the given phenotype matches the known phenotype of diseased genes from human and model organism data.

The functional annotation of variants is handled by [Jannovar](https://github.com/charite/jannovar) and uses any of [UCSC](http://genome.ucsc.edu), [RefSeq](https://www.ncbi.nlm.nih.gov/refseq/) or [Ensembl](https://www.ensembl.org/Homo_sapiens/Info/Index) KnownGene transcript definitions and hg19 or hg38 genomic coordinates.

Variants are prioritised according to user-defined criteria on variant frequency, pathogenicity, quality, inheritance pattern, and model organism phenotype data. Predicted pathogenicity data is extracted from the [dbNSFP](http://www.ncbi.nlm.nih.gov/pubmed/21520341) resource. Variant frequency data is taken from the [1000 Genomes](http://www.1000genomes.org/), [ESP](http://evs.gs.washington.edu/EVS), [TOPMed](http://www.uk10k.org/studies/cohorts.html), [UK10K](http://www.uk10k.org/studies/cohorts.html), [ExAC](http://exac.broadinstitute.org) and [gnomAD](http://gnomad.broadinstitute.org/) datasets. Subsets of these frequency and pathogenicity data can be defined to further tune the analysis. Cross-species phenotype comparisons come from our PhenoDigm tool powered by the OWLTools [OWLSim](https://github.com/owlcollab/owltools) algorithm.

The Exomiser was developed by the Computational Biology and Bioinformatics group at the Institute for Medical Genetics and Human Genetics of the Charité - Universitätsmedizin Berlin, the Mouse Informatics Group at the Sanger Institute and other members of the [Monarch initiative](https://monarchinitiative.org).

#### Download and Installation

The prebuilt Exomiser binaries can be obtained from the [releases](https://github.com/exomiser/Exomiser/releases) page and supporting data files can be downloaded from the [Exomiser FTP site](http://data.monarchinitiative.org/exomiser/latest).

It is possible to use the same data sources for multiple versions, in order to avoid having to download the data files for each software point release. We recommend maintaining a dedicated exomiser data directory where you can extract versions of the hg19, hg38 and phenotype data. To do this, edit the ```exomiser.data-directory``` field in the ```application.properties``` file to point to the dedicated data directory. The version for the data releases should also be specified in the ```application.properties``` file:
    
For example, if you have an exomiser installation located at ```/opt/exomiser-cli-11.0.0``` and you have extracted the data files to the directory ```/opt/exomiser-data```. When there is a new data release, you can change the data versions by specifying the version in the ```/opt/exomiser-cli-11.0.0/application.properties``` from
```properties
# root path where data is to be downloaded and worked on
# it is assumed that all the files required by exomiser listed in this properties file
# will be found in the data directory unless specifically overridden here.
exomiser.data-directory=data

# old data versions
exomiser.hg19.data-version=1802
...
exomiser.hg38.data-version=1802
...
exomiser.phenotype.data-version=1802
```
to
```properties
# overridden data-directory containing multiple data versions
exomiser.data-directory=/opt/exomiser-data

# updated data versions
exomiser.hg19.data-version=1805
...
exomiser.hg38.data-version=1805
...
exomiser.phenotype.data-version=1807
```

We strongly recommend using the latest versions of both the application and the data for optimum results.

For further instructions on installing and running please refer to the [README.md](http://data.monarchinitiative.org/exomiser/README.md) file.

#### Running it

Please refer to the [manual](https://exomiser.readthedocs.io/en/latest/) for details on how to configure and run the Exomiser.

#### Demo site

There is a limited [demo version](http://exomiser.monarchinitiative.org/exomiser/) of the exomiser hosted by the [Monarch Initiative](https://monarchinitiative.org/). This instance is for teaching purposes only and is limited to small exome analysis.

#### Using The Exomiser in your code

The exomiser can also be used as a library in Spring Java applications. Add the ```exomiser-spring-boot-starter``` library to your pom/gradle build script.

In your configuration class add the ```@EnableExomiser``` annotation
 
 ```java
@EnableExomiser
public class MainConfig {
    
}
```

Or if using Spring boot for your application, the exomiser will be autoconfigured if it is on your classpath.

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

In your application use the AnalysisBuilder obtained from the Exomiser instance to configure your analysis. Then run the Analysis using the Exomiser class.
Creation of the Exomiser is a complicated process so defer this to Spring and the exomiser-spring-boot-starter. Calling the ```add``` prefixed methods 
will add that analysis step to the analysis in the order that they have been defined in your code.

Example usage:
```
@Autowired
private final Exomiser exomiser;

...
           
    Analysis analysis = exomiser.getAnalysisBuilder()
                .genomeAssembly(GenomeAssembly.HG19)
                .vcfPath(vcfPath)
                .pedPath(pedPath)
                .probandSampleName(probandSampleId)
                .hpoIds(phenotypes)
                .analysisMode(AnalysisMode.PASS_ONLY)
                .modeOfInheritance(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE))
                .frequencySources(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES)
                .pathogenicitySources(EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER, PathogenicitySource.SIFT))
                .addPhivePrioritiser()
                .addPriorityScoreFilter(PriorityType.PHIVE_PRIORITY, 0.501f)
                .addQualityFilter(500.0)
                .addRegulatoryFeatureFilter()
                .addFrequencyFilter(0.01f)
                .addPathogenicityFilter(true)
                .addInheritanceFilter()
                .addOmimPrioritiser()
                .build();
                
    AnalysisResults analysisResults = exomiser.run(analysis);
```
 
#### Memory usage

Analysing whole genomes using the ``AnalysisMode.FULL`` will use a lot of RAM (~16GB for 4.5 million variants without any extra variant data being loaded) the standard Java GC will fail to cope well with these.
Using the G1GC should solve this issue. e.g. add ``-XX:+UseG1GC`` to your ``java -jar -Xmx...`` incantation. 

#### Caching

Since 9.0.0 caching uses the standard Spring mechanisms.
 
To enable and configure caching in your Spring application, use the ```@EnableCaching``` annotation on a ```@Configuration``` class, include the required cache implementation jar and add the specific properties to the ```application.properties```.

For example, to use [Caffeine](https://github.com/ben-manes/caffeine) just add the dependency to your pom:

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```
and these lines to the ```application.properties```:
```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=300000
```

#### Recognition

The Exomiser is proud to be recognised by the International Rare Diseases Research Consortium ([IRDiRC](http://www.irdirc.org/)) as an [IRDiRC Recognized Resource](http://www.irdirc.org/research/irdirc-recognized-resources/). This is *'a quality indicator, based on a specific set of criteria, that was created to highlight key resources which, if used more broadly, would accelerate the pace of translating discoveries into clinical applications.'* These resources *'must be of fundamental importance to the international rare diseases research and development community'*.

[![IRDiRC recognised resource](LOGO_IRDIRCRR_RVB.jpg)](http://www.irdirc.org/)
