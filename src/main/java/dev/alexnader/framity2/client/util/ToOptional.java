package dev.alexnader.framity2.client.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ToOptional<O extends ToOptional<O>> {
    Optional<O> toOptional();
    <T> T match(Function<O, T> some, Supplier<T> none);

    default boolean isPresent() {
        return toOptional().isPresent();
    }

    interface Some<O extends ToOptional<O>> extends ToOptional<O> {
        default Optional<O> toOptional() {
            //noinspection unchecked
            return Optional.of((O) this);
        }

        @Override
        default <T> T match(final Function<O, T> some, final Supplier<T> none) {
            //noinspection unchecked
            return some.apply((O) this);
        }
    }

    interface None<O extends ToOptional<O>> extends ToOptional<O> {
        default Optional<O> toOptional() {
            return Optional.empty();
        }

        @Override
        default <T> T match(final Function<O, T> some, final Supplier<T> none) {
            return none.get();
        }
    }
}
