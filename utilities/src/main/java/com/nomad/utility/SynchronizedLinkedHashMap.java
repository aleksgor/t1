package com.nomad.utility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class SynchronizedLinkedHashMap<K, V> {
    private final Object mutex;
    private final Map<K, V> m; // Backing Map

    public SynchronizedLinkedHashMap() {
        mutex = this;
        m = new LinkedHashMap<>();
    }

    public SynchronizedLinkedHashMap(final int size) {
        mutex = this;
        m = new LinkedHashMap<>(size, 1);
    }

    public int size() {
        synchronized (mutex) {
            return m.size();
        }
    }

    public V get(final K key) {
        synchronized (mutex) {
            final V result = m.get(key);
            if (result != null) {
                m.remove(key);
                m.put(key, result);
            }
            return result;
        }
    }

    public V getQuietly(final K key) {
        synchronized (mutex) {
            return m.get(key);
        }
    }

    public boolean isEmpty() {
        synchronized (mutex) {
            return m.isEmpty();
        }
    }

    public boolean containsKey(final K key) {
        synchronized (mutex) {
            return m.containsKey(key);
        }
    }

    public V put(final K key, final V value) {
        synchronized (mutex) {
            return m.put(key, value);
        }
    }

    public V remove(final Object key) {
        synchronized (mutex) {
            return m.remove(key);
        }
    }

    public void removeAll(final Collection<K> keys) {
        synchronized (mutex) {
            for (final K k : keys) {
                m.remove(k);
            }
        }
    }

    public Set<K> keySet() {
        synchronized (mutex) {
            return new LinkedHashSet<>(m.keySet());
        }

    }

    public Set<K> keySet(final int i) {
        synchronized (mutex) {
            final LinkedHashSet<K> result = new LinkedHashSet<>(i, 1);
            final Iterator<K> iterator = m.keySet().iterator();
            while (iterator.hasNext() && result.size() < i) {
                result.add(iterator.next());
            }
            return result;
        }

    }

    public Set<Map.Entry<K, V>> entrySet() {
        synchronized (mutex) {
            final Set<Map.Entry<K, V>> entrySet = new LinkedHashSet<>(m.entrySet());
            return entrySet;
        }
    }

    public Set<V> values() {
        synchronized (mutex) {
            return new LinkedHashSet<>(m.values());
        }
    }

    public void clear() {
        synchronized (mutex) {
            m.clear();
        }
    }

    public void putAll(final Map<K, V> data) {
        synchronized (mutex) {
            m.putAll(data);
        }

    }

    public Map<K, V> getAllData() {
        synchronized (mutex) {
            return new HashMap<>(m);
        }

    }

    public Set<Map.Entry<K, V>> entrySet(int size) {
        synchronized (mutex) {
            final Iterator<Map.Entry<K, V>> iterator = m.entrySet().iterator();
            final Set<Map.Entry<K, V>> entrySet = new LinkedHashSet<>(size, 1);
            while (size-- > 0 && iterator.hasNext()) {
                entrySet.add(iterator.next());
            }
            return entrySet;
        }
    }

    @Override
    public String toString() {
        return "SynchronizedLinkedHachMap [m=" + m + "]";
    }

}
