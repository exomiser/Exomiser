


package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

/**
 * The GeneBlacklistFilter removes variants from the analysis which are assigned to a set of blacklisted genes.
 * <p>Blacklisted genes are removed to reduce the noise level. These are 56 pseudogenes, HLA genes,
 * and others that have a high degree of variants called in healthy individuals:</p>
 * </b>
 * <p>COL4A2-AS2, CRIPAK, FCGBP, GOLGA6L2, GOLGA8N, HLA-A, HLA-B, HLA-C, HLA-DMA, HLA-DMB, HLA-DOA, HLA-DOB, HLA-DPA1,
 * HLA-DPB1, HLA-DPB2, HLA-DQA1, HLA-DQA2, HLA-DQB1, HLA-DQB1-AS1, HLA-DQB2, HLA-DRA, HLA-DRB1, HLA-DRB5, HLA-DRB6,
 * HLA-E, HLA-F, HLA-F-AS1, HLA-G, HLA-H, HLA-J, HLA-L, KRTAP4-7, KRTAP4-8, KRTAP9-6, LILRA6, LILRB3, LINC02081,
 * LRRC37A2, MUC12, MUC16, MUC17, MUC19, MUC2, MUC20, MUC21, MUC3A, MUC4, MUC6, PDE4DIP, PRAMEF2, PRAMEF9, SIRPA, TBC1D3I,
 * UGT1A7, USP17L1</p>
 *
 * @since 14.0.0
 */
public record GeneBlacklistFilter(Set<String> blackList) implements VariantFilter {

    private static final FilterResult PASS = FilterResult.pass(FilterType.GENE_BLACKLIST_FILTER);
    private static final FilterResult FAIL = FilterResult.fail(FilterType.GENE_BLACKLIST_FILTER);
    private static final Set<String> DEFAULT_BLACKLIST = Set.of("COL4A2-AS2", "CRIPAK", "FCGBP", "GOLGA6L2", "GOLGA8N", "HLA-A",
            "HLA-B", "HLA-C", "HLA-DMA", "HLA-DMB", "HLA-DOA", "HLA-DOB", "HLA-DPA1", "HLA-DPB1", "HLA-DPB2", "HLA-DQA1",
            "HLA-DQA2", "HLA-DQB1", "HLA-DQB1-AS1", "HLA-DQB2", "HLA-DRA", "HLA-DRB1", "HLA-DRB5", "HLA-DRB6", "HLA-E",
            "HLA-F", "HLA-F-AS1", "HLA-G", "HLA-H", "HLA-J", "HLA-L", "KRTAP4-7", "KRTAP4-8", "KRTAP9-6", "LILRA6",
            "LILRB3", "LINC02081", "LRRC37A2", "MUC12", "MUC16", "MUC17", "MUC19", "MUC2", "MUC20", "MUC21", "MUC3A", "MUC4",
            "MUC6", "PDE4DIP", "PRAMEF2", "PRAMEF9", "SIRPA", "TBC1D3I", "UGT1A7", "USP17L1");

    private static final GeneBlacklistFilter DEFAULT_BLACKLIST_FILTER = new GeneBlacklistFilter(DEFAULT_BLACKLIST);

    public GeneBlacklistFilter {
        Objects.requireNonNull(blackList, "blackList cannot be null");
    }

    public static GeneBlacklistFilter defaultInstance() {
        return DEFAULT_BLACKLIST_FILTER;
    }

    @Override
    public FilterType filterType() {
        return FilterType.GENE_BLACKLIST_FILTER;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        return blackList.contains(variantEvaluation.geneSymbol()) ? FAIL : PASS;
    }

    @Override
    public String toString() {
        return "GeneBlacklistFilter{" + blackList + "}";
    }
}