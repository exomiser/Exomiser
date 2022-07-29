package org.monarchinitiative.exomiser.core.writers;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ScoreRankerTest {

    @Test
    void testOne() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
    }

    @Test
    void testTwo() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
    }

    @Test
    void testThree() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.98), equalTo(3));
    }

    @Test
    void testTwoEqualValues() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(1), equalTo(1));
    }

    @Test
    void testTwoEqualValuesThenThirdRanked() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(3));
    }

    @Test
    void testOneThenTwoEqualValues() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.99), equalTo(2));
    }

    @Test
    void testOneThenTwoEqualValuesThenThird() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.98), equalTo(4));
    }

    @Test
    void testOneThenTwoEqualValuesThenFourthAndFifth() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.98), equalTo(4));
        assertThat(instance.rank(0.97), equalTo(5));
    }

    @Test
    void testOneThenTwoEqualValuesThenFourthFifthAndSixthRank() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.98), equalTo(4));
        assertThat(instance.rank(0.97), equalTo(5));
        assertThat(instance.rank(0.96), equalTo(6));
    }

    @Test
    void testOneThenTwoEqualValuesThenFourthFifthAndTiedSixthRank() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.98), equalTo(4));
        assertThat(instance.rank(0.97), equalTo(5));
        assertThat(instance.rank(0.96), equalTo(6));
        assertThat(instance.rank(0.96), equalTo(6));
    }

    @Test
    void testOneThenTwoEqualValuesThenFourthFifthTiedSixthThenSeventh() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.98), equalTo(4));
        assertThat(instance.rank(0.97), equalTo(5));
        assertThat(instance.rank(0.96), equalTo(6));
        assertThat(instance.rank(0.96), equalTo(6));
        assertThat(instance.rank(0.95), equalTo(8));
    }

    @Test
    void testTwoEqualValuesThenTwoThirdRanked() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(3));
        assertThat(instance.rank(0.99), equalTo(3));
    }

    @Test
    void testTwoEqualValuesThenTwoThirdAndAFourthRanked() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(3));
        assertThat(instance.rank(0.99), equalTo(3));
        assertThat(instance.rank(0.98), equalTo(5));
    }

    @Test
    void testTwoEqualValuesThenThreeThirdAndAFourthRanked() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(3));
        assertThat(instance.rank(0.99), equalTo(3));
        assertThat(instance.rank(0.99), equalTo(3));
        assertThat(instance.rank(0.98), equalTo(6));
    }

    @Test
    void testRanksWithRounding() {
        ScoreRanker instance = new ScoreRanker(4);
        assertThat(instance.rank(1), equalTo(1));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.99), equalTo(2));
        assertThat(instance.rank(0.98), equalTo(4));
        assertThat(instance.rank(0.97), equalTo(5));
        assertThat(instance.rank(0.9655), equalTo(6));
        assertThat(instance.rank(0.9655), equalTo(6));
        assertThat(instance.rank(0.9655), equalTo(6));
        assertThat(instance.rank(0.9654), equalTo(9));
    }

    @Test
    void testRounding() {
        ScoreRanker instance = new ScoreRanker(4);
//        30 ECPAS AD 0.3316190399224868 0.5060619479045272 0.7978162169456482
//        31 MOXD1 AD 0.3222626320487493 0.5000417413721152 0.800000011920929
//        32 ZNF692 AD 0.32222919382960385 0.5000631910879747 0.7999590635299683
//        33 PMVK AD 0.3222141018324461 0.5001061507355189 0.7999029159545898
//        34 A2ML1 AD 0.3221988951499709 0.5000136651897265 0.800000011920929
//        35 PKM AD 0.3201987097777603 0.5026839997153729 0.7959787249565125

        assertThat(instance.rank(0.3316190399224868), equalTo(1));
        assertThat(instance.rank(0.3222626320487493), equalTo(2));
        assertThat(instance.rank(0.32222919382960385), equalTo(3));
        assertThat(instance.rank(0.3222141018324461), equalTo(3));
        assertThat(instance.rank(0.3221988951499709), equalTo(3));
        assertThat(instance.rank(0.3201987097777603), equalTo(6));
   }

}