.. _result_interpretation:

========================
Interpreting the Results
========================

Depending on the output options provided, Exomiser will write out at least an HTML results file in the `results`
sub-directory of the Exomiser installation.

As a general rule all output files contain a ranked list of genes and/or variants with the top-ranked gene/variant
displayed first. The exception being the VCF output which, since version 13.1.0, is sorted according to VCF convention
and tabix indexed.

Exomiser attempts to predict the variant or variants likely to be causative of a patient's phenotype and does so by
associating them with the gene (or genes in the case of large structural variations) they intersect with on the genomic
sequence. Variants occurring in intergenic regions are associated to the closest gene and those overlapping two genes
are associated with the gene in which they are predicted to have the largest consequence.

Once associated with a gene, Exomiser uses the compatible modes of inheritance for a variant to assess it in the context
of any diseases associated with the gene or any mouse knockout models of that gene. These are all bundled together into
a `GeneScore`

HTML
====


JSON
====


TSV GENES
=========


TSV VARIANTS
============


VCF
===


todo: ACMG categories, SV algo