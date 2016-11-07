/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author jj8
 */
public class StreamsTest {

    private final List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

    @Test
    public void streamTest() {
        System.out.println("Running streamTest");
        List<Integer> twoEvenSquares
                = numbers.stream()
                .filter(n -> {
                    System.out.println("filtering " + n);
                    return n % 2 == 0;
                })
                .filter(n -> {
                    System.out.println("filtering " + n);
                    return n % 4 == 0;
                })
                .map(n -> {
                    System.out.println("mapping " + n);
                    return n * n;
                })
                        //                .limit(2)
                .collect(toList());

        assertThat(twoEvenSquares, equalTo(Arrays.asList(16, 64)));
    }

    @Test
    public void parallelStreamTest() {
        System.out.println("Running parallelStreamTest");
        List<Integer> twoEvenSquares
                = numbers.parallelStream()
                .filter(n -> {
                    System.out.println("filtering " + n);
                    return n % 2 == 0;
                })
                .filter(n -> {
                    System.out.println("filtering " + n);
                    return n % 4 == 0;
                })
                .map(n -> {
                    System.out.println("mapping " + n);
                    return n * n;
                })
                        //                .limit(2)
                .collect(toList());

        assertThat(twoEvenSquares, equalTo(Arrays.asList(16, 64)));
    }

    @Test
    public void sumOfStream() {
        int sum = numbers.stream()
                .mapToInt(Integer::intValue)
                .sum();
        assertThat(sum, equalTo(36));
    }

    @Test
    public void streamFile() throws IOException {
        long numberOfLines = Files.lines(Paths.get("src/test/resources/smallTest.vcf"), Charset.defaultCharset()).count();
        assertThat(numberOfLines, equalTo(7L));
    }

    @Test
    public void streamFileCountVariantsUsingPredicate() throws IOException {
        //TODO: can we dynamically add filters from a list of given filters?
        //TODO: try this using FilterResult::FilterType::PASS or something like this
        long numberOfVariants = Files.lines(Paths.get("src/test/resources/smallTest.vcf"), Charset.defaultCharset())
                .filter(isVariantLine())
                .count();
        assertThat(numberOfVariants, equalTo(3L));
    }

    private Predicate<String> isVariantLine() {
        return line -> {
            if (line.startsWith("#")) {
                return false;
            }
            System.out.println(line);
            return true;
        };
    }

    @Test
    public void streamFileCountVariantsUsingStaticClass() throws IOException {
        long numberOfVariants = Files.lines(Paths.get("src/test/resources/smallTest.vcf"), Charset.defaultCharset())
                .filter(line -> VariantLineFilter.isVariantLine(line))
                .count();
        assertThat(numberOfVariants, equalTo(3L));
    }    
    
    private static class VariantLineFilter {

        public static boolean isVariantLine(String line) {
            if (line.startsWith("#")) {
                return false;
            }
            System.out.println(line);
            return true;
        }
    }

    @Test
    public void testInfiniteStreamAndFilter() {
        Stream<Integer> numbers = Stream.iterate(1, n -> n * 2);
        numbers
                .filter(n -> {
                    return n % 8 == 0;
                })
                .limit(8)
                .forEach(System.out::println);
    }

    @Test
    public void testInfiniteStreamCollectToList() {
        Stream<Integer> numbers = Stream.iterate(1, n -> n * 2).limit(40000);
        List<Integer> collected = numbers
                .filter(n -> {
                    System.out.println(n);
                    return n % 8 == 0;
                }).limit(8).collect(toList());

        assertThat(collected, equalTo(Arrays.asList(8, 16, 32, 64, 128, 256, 512, 1024)));
    }


}
