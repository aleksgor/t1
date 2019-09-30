package com.nomad.server.idgenerator;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicBigInteger extends Number {

    private AtomicReference<BigInteger> bigInt;
    public static final BigInteger ONE = new BigInteger("1");

    public AtomicBigInteger(BigInteger bigInt) {
        this.bigInt = new AtomicReference<BigInteger>(bigInt);
    }

    public AtomicBigInteger(byte[] value) {
        bigInt = new AtomicReference<BigInteger>(new BigInteger(value));
    }

    public AtomicBigInteger(String value) {
        bigInt = new AtomicReference<BigInteger>(new BigInteger(value));
    }

    @Override
    public int intValue() {
        return bigInt.get().intValue();
    }

    @Override
    public long longValue() {
        return bigInt.get().longValue();
    }

    @Override
    public float floatValue() {

        return bigInt.get().floatValue();
    }

    @Override
    public double doubleValue() {
        return bigInt.get().doubleValue();
    }

    public final void set(BigInteger newValue) {
        bigInt.set(newValue);
    }

    public BigInteger  get() {
        return bigInt.get();
    }

    public final BigInteger addAndGet(BigInteger delta) {
        while (true) {
            BigInteger currentValue = bigInt.get();
            BigInteger nextValue = currentValue.add(delta);
            if (bigInt.compareAndSet(currentValue, nextValue)) {
                return nextValue;
            }
        }
    }

    public final BigInteger getAndAdd(BigInteger delta) {
        while (true) {
            BigInteger currentValue = bigInt.get();
            BigInteger nextValue = currentValue.add(delta);
            if (bigInt.compareAndSet(currentValue, nextValue)) {
                return currentValue;
            }
        }
    }

    public final BigInteger getAndIncrement() {
        while (true) {
            BigInteger currentValue = bigInt.get();
            BigInteger nextValue = currentValue.add(ONE);
            if (bigInt.compareAndSet(currentValue, nextValue)) {
                return currentValue;
            }
        }
    }

    public final BigInteger incrementAndGet() {
        while (true) {
            BigInteger currentValue = bigInt.get();
            BigInteger nextValue = currentValue.add(ONE);
            if (bigInt.compareAndSet(currentValue, nextValue)) {
                return nextValue;
            }
        }
    }

    @Override
    public String toString() {
        return "AtomicBigInteger [bigInt=" + bigInt + "]";
    }


}
