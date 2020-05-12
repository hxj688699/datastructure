package com.meimeitech.dsa;

import java.util.Arrays;
import java.util.Comparator;

public class Vector<E> {
    private int size;
    private static int DEFAULT_CAPACITY = 10;
    private Object[] elementData;
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    public Vector() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    public Vector(int capacity) {
        if (capacity > 0) {
            this.elementData = new Object[capacity];
        } else {
            this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
        }
    }

    public int size() {
        return this.size;
    }

    public boolean add(E e) {
        ensureCapacity(size + 1);
        this.elementData[size++] = e;
        return true;
    }

    public void add(int index, E e) {
        ensureCapacity(size + 1);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        this.elementData[index] = e;
        size++;
    }

    public void addAll(E[] es) {
        ensureCapacity(size + es.length);
        System.arraycopy(es, 0, elementData, size, es.length);
        size += es.length;
    }

    public E get(int index) {
        rangeCheck(index);
        return (E) elementData[index];
    }

    public E[] subVector(int from, int to) {
        int subSize = to - from;
        Object[] es = new Object[subSize];
        System.arraycopy(elementData, from, es, 0, subSize);
        return (E[]) es;
    }

    private void ensureCapacity(int minCapacity) {
        if (this.elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(minCapacity, DEFAULT_CAPACITY);
        }
        if (minCapacity > this.elementData.length) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (minCapacity > newCapacity) {
            newCapacity = minCapacity;
        }
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    public E remove(int index) {
        rangeCheck(index);
        E oldValue = (E) elementData[index];
        int numMove = size - index - 1;
        if (numMove > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, numMove);
        }
        elementData[--size] = null;
        return oldValue;
    }

    private void rangeCheck(int index) {
        if (index >= size || index < 0) {
            throw new ArrayIndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    public boolean batchRemove(int from, int to) {
        rangeCheck(from);
        rangeCheck(to - 1);
        int numMove = size - to;
        if (numMove > 0)
        System.arraycopy(elementData, to, elementData, from, numMove);
        int newSize = size - (to - from);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;

        return true;
    }

    public int search(E e, Comparator<? super E> c) {
        int lo = 0;
        int hi = size - 1;
        while (lo < hi) {
            int mi = (lo + hi) >> 1;
            int cmp = c != null ? c.compare(e, (E) elementData[mi]) : ((Comparable) e).compareTo(elementData[mi]);
            if (cmp < 0) {
                hi = mi;
            } else {
                lo = mi;
            }
        }
        return --lo;
    }

    public static void main(String[] args) {
        Vector<Integer> v1 = new Vector<>();
        Integer[] src = new Integer[]{1, 2, 3, 4, 5, 6, 7};
        Integer from = src.length / 2 + 1;
        Integer to = src.length;
        Integer[] dest = new Integer[to - from];
        System.arraycopy(src, from, dest, 0, to - from);
        for (int i = 0; i < dest.length; i++) {
            System.out.println(dest[i]);
        }
        v1.addAll(src);
        Vector<Integer> v2 = new Vector<>();
        v2.addAll(v1.subVector(from, to));
        v1.batchRemove(src.length / 2, 5);
        System.out.println();
    }
}
