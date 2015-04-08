/*
 * Copyright © 2011-2013 EMBL - European Bioinformatics Institute
 * and Genome Research Limited
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.  
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.charite.compbio.exomiser.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneIdentifierTest {
    
    public GeneIdentifierTest() {
    }

    /**
     * Test of constructor for class GeneIdentifier.
     */
    @Test
    public void testGeneIdentifierAllFieldsOverloadedConstructor() {
        System.out.println("testGeneIdentifierAllFieldsOverloadedConstructor");
        String geneSymbol = "Fgfr2";
        String databaseIdentifier = "MGI";
        String databaseAccession = "95523";
        GeneIdentifier instance = new GeneIdentifier(geneSymbol, databaseIdentifier, databaseAccession);
        String expResult = "Fgfr2{MGI:95523}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of constructor for class GeneIdentifier.
     */
    @Test
    public void testGeneIdentifierParserOverloadedConstructor() {
        System.out.println("testGeneIdentifierParserOverloadedConstructor");
        String geneSymbol = "Fgfr2";
        String databaseCompoundIdentifier = "MGI:95523";
        GeneIdentifier instance = new GeneIdentifier(geneSymbol, databaseCompoundIdentifier);
        String expResult = "Fgfr2{MGI:95523}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of constructor for class GeneIdentifier.
     */
    @Test
    public void testGeneIdentifierParserOverloadedConstructorUnclassifiedHumanOrtholog() {
        System.out.println("testGeneIdentifierParserOverloadedConstructorUnclassifiedHumanOrtholog");
        String geneSymbol = "";
        String databaseCompoundIdentifier = "";
        GeneIdentifier instance = new GeneIdentifier(geneSymbol, databaseCompoundIdentifier);
        String expResult = "UNKNOWN{-:-}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testCompareTo(){

        GeneIdentifier gene1 = new GeneIdentifier("Fgfr1", "MGI:95524");
        GeneIdentifier gene2 = new GeneIdentifier("Fgfr2", "MGI:95523");
        
        List<GeneIdentifier> genesList = new ArrayList<GeneIdentifier>();
        
        genesList.add(gene2);
        genesList.add(gene1);
        
        Collections.sort(genesList);
        
        assertEquals(gene1, genesList.get(0));
    }

    @Test
    public void testGetMgiExternalUri() {
        GeneIdentifier geneIdentifier = new GeneIdentifier("Fgfr3", "MGI:95524");
        String expected = "http://www.informatics.jax.org/accession/MGI:95524";
        String result = geneIdentifier.getExternalUri();
        System.out.println(result);
        assertEquals(expected, result);
    }
    
    @Test
    public void testGetHgncExternalUri() {
        GeneIdentifier geneIdentifier = new GeneIdentifier("ENAM", "HGNC:3344");
        String expected = "http://www.genenames.org/data/hgnc_data.php?hgnc_id=3344";
        String result = geneIdentifier.getExternalUri();
        System.out.println(result);
        assertEquals(expected, result);
    }
}