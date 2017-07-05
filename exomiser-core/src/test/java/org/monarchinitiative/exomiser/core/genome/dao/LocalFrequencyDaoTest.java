package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalFrequencyDaoTest {

    private LocalFrequencyDao instance;

    @Mock
    private TabixReader tabixReader;

    @Before
    public void setUp() {
        instance = new LocalFrequencyDao(tabixReader);
    }

    private VariantEvaluation variant(int chr, int pos, String ref, String alt) {
        return VariantEvaluation.builder(chr, pos, ref, alt).build();
    }

    private FrequencyData localFrequencyData(float freq) {
        return FrequencyData.of(RsId.empty(), Frequency.valueOf(freq, FrequencySource.LOCAL));
    }

    //Local frequency file defined as tab-delimited lines in 'VCF-lite' format:
    //chr   pos ref alt freq(%)
    //1 12345   A   T   23.0  (an A->T SNP on chr1 at position 12345 with frequency of 23.0%)
    //note in the usual VCF format these would be on a single line
    //1 12345   A   TG   0.01  (an A->TG insertion on chr1 at position 12345 with frequency of 0.01%)
    //1 12345   AT   G   0.02  (an AT->G deletion on chr1 at position 12345 with frequency of 0.02%)
    //1 12345   T   .   0.03  (an T->. monomorphic site (no alt allele) on chr1 at position 12345 with frequency of 0.03%)

    @Test
    public void variantNotInFile() {
        Mockito.when(tabixReader.query("1:12345-12345"))
                .thenReturn(new MockTabixIterator(Collections.emptyList()));

        assertThat(instance.getFrequencyData(variant(1, 12345, "A", "T")), equalTo(FrequencyData.empty()));
    }

    @Test
    public void testSnp(){
        //1 12345   A   T   23.0  (an A->T SNP on chr1 at position 12345 with frequency of 23.0%)
        Mockito.when(tabixReader.query("1:12345-12345"))
                .thenReturn(new MockTabixIterator(Collections.singletonList("1\t12345\tA\tT\t23.0")));

        assertThat(instance.getFrequencyData(variant(1, 12345, "A", "T")), equalTo(localFrequencyData(23.0f)));
    }

    @Test
    public void testInsertionIndel(){
        //1 12345   A   TG   0.01  (an A->TG insertion on chr1 at position 12345 with frequency of 0.01%)
        Mockito.when(tabixReader.query("1:12345-12345"))
                .thenReturn(new MockTabixIterator(Collections.singletonList("1\t12345\tA\tTG\t0.01")));

        assertThat(instance.getFrequencyData(variant(1, 12345, "A", "TG")), equalTo(localFrequencyData(0.01f)));
    }

    @Test
    public void testSnpAndInsertionAtSamePositionInSourceFile(){
        //1 12345   A   TG   0.01  (an A->TG insertion on chr1 at position 12345 with frequency of 0.01%)
        Mockito.when(tabixReader.query("1:12345-12345"))
                .thenReturn(new MockTabixIterator(Arrays.asList("1\t12345\tA\tT\t23.0", "1\t12345\tA\tTG\t0.01")));

        assertThat(instance.getFrequencyData(variant(1, 12345, "A", "TG")), equalTo(localFrequencyData(0.01f)));
    }

    @Test
    public void testDeletionIndel(){
        //1 12345   AT   G   0.02  (an AT->G deletion on chr1 at position 12345 with frequency of 0.02%)
        Mockito.when(tabixReader.query("1:12345-12345"))
                .thenReturn(new MockTabixIterator(Collections.singletonList("1\t12345\tAT\tG\t0.02")));

        assertThat(instance.getFrequencyData(variant(1, 12345, "AT", "G")), equalTo(localFrequencyData(0.02f)));
    }

    @Test
    public void testInsertion(){
        //1 12345   T   .   0.03  (an T->. monomorphic site (no alt allele) on chr1 at position 12345 with frequency of 0.03%)
        Mockito.when(tabixReader.query("1:12345-12345"))
                .thenReturn(new MockTabixIterator(Collections.singletonList("1\t12345\tA\tAT\t0.03")));

        assertThat(instance.getFrequencyData(variant(1, 12345, "A", "AT")), equalTo(localFrequencyData(0.03f)));
    }

    @Test
    public void testDeletion(){
        //1 12345   T   .   0.03  (an T->. monomorphic site (no alt allele) on chr1 at position 12345 with frequency of 0.03%)
        Mockito.when(tabixReader.query("1:12345-12345"))
                .thenReturn(new MockTabixIterator(Collections.singletonList("1\t12345\tAT\tA\t0.03")));

        assertThat(instance.getFrequencyData(variant(1, 12345, "AT", "A")), equalTo(localFrequencyData(0.03f)));
    }
}
