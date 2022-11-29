package org.monarchinitiative.exomiser.cli.command;

import org.monarchinitiative.exomiser.cli.CommandLineParseError;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;

/**
 * Utility for parsing a list of commands into via a {@link CommandLine} into the given object T
 */
public final class CommandLineParser {

    private CommandLineParser() {
    }

    public static <T> Optional<T> parseArgs(T command, String... args) {
        CommandLine cmd = new CommandLine(command);
        cmd.setUsageHelpAutoWidth(true);
        // allow spring args e.g. --spring.config.location=$PWD/application.properties
        cmd.setUnmatchedArgumentsAllowed(true);
        try {
            CommandLine.ParseResult parseResult = cmd.parseArgs(args);
            return handleParseResult(cmd, parseResult);
        } catch (CommandLine.ParameterException ex) { // command line arguments could not be parsed
            cmd.getErr().println(cmd.getColorScheme().errorText(ex.getMessage())); // bold red
            cmd.getErr().println(cmd.getColorScheme().errorText("Caused by input args: " + List.of(args))); // bold red
            if (!CommandLine.UnmatchedArgumentException.printSuggestions(ex, cmd.getErr())) {
                ex.getCommandLine().usage(cmd.getErr());
            }
            throw new CommandLineParseError(ex.getMessage(), ex.getCause());
        }
    }

    private static <T> Optional<T> handleParseResult(CommandLine cmd, CommandLine.ParseResult parseResult) {
        if (helpOrVersionRequested(cmd)) {
            return Optional.empty();
        }
        List<CommandLine> parsedCommands = parseResult.asCommandLineList();
        CommandLine last = parsedCommands.get(parsedCommands.size() - 1);
        return handleLastCommand(last);
    }

    private static <T> Optional<T> handleLastCommand(CommandLine commandLine) {
        if (helpOrVersionRequested(commandLine)) {
            return Optional.empty();
        }
        T lastCommand = commandLine.getCommand();
        return Optional.of(lastCommand);
    }

    private static boolean helpOrVersionRequested(CommandLine commandLine) {
        // Did user request usage help (--help)?
        if (commandLine.isUsageHelpRequested() || commandLine.getParseResult().originalArgs().isEmpty()) {
            commandLine.usage(commandLine.getOut());
            return true;
            // Did user request version help (--version)?
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(commandLine.getOut());
            return true;
        }
        return false;
    }
}
