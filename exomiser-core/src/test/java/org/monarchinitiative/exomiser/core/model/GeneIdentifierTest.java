package org.monarchinitiative.exomiser.core.model;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GeneIdentifierTest {

    private final GeneIdentifier defaultIdentifier = GeneIdentifier.builder().build();

    @Test
    public void getGeneId() {
        assertThat(defaultIdentifier.getGeneId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .geneId("12345")
                .build();
        assertThat(instance.getGeneId(), equalTo("12345"));
    }

    @Test
    public void getGeneSymbol() {
        assertThat(defaultIdentifier.getGeneSymbol(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .geneSymbol("ABC1")
                .build();
        assertThat(instance.getGeneSymbol(), equalTo("ABC1"));
    }

    @Test
    public void getHgncId() {
        assertThat(defaultIdentifier.getHgncId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .hgncId("hgnc")
                .build();
        assertThat(instance.getHgncId(), equalTo("hgnc"));
    }
    @Test
    public void getHgncSymbol() {
        assertThat(defaultIdentifier.getHgncSymbol(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .hgncSymbol("name")
                .build();
        assertThat(instance.getHgncSymbol(), equalTo("name"));
    }

    @Test
    public void testEntrezIdentifier() {
        assertThat(defaultIdentifier.getEntrezId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        assertThat(defaultIdentifier.getEntrezIdAsInteger(), equalTo(-1));

        GeneIdentifier instance = GeneIdentifier.builder()
                .entrezId("2263")
                .build();
        assertThat(instance.getEntrezId(), equalTo("2263"));
        assertThat(instance.getEntrezIdAsInteger(), equalTo(2263));
    }

    @Test
    public void getEnsemblId() {
        assertThat(defaultIdentifier.getEnsemblId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .ensemblId("ensemble")
                .build();
        assertThat(instance.getEnsemblId(), equalTo("ensemble"));
    }

    @Test
    public void getUcscId() {
        assertThat(defaultIdentifier.getUcscId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .ucscId("ucscId")
                .build();
        assertThat(instance.getUcscId(), equalTo("ucscId"));
    }

}