package com.binaryigor.dsa;

public class MaxHeap {

    private int size;
    private final int[] heap;

    public MaxHeap(int capacity) {
        heap = new int[capacity];
        size = 0;
    }

    public void insert(int value) {
        heap[size] = value;
        size++;

        int current = size - 1;
        while (current != 0) {
            int parent = (current - 1) / 2;
            if (heap[parent] >= heap[current]) {
                break;
            }
            swap(current, parent);
            current = parent;
        }
    }

    private void swap(int a, int b) {
        int tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    public int getMax() {
        if (size == 0) {
            throw new RuntimeException("Can't get max of an empty heap!");
        }

        var max = heap[0];

        swap(0, size - 1);
        size--;
        heapify(0);

        return max;
    }

    private void heapify(int idx) {
        int largest = idx;
        int l = 2 * idx + 1;
        int r = 2 * idx + 2;

        if (l < size && heap[l] > heap[largest]) {
            largest = l;
        }
        if (r < size && heap[r] > heap[largest]) {
            largest = r;
        }
        if (largest != idx) {
            swap(idx, largest);
            heapify(largest);
        }
    }

    public void delete(int value) {
        int idx = -1;
        for (int i = 0; i< size; i++) {
            if (heap[i] == value) {
                idx = i;
                break;
            }
        }

        if (idx < 0) {
            throw new RuntimeException("There is no %d item in the heap!".formatted(value));
        }

        swap(idx, size - 1);
        size--;
        heapify(idx);
    }

    public int peekMax() {
        if (size == 0) {
            throw new RuntimeException("Can't peek max of an empty heap!");
        }
        return heap[0];
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
