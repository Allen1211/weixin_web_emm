package com.allen.imsystem.common.utils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @ClassName MutableSingletonList
 * @Description 可变的，永远只有一个元素的List
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public class MutableSingletonList<E> extends AbstractList<E> implements RandomAccess, Serializable {

    private E element;

    private boolean isEmpty;

    public MutableSingletonList() {
        this.isEmpty = true;
    }

    public MutableSingletonList(E element) {
        this.element = element;
        this.isEmpty = false;
    }

    @Override
    public E set(int index, E element) {
        if (index != 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
        this.element = element;
        if(this.isEmpty){
            this.isEmpty = false;
        }
        return element;
    }

    @Override
    public E get(int index) {
        if (index != 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
        return element;
    }

    @Override
    public boolean isEmpty() {
        return this.isEmpty;
    }

    @Override
    public int size() {
        return this.isEmpty ? 0 : 1;
    }

    @Override
    public void sort(Comparator<? super E> c) {
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        action.accept(element);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }


    public Iterator<E> iterator() {
        return singletonIterator(element);
    }

    private static <E> Iterator<E> singletonIterator(final E e) {
        return new Iterator<E>() {
            private boolean hasNext = true;

            public boolean hasNext() {
                return hasNext;
            }

            public E next() {
                if (hasNext) {
                    hasNext = false;
                    return e;
                }
                throw new NoSuchElementException();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                Objects.requireNonNull(action);
                if (hasNext) {
                    action.accept(e);
                    hasNext = false;
                }
            }
        };
    }

}
