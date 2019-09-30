package com.nomad.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class RandomCollection<E> {
    protected NavigableMap<Double, Value> map = new TreeMap<>();
    protected Map<E, Double> counterMap = new HashMap<>();
    protected double total = 0;
    protected final Random random = new Random(System.currentTimeMillis());

    public void add(double weight, final E element) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Parameter weight is:" + weight + " must be more then 0");
        }
        if (counterMap.containsKey(element)) {
            update(weight, element);
        }
        synchronized (map) {
            while (map.containsKey(weight)) {
                weight += weight * 0.00001d;
            }
            total += weight;
            map.put(total, new Value(weight, element));
            counterMap.put(element, total);
        }
    }

    public void update(final double weight, final E element) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Parameter weight is:" + weight + " must be more then 0");
        }
        final NavigableMap<Double, Value> newMap = new TreeMap<>();
        final Map<E, Double> newCounterMap = new HashMap<>();
        synchronized (newMap) {
            final Double key = counterMap.get(element);
            if (key == null) {
                add(weight, element);
                return;
            }
            double newTotal = 0;
            for (final Entry<Double, Value> entry : map.entrySet()) {
                if (entry.getKey().equals(key)) {
                    newTotal += weight;
                    newMap.put(newTotal, new Value(entry.getValue().weight, entry.getValue().value));
                    newCounterMap.put(element, newTotal);
                } else {
                    newTotal += entry.getValue().weight;
                    newMap.put(newTotal, new Value(entry.getValue().weight, entry.getValue().value));
                    newCounterMap.put(entry.getValue().value, newTotal);
                }
            }
            synchronized (map) {
                map = newMap;
                total = newTotal;
                counterMap = newCounterMap;
            }

        }
    }

    public E next() {
        synchronized (map) {
            final double value = random.nextDouble() * total;
            try {
                return map.ceilingEntry(value).getValue().value;
            } catch (final Exception e) {
                throw e;
            }
        }
    }

    public Collection<E> getRandomList(final int length) {
        final Collection<E> result = getRandomList();
        if (result.size() > length) {
            return new ArrayList<>(result).subList(0, length);
        }
        return result;
    }

    public Collection<E> getRandomList() {
        final Set<E> indexes = new HashSet<>(map.size(), 1);
        final Collection<E> result = new ArrayList<>(map.size());
        while (indexes.size() < map.size()) {
            final E next = next();
            if (!indexes.contains(next)) {
                indexes.add(next);
                result.add(next);
            }
        }
        return result;
    }

    public List<E> getAllElements() {
        synchronized (map) {
            final List<E> result = new ArrayList<>(map.size());
            for (final Value value : map.values()) {
                result.add(value.value);
            }
            return result;
        }
    }

    public E removeElement(final E element) {
        if (!counterMap.containsKey(element)) {
            return null;
        }
        final NavigableMap<Double, Value> newMap = new TreeMap<>();
        final Map<E, Double> newCounterMap = new HashMap<>();
        E result = null;
        synchronized (newMap) {
            final Double key = counterMap.get(element);
            double newTotal = 0;
            for (final Entry<Double, Value> entry : map.entrySet()) {
                if (entry.getKey() != key) {
                    newTotal += entry.getKey();
                    newMap.put(entry.getKey(), new Value(entry.getKey(), entry.getValue().value));
                    newCounterMap.put(entry.getValue().value, entry.getKey());
                } else {
                    result = entry.getValue().value;
                }
            }
            synchronized (map) {
                map = newMap;
                total = newTotal;
                counterMap = newCounterMap;
            }
            return result;
        }
    }

    private class Value {
        public Value(final double weight, final E element) {
            value = element;
            this.weight = weight;
        }

        private final Double weight;
        private final E value;
        @Override
        public String toString() {
            return "Value [value=" + value + "]";
        }

    }
    protected E get(final Double weight){
        final Value value=map.get(weight);
        if(value==null){
            return null;
        }
        return value.value;
    }
}
