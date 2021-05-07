/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.monarchinitiative.exomiser.data.genome.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractIndexer<T> implements Indexer<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIndexer.class);

    @Override
    public void index(Resource<T> resource) {
        logger.info("Processing '{}' resource", resource.getName());
        Instant startTime = Instant.now();
        ProgressLogger<T> progressLogger = new ProgressLogger<>(startTime);

        resource.parseResource()
                .map(progressLogger.logCount())
                .forEach(this::write);

        long seconds = Duration.between(startTime, Instant.now()).getSeconds();
        logger.info("Finished '{}' resource - processed {} objects in {} sec. Total {} objects written.",
                resource.getName(),
                progressLogger.count(),
                seconds,
                this.count());
    }

    public abstract long count();

    private static class ProgressLogger<T> {

        private final AtomicLong counter;
        private final Instant startTime;
        private Instant lastInstant;

        public ProgressLogger(Instant startTime) {
            this.counter = new AtomicLong();
            this.startTime = startTime;
            this.lastInstant = startTime;
        }

        public long count() {
            return counter.get();
        }

        public UnaryOperator<T> logCount() {
            return object -> {
                counter.incrementAndGet();
                int logInterval = 1000000;
                if (counter.get() % logInterval == 0) {
                    Instant now = Instant.now();
                    long totalSeconds = Duration.between(startTime, now).getSeconds();
                    long sinceLastCount = Duration.between(lastInstant, now).getSeconds();
                    long totalVar = counter.get();
                    logger.info("Indexed {} objects total in {} sec - {} ops (last {} took {} sec - {} ops)", totalVar, totalSeconds, totalVar / totalSeconds, logInterval, sinceLastCount, logInterval / sinceLastCount);
                    logger.info("{}", object);
                    lastInstant = now;
                }
                return object;
            };
        }
    }

}
