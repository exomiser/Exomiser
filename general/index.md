---
layout: page
title: General information
subtitle: The Exomiser software suite
---

* TOC
{:toc}

# Overview

The Exomiser is a Java program that finds potential disease-causing variants from whole-exome or whole-genome sequencing data.

Starting from a VCF file and a set of phenotypes encoded using the [Human Phenotype Ontology](http://www.human-phenotype-ontology.org) (HPO) it will annotate, filter and prioritise likely causative variants. The program does this based on user-defined criteria such as a variant's predicted pathogenicity, frequency of occurrence in a population and also how closely the given phenotype matches the known phenotype of diseased genes from human and model organism data.

The functional annotation of variants is handled by [Jannovar](https://github.com/charite/jannovar) and uses [UCSC](http://genome.ucsc.edu) KnownGene transcript definitions and hg19 genomic coordinates.

Variants are prioritised according to user-defined criteria on variant frequency, pathogenicity, quality, inheritance pattern, and model organism phenotype data. Predicted pathogenicity data is extracted from the [dbNSFP](http://www.ncbi.nlm.nih.gov/pubmed/21520341) resource. Variant frequency data is taken from the [1000 Genomes](http://www.1000genomes.org), [ESP](http://evs.gs.washington.edu/EVS/) and [ExAC](http://exac.broadinstitute.org) datasets. Subsets of these frequency and pathogenicity data can be defined to further tune the analysis. Cross-species phenotype comparisons come from our [PhenoDigm](http://www.sanger.ac.uk/resources/databases/phenodigm) tool powered by the [OWLTools](https://github.com/owlcollab/owltools) OWLSim algorithm.

The Exomiser was developed by the [Computational Biology and Bioinformatics group](http://compbio.charite.de) at the Institute for [Medical Genetics and Human Genetics](http://genetik.charite.de) of the [Charité - Universitätsmedizin Berlin](http://charite.de), the Mouse Informatics Group at the Sanger Institute and other members of the [Monarch initiative](http://monarchinitiative.org).


# Prioritisers

# Variant annotation
