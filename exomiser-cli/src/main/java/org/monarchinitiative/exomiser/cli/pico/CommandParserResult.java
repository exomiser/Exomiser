package org.monarchinitiative.exomiser.cli.pico;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

public final class CommandParserResult<T> {

    private enum ParseResultType {HELP, VERSION, ERROR, COMMAND}

    private final ParseResultType parseResultType;
    private final int exitCode;
    private final T command;

    private CommandParserResult(ParseResultType parseResultType, int exitCode, @Nullable T command) {
        this.exitCode = exitCode;
        this.parseResultType = parseResultType;
        this.command = command;
    }

    public static <T> CommandParserResult<T> help(int exitCode){
        return new CommandParserResult<>(ParseResultType.HELP, exitCode, null);
    }

    public static <T> CommandParserResult<T> version(int exitCode){
        return new CommandParserResult<>(ParseResultType.VERSION, exitCode, null);
    }

    public static <T> CommandParserResult<T> error(int exitCode){
        return new CommandParserResult<>(ParseResultType.ERROR, exitCode, null);
    }

    public static <T> CommandParserResult<T> command(int exitCode, @Nonnull T command){
        return new CommandParserResult<>(ParseResultType.COMMAND, exitCode, command);
    }

    public boolean isHelp() {
        return parseResultType == ParseResultType.HELP;
    }

    public boolean isVersion() {
        return parseResultType == ParseResultType.VERSION;
    }

    public boolean isError() {
        return parseResultType == ParseResultType.ERROR;
    }

    public boolean isCommand() {
        return parseResultType == ParseResultType.COMMAND;
    }

    public @Nullable T command() {
        return command;
    }

    public int exitCode() {
        return exitCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CommandParserResult<?> that)) return false;
        return exitCode == that.exitCode && parseResultType == that.parseResultType && Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parseResultType, exitCode, command);
    }

    @Override
    public String toString() {
        return "CommandParserResult{" +
               "parseResultType=" + parseResultType +
               ", exitCode=" + exitCode +
               ", command=" + command +
               '}';
    }
}