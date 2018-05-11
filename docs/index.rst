..
    exomiser documentation master file, created by
    sphinx-quickstart on Wed Mar 07 09:59:52 2018.
    You can adapt this file completely to your liking, but it should at least
    contain the root `toctree` directive.

Welcome to the Exomiser documentation!
======================================

Introduction
============
The Exomiser is a Java program, developed as a collaboration between members of the Monarch Initiative for finding potential disease-causing variants in whole-exome or whole-genome sequencing data. It is available for use by all as an offline command-line tool, or on the web as a demo application/teaching tool.

Starting from a VCF file and a set of phenotypes encoded using the Human Phenotype Ontology (HPO) it will annotate, filter and prioritise likely causative variants. The program does this based on user-defined criteria such as a variant's predicted pathogenicity, frequency of occurrence in a population and also how closely the given phenotype matches the known phenotype of diseased genes from human and model organism data.

The functional annotation of variants is handled by the Jannovar library and uses any of UCSC, RefSeq or Ensembl KnownGene transcript definitions and hg19 or hg38 genomic coordinates.

Variants are prioritised according to user-defined criteria on variant frequency, pathogenicity, quality, inheritance pattern, and model organism phenotype data. Predicted pathogenicity data is extracted from the dbNSFP resource. Variant frequency data is taken from the 1000 Genomes, ESP, TOPMed, UK10K, ExAC and gnomAD datasets. Subsets of these frequency and pathogenicity data can be defined to further tune the analysis. Cross-species phenotype comparisons come from our PhenoDigm tool powered by the OWLTools OWLSim algorithm.


.. toctree::
    :caption: Running the Exomiser
    :name: running-it
    :maxdepth: 2
    :hidden:

    installation
    getting_started

.. toctree::
    :caption: Interpretation of Results
    :name: whats-it-all-mean
    :maxdepth: 2
    :hidden:

    result_interpretation

.. toctree::
    :caption: Publications
    :name: publications
    :maxdepth: 1
    :hidden:

    publications