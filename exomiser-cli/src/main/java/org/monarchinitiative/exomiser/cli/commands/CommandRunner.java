package org.monarchinitiative.exomiser.cli.commands;

import org.springframework.boot.ExitCodeGenerator;

/**
 * Handler for executing Exomiser commands after they've been parsed by PicoCLI.
 * Each implementation should be a Spring @Component that handles a specific ExomiserCommand type.
 *
 * @param <T> the specific ExomiserCommand type this runner handles
 */
public interface CommandRunner<T extends ExomiserCommand> extends ExitCodeGenerator {

    /**
     * Execute the command logic.
     *
     * @param command the parsed and validated command
     * @return exit code (0 indicates success, non-zero indicates failure)
     */
    Integer run(T command);
}
