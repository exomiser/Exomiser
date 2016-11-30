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

In your application use the AnalysisFactory to configure your analysis. Run the Analysis using the Exomiser class.
Creation of the AnalysisFactory is a complicated process so defer this to Spring and the exomiser-spring-boot-starter.

Example usage:
```
@Autowired
private final Exomiser exomiser;
@Autowired
private final AnalysisFactory analysisFactory;

...
           
    Analysis analysis = analysisFactory.getAnalysisBuilder()
                .vcfPath(vcfPath)
                .pedPath(pedPath)
                .hpoIds(phenotypes)
                .analysisMode(AnalysisMode.PASS_ONLY)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .frequencySources(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES)
                .pathogenicitySources(EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER, PathogenicitySource.SIFT))
                .addPhivePrioritiser()
                .addPriorityScoreFilterStep(PriorityType.PHIVE_PRIORITY, 0.501f)
                .addQualityFilterStep(500.0)
                .addRegulatoryFeatureFilterStep()
                .addFrequencyFilterStep(0.01f)
                .addPathogenicityFilterStep(true)
                .addInheritanceFilter()
                .addOmimPrioritiser()
                .build();
                
    AnalysisResults analysisResults = exomiser.run(analysis);
```
 


