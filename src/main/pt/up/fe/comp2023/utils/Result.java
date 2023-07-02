package pt.up.fe.comp2023.utils;

import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Result type heavily inspired on Rust's <a href="https://doc.rust-lang.org/src/core/result.rs.html">Result</a> enum
 */
public class Result<Ok, Error> implements Iterable<Ok> {

    private enum ResultType {
        OK, ERROR;
    }

    public static <A, B> Result<A, B> ok(A value) {
        return new Result<>(ResultType.OK, value, null);
    }

    public static <A, B> Result<A, B> error(B error) {
        return new Result<>(ResultType.ERROR, null, error);
    }

    private final ResultType type;
    private final Ok value;
    private final Error error;

    private Result(ResultType type, Ok value, Error error) {
        switch (type) {
            case OK -> SpecsCheck.checkNotNull(value, () -> "Value of an ok result cannot be null");
            case ERROR -> SpecsCheck.checkNotNull(error, () -> "Error of an error result cannot be null");
        }

        this.type = type;
        this.value = value;
        this.error = error;


    }

    public boolean isOk() {
        return type == ResultType.OK;
    }

    public boolean isError() {
        return type == ResultType.ERROR;
    }

    public boolean isOkAnd(Predicate<Ok> function) {
        return switch (type) {
            case OK -> function.test(value);
            case ERROR -> false;
        };
    }

    public boolean isErrorAnd(Predicate<Error> function) {
        return switch (type) {
            case OK -> false;
            case ERROR -> function.test(error);
        };
    }

    public Optional<Ok> ok() {
        return switch (type) {
            case OK -> Optional.of(value);
            case ERROR -> Optional.empty();
        };
    }

    public Optional<Error> error() {
        return switch (type) {
            case OK -> Optional.empty();
            case ERROR -> Optional.of(error);
        };
    }

    public <NewOk> Result<NewOk, Error> map(Function<Ok, NewOk> function) {
        return switch (type) {
            case OK -> Result.ok(function.apply(value));
            case ERROR -> Result.error(error);
        };
    }

    public <NewOk> NewOk mapOr(NewOk defaultValue, Function<Ok, NewOk> function) {
        return switch (type) {
            case OK -> function.apply(value);
            case ERROR -> defaultValue;
        };
    }

    public <NewOk> NewOk mapOrElse(Function<Error, NewOk> defaultValueFunction, Function<Ok, NewOk> function) {
        return switch (type) {
            case OK -> function.apply(value);
            case ERROR -> defaultValueFunction.apply(error);
        };
    }

    public <NewError> Result<Ok, NewError> mapError(Function<Error, NewError> function) {
        return switch (type) {
            case OK -> Result.ok(value);
            case ERROR -> Result.error(function.apply(error));
        };
    }

    public Result<Ok, Error> inspect(Consumer<Ok> consumer) {
        if (type == ResultType.OK) {
            consumer.accept(value);
        }

        return this;
    }

    public Result<Ok, Error> inspectError(Consumer<Error> consumer) {
        if (type == ResultType.ERROR) {
            consumer.accept(error);
        }

        return this;
    }

    @Override
    public Iterator<Ok> iterator() {
        return switch(type) {
            case OK -> Collections.singleton(value).iterator();
            case ERROR -> Collections.emptyIterator();
        };
    }

    public Ok expect(String msg) {
        return switch (type) {
            case OK -> value;
            case ERROR -> throw new RuntimeException(msg);
        };
    }

    public Error expectError(String msg) {
        return switch (type) {
            case OK -> throw new RuntimeException(msg);
            case ERROR -> error;
        };
    }

    public <NewOk> Result<NewOk, Error> and(Result<NewOk, Error> res) {
        return switch (type) {
            case OK -> res;
            case ERROR -> Result.error(error);
        };
    }

    public <NewOk> Result<NewOk, Error> andThen(Function<Ok, Result<NewOk, Error>> function) {
        return switch (type) {
            case OK -> function.apply(value);
            case ERROR -> Result.error(error);
        };
    }

    public <NewError> Result<Ok, NewError> or(Result<Ok, NewError> res) {
        return switch (type) {
            case OK -> Result.ok(value);
            case ERROR -> res;
        };
    }

    public <NewError> Result<Ok, NewError> orElse(Function<Error, Result<Ok, NewError>> function) {
        return switch (type) {
            case OK -> Result.ok(value);
            case ERROR -> function.apply(error);
        };
    }

    public Ok unwrap() {
        return switch (type) {
            case OK -> value;
            case ERROR -> throw new RuntimeException("Can't unwrap error variant of Result");
        };
    }

    public Error unwrapErr() {
        return switch (type) {
            case OK -> throw new RuntimeException("Can't unwrap error of ok variant of Result");
            case ERROR -> error;
        };
    }

    public Ok unwrapOr(Ok defaultValue) {
        return switch (type) {
            case OK -> value;
            case ERROR -> defaultValue;
        };
    }

    public Ok unwrapOrElse(Function<Error, Ok> function) {
        return switch (type) {
            case OK -> value;
            case ERROR -> function.apply(error);
        };
    }

    public boolean contains(Ok value) {
        return switch (type) {
            case OK ->  this.value.equals(value);
            case ERROR -> false;
        };
    }

    public boolean containsError(Error error) {
        return switch (type) {
            case OK -> false;
            case ERROR -> this.error.equals(error);
        };
    }
}
