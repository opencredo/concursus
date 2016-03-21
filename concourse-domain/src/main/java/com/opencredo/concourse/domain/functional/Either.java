package com.opencredo.concourse.domain.functional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
public interface Either<L, R> {

    static <L, R> Either<L, R> ofLeft(L left) {
        return new Left<>(left);
    }

    static <L, R> Either<L, R> ofRight(R right) {
        return new Right<>(right);
    }

    default boolean isLeft() {
        return join(l -> true, r -> false);
    }

    default boolean isRight() {
        return join(l -> false, r -> true);
    }

    default LeftProjection<L, R> left() { return new ConcreteLeftProjection<>(this); }
    default RightProjection<L, R> right() { return new ConcreteRightProjection<>(this); }

    default Either<R, L> swap() {
        return join(Either::<R, L>ofRight, Either::<R, L>ofLeft);
    }

    <O> O join(Function<? super L, ? extends O> left, Function<? super R, ? extends O> right);

    default <L2, R2> Either<L2, R2> map(Function<? super L, ? extends L2> left, Function<? super R, ? extends R2> right) {
        return join(left.andThen(Either::<L2, R2>ofLeft), right.andThen(Either::<L2, R2>ofRight));
    }

    default void forEither(Consumer<? super L> left, Consumer<? super R> right) {
        join(
                l -> { left.accept(l); return null; },
                r -> { right.accept(r); return null; });
    }

    interface LeftProjection<L, R> extends Either<L, R> {
        default <L2> LeftProjection<L2, R> map(Function<? super L, ? extends L2> f) {
            return join(
                    f.andThen(Either::<L2, R>ofLeft),
                    Either::<L2, R>ofRight).left();
        }
        default <L2> LeftProjection<L2, R> flatMap(Function<? super L, ? extends Either<L2, R>> ff) {
            return join(ff, Either::<L2, R>ofRight).left();
        }
        default Optional<L> toOptional() {
            return join(Optional::<L>of, r -> Optional.<L>empty());
        }
        default Stream<L> stream() {
            return join(Stream::<L>of, r -> Stream.<L>empty());
        }
        default void ifPresent(Consumer<? super L> consumer) {
            forEither(consumer, r -> {});
        }
    }

    interface RightProjection<L, R> extends Either<L, R> {
        default <R2> RightProjection<L, R2> map(Function<? super R, ? extends R2> f) {
            return join(
                    Either::<L, R2>ofLeft,
                    f.andThen(Either::<L, R2>ofRight)).right();
        }
        default <R2> RightProjection<L, R2> flatMap(Function<? super R, ? extends Either<L, R2>> ff) {
            return join(Either::<L, R2>ofLeft, ff).right();
        }
        default Optional<R> toOptional() {
            return join(l -> Optional.<R>empty(), Optional::<R>of);
        }
        default Stream<R> stream() {
            return join(l -> Stream.<R>empty(), Stream::<R>of);
        }
        default void ifPresent(Consumer<? super R> consumer) {
            forEither(l -> {}, consumer);
        }
    }

    abstract class BaseProjection<L, R> extends Base<L, R> {
        private final Either<L, R> either;
        private BaseProjection(Either<L, R> either) {
            this.either = either;
        }

        @Override
        public <O> O join(Function<? super L, ? extends O> left, Function<? super R, ? extends O> right) {
            return either.join(left, right);
        }
    }

    final class ConcreteLeftProjection<L, R> extends BaseProjection<L, R> implements LeftProjection<L, R> {
        private ConcreteLeftProjection(Either<L, R> either) {
            super(either);
        }
    }

    final class ConcreteRightProjection<L, R> extends BaseProjection<L, R> implements RightProjection<L, R> {
        private ConcreteRightProjection(Either<L, R> either) {
            super(either);
        }
    }

    abstract class Base<L, R> implements Either<L, R> {
        @Override
        public boolean equals(Object o) {
            return o == this
                    || ((o instanceof Either)
                    && ((Either<?, ?>) o).join(
                    otherL -> join(l -> otherL.equals(l), r -> false),
                    otherR -> join(l -> false, r -> otherR.equals(r))));
        }

        @Override
        public int hashCode() {
            return join(Object::hashCode, r -> -r.hashCode());
        }

        @Override
        public String toString() {
            return join(
                    l -> String.format("left(%s)", l),
                    r -> String.format("right(%s)", r));
        }
    }

    class Left<L, R> extends Base<L, R> {
        private final L left;

        private Left(L left) {
            this.left = left;
        }

        @Override
        public <O> O join(Function<? super L, ? extends O> lf, Function<? super R, ? extends O> rf) {
            return lf.apply(left);
        }
    }

    class Right<L, R> extends Base<L, R> {
        private final R right;

        private Right(R right) {
            this.right = right;
        }

        @Override
        public <O> O join(Function<? super L, ? extends O> lf, Function<? super R, ? extends O> rf) {
            return rf.apply(right);
        }
    }
}
