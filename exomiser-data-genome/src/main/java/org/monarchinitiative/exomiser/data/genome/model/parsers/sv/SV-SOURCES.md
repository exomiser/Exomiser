# SV data sources and processing

The aim of this document is to catalogue available SV datasources so as to decide how to merge them into two tables for
use in the Exomiser sv database. These tables should describe the ClinVar pathogenicity assertions and various frequency
sources.

## Tables

Common elements from all the datasources can be distilled into two tables:

### Pathogenicity:

```text
CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | RCV_AC | VARIATION_ID | ALLELE_ID | CLIN_SIG | CLIN_REV_STAT   
```

### Frequency:

```text
CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AN | AC | AF (alternatively add in VCF INFO field?)
```

### Gene Constraint / Dosage Sensitivity:

```text

```

## Pathogenicity Sources

```text
CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | RCV_AC | VARIATION_ID | ALLELE_ID | CLIN_SIG | CLIN_REV_STAT   
```

### ClinVar

1698542 records (38084 with nsv identifiers)

This is a TSV file with both GRCh37 and GRCh38 data, small and large variations. Not all data is present on both
builds.</br>

#### Data type:

pathogenicity

1-based coordinates

GRCh37 & GRCh38 in same file (indicated in Assembly field)

#### URL:

https://ftp.ncbi.nlm.nih.gov/pub/clinvar/tab_delimited/variant_summary.txt.gz

#### File:

```text
#AlleleID       Type    Name    GeneID  GeneSymbol      HGNC_ID ClinicalSignificance    ClinSigSimple   LastEvaluated   RS# (dbSNP)     nsv/esv (dbVar) RCVaccession    PhenotypeIDS    PhenotypeList   Origin  OriginSimple    Assembly        Chromosome  Accession     Chromosome      Start   Stop    ReferenceAllele AlternateAllele Cytogenetic     ReviewStatus    NumberSubmitters        Guidelines      TestedInGTR     OtherIDs        SubmitterCategories     VariationID     PositionVCF     ReferenceAlleleVCF      AlternateAlleleVCF
15041   Indel   NM_014855.3(AP5Z1):c.80_83delinsTGCTGTAAACTGTAACTGTAAA (p.Arg27_Ile28delinsLeuLeuTer)   9907    AP5Z1   HGNC:22197      Pathogenic      1       Jun 29, 2010    397704705       -       RCV000000012    MONDO:MONDO:0013342,MedGen:C3150901,OMIM:613647,Orphanet:306511 Spastic paraplegia 48, autosomal recessive      germline        germline        GRCh37  NC_000007.13    7       4820844 4820847 na      na      7p22.1  no assertion criteria provided  1       -       N       ClinGen:CA215070,OMIM:613653.0001       1       2       4820844 GGAT    TGCTGTAAACTGTAACTGTAAA
15041   Indel   NM_014855.3(AP5Z1):c.80_83delinsTGCTGTAAACTGTAACTGTAAA (p.Arg27_Ile28delinsLeuLeuTer)   9907    AP5Z1   HGNC:22197      Pathogenic      1       Jun 29, 2010    397704705       -       RCV000000012    MONDO:MONDO:0013342,MedGen:C3150901,OMIM:613647,Orphanet:306511 Spastic paraplegia 48, autosomal recessive      germline        germline        GRCh38  NC_000007.14    7       4781213 4781216 na      na      7p22.1  no assertion criteria provided  1       -       N       ClinGen:CA215070,OMIM:613653.0001       1       2       4781213 GGAT    TGCTGTAAACTGTAACTGTAAA
15042   Deletion        NM_014855.3(AP5Z1):c.1413_1426del (p.Leu473fs)  9907    AP5Z1   HGNC:22197      Pathogenic      1       Jun 29, 2010    397704709       -       RCV000000013    MONDO:MONDO:0013342,MedGen:C3150901,OMIM:613647,Orphanet:306511 Spastic paraplegia 48, autosomal recessive      germline        germline        GRCh37  NC_000007.13    7       4827361 4827374 na      na      7p22.1  no assertion criteria provided  1       -       N       ClinGen:CA215072,OMIM:613653.0002       1       3       4827360 GCTGCTGGACCTGCC G
15042   Deletion        NM_014855.3(AP5Z1):c.1413_1426del (p.Leu473fs)  9907    AP5Z1   HGNC:22197      Pathogenic      1       Jun 29, 2010    397704709       -       RCV000000013    MONDO:MONDO:0013342,MedGen:C3150901,OMIM:613647,Orphanet:306511 Spastic paraplegia 48, autosomal recessive      germline        germline        GRCh38  NC_000007.14    7       4787730 4787743 na      na      7p22.1  no assertion criteria provided  1       -       N       ClinGen:CA215072,OMIM:613653.0002       1       3       4787729 GCTGCTGGACCTGCC G
15043   single nucleotide variant       NM_014630.3(ZNF592):c.3136G>A (p.Gly1046Arg)    9640    ZNF592  HGNC:28986      Uncertain significance  0       Jun 29, 2015    150829393       -       RCV000000014    MONDO:MONDO:0033005,MedGen:C4551772,OMIM:251300,Orphanet:2065,Orphanet:83472    Galloway-Mowat syndrome 1       germline        germline        GRCh37  NC_000015.9     15      85342440        85342440        na      na      15q25.3 no assertion criteria provided  1       -       N       ClinGen:CA210674,UniProtKB:Q92610#VAR_064583,OMIM:613624.0001   1       4       85342440        G       A
15043   single nucleotide variant       NM_014630.3(ZNF592):c.3136G>A (p.Gly1046Arg)    9640    ZNF592  HGNC:28986      Uncertain significance  0       Jun 29, 2015    150829393       -       RCV000000014    MONDO:MONDO:0033005,MedGen:C4551772,OMIM:251300,Orphanet:2065,Orphanet:83472    Galloway-Mowat syndrome 1       germline        germline        GRCh38  NC_000015.10    15      84799209        84799209        na      na      15q25.3 no assertion criteria provided  1       -       N       ClinGen:CA210674,UniProtKB:Q92610#VAR_064583,OMIM:613624.0001   1       4       84799209        G       A
15061   Deletion        NM_020779.4(WDR35):c.2858del (p.Pro953fs)       57539   WDR35   HGNC:29250      Pathogenic      1       Sep 12, 2013    397515334       -       RCV000000039    MONDO:MONDO:0013323,MedGen:C3150874,OMIM:613610,Orphanet:1515   Cranioectodermal dysplasia 2    germline        germline        GRCh37  NC_000002.11    2       20131136        20131136        na      na      2p24.1  no assertion criteria provided  2       -       N       ClinGen:CA339781,OMIM:613602.0003       1       22      20131135        AG      A
15061   Deletion        NM_020779.4(WDR35):c.2858del (p.Pro953fs)       57539   WDR35   HGNC:29250      Pathogenic      1       Sep 12, 2013    397515334       -       RCV000000039    MONDO:MONDO:0013323,MedGen:C3150874,OMIM:613610,Orphanet:1515   Cranioectodermal dysplasia 2    germline        germline        GRCh38  NC_000002.12    2       19931375        19931375        na      na      2p24.1  no assertion criteria provided  2       -       N       ClinGen:CA339781,OMIM:613602.0003       1       22      19931374        AG      A
15064   Indel   NM_015600.4(ABHD12):c.-6898_191+7002delinsCC    26090   ABHD12  HGNC:15868      Pathogenic      1       Sep 10, 2010    -1      nsv1067853      RCV000000042    MONDO:MONDO:0012984,MedGen:C2675204,OMIM:612674,Orphanet:171848 Polyneuropathy, hearing loss, ataxia, retinitis pigmentosa, and cataract        germline        germline        GRCh37  NC_000020.10    20      25364147        25378237        na      GG      20p11.21        no assertion criteria provided  1       -       N       dbVar:nssv3761628,OMIM:613599.0002      1       25      -1      na      na
15064   Indel   NM_015600.4(ABHD12):c.-6898_191+7002delinsCC    26090   ABHD12  HGNC:15868      Pathogenic      1       Sep 10, 2010    -1      nsv1067853      RCV000000042    MONDO:MONDO:0012984,MedGen:C2675204,OMIM:612674,Orphanet:171848 Polyneuropathy, hearing loss, ataxia, retinitis pigmentosa, and cataract        germline        germline        GRCh38  NC_000020.11    20      25383511        25397601        na      GG      20p11.21        no assertion criteria provided  1       -       N       dbVar:nssv3761628,OMIM:613599.0002      1       25      -1      na      na
29048   copy number gain        GRCh38/hg38 4q22.1(chr4:88504598-90127832)      6622    SNCA    HGNC:11138      Pathogenic      1       Mar 20, 2007    -1      -       RCV000015046    MONDO:MONDO:0011562,MedGen:C1854182,OMIM:605543,Orphanet:411602 Parkinson disease 4     germline        germline        GRCh38  NC_000004.12    4       88268907        90128499        na      na      4q22.1  no assertion criteria provided  1       -       N       OMIM:163890.0003        1       14009   -1      na      na
21676	Insertion	NM_007129.4(ZIC2):c.177_178ins56	7546	ZIC2	HGNC:12873	Pathogenic	1	Aug 29, 2013	-1	nsv1067866	RCV000007016	MONDO:MONDO:0012322,MedGen:C1864827,OMIM:609637,Orphanet:2162	Holoprosencephaly 5	germline	germline	GRCh37	NC_000013.10	13	100634495	100634496	na	na	13q32.3	no assertion criteria provided	2	-	N	dbVar:nssv3761594,OMIM:603073.0001	1	6637	-1	na	na
594739	copy number gain	GRCh37/hg19 19p13.2(chr19:11915185-12061384)x3	-1	ZNF69;ZNF700;ZNF439;ZNF491;ZNF440	-	Benign	0	Feb 04, 2016	-1	-	RCV000740039	MedGen:CN517202	not provided	unknown	unknown	GRCh37	NC_000019.9	19	11915185	12061384	na	na	19p13.2	no assertion criteria provided	1	-	N	-	2	603403	-1	na	na
```

#### Database representation

CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | RCV_AC | VARIATION_ID | ALLELE_ID | CLIN_SIG | CLIN_REV_STAT
-------|-------|-----|---------------|--------------|--------|--------|--------------|-----------|----------|--------------
20   | 25364147 | 25378237 |  | INS | nsv1067853 |RCV000000042 | 25 | 15064 | Pathogenic | no assertion criteria provided
13    | 100634495    | 100634496 | 56 |  INS | nsv1067866 | RCV000007016 | 6637 | 21676 | Pathogenic | no assertion criteria provided
19    | 11915185    | 12061384 | - |  CNV_GAIN | - | RCV000740039 | 603403 | 594739 | Benign | no assertion criteria provided

#### Details:

Might be easiest to parse out the `ClinVarData` objects where `nsv/esv != '-'` into a map with the `RCV` as the key
e.g. `RCV000450793` and then use the dbvar_nr data to import the variant info from. This way the ClinVar data remains
consistent between precise and imprecise variants.

## Frequency Sources

```text
CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AN | AC | AF (alternatively add in VCF INFO field?)
```

### dbVar nr

Non-redundant SV data.

4304434 records

#### Data type:

Pathogenicity Frequency?

1-based coordinates

#### URL:

- https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/
- https://github.com/ncbi/dbvar/blob/master/Structural_Variant_Sets/Nonredundant_Structural_Variants/README.md
- https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/deletions/GRCh37.nr_deletions.tsv.gz
- https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/insertions/GRCh37.nr_insertions.tsv.gz
- https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/duplications/GRCh37.nr_duplications.tsv.gz

#### File:

* Sets of "non-redundant structural variations" (NR SVs) derived from dbVar are available via FTP as tab delimited files
  by assembly, GRCh37 & GRCh38, and type of variant.
* Non-redundant refers to variant coordinates, i.e. chr, outermost start, and outermost stop. Please note: the
  non-redundant coordinates are based strictly on exact overlap of coordinates, not on partial overlaps.
* Other features of NR SV files:
    - variant calls are from germline samples only (no somatic)
    - placements are "BestAvailable" on the assembly (guarantees no duplicate placements for a variant)
    - placements are on finished chromosomes only (not on NT_ or NW_ contigs)
    - placements are 1-based in the .tsv files
    - placements are zero-based start and 1-based stop in .bed and .bedpe files
    - insertion_length is set to sequence length if the sequence was submitted to dbVar without a specific
      insertion_length
    - insertions submitted to dbVar without insertion_length or submitted sequence are not included in the NR files

##### File format:

Column | NR SV TSV File | BED File | BEDPE File |
-----|---------|---|------
|1|chr|chr|chr|
|2|outermost_start (1-based)|outermost_start (0-based)|outermost_start (0-based)|
|3|outermost_stop (1-based)|outermost_stop (1-based)|outermost_stop (1-based)|
|4|variant_count|NR_SV_id|.  (chrom2)|
|5|variant_type| |-1  (start2)|
|6|method| |-1  (end2)|
|7|analysis| |NR_SV_id|
|8|platform| |.  (score)|
|9|study| |.  (strand1)|
|10|variant| |.  (stramd2)|
|11|clinical_assertion| |variant_count|
|12|clinvar_accession| |variant_type|
|13|bin_size| |method|
|14|min_insertion_length*| |analysis|
|15|max_insertion_length*| |platform|
|16| | |study|
|17| | |variant|
|18| | |clinical_assertion|
|19| | |clinvar_accession|
|20| | |bin_size|
|21| | |min_insertion_length|
|22| | |max_insertion_length|

Please note:

* \* = NR_SV TSV fields 14 and 15 are in nr_insertion.tsv files only
* NR_SV_id = chr_outermost_start_outermost_stop_type where type is del, dup, or ins
* bin_size = small (length < 50 bp), medium (< 1000000), large (>= 1000000). Length = outermost_stop - outermost_start +
  1.
* In all cases, bedpe columns 4 through 6, and 8 through 10, are populated with default values per the bedpe
  specification
* The bed and bedpe specifications are found here: https://bedtools.readthedocs.io/en/latest/content/general-usage.html

###### Some fields may have multiple values:

* The fields type, method, analysis, platform, variant, study, clinical_significance, clinvar_accession, and gene may
  contain multiple values.
* Each of the values is associated with one or more calls found in the variant field.
* The values in the variant field are "dbVar call accessions".

Example:
https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/deletions/GRCh37.nr_deletions.bed.gz (
coordinates only)

https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/deletions/GRCh37.nr_deletions.bedpe.gz (BEDPE
coordinates, no strand info, plus tsv data)

```text
chr1    0       10000   .       -1      -1      chr1_0_10000_del        .       .       .       1       deletion        Sequencing      Read_depth      Illumina HiSeq 2000     Dogan2013       nssv2997092     .       .       medium  .       .
chr1    0       2300000 .       -1      -1      chr1_0_2300000_del      .       .       .       1       copy_number_loss        BAC_aCGH        Probe_signal_intensity  Spectral Genomics 2600 BAC array        Iafrate2004     nssv2995976     .       .       large
   .       .
chr1    0       2857518 .       -1      -1      chr1_0_2857518_del      .       .       .       1       deletion        Oligo_aCGH      Probe_signal_intensity  NA      Boone2013       nssv1613277     .       .       large   .       .
chr1    0       5592835 .       -1      -1      chr1_0_5592835_del      .       .       .       1       copy_number_loss        Multiple        Multiple        NA      Clinical_Structural_Variants    nssv16255736    Pathogenic      RCV001260116.1  large   .
       .
chr1    0       7200000 .       -1      -1      chr1_0_7200000_del      .       .       .       1       copy_number_loss        BAC_aCGH        Probe_signal_intensity  GSE5373 Sharp2006       nssv3008963     .       .       large   .       .
chr1    0       16200000        .       -1      -1      chr1_0_16200000_del     .       .       .       1       copy_number_loss        BAC_aCGH        Probe_signal_intensity  Spectral Genomics 2600 BAC array        Iafrate2004     nssv2996525     .       .
       large   .       .
chr1    10000   1471073 .       -1      -1      chr1_10000_1471073_del  .       .       .       1       deletion        Oligo_aCGH      Probe_signal_intensity  NA      Boone2013       nssv1614481     .       .       large   .       .
chr1    10000   2694017 .       -1      -1      chr1_10000_2694017_del  .       .       .       1       deletion        Oligo_aCGH      Probe_signal_intensity  NA      Boone2013       nssv1613509     .       .       large   .       .
```

https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/deletions/GRCh37.nr_deletions.tsv.gz

```text
#NR_SVs GRCh37
#chr    outermost_start outermost_stop  variant_count   variant_type    method  analysis        platform        study   variant clinical_assertion      clinvar_accession       bin_size
1       1       10000   1       deletion        Sequencing      Read_depth      Illumina HiSeq 2000     Dogan2013       nssv2997092                     medium
1       1       2300000 1       copy_number_loss        BAC_aCGH        Probe_signal_intensity  Spectral Genomics 2600 BAC array        Iafrate2004     nssv2995976                     large
1       1       2857518 1       deletion        Oligo_aCGH      Probe_signal_intensity  NA      Boone2013       nssv1613277                     large
1       1       7200000 1       copy_number_loss        BAC_aCGH        Probe_signal_intensity  GSE5373 Sharp2006       nssv3008963                     large
1       1       16200000        1       copy_number_loss        BAC_aCGH        Probe_signal_intensity  Spectral Genomics 2600 BAC array        Iafrate2004     nssv2996525                     large
1       10001   1471073 1       deletion        Oligo_aCGH      Probe_signal_intensity  NA      Boone2013       nssv1614481                     large
1       10001   2694017 1       deletion        Oligo_aCGH      Probe_signal_intensity  NA      Boone2013       nssv1613509                     large
# multiple samples observed with this variant
1       10623   10740   10      deletion        Sequencing      de_novo_and_local_sequence_assembly     PacBio RS II P6C4, PacBio Sequel v2.1   Audano2018      nssv14473684;nssv14475664;nssv14475772;nssv14477189;nssv14485522;nssv14485713;nssv14486036;nssv1448608
6;nssv14488099;nssv14489123                       medium
# ClinVar variants
1       47851   91538   1       copy_number_loss        Multiple        Multiple        NA      Clinical_Structural_Variants    nssv15171013    Benign  RCV000748793.1  medium
1       47851   1165310 1       copy_number_loss        Multiple        Multiple        NA      Clinical_Structural_Variants    nssv15156678    Benign  RCV000736293.1  large
1       47851   6659872 1       copy_number_loss        Multiple        Multiple        NA      Clinical_Structural_Variants    nssv15156679    Pathogenic      RCV000736294.1  large
# Multiple data sources
2	126814801	126820072	3	deletion	Curated;Merging;Sequencing	Curated;Merging;Other	NA;See merged experiments	DECIPHER_Consensus_CNVs;McVean2012;gnomAD_Structural_Variants	nssv16093204;essv6246065;nssv15873970		medium
8	8583109	8587589	15	copy_number_loss;deletion	Merging;SNP_array;Curated;Multiple	Merging;Probe_signal_intensity;SNP_genotyping_analysis;Curated;Multiple	See merged experiments;Illumina HumanOmniExpress-12v1 Beadchip;NA;[GenomeWideSNP_6] Affymetrix Genome-Wide Human SNP 6.0 Array;Illumina HumanOmniExpress Beadchip	Vogler2010;Simpson2014;Cooper2011;Pinto2011;DECIPHER_Consensus_CNVs;Clinical_Structural_Variants;Kasak2014	essv6992379;essv6992380;essv6992381;essv6992382;essv6992383;essv26047667;essv26050923;essv26051835;nssv1105940;essv4427153;nssv16129178;nssv15123121;nssv15123840;essv16462796;essv16462797	not provided	RCV000161523.1;RCV000161522.1	medium
```

```text
zcat GRCh37.nr_deletions.tsv.gz | awk -F '\t' '{print $5}' | sort | uniq -c
      1 
   8823 alu_deletion
      6 alu_deletion;copy_number_loss;deletion
   2654 alu_deletion;deletion
 372051 copy_number_loss
  19106 copy_number_loss;deletion
      1 copy_number_loss;line1_deletion
2149678 deletion
     54 deletion;herv_deletion
    563 deletion;line1_deletion
    203 deletion;sva_deletion
    171 herv_deletion
   1992 line1_deletion
    803 sva_deletion
      1 variant_type
```

https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/insertions/GRCh37.nr_insertions.tsv.gz

```text
#NR_SVs GRCh37
#chr    outermost_start outermost_stop  variant_count   variant_type    method  analysis        platform        study   variant clinical_assertion      clinvar_accession       bin_size        min_insertion_length    max_insertion_length
1       10726   10726   1       insertion       Sequencing      de_novo_and_local_sequence_assembly     PacBio RS II P6C4, PacBio Sequel v2.1   Audano2018      nssv14489056                    small   58      58
1       19742   28927   1       insertion       Optical_mapping Optical_mapping NA      Levy-Sakin2019  nssv15208719                    medium  8290    8290
1       20848   20848   9       insertion       Sequencing      de_novo_and_local_sequence_assembly     PacBio RS II P6C4, PacBio Sequel v2.1   Audano2018      nssv14472145;nssv14473644;nssv14478404;nssv14479308;nssv14481701;nssv14486411;nssv14488896;nssv14489124;nssv14489644                    small   2001    2001
1       34042   34042   1       insertion       Sequencing      de_novo_and_local_sequence_assembly     PacBio RS II P6C4, PacBio Sequel v2.1   Audano2018      nssv14487555                    small   3065    3065
1       54665   54665   1       insertion       Sequencing      Other   NA      gnomAD_Structural_Variants      nssv16024555                    small   52      52
1       58267   83446   1       insertion       Optical_mapping Optical_mapping NA      Levy-Sakin2019  nssv15220733                    medium  24024   24024
# ClinVar variants (n.b. this is *all* of them!)
2	179315141	179315141	1	insertion	Multiple	Multiple	NA	Clinical_Structural_Variants	nssv16216556	Benign	RCV001197596.1	small	57	57
11	47362807	47362807	1	insertion	Multiple	Multiple	NA	Clinical_Structural_Variants	nssv16215206	Likely benign	RCV001177978.1	small	71	71
21	44483172	44483172	1	insertion	Multiple	Multiple	NA	Clinical_Structural_Variants	nssv16215109	Benign	RCV000200688.1	small	68	68
21	44483183	44483183	1	insertion	Multiple	Multiple	NA	Clinical_Structural_Variants	nssv16215108	Benign	RCV000204184.5	small	55	55
21	44483184	44483184	5	insertion	Multiple	Multiple	NA	Clinical_Structural_Variants	nssv16215107;nssv16215118;nssv16215140;nssv16296928;nssv16297010	Benign;Benign/Likely benign	RCV000222887.1;RCV000230973.3;RCV000990351.2;RCV000928617.1;RCV000971089.1	small	68	68
```

```text
zcat GRCh37.nr_insertions.tsv.gz | awk -F '\t' '{print $5}' | sort | uniq -c
      1 
  81538 alu_insertion
   4754 alu_insertion;insertion
      5 alu_insertion;insertion;line1_insertion
      1 alu_insertion;insertion;line1_insertion;novel_sequence_insertion
    675 alu_insertion;insertion;mobile_element_insertion
    128 alu_insertion;insertion;mobile_element_insertion;novel_sequence_insertion
      2 alu_insertion;insertion;mobile_element_insertion;sva_insertion
    263 alu_insertion;insertion;novel_sequence_insertion
      9 alu_insertion;insertion;sva_insertion
      8 alu_insertion;line1_insertion
    484 alu_insertion;mobile_element_insertion
     24 alu_insertion;mobile_element_insertion;novel_sequence_insertion
     45 alu_insertion;novel_sequence_insertion
     10 alu_insertion;sva_insertion
     97 herv_insertion
     44 herv_insertion;insertion
      3 herv_insertion;insertion;novel_sequence_insertion
      1 herv_insertion;novel_sequence_insertion
1182530 insertion
    551 insertion;line1_insertion
     37 insertion;line1_insertion;mobile_element_insertion
     66 insertion;line1_insertion;novel_sequence_insertion
    820 insertion;mobile_element_insertion
     65 insertion;mobile_element_insertion;novel_sequence_insertion
      7 insertion;mobile_element_insertion;sva_insertion
   1293 insertion;novel_sequence_insertion
      3 insertion;novel_sequence_insertion;sva_insertion
    653 insertion;sva_insertion
  14427 line1_insertion
     61 line1_insertion;mobile_element_insertion
      4 line1_insertion;novel_sequence_insertion
      1 line1_insertion;sva_insertion
   4738 mobile_element_insertion
      4 mobile_element_insertion;novel_sequence_insertion
     13 mobile_element_insertion;sva_insertion
   2084 novel_sequence_insertion
      1 novel_sequence_insertion;sva_insertion
   8365 sva_insertion
      1 variant_type
```

https://ftp.ncbi.nlm.nih.gov/pub/dbVar/sandbox/sv_datasets/nonredundant/duplications/GRCh37.nr_duplications.tsv.gz

```text
#NR_SVs GRCh37
#chr    outermost_start outermost_stop  variant_count   variant_type    method  analysis        platform        study   variant clinical_assertion      clinvar_accession       bin_size
1       1       2300000 1       copy_number_gain        BAC_aCGH        Probe_signal_intensity  GSE5373 Sharp2006       nssv3009025                     large
1       1       16200000        2       copy_number_gain        BAC_aCGH        Probe_signal_intensity  Spectral Genomics 2600 BAC array        Iafrate2004     nssv2996030;nssv2996227                 large
1       10001   19818   80      duplication     Sequencing      Read_depth      NA      Sudmant2013     nssv1749199;nssv1749200;nssv1749201;nssv1749212;nssv1749223;nssv1749234;nssv1749245;nssv1749256;nssv1749267;nssv1749278;nssv1749289;nssv1749300;nssv1749337;nssv1749338;nssv1749449;nssv1750484;nssv1750595;nssv1750706;nssv1751732;nssv1751743;nssv1751754;nssv1751765;nssv1751776;nssv1751787;nssv1751788;nssv1751799;nssv1751810;nssv1751821;nssv1751832;nssv1751843;nssv1751854;nssv1751865;nssv1751876;nssv1751887;nssv1751898;nssv1751899;nssv1752886;nssv1752995;nssv1753096;nssv1754121;nssv1754222;nssv1754288;nssv1754289;nssv1754290;nssv1754291;nssv1754292;nssv1754293;nssv1754294;nssv1754295;nssv1754296;nssv1754297;nssv1754298;nssv1754299;nssv1754300;nssv1754301;nssv1754302;nssv1754303;nssv1754304;nssv1754305;nssv1754306;nssv1754307;nssv1754308;nssv1754309;nssv1754310;nssv1754376;nssv1755093;nssv1755194;nssv1755295;nssv1755296;nssv1755397;nssv1755451;nssv1755452;nssv1755453;nssv1755454;nssv1755455;nssv1755456;nssv1755457;nssv1755458;nssv1755459;nssv1755470                 medium
1       10001   22118   11      duplication     Sequencing      Read_depth      NA      Sudmant2013     nssv2570917;nssv2570918;nssv2570919;nssv2571098;nssv2572825;nssv2574189;nssv2575216;nssv2575439;nssv2576553;nssv2576564;nssv2576575                     medium
1       10001   82189   2       copy_number_gain        Multiple;Oligo_aCGH     Multiple;Probe_signal_intensity NA;Agilent-015686 Custom Human 244K CGH Microarray      DGV_Gold_Standard;Perry2008     nssv15707940;nssv14781                  medium
1       12801   17551   4       copy_number_variation;duplication       Sequencing;Merging      Paired-end_mapping;Merging      Illumina HiSeq2000;See merged experiments       Mwapagha2020;Fakhro2015 nssv16245723;essv25943285;essv25982361;essv25991134                     medium
1       13516   91073   1       duplication     Curated Curated NA      DECIPHER_Consensus_CNVs nssv16101249                    medium
# ClinVar variants
1       14874   368661  2       copy_number_gain        Multiple;Oligo_aCGH     Multiple;Probe_signal_intensity NA      Clinical_Structural_Variants;ClinGen_Laboratory-Submitted       nssv15129004;nssv13638640       Uncertain significance  RCV000453780.1  medium
1       18888   35451   1       duplication     Curated Curated NA      DECIPHER_Consensus_CNVs nssv16108719                    medium
1       19225   4401691 2       copy_number_gain        Multiple;Oligo_aCGH     Multiple;Probe_signal_intensity NA      Clinical_Structural_Variants;ClinGen_Laboratory-Submitted       nssv15150044;nssv13654953       Pathogenic      RCV000447000.1  large 
19	11915185	12061384	6	copy_number_gain;duplication	Multiple;SNP_array;Curated	Multiple;Probe_signal_intensity;Curated	NA;Affymetrix SNP Array 6.0;Illumina 1.2M WTCCC Custom	Clinical_Structural_Variants;Coe2014;Duyzend2015;DECIPHER_Consensus_CNVs	nssv15160047;nssv3564697;nssv3564698;nssv3723284;nssv8087550;nssv16138636	Benign	RCV000740039.1	medium
```

```text
zcat GRCh37.nr_duplications.tsv.gz | awk -F '\t' '{print $5}' | sort | uniq -c
      1 
 246152 copy_number_gain
    975 copy_number_gain;copy_number_variation
   4544 copy_number_gain;duplication
     13 copy_number_gain;tandem_duplication
   9613 copy_number_variation
   1250 copy_number_variation;duplication
 177674 duplication
    237 duplication;tandem_duplication
   4051 tandem_duplication
      1 variant_type
```

#### Database representation

CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AC | AN | AF
-------|-------|-----|---------------|--------------|--------|--------|-----------|----|----|---
1 | 1  | 1000 | -999 | DEL | nssv2997092 | DBVAR |  | 1 |  |
1 | 1  | 2300000 | -2999999 | CNV_GAIN | nssv3009025 | DBVAR |  | 1 |  |
1 | 10726 | 10726 | 58 | INS | nssv14489056 | DBVAR |  | 1 |  |
1 | 10001 | 19818 | 9817 | DUP | nssv1749199;nssv1749200;nssv1749201;nssv1749212;nssv1749223;nssv1749234;nssv1749245;nssv1749256;nssv1749267 | DBVAR |  | 80 |  |  |
1 | 18888 | 35451 |  | DUP | nssv16108719 | DBVAR |  | 1 |  |
19 | 11915185 | 12061384 |  | DUP | nssv15160047;nssv3564697;nssv3564698;nssv3723284;nssv8087550;nssv16138636 | DBVAR |  | 6 |  |

#### Details:

Limit SSV list to 10 as there are some large-scale analyses which all map to the same `nsv` locus e.g.
Sudmant2003 `nsv945697`
ClinVar RCV requires the version suffix to be removed in order to match-up with a ClinVar record. e.g. `RCV000453780.1`
-> `RCV000453780`

dbVar sites.vcf

```text
1	174116832	nssv15534668	T	<INS>	.	.	DBVARID;SVTYPE=INS;END=174116846;SVLEN=320;EXPERIMENT=1;SAMPLESET=1;REGIONID=nsv4286019;AC=5;AF=0.000233;AN=21476
```

### DECIPHER

Common copy-number variants and their frequencies - 4184 records.

Updated predictions of haploinsufficiency as described
by [Huang et al., 2010](https://europepmc.org/article/MED/20976243). Available as a Bed file with coordinates and gene
names in GRCh37/hg19.

#### Data type:

Frequency

1-based coordinates

#### URL:

- https://www.deciphergenomics.org/ddd/overview
- https://www.deciphergenomics.org/about/downloads/data
- https://www.deciphergenomics.org/files/downloads/population_cnv_grch37.txt.gz
- https://www.deciphergenomics.org/files/downloads/HI_Predictions_Version3.bed.gz

#### File:

```text
#population_cnv_id      chr     start   end     deletion_observations   deletion_frequency      deletion_standard_error duplication_observations        duplication_frequency   duplication_standard_error      observations    frequency       standard_error  type    sample_size     study
1       1       10529   177368  0       0       1       3       0.075   0.555277708     3       0.075   0.555277708     1       40      42M calls
2       1       13516   91073   0       0       1       27      0.675   0.109713431     27      0.675   0.109713431     1       40      42M calls
3       1       18888   35451   0       0       1       2       0.002366864     0.706269473     2       0.002366864     0.706269473     1       845     DDD
4       1       23946   88271   27      0.031952663     0.189350482     21      0.024852071     0.215489247     48      0.056804734     0.140178106     0       845     DDD
5       1       25112   233565  16      0.018934911     0.247621825     14      0.016568047     0.265037996     30      0.035502959     0.179303936     0       845     DDD
6       1       30322   35509   1       0.001183432     0.999408109     1       0.001183432     0.999408109     2       0.002366864     0.706269473     0       845     DDD
7       1       35462   35534   0       0       1       1       0.001183432     0.999408109     1       0.001183432     0.999408109     1       845     DDD
8       1       40718   731985  38      0.044970414     0.158531882     49      0.057988166     0.138653277     87      0.10295858      0.101542213     0       845     DDD
# This row is in both dbVar (nssv15160047;nssv3564697;nssv3564698;nssv3723284;nssv8087550;nssv16138636) and ClinVar (RCV000740039) with the same coordinates
52401	19	11915185	12061384	0	0	1	2	0.000337895	0.706987307	2	0.000337895	0.706987307	15919	Affy6
```

#### Database representation

CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AC | AN | AF
-------|-------|-----|---------------|--------------|--------|--------|-----------|----|----|---
1 | 10529 | 177368 | 1 | DUP | - | DECIPHER | 1 | 3 | 40 | 0.075
1 | 23946 | 88271 | -1 | DEL | - | DECIPHER | 4 | 27 | 845 | 0.031952663
1 | 23946 | 88271 | 1 | DUP | - | DECIPHER | 4 | 21 | 845 | 0.024852071
19 | 11915185 | 12061384 | 1 | DUP | - | DECIPHER | 52401 | 2 | 15919 | 0.000337895

#### Details:

- Population CNV (gain or loss) frequency data.
- Contains no pathogenicity assertions.
- Does _not_ contain a dbVar accession e.g. `nssv16138636`, but the data *is* contained in dbVar nr set.
- **Sites are multi-allelic**

### DGV

The objective of the Database of Genomic Variants is to provide a comprehensive summary of structural variation in the
human genome. We define structural variation as genomic alterations that involve segments of DNA that are larger than
50bp. The content of the database is only representing structural variation identified in healthy control samples.

The Database of Genomic Variants provides a useful catalog of control data for studies aiming to correlate genomic
variation with phenotypic data. The database is continuously updated with new data from peer reviewed research studies.
We always welcome suggestions and comments regarding the database from the research community.

If you want to submit variation data to the database, information about the submission process can be found here.

For data sets where the variation calls are reported at a sample by sample level, we merge calls with similar boundaries
across the sample set. Only variants of the same type (i.e. CNVs, inversions) are merged, and gains and losses are
merged separately. In addition, if several different platforms/approaches are used within the same study, these datasets
are merged separately. Sample level calls that overlap by >= 70% are merged in this process.

Click here for answers to commonly asked questions and an overview about how to interpret the data in the database.

When citing the Database of Genomic Variants, please refer to: MacDonald JR, Ziman R, Yuen RK, Feuk L, Scherer SW. The
database of genomic variants: a curated collection of structural variation in the human genome. Nucleic Acids Res. 2013
Oct 29. PubMed PMID: 24174537

An Advisory Board for the Database of Genomic Variants was formed in 2008. The board has the following members:

    Dr. Nigel Carter - The Wellcome Trust Sanger Institute
    Dr. Deanna Church - National Center for Biotechnology Information (NCBI)
    Dr. Lars Feuk - Uppsala University
    Dr. Paul Flicek - European Bioinformatics Institute (EBI)
    Dr. David Ledbetter - Emory University
    Dr. Stephen Scherer - The Hospital for Sick Children 

#### Data type:

Frequency

1-based

#### URL:

- http://dgv.tcag.ca/dgv/app/faq?ref=GRCh37/hg19
- http://dgv.tcag.ca/dgv/app/downloads
- http://dgv.tcag.ca/dgv/docs/GRCh37_hg19_variants_2020-02-25.txt
- http://dgv.tcag.ca/dgv/docs/GRCh38_hg38_variants_2020-02-25.txt

#### File:

```text
variantaccession        chr     start   end     varianttype     variantsubtype  reference       pubmedid        method  platform        mergedvariants  supportingvariants      mergedorsample  frequency       samplesize      observedgains   observedlosses  cohortdescription       genes   samples
nsv482937       1       1       2300000 CNV     loss    Iafrate_et_al_2004      15286789        BAC aCGH,FISH                   nssv2995976     M               39      0       1               ACAP3,AGRN,ANKRD65,ATAD3A,ATAD3B,ATAD3C,AURKAIP1,B3GALT6,C1orf159,C1orf170,C1orf233,C1orf86,CALML6,CCNL2,CDK11A,CDK11B,CPSF3L,DDX11L1,DVL1,FAM132A,FAM138A,FAM138F,FAM41C,FAM87B,GABRD,GLTPD1,GNB1,HES4,ISG15,KIAA1751,KLHL17,LINC00115,LINC01128,LOC100129534,LOC100130417,LOC100132062,LOC100132287,LOC100133331,LOC100288069,LOC148413,LOC254099,LOC729737,MIB2,MIR200A,MIR200B,MIR429,MIR6723,MIR6726,MIR6727,MIR6808,MIR6859-1,MIR6859-2,MMP23A,MMP23B,MORN1,MRPL20,MXRA8,NADK,NOC2L,OR4F16,OR4F29,OR4F3,OR4F5,PLEKHN1,PRKCZ,PUSL1,RNF223,SAMD11,SCNN1D,SDF4,SKI,SLC35E2,SLC35E2B,SSU72,TAS1R3,TMEM240,TMEM52,TMEM88B,TNFRSF18,TNFRSF4,TTLL10,UBE2J2,VWA1,WASH7P   
dgv1n82 1       10001   22118   CNV     duplication     Sudmant_et_al_2013      23825009        Oligo aCGH,Sequencing                   nsv945697,nsv945698     M               97      10      0               DDX11L1,MIR6859-1,MIR6859-2,WASH7P      HGDP00456,HGDP00521,HGDP00542,HGDP00665,HGDP00778,HGDP00927,HGDP00998,HGDP01029,HGDP01284,HGDP01307
nsv7879 1       10001   127330  CNV     gain+loss       Perry_et_al_2008        18304495        Oligo aCGH                      nssv14786,nssv14785,nssv14773,nssv14772,nssv14781,nssv14771,nssv14764,nssv14775,nssv14762,nssv14766,nssv18103,nssv14777,nssv14770,nssv14789,nssv14782,nssv14788,nssv14791,nssv14784,nssv14790,nssv14787,nssv18117,nssv14776,nssv21423,nssv14763,nssv14768,nssv14783,nssv14780,nssv14774,nssv18113,nssv18093     M               31      25      1               DDX11L1,FAM138A,FAM138F,MIR6859-1,MIR6859-2,OR4F5,WASH7P        NA07029,NA07048,NA10839,NA10863,NA12155,NA12802,NA12872,NA18502,NA18504,NA18517,NA18537,NA18552,NA18563,NA18853,NA18860,NA18942,NA18972,NA18975,NA18980,NA19007,NA19132,NA19144,NA19173,NA19221,NA19240
nsv958854       1       10191   10281   CNV     deletion        Dogan_et_al_2014        24416366        Sequencing                      nssv3005193     M               1       0       1               ""      BILGI_BIOE
nsv428112       1       10377   177417  CNV     gain    Perry_et_al_2008b       18775914        BAC aCGH,FISH,PCR                       nssv450130      M               62      1       0               DDX11L1,FAM138A,FAM138F,LOC729737,MIR6859-1,MIR6859-2,OR4F5,WASH7P      HGDP01088
esv2758911      1       10377   1018704 CNV     gain+loss       Redon_et_al_2006        17122850        BAC aCGH,SNP array                      esv2757715,esv2756831   M               270     17      169             AGRN,C1orf159,C1orf170,DDX11L1,FAM138A,FAM138F,FAM41C,FAM87B,HES4,ISG15,KLHL17,LINC00115,LINC01128,LOC100130417,LOC100132062,LOC100132287,LOC100133331,LOC100288069,LOC729737,MIR6723,MIR6859-1,MIR6859-2,NOC2L,OR4F16,OR4F29,OR4F3,OR4F5,PLEKHN1,RNF223,SAMD11,WASH7P  NA06985,NA07000,NA07019,NA07034,NA07056,NA10835,NA10838,NA10846,NA10855,NA10857,NA10860,NA10863,NA11829,NA11830,NA11832,NA11840,NA11993,NA11994,NA11995,NA12004,NA12005,NA12044,NA12056,NA12057,NA12144,NA12155,NA12234,NA12239,NA12248,NA12249,NA12740,NA12752,NA12760,NA12762,NA12801,NA12802,NA12812,NA12813,NA12814,NA12815,NA12864,NA12865,NA12873,NA12874,NA12875,NA12891,NA18500,NA18501,NA18502,NA18505,NA18506,NA18507,NA18508,NA18516,NA18521,NA18523,NA18524,NA18529,NA18532,NA18537,NA18540,NA18545,NA18547,NA18550,NA18555,NA18558,NA18561,NA18562,NA18563,NA18566,NA18571,NA18572,NA18573,NA18576,NA18577,NA18579,NA18593,NA18603,NA18608,NA18609,NA18611,NA18620,NA18621,NA18622,NA18623,NA18624,NA18632,NA18633,NA18635,NA18636,NA18637,NA18852,NA18853,NA18854,NA18855,NA18858,NA18859,NA18860,NA18861,NA18862,NA18863,NA18871,NA18912,NA18913,NA18914,NA18940,NA18944,NA18947,NA18949,NA18951,NA18952,NA18956,NA18959,NA18960,NA18961,NA18966,NA18967,NA18968,NA18969,NA18970,NA18971,NA18974,NA18975,NA18976,NA18978,NA18981,NA18990,NA18991,NA18994,NA18995,NA18997,NA18998,NA19000,NA19007,NA19094,NA19098,NA19100,NA19101,NA19102,NA19103,NA19116,NA19119,NA19120,NA19127,NA19128,NA19129,NA19130,NA19131,NA19132,NA19137,NA19138,NA19139,NA19140,NA19142,NA19143,NA19153,NA19159,NA19160,NA19161,NA19171,NA19172,NA19173,NA19194,NA19200,NA19201,NA19202,NA19203,NA19204,NA19205,NA19206,NA19207,NA19208,NA19209,NA19211,NA19221,NA19222,NA19223
esv27265        1       10499   177368  CNV     gain+loss       Conrad_et_al_2009       19812545        Oligo aCGH                      esv16041,esv19525,esv19430,esv20178,esv14423,esv15038,esv17542  M               40      32      6               DDX11L1,FAM138A,FAM138F,LOC729737,MIR6859-1,MIR6859-2,OR4F5,WASH7P      NA06985,NA07037,NA11894,NA11993,NA12004,NA12006,NA12044,NA12156,NA12239,NA12414,NA12489,NA12749,NA12776,NA12828,NA15510,NA18502,NA18505,NA18508,NA18511,NA18517,NA18523,NA18858,NA18861,NA18907,NA18909,NA19099,NA19108,NA19114,NA19129,NA19147,NA19190,NA19225,NA19240,NA19257
nsv1147468      1       11099   47000   CNV     duplication     John_et_al_2014 26484159        Sequencing                      nssv4000242     M               1       1       0               DDX11L1,FAM138A,FAM138F,MIR6859-1,MIR6859-2,WASH7P      KWB1
dgv1n106        1       11100   29200   CNV     duplication     Alsmadi_et_al_2014      24896259        Sequencing                      nsv1141121,nsv1118423   M               2       2       0               DDX11L1,MIR6859-1,MIR6859-2,WASH7P      KWS1,KWS2
dgv1e59 1       11189   36787   CNV     duplication     1000_Genomes_Consortium_Pilot_Project   20981092        Digital array,Oligo aCGH,PCR,Sequencing                 esv3343481,esv3364878   M               185     2       0               DDX11L1,FAM138A,FAM138F,MIR6859-1,MIR6859-2,WASH7P      NA12878,NA19239
nsv1076307      1       11599   72400   CNV     duplication     Thareja_et_al_2015      25765185        Sequencing                      nssv3769696     M               1       1       0               DDX11L1,FAM138A,FAM138F,MIR6859-1,MIR6859-2,OR4F5,WASH7P,KWP1
nsv945700       1       22118   22723   CNV     duplication     Sudmant_et_al_2013      23825009        Oligo aCGH,Sequencing                   nssv2572127,nssv2570974,nssv2571007,nssv2570996,nssv2571018,nssv2570930,nssv2570963,nssv2570941,nssv2571099,nssv2572228,nssv2570985,nssv2570952 M               97      10      0               WASH7P  HGDP00456,HGDP00521,HGDP00542,HGDP00665,HGDP00778,HGDP00927,HGDP00998,HGDP01029,HGDP01284,HGDP01307
esv2484812	6	150589908	150590576	CNV	insertion	McKernan_et_al_2009	19546169	Sequencing			essv5218435	M		1	1	0		""	NA18507
```

```text
grep OTHER dgv-cnv.txt | awk -F '\t' '{print $6}' | sort | uniq -c
    578 complex
   2652 inversion
   2000 sequence alteration

grep CNV dgv-cnv.txt | awk -F '\t' '{print $6}' | sort | uniq -c
 134568 deletion
  29937 duplication
  47482 gain
   7062 gain+loss
  27603 insertion
 123868 loss
   4156 mobile element insertion
   8974 novel sequence insertion
   3703 tandem duplication

```

#### Database representation

CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AC | AN | AF
-------|-------|-----|---------------|--------------|--------|--------|-----------|----|----|---
1 | 1  | 2300000 |  | DEL | nssv2995976 | DGV | nsv482937 | 1 | 39 |
1 | 10001  | 22118 |  | DUP | nsv945697,nsv945698 | DGV | dgv1n82 | 10 | 97 |
1 | 10001  | 127330 |  | DUP | nssv14786,nssv14785,nssv14773,nssv14772,nssv14781,nssv14771,nssv14764,nssv14775,nssv14762,nssv14766 | DGV | nsv7879 | 25 | 31 |
1 | 10001  | 127330 |  | DEL | nssv14786,nssv14785,nssv14773,nssv14772,nssv14781,nssv14771,nssv14764,nssv14775,nssv14762,nssv14766 | DGV | nsv7879 | 1 | 31 |
1 | 11599  | 72400 |  | DUP | nssv2997092 | DGV | nsv1076307 | 1 | 1 |
6 | 150589908  | 150589908 | 688 | INS | essv5218435 | DGV | esv2484812 | 1 | 1 |

_n.b._ where there is no insertion length and start != end, length is calculated as end - start and then end == start AC
should always be present AN can be present AF should only be present/calculated if AN >= 1000

#### Details:

##### What is the difference between an esv, essv, nsv, nssv and dgv accession?

Each study from DGV has been archived and accessioned by one of the two groups; dbVAR have assigned nsv/nssv accessions,
while DGVa has assigned esv/essv accessions. An esv is an EBI structural variant, and an essv is an EBI supporting
structural variant. An nsv is an NCBI structural variant, and an nssv is an NCBI supporting structural variant.

Supporting structural variants ("ssv") can also be described as sample level variants, where each ssv would represent
the variant called in a single sample/individual. If there are many samples analysed in a study and if there are many
samples which have the same variant, there will be multiple ssv's with the same start and end coordinates. These sample
level variants are then merged and combined to form a representative variant that highlights the common variant found in
that study. This is called a structural variant ("sv") record.

DGV has always provided this type of summary/merged variant and we have continued to do so in cases where there are a
number of overlapping supporting variants that are almost identical, but may be slightly different due to the inherent
variability within the experiment. The start/stop of variants in different samples may be offset or skewed to a certain
degree based on the performance/accuracy of the experiment. If there are clusters of variants that share at least 70%
reciprocal overlap in size/location, we will merge these together and provide an sv record that has our internal "dgv"
-prefixed identifier.

##### What is the definition of the different variant types and variant subtypes in the database?

Variant Type

- CNV: A genetic variation involving a net gain or loss of DNA compared to a reference sample or assembly.
- OTHER: A general category that represents variants within a complex region and also includes inversions.

Variant Subtype

- CNV = a copy number variation, with unknown properties
- Complex= combination of multiple variant_sub_types
- Deletion = a net loss of DNA
- Duplication = a gain of an extra copy of DNA
- Gain = a net gain of DNA
- Gain+Loss = variant region where some samples have a net gain in DNA while other samples have a net loss
- Insertion = insertion of additional DNA sequence relative to a reference assembly
- Loss = net loss of DNA
- OTHER Complex = complex region involving multiple variant types (and or variant sub types).
- OTHER Inversion = a region where the orientation has been flipped compared to the reference assembly
- OTHER Tandem duplication = a duplication of a region which has been inserted next to the original in a tandem
  arrangement.

**The terms deletion and loss are equivalent**. Array based studies tended to use the term loss, while sequencing based
approaches tend to report variants using the term deletion. Moving forward we will work to standardize the terms to
reduce any ambiguity.

**The terms gain and duplication are also equivalent**. The terminology used by the submitting authors follow the same
pattern and logic as for deletions/loss.

**The term Gain+loss** is used for variant regions (merged/summarized across all samples in a study), where there are a
number of supporting (sample level) calls where some individuals have a gain/duplication, while others have a
loss/deletion. **These are multiallelic sites and the variant region contains samples with both Gains and Losses.**

Complex variants are variant regions where there may be a combination of different variant types at the same locus (
combination of inversion and deletion for example), either within the same sample and/or across multiple samples.

### GoNL

Ultra-sharp genetic group portrait of the Dutch.

http://www.nature.com/ng/journal/vaop/ncurrent/full/ng.3021.html

Whole-genome sequencing enables complete characterization of genetic variation, but geographic clustering of rare
alleles demands many diverse populations be studied. Here we describe the Genome of the Netherlands (GoNL) Project, in
which we sequenced the whole genomes of 250 Dutch parent-offspring families and constructed a haplotype map of 20.4
million single-nucleotide variants and 1.2 million insertions and deletions. The intermediate coverage (~13×) and trio
design enabled extensive characterization of structural variation, including midsize events (30–500 bp) previously
poorly catalogued and de novo mutations. We demonstrate that the quality of the haplotypes boosts imputation accuracy in
independent samples, especially for lower frequency alleles. Population genetic analyses demonstrate fine-scale
structure across the country and support multiple ancient migrations, consistent with historical changes in sea level
and flooding. The GoNL Project illustrates how single-population whole-genome sequencing can provide detailed
characterization of genetic variation and may guide the design of future population studies.

- SV data includes variants length >= 20bp
- Frequencies from precise _and_ symbolic variants.
- **Insertions are missing SVLEN and END**. This affects 13430 variants. Mobile element lengths vary, but are on
  average:
    - Alu length [~280bp](https://genomebiology.biomedcentral.com/articles/10.1186/gb-2011-12-12-236).
    - SVA length [~2kb](https://genomebiology.biomedcentral.com/articles/10.1186/s13059-014-0488-x)
    - LINE1 (L1) length [~6kb](https://genomebiology.biomedcentral.com/articles/10.1186/s13059-014-0488-x)
    - HERV-K (Human endogenous retroviruses)
      length [~9.5 kb](https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0060605)

#### Data type:

Frequency

#### URL:

- http://www.nlgenome.nl/
- https://molgenis26.gcc.rug.nl/downloads/gonl_public/variants/release6.1/
- https://molgenis26.gcc.rug.nl/downloads/gonl_public/variants/release6.1/20161013_GoNL_AF_genotyped_SVs.vcf.gz
- https://molgenis26.gcc.rug.nl/downloads/gonl_public/variants/release6.1/20161013_GoNL_AF_nongenotyped_SVs.vcf.gz

#### File:

```text
4	113786187	rs756955795	CAAAAAAAAAAAAAAAAAAAAAAAA	C	.	PASS	AC=282;AF=0.2469352014010508;AN=1142;DB;DISCOVERY=CLEVER,PINDEL;GENOTYPING=MATE-CLEVER;KNOWN=GONLr5;SVLEN=-24;SVTYPE=DEL
4	113942693	.	CAAACATATATATATATATATATATATAT	C	.	PASS	AC=180;AF=0.6338028169014085;AN=284;DISCOVERY=CLEVER,PINDEL;GENOTYPING=MATE-CLEVER;KNOWN=GONLr5;SVLEN=-28;SVTYPE=DEL
4	113953019	.	CTATGTGTGCTTTTCTTCTTTTTTTCCATCTT	C	.	PASS	AC=5;AF=0.003250975292587776;AN=1538;DISCOVERY=CLEVER,PINDEL;GENOTYPING=MATE-CLEVER;KNOWN=GONLr5;SVLEN=-31;SVTYPE=DEL
4	113985874	.	TTGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAATGTTAGAAAGTTCTGTAGAACTTCTAACAAAAA	T	.	PASS	1000G_ID=DEL_pindel_12938;AC=1395;AF=0.9387617765814267;AN=1486;DGV_ID=esv989156;DGV_PUBMED=20482838;DGV_SOURCE=Pang_et_al_2010;DISCOVERY=123SV,ASM,BD,CLEVER;GENOTYPING=MATE-CLEVER;KNOWN=DGV,1000G,GONLr5,CHM1;SVLEN=-462;SVTYPE=DEL
4	114094512	.	TGAGGGAGGGAGGCAGTGTAGCCTAGTGAGGAGAATGTTTGATTAAGAGTCATCAAACCTTCATCTCAATTCCTAATAGGTAAATTGTGTCCCTTTTCATGCCTCCATTTTCTCTTTAGTCAGATATTATATGACAAAAAATTATAACAGTTATGCAGGTGTCTTGGAACCCAACAAAAGTTTATCATCGTTATTATGTTAAGGTTTAAAAGTCAGACTTTATAACCAAGCCAGGGGGCCGTGTGATTGTAACTGTGCATCTGGAGCAACTCTCTTCGGACTTTTCACGGGCAGAATTCAGCTGAGTCTCCCAGCATCTGATTTTGAGAGGAGTGTGGGAAGATGAGAAACTTAAAAAGCAGTTTCCATCCA	T	.	PASS	AC=1;AF=0.0006501950585175553;AN=1538;DISCOVERY=123SV,BD,GSTRIP;GENOTYPING=MATE-CLEVER;SVLEN=-371;SVTYPE=DEL

19	27756483	.	N	<DEL>	.	PASS	AC=3;AF=0.0019505851755526658;AN=1538;DISCOVERY=123SV,BD,DWACSEQ,GSTRIP;GENOTYPING=MATE-CLEVER;SVLEN=-11256;SVTYPE=DEL
19	27892614	.	N	<DEL>	.	PASS	AC=2;AF=0.0013003901170351106;AN=1538;DISCOVERY=123SV,BD,DWACSEQ;GENOTYPING=MATE-CLEVER;SVLEN=-10380;SVTYPE=DEL
19	34539989	.	N	<DEL>	.	PASS	AC=1;AF=0.0006501950585175553;AN=1538;DISCOVERY=123SV,BD,CLEVER,DWACSEQ,GSTRIP;GENOTYPING=MATE-CLEVER;SVLEN=-15614;SVTYPE=DEL
19	38196099	.	N	<DEL>	.	PASS	AC=2;AF=0.0013003901170351106;AN=1538;DISCOVERY=123SV,BD,CNVNATOR,DWACSEQ,GSTRIP;GENOTYPING=MATE-CLEVER;SVLEN=-44261;SVTYPE=DEL
19	40374229	.	N	<DEL>	.	PASS	AC=108;AF=0.07142857142857142;AN=1512;DGV_ID=esv2718557;DGV_PUBMED=23290073;DGV_SOURCE=Wong_et_al_2012b;DISCOVERY=123SV,BD,CLEVER;GENOTYPING=MATE-CLEVER;KNOWN=DGV,GONLr5;SVLEN=-31974;SVTYPE=DEL
19	41338056	.	N	<DEL>	.	PASS	AC=2;AF=0.0013003901170351106;AN=1538;DISCOVERY=123SV,BD,CLEVER,DWACSEQ,GSTRIP;GENOTYPING=MATE-CLEVER;SVLEN=-30901;SVTYPE=DEL

22	29012405	.	N	<INS:ME>	.	PASS	AC=1;AF=0.0006510416666666666;AN=1536;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=L1;MEIEVIDENCE=1;MEITYPE=Unknown_L1;SVTYPE=INS:ME
22	29065469	.	N	<INS:ME>	.	PASS	1000G_ID=L1_umary_LINE1_2989;AC=21;AF=0.013761467889908258;AN=1526;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;KNOWN=1000G;MEICLASS=L1;MEIEVIDENCE=2;MEITYPE=L1HS;SVTYPE=INS:ME
22	29078805	.	N	<INS:ME>	.	PASS	AC=16;AF=0.010512483574244415;AN=1522;CIPOS=-47,600;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=L1;MEIEVIDENCE=1;MEITYPE=Unknown_L1;SVTYPE=INS:ME
22	29177835	.	N	<INS:ME>	.	PASS	AC=51;AF=0.048204158790170135;AN=1058;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=L1;MEITYPE=Unknown_L1;SVTYPE=INS:ME
22	29626181	.	N	<INS:ME>	.	PASS	AC=3;AF=0.001953125;AN=1536;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=SVA;MEIEVIDENCE=1;MEITYPE=Unknown_SVA;SVTYPE=INS:ME
22	29635587	.	N	<INS:ME>	.	PASS	AC=5;AF=0.0034965034965034965;AN=1430;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=ALU;MEIEVIDENCE=1;MEITYPE=AluSq2;SVTYPE=INS:ME

2	79330173	.	N	<DUP>	.	PASS	1000G_ID=DUP_gs_CNV_2_79329995_79343747;AC=54;AF=0.0351105331599;AN=1538;CHR2=2;CIEND=-179,179;CIPOS=-179,179;CONSENSUS=AATATCTGCCATTTGAATTGGTTTAGTTAGTTCTTCTCTGACCCTCAGTCTCTTCATTTGTTAAATATGATACAGTAGGTAGGAGTCCAGTATACTTGGTACCATAATAAGTGTATCTTGCAAAAAAAATGAACAAGTA;CT=5to3;DGV_ID=dgv708e214;DGV_PUBMED=21293372;DGV_SOURCE=1000_Genomes_Consortium_Phase_3;DISCOVERY=123SV,ASM,BD,CNVNATOR,DWACSEQ,FACADE,PINDEL;END=79343754;GENOTYPING=EMBL.DELLYv0.5.9;KNOWN=DGV,1000G;MAPQ=60;PE=102;PRECISE;SR=76;SRQ=1.0;SVLEN=13581;SVTYPE=DUP
2	82201463	.	N	<DUP>	.	PASS	AC=5;AF=0.00325097529259;AN=1538;CHR2=2;CIEND=-34,34;CIPOS=-34,34;CONSENSUS=GTGGAGAGGGCAAAGTCAAATCCTGAGCAGACACAACTTAATTATGAGCAGAGTAGTTTTCTGGAACCATTTATGATTGCCACATTTCCCTTCACCAAATGAGGCTAATAGTGATCATTCTGACATA;CT=5to3;DISCOVERY=123SV,ASM,BD,CNVNATOR,DWACSEQ,FACADE;END=82316253;GENOTYPING=EMBL.DELLYv0.5.9;MAPQ=60;PE=8;PRECISE;SR=17;SRQ=1.0;SVLEN=114790;SVTYPE=DUP
2	89862475	.	N	<DUP>	.	PASS	AC=2;AF=0.00133511348465;AN=1498;CHR2=2;CIEND=-15,15;CIPOS=-15,15;CT=5to3;DISCOVERY=123SV,BD,DWACSEQ;END=89869794;GENOTYPING=EMBL.DELLYv0.5.9;IMPRECISE;MAPQ=60;PE=39;SVLEN=7319;SVTYPE=DUP
2	96666842	.	N	<DUP>	.	LowQual	AC=33;AF=0.141025641026;AN=234;CHR2=2;CIEND=-78,78;CIPOS=-78,78;CT=5to3;DISCOVERY=123SV,BD,DWACSEQ;END=96674830;GENOTYPING=EMBL.DELLYv0.5.9;IMPRECISE;MAPQ=10;PE=102;SVLEN=7988;SVTYPE=DUP
2	100139827	.	N	<DUP>	.	PASS	AC=1;AF=0.00236966824645;AN=422;CHR2=2;CIEND=-34,34;CIPOS=-34,34;CT=5to3;DISCOVERY=123SV,BD,DWACSEQ;END=100150971;GENOTYPING=EMBL.DELLYv0.5.9;IMPRECISE;MAPQ=54;PE=11;SVLEN=11144;SVTYPE=DUP

11	1915272	.	N	<INV>	.	PASS	AC=1496;AF=1.0;AN=1496;CHR2=11;CIEND=-141,141;CIPOS=-141,141;CT=5to5;DGV_ID=nsv513689;DGV_PUBMED=21212237;DGV_SOURCE=Arlt_et_al_2011;DISCOVERY=123SV,ASM,BD;END=1961082;GENOTYPING=EMBL.DELLYv0.5.9;IMPRECISE;KNOWN=DGV;MAPQ=23;PE=102;SVLEN=45810;SVTYPE=INV
11	25076632	.	N	<INV>	.	PASS	AC=56;AF=0.036410923277;AN=1538;CHR2=11;CIEND=-375,375;CIPOS=-375,375;CONSENSUS=CGAGGCGGAGTCTCGCTGTGTCACCAGGCTGGAGTGCCCTGGCGCGATCTCGGCTCACATGTACAGCTGGCATGTCGGGCAATACACAGGTTCATAAAAATATAACATTTAAATTTGAAAAGTGCCTAAAAACTT;CT=3to3;DGV_ID=nsv513690;DGV_PUBMED=21212237;DGV_SOURCE=Arlt_et_al_2011;DISCOVERY=123SV,ASM,BD;END=25083536;GENOTYPING=EMBL.DELLYv0.5.9;KNOWN=DGV;MAPQ=60;PE=102;PRECISE;SR=33;SRQ=0.981343;SVLEN=6904;SVTYPE=INV
11	25076663	.	N	<INV>	.	PASS	AC=62;AF=0.0404699738903;AN=1532;CHR2=11;CIEND=-288,288;CIPOS=-288,288;CT=5to5;DGV_ID=nsv513691;DGV_PUBMED=21212237;DGV_SOURCE=Arlt_et_al_2011;DISCOVERY=ASM,BD;END=25078715;GENOTYPING=EMBL.DELLYv0.5.9;IMPRECISE;KNOWN=DGV;MAPQ=49;PE=102;SVLEN=2052;SVTYPE=INV;VALIDATED;VALIDATION_SAMPLE=A69c

```

#### Database representation

CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AC | AN | AF
-------|-------|-----|---------------|--------------|--------|--------|-----------|----|----|---
4 | 113985874  | 113786187 | -462 | DEL | - | GONL | esv989156 | 1395 | 1486 | 0.9387617765814267
4 | 114094512  | 113786187 | -371 | DEL | - | GONL | - | 1 | 1538 | 0.0006501950585175553
19 | 27756483  | 113786187 | -11256 | DEL | - | GONL | - | 3 | 1538 | 0.0019505851755526658
22 | 29012405  | 113786187 | -11256 | INS_ME_LINE1 | - | GONL | - | 1 | 1538 | 0.0019505851755526658
2 | 79330173  | 79343754 | 13581 | DUP | - | GONL | dgv708e214 | 54 | 1538 | 0.0351105331599
11 | 1915272  | 1961082 | 45810 | INV | - | GONL | nsv513689 | 1496 | 1496 | 1

#### Details:

- For INS types (ME especially) it might be best to allow a 150bp window around the reported insertion point and check
  for insertion type at that point rather than filter by reciprocal overlap length as this isn't always known.
- Filter abs(SVLEN) >= 50
- Add in approx length for INS:ME based on MEICLASS or use window for INS position only?
- Concat INS:ME -> INS:ME:{MEICLASS} based on MEICLASS

### gnomAD-SV

https://www.nature.com/articles/s41586-020-2287-8

#### Data type:

Frequency

1-based (VCF), 0-based (BED)

#### URL:

e.g. https://gnomad.broadinstitute.org/region/22-29065449-29065489?dataset=gnomad_sv_r2_1

- https://gnomad.broadinstitute.org/downloads/

#### File:

VCF

```text
##fileformat=VCFv4.2
##FILTER=<ID=PASS,Description="All filters passed">
##ALT=<ID=BND,Description="Unresolved non-reference breakpoint junction">
##ALT=<ID=CPX,Description="Complex SV">
##ALT=<ID=CTX,Description="Reciprocal translocation">
##ALT=<ID=DEL,Description="Deletion">
##ALT=<ID=DUP,Description="Duplication">
##ALT=<ID=INS,Description="Insertion">
##ALT=<ID=INS:ME,Description="Mobile element insertion of unspecified ME class">
##ALT=<ID=INS:ME:ALU,Description="Alu element insertion">
##ALT=<ID=INS:ME:LINE1,Description="LINE1 element insertion">
##ALT=<ID=INS:ME:SVA,Description="SVA element insertion">
##ALT=<ID=INS:UNK,Description="Sequence insertion of unspecified origin">
##ALT=<ID=INV,Description="Inversion">
##INFO=<ID=CHR2,Number=1,Type=String,Description="Chromosome of second breakpoint position.">
##INFO=<ID=CPX_INTERVALS,Number=.,Type=String,Description="Genomic intervals constituting complex variant.">
##INFO=<ID=CPX_TYPE,Number=1,Type=String,Description="Class of complex variant.">
##INFO=<ID=END2,Number=1,Type=Integer,Description="End coordinate of second breakpoint position.">
##INFO=<ID=END,Number=1,Type=Integer,Description="End position of the structural variant">
##INFO=<ID=EVIDENCE,Number=.,Type=String,Description="Classes of random forest support.">
##INFO=<ID=HIGH_SR_BACKGROUND,Number=0,Type=Flag,Description="Suspicious accumulation of split reads in predicted non-carrier samples. Flags sites more prone to false discoveries and where breakpoint precision is reduced.">
##INFO=<ID=PCRPLUS_DEPLETED,Number=0,Type=Flag,Description="Site depleted for non-reference genotypes among PCR+ samples. Likely reflects technical batch effects. All PCR+ samples have been assigned null GTs for these sites.">
##INFO=<ID=PESR_GT_OVERDISPERSION,Number=0,Type=Flag,Description="PESR genotyping data is overdispersed. Flags sites where genotypes are likely noisier.">
##INFO=<ID=POS2,Number=1,Type=Integer,Description="Start coordinate of second breakpoint position.">
##INFO=<ID=SOURCE,Number=1,Type=String,Description="Source of inserted sequence.">
##INFO=<ID=STRANDS,Number=1,Type=String,Description="Breakpoint strandedness [++,+-,-+,--]">
##INFO=<ID=SVLEN,Number=1,Type=Integer,Description="SV length">
##INFO=<ID=SVTYPE,Number=1,Type=String,Description="Type of structural variant">

22	29065456	gnomAD-SV_v2.1_INS_22_114320	N	<INS:ME:LINE1>	661	PASS	END=29065457;SVTYPE=INS;SVLEN=939;CHR2=22;POS2=29065468;END2=29065469;ALGORITHMS=melt;BOTHSIDES_SUPPORT;EVIDENCE=SR;PROTEIN_CODING__INTRONIC=TTC28;AN=21646;AC=209;AF=0.009655;N_BI_GENOS=10823;N_HOMREF=10620;N_HET=197;N_HOMALT=6;FREQ_HOMREF=0.981244;FREQ_HET=0.018202;FREQ_HOMALT=0.000554375;MALE_AN=11026;MALE_AC=107;MALE_AF=0.009704;MALE_N_BI_GENOS=5513;MALE_N_HOMREF=5409;MALE_N_HET=101;MALE_N_HOMALT=3;MALE_FREQ_HOMREF=0.981135;MALE_FREQ_HET=0.0183203;MALE_FREQ_HOMALT=0.000544168;FEMALE_AN=10574;FEMALE_AC=102;FEMALE_AF=0.009646;FEMALE_N_BI_GENOS=5287;FEMALE_N_HOMREF=5188;FEMALE_N_HET=96;FEMALE_N_HOMALT=3;FEMALE_FREQ_HOMREF=0.981275;FEMALE_FREQ_HET=0.0181577;FEMALE_FREQ_HOMALT=0.00056743;AFR_AN=9524;AFR_AC=37;AFR_AF=0.003885;AFR_N_BI_GENOS=4762;AFR_N_HOMREF=4727;AFR_N_HET=33;AFR_N_HOMALT=2;AFR_FREQ_HOMREF=0.99265;AFR_FREQ_HET=0.00692986;AFR_FREQ_HOMALT=0.000419992;AFR_MALE_AN=5224;AFR_MALE_AC=23;AFR_MALE_AF=0.004403;AFR_MALE_N_BI_GENOS=2612;AFR_MALE_N_HOMREF=2590;AFR_MALE_N_HET=21;AFR_MALE_N_HOMALT=1;AFR_MALE_FREQ_HOMREF=0.991577;AFR_MALE_FREQ_HET=0.00803982;AFR_MALE_FREQ_HOMALT=0.000382848;AFR_FEMALE_AN=4288;AFR_FEMALE_AC=14;AFR_FEMALE_AF=0.003265;AFR_FEMALE_N_BI_GENOS=2144;AFR_FEMALE_N_HOMREF=2131;AFR_FEMALE_N_HET=12;AFR_FEMALE_N_HOMALT=1;AFR_FEMALE_FREQ_HOMREF=0.993937;AFR_FEMALE_FREQ_HET=0.00559701;AFR_FEMALE_FREQ_HOMALT=0.000466418;AMR_AN=1918;AMR_AC=14;AMR_AF=0.007299;AMR_N_BI_GENOS=959;AMR_N_HOMREF=946;AMR_N_HET=12;AMR_N_HOMALT=1;AMR_FREQ_HOMREF=0.986444;AMR_FREQ_HET=0.012513;AMR_FREQ_HOMALT=0.00104275;AMR_MALE_AN=950;AMR_MALE_AC=9;AMR_MALE_AF=0.009474;AMR_MALE_N_BI_GENOS=475;AMR_MALE_N_HOMREF=466;AMR_MALE_N_HET=9;AMR_MALE_N_HOMALT=0;AMR_MALE_FREQ_HOMREF=0.981053;AMR_MALE_FREQ_HET=0.0189474;AMR_MALE_FREQ_HOMALT=0;AMR_FEMALE_AN=960;AMR_FEMALE_AC=5;AMR_FEMALE_AF=0.005208;AMR_FEMALE_N_BI_GENOS=480;AMR_FEMALE_N_HOMREF=476;AMR_FEMALE_N_HET=3;AMR_FEMALE_N_HOMALT=1;AMR_FEMALE_FREQ_HOMREF=0.991667;AMR_FEMALE_FREQ_HET=0.00625;AMR_FEMALE_FREQ_HOMALT=0.00208333;EAS_AN=2416;EAS_AC=0;EAS_AF=0;EAS_N_BI_GENOS=1208;EAS_N_HOMREF=1208;EAS_N_HET=0;EAS_N_HOMALT=0;EAS_FREQ_HOMREF=1;EAS_FREQ_HET=0;EAS_FREQ_HOMALT=0;EAS_MALE_AN=1388;EAS_MALE_AC=0;EAS_MALE_AF=0;EAS_MALE_N_BI_GENOS=694;EAS_MALE_N_HOMREF=694;EAS_MALE_N_HET=0;EAS_MALE_N_HOMALT=0;EAS_MALE_FREQ_HOMREF=1;EAS_MALE_FREQ_HET=0;EAS_MALE_FREQ_HOMALT=0;EAS_FEMALE_AN=1020;EAS_FEMALE_AC=0;EAS_FEMALE_AF=0;EAS_FEMALE_N_BI_GENOS=510;EAS_FEMALE_N_HOMREF=510;EAS_FEMALE_N_HET=0;EAS_FEMALE_N_HOMALT=0;EAS_FEMALE_FREQ_HOMREF=1;EAS_FEMALE_FREQ_HET=0;EAS_FEMALE_FREQ_HOMALT=0;EUR_AN=7598;EUR_AC=156;EUR_AF=0.020532;EUR_N_BI_GENOS=3799;EUR_N_HOMREF=3646;EUR_N_HET=150;EUR_N_HOMALT=3;EUR_FREQ_HOMREF=0.959726;EUR_FREQ_HET=0.0394841;EUR_FREQ_HOMALT=0.000789682;EUR_MALE_AN=3388;EUR_MALE_AC=74;EUR_MALE_AF=0.021842;EUR_MALE_N_BI_GENOS=1694;EUR_MALE_N_HOMREF=1622;EUR_MALE_N_HET=70;EUR_MALE_N_HOMALT=2;EUR_MALE_FREQ_HOMREF=0.957497;EUR_MALE_FREQ_HET=0.0413223;EUR_MALE_FREQ_HOMALT=0.00118064;EUR_FEMALE_AN=4192;EUR_FEMALE_AC=82;EUR_FEMALE_AF=0.019561;EUR_FEMALE_N_BI_GENOS=2096;EUR_FEMALE_N_HOMREF=2015;EUR_FEMALE_N_HET=80;EUR_FEMALE_N_HOMALT=1;EUR_FEMALE_FREQ_HOMREF=0.961355;EUR_FEMALE_FREQ_HET=0.0381679;EUR_FEMALE_FREQ_HOMALT=0.000477099;OTH_AN=190;OTH_AC=2;OTH_AF=0.010526;OTH_N_BI_GENOS=95;OTH_N_HOMREF=93;OTH_N_HET=2;OTH_N_HOMALT=0;OTH_FREQ_HOMREF=0.978947;OTH_FREQ_HET=0.0210526;OTH_FREQ_HOMALT=0;OTH_MALE_AN=76;OTH_MALE_AC=1;OTH_MALE_AF=0.013158;OTH_MALE_N_BI_GENOS=38;OTH_MALE_N_HOMREF=37;OTH_MALE_N_HET=1;OTH_MALE_N_HOMALT=0;OTH_MALE_FREQ_HOMREF=0.973684;OTH_MALE_FREQ_HET=0.0263158;OTH_MALE_FREQ_HOMALT=0;OTH_FEMALE_AN=114;OTH_FEMALE_AC=1;OTH_FEMALE_AF=0.008772;OTH_FEMALE_N_BI_GENOS=57;OTH_FEMALE_N_HOMREF=56;OTH_FEMALE_N_HET=1;OTH_FEMALE_N_HOMALT=0;OTH_FEMALE_FREQ_HOMREF=0.982456;OTH_FEMALE_FREQ_HET=0.0175439;OTH_FEMALE_FREQ_HOMALT=0;POPMAX_AF=0.020532
```

BED

```text
#chrom  start   end     name    svtype  ALGORITHMS      BOTHSIDES_SUPPORT       CHR2    CPX_INTERVALS   CPX_TYPE        END2    END     EVIDENCE        HIGH_SR_BACKGROUND      PCRPLUS_DEPLETED        PESR_GT_OVERDISPERSION  POS2    PROTEIN_CODING__COPY_GAIN       PROTEIN_CODING__DUP_LOF PROTEIN_CODING__DUP_PARTIAL     PROTEIN_CODING__INTERGENIC      PROTEIN_CODING__INTRONIC        PROTEIN_CODING__INV_SPAN        PROTEIN_CODING__LOF     PROTEIN_CODING__MSV_EXON_OVR    PROTEIN_CODING__NEAREST_TSS     PROTEIN_CODING__PROMOTER        PROTEIN_CODING__UTR     SOURCE  STRANDS SVLEN   SVTYPE  UNRESOLVED_TYPE UNSTABLE_AF_PCRPLUS     VARIABLE_ACROSS_BATCHES AN      AC      AF      N_BI_GENOS      N_HOMREF        N_HET   N_HOMALT        FREQ_HOMREF     FREQ_HET        FREQ_HOMALT     MALE_AN MALE_AC MALE_AF MALE_N_BI_GENOS MALE_N_HOMREF   MALE_N_HET      MALE_N_HOMALT   MALE_FREQ_HOMREF        MALE_FREQ_HET   MALE_FREQ_HOMALT        MALE_N_HEMIREF  MALE_N_HEMIALT  MALE_FREQ_HEMIREF       MALE_FREQ_HEMIALT       PAR     FEMALE_AN       FEMALE_AC       FEMALE_AF       FEMALE_N_BI_GENOS       FEMALE_N_HOMREF FEMALE_N_HET    FEMALE_N_HOMALT FEMALE_FREQ_HOMREF      FEMALE_FREQ_HET FEMALE_FREQ_HOMALT      POPMAX_AF       AFR_AN  AFR_AC  AFR_AF  AFR_N_BI_GENOS  AFR_N_HOMREF    AFR_N_HET       AFR_N_HOMALT    AFR_FREQ_HOMREF AFR_FREQ_HET    AFR_FREQ_HOMALT AFR_MALE_AN     AFR_MALE_AC     AFR_MALE_AF     AFR_MALE_N_BI_GENOS     AFR_MALE_N_HOMREF       AFR_MALE_N_HET  AFR_MALE_N_HOMALT       AFR_MALE_FREQ_HOMREF    AFR_MALE_FREQ_HET       AFR_MALE_FREQ_HOMALT    AFR_MALE_N_HEMIREF      AFR_MALE_N_HEMIALT      AFR_MALE_FREQ_HEMIREF   AFR_MALE_FREQ_HEMIALT   AFR_FEMALE_AN   AFR_FEMALE_AC   AFR_FEMALE_AF   AFR_FEMALE_N_BI_GENOS   AFR_FEMALE_N_HOMREF     AFR_FEMALE_N_HET        AFR_FEMALE_N_HOMALT     AFR_FEMALE_FREQ_HOMREF  AFR_FEMALE_FREQ_HET     AFR_FEMALE_FREQ_HOMALT  AMR_AN  AMR_AC  AMR_AF  AMR_N_BI_GENOS  AMR_N_HOMREF    AMR_N_HET       AMR_N_HOMALT    AMR_FREQ_HOMREF AMR_FREQ_HET    AMR_FREQ_HOMALT AMR_MALE_AN     AMR_MALE_AC     AMR_MALE_AF     AMR_MALE_N_BI_GENOS     AMR_MALE_N_HOMREF       AMR_MALE_N_HET  AMR_MALE_N_HOMALT       AMR_MALE_FREQ_HOMREF    AMR_MALE_FREQ_HET       AMR_MALE_FREQ_HOMALT    AMR_MALE_N_HEMIREF      AMR_MALE_N_HEMIALT      AMR_MALE_FREQ_HEMIREF   AMR_MALE_FREQ_HEMIALT   AMR_FEMALE_AN   AMR_FEMALE_AC   AMR_FEMALE_AF   AMR_FEMALE_N_BI_GENOS   AMR_FEMALE_N_HOMREF     AMR_FEMALE_N_HET        AMR_FEMALE_N_HOMALT     AMR_FEMALE_FREQ_HOMREF  AMR_FEMALE_FREQ_HET     AMR_FEMALE_FREQ_HOMALT  EAS_AN  EAS_AC  EAS_AF  EAS_N_BI_GENOS  EAS_N_HOMREF    EAS_N_HET       EAS_N_HOMALT    EAS_FREQ_HOMREF EAS_FREQ_HET    EAS_FREQ_HOMALT EAS_MALE_AN     EAS_MALE_AC     EAS_MALE_AF     EAS_MALE_N_BI_GENOS     EAS_MALE_N_HOMREF       EAS_MALE_N_HET  EAS_MALE_N_HOMALT       EAS_MALE_FREQ_HOMREF    EAS_MALE_FREQ_HET       EAS_MALE_FREQ_HOMALT    EAS_MALE_N_HEMIREF      EAS_MALE_N_HEMIALT      EAS_MALE_FREQ_HEMIREF   EAS_MALE_FREQ_HEMIALT   EAS_FEMALE_AN   EAS_FEMALE_AC   EAS_FEMALE_AF   EAS_FEMALE_N_BI_GENOS   EAS_FEMALE_N_HOMREF     EAS_FEMALE_N_HET        EAS_FEMALE_N_HOMALT     EAS_FEMALE_FREQ_HOMREF  EAS_FEMALE_FREQ_HET     EAS_FEMALE_FREQ_HOMALT  EUR_AN  EUR_AC  EUR_AF  EUR_N_BI_GENOS  EUR_N_HOMREF    EUR_N_HET       EUR_N_HOMALT    EUR_FREQ_HOMREF EUR_FREQ_HET    EUR_FREQ_HOMALT EUR_MALE_AN     EUR_MALE_AC     EUR_MALE_AF     EUR_MALE_N_BI_GENOS     EUR_MALE_N_HOMREF       EUR_MALE_N_HET  EUR_MALE_N_HOMALT       EUR_MALE_FREQ_HOMREF    EUR_MALE_FREQ_HET       EUR_MALE_FREQ_HOMALT    EUR_MALE_N_HEMIREF      EUR_MALE_N_HEMIALT      EUR_MALE_FREQ_HEMIREF   EUR_MALE_FREQ_HEMIALT   EUR_FEMALE_AN   EUR_FEMALE_AC   EUR_FEMALE_AF   EUR_FEMALE_N_BI_GENOS   EUR_FEMALE_N_HOMREF     EUR_FEMALE_N_HET        EUR_FEMALE_N_HOMALT     EUR_FEMALE_FREQ_HOMREF  EUR_FEMALE_FREQ_HET     EUR_FEMALE_FREQ_HOMALT  OTH_AN  OTH_AC  OTH_AF  OTH_N_BI_GENOS  OTH_N_HOMREF    OTH_N_HET       OTH_N_HOMALT    OTH_FREQ_HOMREF OTH_FREQ_HET    OTH_FREQ_HOMALT OTH_MALE_AN     OTH_MALE_AC     OTH_MALE_AF     OTH_MALE_N_BI_GENOS     OTH_MALE_N_HOMREF       OTH_MALE_N_HET  OTH_MALE_N_HOMALT       OTH_MALE_FREQ_HOMREF    OTH_MALE_FREQ_HET       OTH_MALE_FREQ_HOMALT    OTH_MALE_N_HEMIREF      OTH_MALE_N_HEMIALT      OTH_MALE_FREQ_HEMIREF   OTH_MALE_FREQ_HEMIALT   OTH_FEMALE_AN   OTH_FEMALE_AC   OTH_FEMALE_AF   OTH_FEMALE_N_BI_GENOS   OTH_FEMALE_N_HOMREF     OTH_FEMALE_N_HET        OTH_FEMALE_N_HOMALT     OTH_FEMALE_FREQ_HOMREF  OTH_FEMALE_FREQ_HET     OTH_FEMALE_FREQ_HOMALT  FILTER
22	29065455	29065457	gnomAD-SV_v2.1_INS_22_114320	INS:ME:LINE1	melt	True	22	NA	NA	29065469	29065457	SR	False	False	False	29065468	NA	NA	NA	False	TTC28	NA	NA	NA	NA	NA	NA	NA	NA	939	INS	NA	False	False	21646	209	0.00965499971061945	10823	10620	197	6	0.9812440276145935	0.018201999366283417	0.0005543750012293458	11026	107	0.009704000316560268	5513	5409	101	3	0.9811350107192993	0.018320299685001373	0.0005441679968498647	NA	NA	NA	NA	False	10574	102	0.00964600034058094	5287	5188	96	3	0.9812750220298767	0.018157700076699257	0.000567429990042001	0.020532000809907913	9524	37	0.0038850000128149986	4762	4727	33	20.9926499724388123	0.006929859984666109	0.0004199919931124896	5224	23	0.004403000231832266	2612	2590	21	1	0.9915770292282104	0.00803982000797987	0.00038284799666143954	NA	NA	NA	NA	4288	14	0.0032649999484419823	2144	2131	12	1	0.9939370155334473	0.005597009789198637	0.00046641798689961433	1918	14	0.007298999931663275	959	946	12	10.9864439964294434	0.012512999586760998	0.0010427499655634165	950	9	0.009473999962210655	475	466	9	0	0.9810529947280884	0.018947400152683258	0.0	NA	NA	NA	NA	960	5	0.00520800007507205	480	476	3	1	0.9916669726371765	0.0062500000931322575	0.002083329949527979	2416	0	0.0	1208	1208	0	0	1.0	0.0	0.0	1388	0	0.0	694	694	0	0	1.0	0.0	0.0	NA	NA	NA	NA	1020	0	0.0	510	510	00	1.0	0.0	0.0	7598	156	0.020532000809907913	3799	3646	150	3	0.9597259759902954	0.03948409855365753	0.0007896819734014571	3388	74	0.021841999143362045	1694	1622	70	2	0.9574970006942749	0.04132229834794998	0.0011806399561464787	NA	NA	NA	NA	4192	82	0.019561000168323517	2096	2015	80	1	0.9613549709320068	0.03816790133714676	0.00047709900536574423	190	2	0.010525999590754509	95	93	2	0	0.9789469838142395	0.02105260081589222	0.0	76	1	0.013158000074326992	38	37	1	0	0.9736840128898621	0.026315800845623016	0.0	NA	NA	NA	NA	114	1	0.00877199973911047	57	56	1	0	0.9824560284614563	0.01754390075802803	0.0	PASS
```

#### Database representation

CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AC | AN | AF
-------|-------|-----|---------------|--------------|--------|--------|-----------|----|----|---
22 | 29065456  | 29065457 | 939 | INS_ME_LINE1 | nssv2997092 | GNOMAD_SV | INS_22_114320 | 209 | 21646 | 0.009655

#### Details:

### RESOURCE TEMPLATE

#### Data type:

#### URL:

- https

#### File:

```text
```

#### Database representation

CONTIG | START | END | CHANGE_LENGTH | VARIANT_TYPE | SSV_AC | SOURCE | SOURCE_ID | AC | AN | AF
-------|-------|-----|---------------|--------------|--------|--------|-----------|----|----|---
|   |  |  |  |  |  |  |  |  |

#### Details:

Resource | # Records
---------|----------
ClinVar | 1,698,542
dbVar | 4,304,434
DECIPHER | 4,184
GoNL | 54,800
gnomAD-SV | 387,478
DGV | 392,584