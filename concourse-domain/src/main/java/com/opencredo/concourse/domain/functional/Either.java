package com.opencredo.concourse.domain.functional;

import java.util.function.Consumer;
import java.util.function.Function;

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

    <O> O join(Function<? super L, ? extends O> left, Function<? super R, ? extends O> right);

    default void forEither(Consumer<? super L> left, Consumer<? super R> right) {
        join(
                l -> { left.accept(l); return null; },
                r -> { right.accept(r); return null; });
    }

    interface LeftProjection<L, R> extends Either<L, R> {
        <L2> LeftProjection<L2, R> map(Function<? super L, ? extends L2> f);
    }

    interface RightProjection<L, R> extends Either<L, R> {
        <R2> RightProjection<L, R2> map(Function<? super R, ? extends R2> f);
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

        @Override
        public <L2> LeftProjection<L2, R> map(Function<? super L, ? extends L2> f) {
            return new ConcreteLeftProjection<>(join(
                    left -> Either.<L2, R>ofLeft(f.apply(left)),
                    Either::<L2, R>ofRight));
        }
    }

    final class ConcreteRightProjection<L, R> extends BaseProjection<L, R> implements RightProjection<L, R> {

        private ConcreteRightProjection(Either<L, R> either) {
            super(either);
        }

        @Override
        public <R2> RightProjection<L, R2> map(Function<? super R, ? extends R2> f) {
            return new ConcreteRightProjection<>(join(
                    Either::<L, R2>ofLeft,
                    right -> Either.<L, R2>ofRight(f.apply(right))));
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
