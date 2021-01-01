package dev.alexnader.framed.util;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.ToIntFunction;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FunctionalUtil {
    private FunctionalUtil() {
        throw new IllegalStateException("Should not instantiate utility class.");
    }

    public static <A> OptionalInt mapToInt(final Optional<A> optional, final ToIntFunction<A> f) {
        return optional.map(a -> OptionalInt.of(f.applyAsInt(a))).orElseGet(OptionalInt::empty);
    }

    public static <A> OptionalInt flatMapToInt(final Optional<A> optional, final Function<A, OptionalInt> f) {
        return optional.isPresent() ? f.apply(optional.get()) : OptionalInt.empty();
    }
}
