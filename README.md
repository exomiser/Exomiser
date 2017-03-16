The Exomiser - A Tool to Annotate and Prioritize Exome Variants
===============================================================
#### Branch build status:
Master: [![CircleCI](https://circleci.com/gh/exomiser/Exomiser/tree/master.svg?style=shield)](https://circleci.com/gh/exomiser/Exomiser/tree/master)
Development: [![CircleCI](https://circleci.com/gh/exomiser/Exomiser/tree/development.svg?style=shield)](https://circleci.com/gh/exomiser/Exomiser/tree/development)

#### Overview:

The Exomiser is a Java program that finds potential disease-causing variants from whole-exome or whole-genome sequencing data.

Starting from a VCF file and a set of phenotypes encoded using the [Human Phenotype Ontology](http://www.human-phenotype-ontology.or) (HPO) it will annotate, filter and prioritise likely causative variants. The program does this based on user-defined criteria such as a variant's predicted pathogenicity, frequency of occurrence in a population and also how closely the given phenotype matches the known phenotype of diseased genes from human and model organism data.

The functional annotation of variants is handled by [Jannovar](https://github.com/charite/jannovar) and uses [UCSC](http://genome.ucsc.edu) KnownGene transcript definitions and hg19 genomic coordinates.

Variants are prioritised according to user-defined criteria on variant frequency, pathogenicity, quality, inheritance pattern, and model organism phenotype data. Predicted pathogenicity data is extracted from the [dbNSFP](http://www.ncbi.nlm.nih.gov/pubmed/21520341) resource. Variant frequency data is taken from the [1000 Genomes](http://www.1000genomes.org/), [ESP](http://evs.gs.washington.edu/EVS) and [ExAC](http://exac.broadinstitute.org) datasets. Subsets of these frequency and pathogenicity data can be defined to further tune the analysis. Cross-species phenotype comparisons come from our PhenoDigm tool powered by the OWLTools [OWLSim](https://github.com/owlcollab/owltools) algorithm.

The Exomiser was developed by the Computational Biology and Bioinformatics group at the Institute for Medical Genetics and Human Genetics of the Charité - Universitätsmedizin Berlin, the Mouse Informatics Group at the Sanger Institute and other members of the [Monarch initiative](https://monarchinitiative.org).

#### Download and Installation

The prebuilt Exomiser binaries can be obtained from the [releases](https://github.com/exomiser/Exomiser/releases) page and supporting data files can be downloaded from the [Exomiser FTP site](http://data.monarchinitiative.org/exomiser).

For instructions on installing and running please refer to the [README.md](http://data.monarchinitiative.org/exomiser/README.md) file.

#### Running it

Please refer to the [manual](http://exomiser.github.io/Exomiser/) for details on how to configure and run the Exomiser.

#### Using The Exomiser in your code

The exomiser can also be used as a library in Spring Java applications. Add the ```exomiser-spring-boot-starter``` library to your pom/gradle build script.

In your configuration class add the ```@EnableExomiser``` annotation
 
 ```java
@EnableExomiser
public class MainConfig {
    
}
```

Or if using Spring boot for your application, you can add it on your main class

```java
@EnableExomiser
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
                .vcfPath(vcfPath)
                .pedPath(pedPath)
                .probandSampleName(probandSampleId)
                .hpoIds(phenotypes)
                .analysisMode(AnalysisMode.PASS_ONLY)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
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

Analysing whole genomes using the ``AnalysisMode.FULL`` or ``AnalysisMode.SPARSE`` will use a lot of RAM (~16GB for 4.5 million variants without any extra variant data being loaded) the standard Java GC will fail to cope well with these.
Using the G1GC should solve this issue. e.g. add ``-XX:+UseG1GC`` to your ``java -jar -Xmx...`` incantation. 
 