.. _acmg_assignment:

===============
ACMG Assignment
===============

Starting with version 13.1.0, Exomiser performs a partial categorisation of the variants contributing to the gene
score for a mode of inheritance using the ACMG/AMP `Standards and guidelines for the interpretation of sequence
variants: a joint consensus recommendation of the American College of Medical Genetics and Genomics and the Association
 for Molecular Pathology <https://doi.org/10.1038/gim.2015.30>`_. The scores are scored and combined according to the
`UK ACGS 2020 guidelines <https://www.acgs.uk.com/media/11631/uk-practice-guidelines-for-variant-classification-v4-01-2020.pdf>`_.
Exomiser is capable of assigning the following ACMG categories. It is important to be aware that these scores are not a substitute
for manual assignment by a qualified clinical geneticist or clinician - The scores displayed utilise the data found in
the Exomiser database and are a subset of the possible criteria by which to assess a variant. Nonetheless, in our
benchmarking on the returned cases from the 100K Genomes Project, variants in the top 5 with a P/LP classification had a
much higher precision than those not classified as P/LP.


Computational and Predictive Data
=================================
PVS1
----
Variants must have a predicted loss of function effect, be in a gene with known disease associations and have a gene
constraint LOF O/E < 0.7635 (gnomAD 2.1.1) to suggest that a gene is LoF intolerant. Variants not predicted to lead to
NMD (those located in the last exon) will have the modifier downgraded to Strong.

PM4
---
Stop-loss and in-frame insertions or deletions, not previously assigned a `PVS1` criterion are assigned `PM4`.

PP3 / BP4
---------
If REVEL is chosen as a pathogenicity predictor for missense variants, `PP3` and `BP4` are assigned using the modifiers
according to table 2 of `Evidence-based calibration of computational tools for missense variant pathogenicity classification
 and ClinGen recommendations for clinical use of PP3/BP4 criteria <https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1>`_.
Note that this suggests the use of modifiers up to Strong in the case of pathogenic or Very Strong in the case of benign predictions.
Otherwise, an ensemble-based approach will be used for other pathogenicity predictors as per the original 215 guidelines.
It should be noted we found better performance using the REVEL-based approach when testing against the 100K genomes data.

Segregation Data
================
BS4
---
If a pedigree with two or more members, including the proband is provided, Exomiser will assign `BS4` for variants not
segregating with affected members of the family.

De novo Data
===========

PS2
---
Exomiser assigns the `PS2` criterion for variants compatible with a dominant mode of inheritance, with a pedigree containing
at least two ancestors of the proband, none of whom are affected and none of whom share the same allele as the proband.

Population Data
===============
BA1
---
Given Exomiser will filter out alleles with an allele frequency of >= 2.0%, this is unlikely to be seen. However, alleles
with a maximum frequency >= 5.0% in the frequency sources specified will be assigned the `BA1` criterion.

PM2
---
Alleles not present in the ESP, ExAC and 1000 Genomes data sets (i.e. the allele must be absent from all three) are
assigned the `PM2` criterion.

Allelic Data
============
PM3 / BP2
---------
If Exomiser is provided with a phased VCF and a variant is found to be *in-trans* with a ClinVar Pathogenic variant and
associated with a recessive disorder, the `PM3` criterion will be applied. However, in cases where variant is being
considered for a recessive disorder and is *in-cis* or a dominant disorder and *in-trans* with another pathogenic variant
the `BP2` criterion is applied.


Phenotype
=========
PP4
---
Given Exomiser's focus on phenotype-driven variant prioritisation, variants in a gene associated with a disorder with a
phenotype match score > 0.6 to the patient's phenotype are assigned the `PP4` criterion at the Moderate, rather than
Supporting level.

Clinical
========
PP5 / BP6
--------
If a variant is previously reported as P/LP in ClinVar with a 1-start rating, it will be assigned `PP5`, those with >= 2
stars (multiple submitters, criteria provided, no conflicts / reviewed by expert panel / practice guideline) will be
assigned a Strong level. Conversely, if the variant is previously reported as B/LB it will be assigned `BP6` with the same
modification criteria. Typically these P/LP variants will be in the Exomiser ClinVar 'whitelist', and will have
a very high variant score irrespective of the predicted variant effect and always survive any filtering criteria.


Transcript Selection
====================

Transcripts will be selected using the most deleterious predicted variant effect from Jannovar
(`Jannovar: A Java Library for Exome Annotation <https://doi.org/10.1002/humu.22531>`_) according to the selected
database. We recommend using the Ensembl transcript datasource as the Exomiser build uses the GENCODE basic set of
transcripts. Future versions should use MANE transcripts.