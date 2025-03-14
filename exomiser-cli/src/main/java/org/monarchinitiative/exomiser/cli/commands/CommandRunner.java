package org.monarchinitiative.exomiser.cli.commands;

public interface CommandRunner<T extends ExomiserCommand> {

    public Integer run(T command);
}
