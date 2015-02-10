package de.charite.compbio.exomiser.core.dao;

import com.google.common.collect.ImmutableList;

import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;

/**
 * Allows the easy creation of {@link JannovarData} objects for testing.
 *
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class TestJannovarDataFactory {

    public final ReferenceDictionary refDict;
    public final JannovarData jannovarData;

    public TestJannovarDataFactory() {
        this.refDict = HG19RefDictBuilder.build();
        this.jannovarData = buildJannovarData();
    }

    private JannovarData buildJannovarData() {
        return new JannovarData(refDict, ImmutableList.of(new TestVariantFactory().buildTMForFGFR2()));
    }

}
