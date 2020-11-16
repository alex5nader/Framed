package dev.alexnader.framity2.client.transform;

import dev.alexnader.framity2.client.util.ToOptional;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public abstract class IntApplier implements ToOptional<IntApplier> {
    public static final IntApplier NONE = new IntApplier() {
        @Override
        public void apply(final int value) { }

        @Override
        public Optional<IntApplier> toOptional() {
            return Optional.empty();
        }

        @Override
        public <T> T match(final Function<IntApplier, T> some, final Supplier<T> none) {
            return none.get();
        }
    };

    public abstract void apply(int value);

    public static class Some extends IntApplier implements ToOptional.Some<IntApplier> {
        private final IntConsumer f;

        public Some(final IntConsumer f) {
            this.f = f;
        }

        @Override
        public void apply(final int value) {
            f.accept(value);
        }
    }
}
