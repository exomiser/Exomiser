---
layout: page
title: Command-line Interface 
subtitle: Instructions for configuring the Exomiser from the command line
---

### Multiple output formats

    --output-format HTML (default)
    --output-format TSV-GENE (TSV summary of genes)
    --output-format TSV-VARIANT (TSV summary of variants)
    --output-format VCF (VCF summary)

Output options can be combined, for example:

    --output-format TSV-GENE,VCF (TSV-GENE and VCF)
    --output-format TSV-GENE, TSV-VARIANT, VCF (TSV-GENE, TSV-VARIANT and VCF)