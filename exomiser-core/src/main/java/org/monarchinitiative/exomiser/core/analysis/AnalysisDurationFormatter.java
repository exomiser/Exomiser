package org.monarchinitiative.exomiser.core.analysis;

import java.time.Duration;

public class AnalysisDurationFormatter {

    private AnalysisDurationFormatter() {
    }

    /**
     * Outputs a simple duration format for easy human parsing of run times in the Exomiser logs.
     * <p>
     * Formats the input duration to the form:
     * <pre>
     *     1d 23h 59m 59s 999ms
     *     1d 0h 59m 59s 999ms
     *     23h 59m 59s 999ms
     *     59m 59s 999ms
     *     0m 59s 999ms
     * </pre>
     *
     * @param duration
     * @return A string representation of the input duration in days, hours, minutes, seconds, milliseconds.
     */
    public static String format(Duration duration) {
        // e.g. 1d 23h 59m 59s 999ms
        StringBuilder stringBuilder = new StringBuilder(20);

        long days = duration.toDaysPart();
        int hours = duration.toHoursPart();
        if (days != 0) {
            stringBuilder.append(days).append("d ");
            // include hours, even if zero
            stringBuilder.append(hours).append("h ");
        } else if (hours != 0) {
            stringBuilder.append(hours).append("h ");
        }
        // always retain m s ms
        stringBuilder.append(duration.toMinutesPart()).append("m ");
        stringBuilder.append(duration.toSecondsPart()).append("s ");
        stringBuilder.append(duration.toMillisPart()).append("ms");
        return stringBuilder.toString();
    }
}
