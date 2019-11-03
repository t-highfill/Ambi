package org.thighfill.ambi.data.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RecencyCache<K, E> extends Cache<K, E> {

    private final int maxCapacity;
    private final List<E> queue;

    public RecencyCache(Function<K, E> builder, int maxCapacity) {
        super(builder);
        this.maxCapacity = maxCapacity;
        queue = new LinkedList<>();
    }

    @Override
    protected void accessed(K key, E val) {
        queue.remove(val);
        queue.add(val);
    }

    @Override
    protected void added(K key, E val) {
        queue.add(val);
        while (queue.size() > maxCapacity) {
            queue.remove(0);
        }
    }
}
