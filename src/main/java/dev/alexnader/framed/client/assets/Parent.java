package dev.alexnader.framed.client.assets;

import dev.alexnader.framed.client.util.ToOptional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class Parent implements ToOptional<Parent> {
    public static Parent ofNullable(@Nullable final Identifier parentId) {
        if (parentId == null) {
            return NONE;
        } else {
            return new Some(parentId);
        }
    }

    public abstract Optional<Identifier> id();
    public abstract <T> T run(@Nonnull Function<Identifier, T> f, T whenNone);

    public static class Some extends Parent implements ToOptional.Some<Parent> {
        private final @Nonnull Identifier parent;

        public Some(final @Nonnull Identifier parent) {
            this.parent = parent;
        }

        @Override
        public Optional<Identifier> id() {
            return Optional.of(parent);
        }

        @Override
        public <T> T run(@Nonnull final Function<Identifier, T> f, final T whenNone) {
            return f.apply(parent);
        }
    }

    public static final Parent NONE = new Parent() {
        @Override
        public Optional<Identifier> id() {
            return Optional.empty();
        }

        @Override
        public <T> T run(@Nonnull final Function<Identifier, T> f, final T whenNone) {
            return whenNone;
        }

        @Override
        public <T> T match(final Function<Parent, T> some, final Supplier<T> none) {
            return none.get();
        }

        @Override
        public Optional<Parent> toOptional() {
            return Optional.empty();
        }
    };
}
