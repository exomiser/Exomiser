package org.monarchinitiative.exomiser.data.genome;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class BuildRunner implements CommandLineRunner, ExitCodeGenerator {

    private final BuildCommand buildCommand;

    private final IFactory factory; // auto-configured to inject PicocliSpringFactory

    private int exitCode;

    public BuildRunner(BuildCommand buildCommand, IFactory factory) {
        this.buildCommand = buildCommand;
        this.factory = factory;
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(buildCommand, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
