package org.thighfill.ambi.data.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RecencyCache<K, E> extends Cache<K, E> {

    private final int maxCapacity;
    private final List<K> queue;

    public RecencyCache(Function<K, E> builder, int maxCapacity) {
        super(builder);
        this.maxCapacity = maxCapacity;
        queue = new LinkedList<>();
    }

    @Override
    protected void accessed(K key, E val) {
        queue.remove(key);
        queue.add(key);
    }

    @Override
    protected void added(K key, E val) {
        queue.add(key);
        while (queue.size() > maxCapacity) {
            drop(queue.remove(0));
        }
    }
}
