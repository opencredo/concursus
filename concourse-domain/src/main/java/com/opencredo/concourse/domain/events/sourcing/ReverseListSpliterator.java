package com.opencredo.concourse.domain.events.sourcing;

import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class ReverseListSpliterator<T> implements Spliterator<T> {

    public static <T> ReverseListSpliterator<T> over(List<T> list) {
        return new ReverseListSpliterator<>(list.listIterator(list.size()), list.size());
    }

    private final ListIterator<T> iterator;
    private final int listSize;

    private ReverseListSpliterator(ListIterator<T> iterator, int listSize) {
        this.iterator = iterator;
        this.listSize = listSize;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (iterator.hasPrevious()) {
            action.accept(iterator.previous());
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return listSize;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED;
    }
}
