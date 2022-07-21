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


Multiple Gene Overlaps
======================

Similarity
==========

Frequency
=========

Pathogenicity
=============

