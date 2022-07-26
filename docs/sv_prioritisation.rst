.. _sv_prioritisation:

=================================
Structural Variant Prioritisation
=================================

Exomiser v13.0.0 is capable of jointly prioritising structural and sequence variants from a combined VCF file. For the
purposes of the analysis a structural variant is defined as a variant >= 50 nucleotides in length or a symbolic variant
(e.g. a variant with a VCF ALT allele of the form ``<DEL>`` instead of an actual sequence). There are many, many callers
to choose from so performance will depend heavily on these as well as the underlying sequencing technology (e.g. Illumina
short-read vs PacBio or Oxford Nanopore long-read sequencing). Exomiser has been tested on `Manta
<https://github.com/Illumina/manta>`_ and `Canvas <https://github.com/Illumina/canvas>`_ calls from Illumina short-reads
produced for the 100K genomes project, with some compatibility testing against `PacBio pbsv <https://github.com/PacificBiosciences/pbsv>`_.

Prioritisation Overview
=======================

Assuming the recommended analysis steps are being used, Exomiser will broadly consider structural variants in a similar
manner to sequence variants, with the major difference being that the precise size, position and sequence change is not
known, especially for symbolic variants. Briefly, Exomiser performs these steps on each SV:

- Predict variant effects based on overlapping transcripts
- Assign variant to all genes for which it overlaps a transcript
- Assign variant pathogenicity score according to variant effect and known similar ClinVar variants
- Assign variant frequency score according to similar alleles in gnomAD-SV, DECIPHER, dbVar, DGV, GoNL

Following these SV-specific steps, the variant is considered in the same way as a sequence variant and is filtered and
prioritised accordingly. This allows compound heterozygous genotypes of structural and sequence variants to be considered
during an analysis.

Multiple Gene Overlaps
======================

If a variant overlaps more than one gene, the variant will be associated with all of these genes and reported with the
most severe variant effect it is predicted to have on each gene in the results.

For example, if a variant 1-23456-78910-N-<DEL> completely deletes GENE:1 and deletes the first exon of GENE:2, the
variant will be reported twice, once for each associated gene. The following pseudo-code tries to illustrate this.

.. code-block:: yaml

    GENE1:
        variants:
            - 1-23456-78910-N-<DEL>
                variantEffect: TRANSCRIPT_ABLATION
    GENE2:
        variants:
            - 1-23456-78910-N-<DEL>
                variantEffect: EXON_LOSS_VARIANT


SV Similarity
=============

Given their imprecise nature, SVs are not looked-up in the database using precise coordinates, instead they are
considered 'equal' if their genomic coordinates have a jaccard similarity of 0.8 and they constitute an equivalent broad
type - gain, loss, me_gain, me_loss, inversion or complex.
