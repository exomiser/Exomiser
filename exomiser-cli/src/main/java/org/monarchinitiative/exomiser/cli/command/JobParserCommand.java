package org.monarchinitiative.exomiser.cli.command;

import org.monarchinitiative.exomiser.api.v1.JobProto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface JobParserCommand {

    List<JobProto.Job> parseJobs();

    static Function<ExomiserCommand, Stream<JobProto.Job>> parseJobStream() {
        return exomiserCommand -> {
            if (exomiserCommand instanceof JobParserCommand jobParserCommand) {
                return jobParserCommand.parseJobs().stream();
            }
            return Stream.empty();
        };
    }
}
