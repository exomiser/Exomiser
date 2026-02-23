package org.monarchinitiative.exomiser.cli.commands;

import java.util.Objects;

/**
 * A lightweight version of the Rust Result enum. Good for being able to group errors or correct data to be able to
 * handle them where needed. Not fully fledged or intended for export.
 *
 * @param ok
 * @param err
 * @param <T>
 * @param <E>
 *
 * @since 15.0.0
 */
record Result<T, E>(T ok, E err) {

    public Result {
        if (ok != null && err != null) {
            throw new IllegalStateException();
        }
    }

    public static <T, E> Result<T, E> ok(T t) {
        Objects.requireNonNull(t);
        return new Result<>(t, null);
    }

    public static <T, E> Result<T, E> err(E err) {
        Objects.requireNonNull(err);
        return new Result<>(null, err);
    }

    public boolean isOk() {
        return ok != null;
    }

    public boolean isErr() {
        return err != null;
    }

    public T get() {
        return ok;
    }
}
