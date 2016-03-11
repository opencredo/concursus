package com.opencredo.concourse.domain.functional;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.*;

public class EitherTest {

    @Test
    public void leftIsLeft() {
        Either<String, Integer> either = Either.ofLeft("left");
        assertTrue(either.isLeft());
        assertFalse(either.isRight());
    }

    @Test
    public void rightIsRight() {
        Either<String, Integer> either = Either.ofRight(23);
        assertFalse(either.isLeft());
        assertTrue(either.isRight());
    }

    @Test
    public void equality() {
        assertEquals(Either.ofLeft("left"), Either.ofLeft("left"));
        assertEquals(Either.ofRight("right"), Either.ofRight("right"));

        assertNotEquals(Either.ofLeft("foo"), Either.ofLeft("bar"));
        assertNotEquals(Either.ofRight("foo"), Either.ofRight("bar"));

        assertNotEquals(Either.ofRight("left"), Either.ofLeft("left"));
        assertNotEquals(Either.ofLeft("left"), Either.ofRight("left"));
    }

    @Test
    public void hashCoding() {
        assertEquals(Either.ofLeft("left").hashCode(), Either.ofLeft("left").hashCode());
        assertEquals(Either.ofRight("right").hashCode(), Either.ofRight("right").hashCode());

        assertNotEquals(Either.ofLeft("foo").hashCode(), Either.ofLeft("bar").hashCode());
        assertNotEquals(Either.ofRight("foo").hashCode(), Either.ofRight("bar").hashCode());

        assertNotEquals(Either.ofRight("left").hashCode(), Either.ofLeft("left").hashCode());
        assertNotEquals(Either.ofLeft("left").hashCode(), Either.ofRight("left").hashCode());
    }

    @Test
    public void toStringing() {
        assertEquals(Either.ofLeft("foo").toString(), "left(foo)");
        assertEquals(Either.ofRight(23).toString(), "right(23)");
    }

    @Test
    public void joining() {
        assertEquals(Either.<String, Integer>ofLeft("foo").join(String::toUpperCase, Object::toString), "FOO");
        assertEquals(Either.<String, Integer>ofRight(23).join(String::toUpperCase, Object::toString), "23");
    }

    @Test
    public void consuming() {
        AtomicReference<String> left = new AtomicReference<>();
        AtomicReference<Integer> right = new AtomicReference<>();

        Either.<String, Integer>ofLeft("foo").forEither(left::set, right::set);

        assertEquals(left.get(), "foo");
        assertNull(right.get());

        Either.<String, Integer>ofRight(23).forEither(left::set, right::set);
        assertEquals(right.get(), Integer.valueOf(23));
    }

    @Test
    public void projectingLeft() {
        Either<Integer, Integer> either = Either.<String, Integer>ofLeft("foo").left().map(String::length);

        assertEquals(either.join(Function.identity(), Function.identity()), Integer.valueOf(3));
    }

    @Test
    public void projectingRight() {
        Either<String, String> either = Either.<String, Integer>ofRight(23).right().map(i -> Integer.toString(i));

        assertEquals(either.join(Function.identity(), Function.identity()), "23");
    }
}
