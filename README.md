The Exomiser - A Tool to Annotate and Prioritize Exome Variants
===============================================================
####Branch build status:
Master: [![CircleCI](https://circleci.com/gh/exomiser/Exomiser/tree/master.svg?style=shield)](https://circleci.com/gh/exomiser/Exomiser/tree/master)
Development: [![CircleCI](https://circleci.com/gh/exomiser/Exomiser/tree/development.svg?style=shield)](https://circleci.com/gh/exomiser/Exomiser/tree/development)

####Package Structure:

- exomiser-core (core library)
- exomiser-spring-boot-starter 
- exomiser-cli (command-line interface)
- exomiser-db (database build)
- exomiser-web (web interface)

####Using The Exomiser in your code

Add exomiser-spring-boot-starter to your pom/gradle build script.

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
 
####Memory usage

Analysing whole genomes using the ``AnalysisMode.FULL`` or ``AnalysisMode.SPARSE`` will use a lot of RAM (~16GB for 4.5 million variants without any extra variant data being loaded) the standard Java GC will fail to cope well with these.
Using the G1GC should solve this issue. e.g. add ``-XX:+UseG1GC`` to your ``java -jar -Xmx...`` incantation. 
 