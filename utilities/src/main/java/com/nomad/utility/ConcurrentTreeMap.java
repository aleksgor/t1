package com.nomad.utility;


import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentTreeMap<K, V> {
    private final TreeMap<K, V> map;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ConcurrentTreeMap() {
        map = new TreeMap<K, V>();
    }

    public SortedMap<K, V> headMap(final K toKey) {
        lock.readLock().lock();
        try {
            return headMap(toKey);
        } finally {
            lock.readLock().unlock();
        }
    }

    public K getKeyGreaterOrEqualTo(final K fromKey) {
        lock.readLock().lock();
        try {
            final SortedMap<K, V> tailMap = tailMap(fromKey);
            return tailMap.firstKey();
        } finally {
            lock.readLock().unlock();
        }
    }

    public V getGreaterOrEqualTo(final K fromKey) {
        lock.readLock().lock();
        try {
            final SortedMap<K, V> tailMap = tailMap(fromKey);
            return tailMap.get(tailMap.firstKey());
        } finally {
            lock.readLock().unlock();
        }
    }

    public SortedMap<K, V> tailMap(final K fromKey) {
        lock.readLock().lock();
        try {
            return map.tailMap(fromKey);
        } finally {
            lock.readLock().unlock();
        }
    }

    public SortedMap<K, V> subMap(final K fromKey, final K toKey) {
        lock.readLock().lock();
        try {
            return map.subMap(fromKey, toKey);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<K> keySet() {
        lock.readLock().lock();
        try {
            return map.keySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    public K firstKey() {
        lock.readLock().lock();
        try {
            return map.firstKey();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean containsKey(final K key) {
        lock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public V lastValue() {
        lock.readLock().lock();
        try {
            final K key = map.lastKey();
            if (key == null)
                return null;
            else
                return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<V> values() {
        lock.readLock().lock();
        try {
            return map.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    public V get(final K key) {
        lock.readLock().lock();
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public K lastKey() {
        lock.readLock().lock();
        try {
            return map.lastKey();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void remove(final K key) {
        lock.writeLock().lock();
        try {
            map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void put(final K key, final V value) {
        lock.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}