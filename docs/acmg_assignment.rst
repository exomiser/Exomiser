.. _acmg_assignment:

===============
ACMG Assignment
===============

Starting with version 13.1.0, Exomiser performs a partial categorisation of the variants contributing to the gene
score for a mode of inheritance using the ACMG/AMP `Standards and guidelines for the interpretation of sequence
variants: a joint consensus recommendation of the American College of Medical Genetics and Genomics and the Association
for Molecular Pathology <https://doi.org/10.1038/gim.2015.30>`_. The criteria are assigned according to the
`UK ACGS 2020 guidelines <https://www.acgs.uk.com/media/11631/uk-practice-guidelines-for-variant-classification-v4-01-2020.pdf>`_
and scored according to the
`ClinGen SVI <https://clinicalgenome.org/working-groups/sequence-variant-interpretation/>`_
`updated 2020 guidelines <https://clinicalgenome.org/docs/fitting-a-naturally-scaled-point-system-to-the-acmg-amp-variant-classification-guidelines/>`_.

It is important to be aware that these scores are not a substitute for manual assignment by a qualified clinical geneticist
or clinician - The scores displayed utilise the data found in the Exomiser database and are a subset of the possible
criteria by which to assess a variant. Nonetheless, in our benchmarking on the returned cases from the 100K Genomes Project,
restricting to variants with these automated P/LP classifications increases precision (positive predictive value) markedly
without excluding many real diagnoses. For example, on a cohort of 742 solved cases where the top 5 Exomiser candidates
were considered, including the P/LP criteria increased precision 3.8-fold from 15% to 57% with only a small drop in the
recall of the diagnoses from 94% to 83%. An even larger 5.7-fold increase of precision from 3% to 17% was observed when
considering a larger cohort of 31k cases where only 17% had received a positive diagnosis (again with a modest drop in
recall from 91% to 75%).

Exomiser is capable of assigning the following ACMG categories:

Computational and Predictive Data
=================================
PVS1
----
Variants must have a predicted loss of function effect, be in a gene with known disease associations and have a gene
constraint LOF O/E < 0.7635 (gnomAD 4.0) to suggest that a gene is LoF intolerant. Variants not predicted to lead to
NMD (those located in the last exon) will have the modifier downgraded to Strong.

PS1
---
Variants with the same amino acid change as previously reported P/LP missense or in-frame indel ClinVar variants will be
assigned `PS1` with a strength of `Strong` for variants >= 2 stars, `Moderate` for variants with 1 star or `Supporting`
for those without a ClinVar start rating.

PM4
---
Stop-loss and in-frame insertions or deletions, not previously assigned a `PVS1` criterion are assigned `PM4`.

PM5
---
Variants having a novel missense change to an amino acid where a previously reported ClinVar P/LP variant has been seen
will be assigned `PM5` with a strength of `Moderate` for those with >=2 stars or `Supporting` otherwise.

PP3 / BP4
---------
If REVEL is chosen as a pathogenicity predictor for missense variants, `PP3` and `BP4` are assigned using the modifiers
according to table 2 of `Evidence-based calibration of computational tools for missense variant pathogenicity classification
and ClinGen recommendations for clinical use of PP3/BP4 criteria <https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1>`_.
Note that this suggests the use of modifiers up to Strong in the case of pathogenic or Very Strong in the case of benign predictions.
Otherwise, an ensemble-based approach will be used for other pathogenicity predictors as per the original 215 guidelines.
It should be noted we found better performance using the REVEL-based approach when testing against the 100K genomes data.

Functional Data
===============
PM1
---
Missense and inframe indels are assigned `PM1` if the surrounding region of 25 nucleotides either side of the variant
contain at least 4 reported P/LP variants in ClinVar and no B/LB variants. If the number of P/LP variants is greater
than the number of VUS in the region the strength will be assigned `Moderate` but regions containing P/LP <= VUS
(and no B/BL) will have the strength downgraded to `Supporting`.


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

For the population data criteria, all frequencies are considered using the populations set by the user in the
:ref:`frequencysources`, apart from any bottle-necked populations not recommended for frequency filtering from gnomAD
according to their `filtering allele frequency <https://gnomad.broadinstitute.org/help/faf>`_ document. This excludes
the Ashkenazi Jewish (ASJ), European Finnish (FIN), Other (OTH), Amish (AMI) and Middle Eastern (MID) populations. In
addition the LOCAL frequency will also not be used.

BA1
---
Given Exomiser will filter out alleles with an allele frequency of >= 2.0%, this is unlikely to be seen. However, alleles
with a maximum frequency >= 5.0% in the frequency sources specified will be assigned the `BA1` criterion. Variants listed
as being excluded from this category by the ClinGen SVI working group `BA1 exclusion list <https://www.clinicalgenome.org/site/assets/files/3460/ba1_exception_list_07_30_2018.pdf>`_
will not be marked as `BA1`, assuming they survived variant filtering.

PM2
---
In accordance with the `updated PM2 guidance <https://clinicalgenome.org/site/assets/files/5182/pm2_-_svi_recommendation_-_approved_sept2020.pdf>`_, variants absent from all of the user-defined :ref:`frequencysources`
will be assigned the `PM2_Supporting` criterion. Additionally, for variants considered under a recessive mode of inheritance they
can have a frequency of < 0.01% (0.0001) in all non-bottlenecked populations to be assigned `PM2_Supporting`.

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

Transcripts will be selected using the most deleterious predicted variant effect from `Jannovar <https://doi.org/10.1002/humu.22531>`_
according to the `transcript-source` property set in the `application.properties`. We recommend using the Ensembl
transcript datasource as the Exomiser build uses the GENCODE basic set of transcripts. Future versions should use MANE transcripts.

ACMG assignments will be reported for a variant on a transcript consistent with a particular mode of inheritance in
conjunction with a disorder, the assigned criteria with any modifiers and the final classification e.g.

.. parsed-literal::

   1-12335-A-T, NC_000001.10:g.12335A>T, GENE1(ENST12345678):c.2346A>T:p.1234A>-, PATHOGENIC, [PVS1, PS1, PP4_Strong], Disease (OMIM:12345), AUTOSOMAL_DOMINANT


.. code-block:: json

        "acmgAssignments": [
          {
            "variantEvaluation": {
              "genomeAssembly": "HG19",
              "contigName": "10",
              "start": 123256215,
              "end": 123256215,
              "ref": "T",
              "alt": "G",
              "type": "SNV",
              "length": 1,
              "phredScore": 100,
              "variantEffect": "MISSENSE_VARIANT",
              "whiteListed": true,
              "filterStatus": "PASSED",
              "contributesToGeneScore": true,
              "variantScore": 1,
              "frequencyScore": 1,
              "pathogenicityScore": 1,
              "predictedPathogenic": true,
              "passedFilterTypes": [
                "FAILED_VARIANT_FILTER",
                "PATHOGENICITY_FILTER",
                "FREQUENCY_FILTER",
                "VARIANT_EFFECT_FILTER",
                "INHERITANCE_FILTER"
              ],
              "frequencyData": {
                "rsId": "rs121918506",
                "frequencyScore": 1
              },
              "pathogenicityData": {
                "clinVarData": {
                  "alleleId": "28333",
                  "primaryInterpretation": "LIKELY_PATHOGENIC",
                  "reviewStatus": "criteria provided, single submitter"
                },
                "pathogenicitycore": 0.965,
                "pathogenicityScores": [
                  {
                    "source": "REVEL",
                    "score": 0.965
                  },
                  {
                    "source": "MVP",
                    "score": 0.9517972
                  }
                ],
                "mostPathogenicScore": {
                  "source": "REVEL",
                  "score": 0.965
                }
              },
              "compatibleInheritanceModes": [
                "AUTOSOMAL_DOMINANT"
              ],
              "contributingInheritanceModes": [
                "AUTOSOMAL_DOMINANT"
              ],
              "transcriptAnnotations": [
                {
                  "variantEffect": "MISSENSE_VARIANT",
                  "geneSymbol": "FGFR2",
                  "accession": "ENST00000346997.2",
                  "hgvsGenomic": "g.12278533A>C",
                  "hgvsCdna": "c.1688A>C",
                  "hgvsProtein": "p.(Glu563Ala)",
                  "rankType": "EXON",
                  "rank": 12,
                  "rankTotal": 17
                },
                {
                  "variantEffect": "MISSENSE_VARIANT",
                  "geneSymbol": "FGFR2",
                  "accession": "ENST00000351936.6",
                  "hgvsGenomic": "g.12278533A>C",
                  "hgvsCdna": "c.1688A>C",
                  "hgvsProtein": "p.(Glu563Ala)",
                  "rankType": "EXON",
                  "rank": 13,
                  "rankTotal": 18
                }
              ]
            },
            "geneIdentifier": {
              "geneId": "ENSG00000066468",
              "geneSymbol": "FGFR2",
              "hgncId": "HGNC:3689",
              "hgncSymbol": "FGFR2",
              "entrezId": "2263",
              "ensemblId": "ENSG00000066468",
              "ucscId": "uc057wle.1"
            },
            "modeOfInheritance": "AUTOSOMAL_DOMINANT",
            "disease": {
              "diseaseId": "OMIM:123150",
              "diseaseName": "Jackson-Weiss syndrome",
              "associatedGeneId": 2263,
              "diseaseType": "DISEASE",
              "inheritanceMode": "AUTOSOMAL_DOMINANT",
              "phenotypeIds": [
                "HP:0000006",
                "HP:0000272",
                "HP:0001363",
                "HP:0001783",
                "HP:0004691",
                "HP:0008080",
                "HP:0008122",
                "HP:0010055",
                "HP:0010743",
                "HP:0011800"
              ],
              "id": "OMIM:123150",
              "associatedGeneSymbol": "FGFR2"
            },
            "acmgEvidence": {
              "evidence": {
                "PM2": "MODERATE",
                "PP3": "STRONG",
                "PP4": "SUPPORTING",
                "PP5": "SUPPORTING"
              }
            },
            "acmgClassification": "LIKELY_PATHOGENIC"
          }
        ]
      }