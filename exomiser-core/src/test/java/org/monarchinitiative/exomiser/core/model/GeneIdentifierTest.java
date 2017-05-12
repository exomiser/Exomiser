package org.monarchinitiative.exomiser.core.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GeneIdentifierTest {

    private static final GeneIdentifier EMPTY_GENE_IDENTIFIER = GeneIdentifier.builder().build();

    @Test(expected = NullPointerException.class)
    public void testConstructorChecksForNullGeneSymbol() {
        GeneIdentifier.builder().geneSymbol(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorChecksGeneIdIsNotNull() {
        new Gene(GeneIdentifier.builder().geneId(null).build());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorChecksEntrezIdIsNotNull() {
        new Gene(GeneIdentifier.builder().entrezId(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorChecksEntrezIdIsValidInteger() {
        new Gene(GeneIdentifier.builder().entrezId("wibble").build());
    }

    @Test
    public void getGeneId() {
        assertThat(EMPTY_GENE_IDENTIFIER.getGeneId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .geneId("12345")
                .build();
        assertThat(instance.getGeneId(), equalTo("12345"));
    }

    @Test
    public void getGeneSymbol() {
        assertThat(EMPTY_GENE_IDENTIFIER.getGeneSymbol(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .geneSymbol("ABC1")
                .build();
        assertThat(instance.getGeneSymbol(), equalTo("ABC1"));
    }

    @Test
    public void getHgncId() {
        assertThat(EMPTY_GENE_IDENTIFIER.getHgncId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .hgncId("hgnc")
                .build();
        assertThat(instance.getHgncId(), equalTo("hgnc"));
    }
    @Test
    public void getHgncSymbol() {
        assertThat(EMPTY_GENE_IDENTIFIER.getHgncSymbol(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .hgncSymbol("name")
                .build();
        assertThat(instance.getHgncSymbol(), equalTo("name"));
    }

    @Test
    public void testEntrezIdentifier() {
        assertThat(EMPTY_GENE_IDENTIFIER.getEntrezId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        assertThat(EMPTY_GENE_IDENTIFIER.getEntrezIdAsInteger(), equalTo(-1));

        GeneIdentifier instance = GeneIdentifier.builder()
                .entrezId("2263")
                .build();
        assertThat(instance.getEntrezId(), equalTo("2263"));
        assertThat(instance.getEntrezIdAsInteger(), equalTo(2263));
    }

    @Test
    public void getEnsemblId() {
        assertThat(EMPTY_GENE_IDENTIFIER.getEnsemblId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .ensemblId("ensemble")
                .build();
        assertThat(instance.getEnsemblId(), equalTo("ensemble"));
    }

    @Test
    public void getUcscId() {
        assertThat(EMPTY_GENE_IDENTIFIER.getUcscId(), equalTo(GeneIdentifier.EMPTY_FIELD));
        GeneIdentifier instance = GeneIdentifier.builder()
                .ucscId("ucscId")
                .build();
        assertThat(instance.getUcscId(), equalTo("ucscId"));
    }

    @Test
    public void testKnownGeneIdentifier() {
        assertThat(EMPTY_GENE_IDENTIFIER.hasEntrezId(), is(false));
        GeneIdentifier geneIdentifier = GeneIdentifier.builder()
                .entrezId("2263")
                .build();
        assertThat(geneIdentifier.hasEntrezId(), is(true));
    }

}