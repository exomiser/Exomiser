package org.monarchinitiative.exomiser.core.analysis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Playground/notebook for parallel execution
 */
@Disabled
public class ParallelWorkerTests {

    private static final Logger logger = LoggerFactory.getLogger(ParallelWorkerTests.class);
    final GenomeAnalysisService genomeAnalysisService = TestFactory.buildDefaultHg19GenomeAnalysisService();

    @Test
    void paprllelStreamForkJoinSubclass() throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(4, ExomiserForkJoinThread::new, null, false);
        try (Stream<String> stream = IntStream.range(1, 10000).parallel().mapToObj(String::valueOf).peek(x -> logger.info("{}", x))){
            var result= pool.submit(stream::toList).get();
            result.stream().limit(10).forEach(System.out::println);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        pool.shutdownNow();
    }

    @Test
    void testVcfReadForkJoinPool() throws InterruptedException {
//        Thread.sleep(3000);
        VariantFactory variantFactory = new VariantFactoryImpl(genomeAnalysisService.getVariantAnnotator(), new VcfFileReader(Path.of("src/test/resources/Pfeiffer.vcf")));
        ForkJoinPool pool = new ForkJoinPool(4, exomiserThreadFactory, null, false);
        try (
                Stream<VariantEvaluation> variantStream = variantFactory.createVariantEvaluations()
                        .parallel()
                        .peek(var -> logger.info("{}", var))) {
            var result= pool.submit(variantStream::toList).join();
        } finally {
            pool.shutdown();
        }
    }

    private static final ForkJoinPool.ForkJoinWorkerThreadFactory exomiserThreadFactory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
        private final AtomicInteger workerNumber = new AtomicInteger(1);
        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool) {
                @Override
                protected void onStart() {
                    super.onStart();
                    setName("Exomiser-work-" + workerNumber.getAndIncrement());
                }
            };
        }
    };

    static class ExomiserForkJoinThread extends ForkJoinWorkerThread {

        private static final AtomicInteger count = new AtomicInteger(0);

        /**
         * Creates a ForkJoinWorkerThread operating in the given pool.
         *
         * @param pool the pool this thread works in
         * @throws NullPointerException if pool is null
         */
        protected ExomiserForkJoinThread(ForkJoinPool pool) {
            super(pool);
            setName("Exomiser-work-" + count.incrementAndGet());
        }
    }
}
