The Exomiser - A Tool to Annotate and Prioritize Exome Variants
===============================================================

####Overview:

The Exomiser is a Java program that finds potential disease-causing variants from whole-exome or whole-genome sequencing data.

Starting from a VCF file and a set of phenotypes encoded using the [Human Phenotype Ontology](http://www.human-phenotype-ontology.or) (HPO) it will annotate, filter and prioritise likely causative variants. The program does this based on user-defined criteria such as a variant's predicted pathogenicity, frequency of occurrence in a population and also how closely the given phenotype matches the known phenotype of diseased genes from human and model organism data.

The functional annotation of variants is handled by [Jannovar](https://github.com/charite/jannovar) and uses [UCSC](http://genome.ucsc.edu) KnownGene transcript definitions and hg19 genomic coordinates.

Variants are prioritised according to user-defined criteria on variant frequency, pathogenicity, quality, inheritance pattern, and model organism phenotype data. Predicted pathogenicity data is extracted from the [dbNSFP](http://www.ncbi.nlm.nih.gov/pubmed/21520341) resource. Variant frequency data is taken from the [1000 Genomes](http://www.1000genomes.org/), [ESP](http://evs.gs.washington.edu/EVS) and [ExAC](http://exac.broadinstitute.org) datasets. Subsets of these frequency and pathogenicity data can be defined to further tune the analysis. Cross-species phenotype comparisons come from our PhenoDigm tool powered by the OWLTools [OWLSim](https://github.com/owlcollab/owltools) algorithm.

The Exomiser was developed by the Computational Biology and Bioinformatics group at the Institute for Medical Genetics and Human Genetics of the Charité - Universitätsmedizin Berlin, the Mouse Informatics Group at the Sanger Institute and other members of the [Monarch initiative](https://monarchinitiative.org).

####Download and Installation

The prebuilt Exomiser binaries can be obtained from the [releases](https://github.com/exomiser/Exomiser/releases) page and supporting data files can be downloaded from the [Exomiser FTP site](http://data.monarchinitiative.org/exomiser).

For instructions on installing and running please refer to the [README.md](http://data.monarchinitiative.org/exomiser/README.md) file.

####Running it

Please refer to the [manual](http://exomiser.github.io/Exomiser/) for details on how to configure and run the Exomiser.
