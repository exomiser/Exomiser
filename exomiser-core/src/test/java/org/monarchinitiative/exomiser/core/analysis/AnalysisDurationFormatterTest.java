package org.monarchinitiative.exomiser.core.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AnalysisDurationFormatterTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0, 0, 0, 0m 0s 0ms",
            "0, 0, 0, 0, 1, 0m 0s 1ms",
            "0, 0, 0, 0, 999, 0m 0s 999ms",
            "0, 0, 0, 0, 1000, 0m 1s 0ms",
            "0, 0, 0, 1, 1000, 0m 2s 0ms",
            "0, 0, 0, 2, 1, 0m 2s 1ms",
            "0, 0, 3, 2, 1, 3m 2s 1ms",
            "0, 4, 3, 2, 1, 4h 3m 2s 1ms",
            "5, 4, 3, 2, 1, 5d 4h 3m 2s 1ms",
            "5, 0, 3, 2, 1, 5d 0h 3m 2s 1ms",
            "5, 25, 3, 2, 1, 6d 1h 3m 2s 1ms",
            "5, 23, 59, 59, 999, 5d 23h 59m 59s 999ms",
            "5, 23, 59, 59, 1000, 6d 0h 0m 0s 0ms",
            "5, 25, 60, 60, 1000, 6d 2h 1m 1s 0ms",
    })
    void format(long days, int hrs, int mins, int secs, int millis, String expected) {
        long sec = 1000;
        long min = sec * 60;
        long hour = min * 60;
        long day = hour * 24;
        Duration duration = Duration.of(days * day + hrs * hour + mins * min + secs * sec + millis, ChronoUnit.MILLIS);

        assertThat(AnalysisDurationFormatter.format(duration), equalTo(expected));
    }

    @Test
    void formatFromHours() {
        Duration duration = Duration.of(2, ChronoUnit.HOURS);
        assertThat(AnalysisDurationFormatter.format(duration), equalTo("2h 0m 0s 0ms"));
    }
}