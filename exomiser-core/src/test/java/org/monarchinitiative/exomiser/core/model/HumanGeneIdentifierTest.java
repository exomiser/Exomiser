package org.monarchinitiative.exomiser.core.model;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HumanGeneIdentifierTest {

    private final HumanGeneIdentifier defaultIdentifier = HumanGeneIdentifier.builder().build();

    @Test
    public void getHgncId() {
        assertThat(defaultIdentifier.getHgncId(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .hgncId("hgnc")
                .build();
        assertThat(instance.getHgncId(), equalTo("hgnc"));
    }

    @Test
    public void getGeneSymbol() {
        assertThat(defaultIdentifier.getGeneSymbol(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .geneSymbol("ABC1")
                .build();
        assertThat(instance.getGeneSymbol(), equalTo("ABC1"));
    }

    @Test
    public void getGeneName() {
        assertThat(defaultIdentifier.getGeneName(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .geneName("name")
                .build();
        assertThat(instance.getGeneName(), equalTo("name"));
    }

    @Test
    public void getLocation() {
        assertThat(defaultIdentifier.getLocation(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .location("10q26.13")
                .build();
        assertThat(instance.getLocation(), equalTo("10q26.13"));
    }

    @Test
    public void getLocusGroup() {
        assertThat(defaultIdentifier.getLocusGroup(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .locusGroup("locus group")
                .build();
        assertThat(instance.getLocusGroup(), equalTo("locus group"));
    }

    @Test
    public void getLocusType() {
        assertThat(defaultIdentifier.getLocusType(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .locusType("locus type")
                .build();
        assertThat(instance.getLocusType(), equalTo("locus type"));
    }

    @Test
    public void testEntrezIdentifier() {
        assertThat(defaultIdentifier.getEntrezId(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        assertThat(defaultIdentifier.getEntrezIdAsInteger(), equalTo(-1));

        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .entrezId("2263")
                .build();
        assertThat(instance.getEntrezId(), equalTo("2263"));
        assertThat(instance.getEntrezIdAsInteger(), equalTo(2263));
    }

    @Test
    public void getEnsemblId() {
        assertThat(defaultIdentifier.getEnsemblId(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .ensemblId("ensemble")
                .build();
        assertThat(instance.getEnsemblId(), equalTo("ensemble"));
    }

    @Test
    public void getUcscId() {
        assertThat(defaultIdentifier.getUcscId(), equalTo(HumanGeneIdentifier.EMPTY_FIELD));
        HumanGeneIdentifier instance = HumanGeneIdentifier.builder()
                .ucscId("ucscId")
                .build();
        assertThat(instance.getUcscId(), equalTo("ucscId"));
    }

    @Test
    public void isWithdrawn() {
        assertThat(defaultIdentifier.isWithdrawn(), is(false));

        HumanGeneIdentifier withdrawn = HumanGeneIdentifier.builder()
                .withdrawn(true)
                .build();
        assertThat(withdrawn.isWithdrawn(), is(true));
    }

}