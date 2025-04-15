package org.monarchinitiative.exomiser.cli.pico;

import jakarta.annotation.Nonnull;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CommandParser<T>(CommandLine commandLine) {

    public CommandParser {
        Objects.requireNonNull(commandLine);
    }

    public @Nonnull CommandParserResult<T> parseArgs(String... args) {
        if (args.length == 0) {
            commandLine.usage(System.out);
            return CommandParserResult.help(0);
        }
        try {
            CommandLine.ParseResult parseResult = commandLine.parseArgs(args);
            var lastParsedCommand = findLastParsedCommandLine(parseResult);
            if (commandLine.isUsageHelpRequested()) {
                CommandLine.executeHelpRequest(parseResult);
                return CommandParserResult.help(0);
            } else if (commandLine.isVersionHelpRequested()) {
                commandLine.printVersionHelp(System.out);
                return CommandParserResult.version(0);
            }
            else if (parseResult.subcommand() == null && lastParsedCommand.isEmpty()) {
                commandLine.usage(System.err);
                return CommandParserResult.error(-1);
            }
            else {
                var lastCommand = lastParsedCommand.get();
                if (lastCommand.isUsageHelpRequested()) {
                    CommandLine.executeHelpRequest(parseResult);
                    return CommandParserResult.help(0);
                } else {
                    return CommandParserResult.command(0, lastCommand.getCommand());
                }
            }
        } catch (CommandLine.ParameterException ex) {
            try {
                return CommandParserResult.error(commandLine.getParameterExceptionHandler().handleParseException(ex, args));
            } catch (Exception ex2) {
                return CommandParserResult.error(handleUnhandled(ex2, ex.getCommandLine(), ex.getCommandLine().getCommandSpec().exitCodeOnInvalidInput()));
            }
        }
    }

    private Optional<CommandLine> findLastParsedCommandLine(CommandLine.ParseResult parseResult) {
        List<CommandLine> parsedCommands = parseResult.asCommandLineList();
        int start = indexOfLastSubcommandWithSameParent(parsedCommands);
        CommandLine last = null;
        for (int i = start; i < parsedCommands.size(); i++) {
            last = parsedCommands.get(i);
        }
        return Optional.ofNullable(last);
    }

    // find list of most deeply nested sub-(sub*)-commands
    private static int indexOfLastSubcommandWithSameParent(List<CommandLine> parsedCommands) {
        int start = parsedCommands.size() - 1;
        for (int i = parsedCommands.size() - 2; i >= 0; i--) {
            if (parsedCommands.get(i).getParent() != parsedCommands.get(i + 1).getParent()) {
                break;
            }
            start = i;
        }
        return start;
    }

    private static int handleUnhandled(Exception ex, CommandLine cmd, int defaultExitCode) {
        ex.printStackTrace(cmd.getErr());
        cmd.getErr().flush();
        return mappedExitCode(ex, cmd.getExitCodeExceptionMapper(), defaultExitCode);
    }

    private static int mappedExitCode(Throwable t, CommandLine.IExitCodeExceptionMapper mapper, int defaultExitCode) {
        try {
            return (mapper != null) ? mapper.getExitCode(t) : defaultExitCode;
        } catch (Exception ex) {
            ex.printStackTrace();
            return defaultExitCode;
        }
    }

}
