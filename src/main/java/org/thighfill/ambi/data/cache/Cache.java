package org.thighfill.ambi.data.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Cache<K, E> {
    private final Map<K, E> cacheMap = new HashMap<>();
    private final Function<K, E> builder;

    protected Cache(Function<K, E> builder) {
        this.builder = builder;
    }

    public E get(K key){
        E val;
        if(cacheMap.containsKey(key)){
            val = cacheMap.get(key);
            accessed(key, val);
        }else{
            val = builder.apply(key);
            cacheMap.put(key, val);
            added(key, val);
        }
        return val;
    }

    protected abstract void accessed(K key, E val);
    protected abstract void added(K key, E val);
}
