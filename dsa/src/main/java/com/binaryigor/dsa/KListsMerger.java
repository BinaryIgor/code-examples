package com.binaryigor.dsa;

import java.util.Comparator;
import java.util.PriorityQueue;

public class KListsMerger {
    static ListNode mergeKLists(ListNode[] lists) {
        var heap = new PriorityQueue<ListNode>(Comparator.comparingInt(a -> a.val));

        for (var l : lists) {
            if (l != null) {
                heap.add(l);
            }
        }

        ListNode head = null, prev = null;

        while (!heap.isEmpty()) {
            var node = heap.poll();
            if (node.next != null) {
                heap.add(node.next);
            }

            if (head == null) {
                head = node;
            }
            if (prev != null) {
                prev.next = node;
            }

            prev = node;
        }

        return head;
    }
}
