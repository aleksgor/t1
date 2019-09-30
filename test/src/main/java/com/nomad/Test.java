package com.nomad;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Test {

    public static void main(final String[] args) {
        new Test().test();
    }

    private void test() {
        int len = 10;
        List<Integer> numbers = new ArrayList<>(len);
        for (int i = 0; i < len*8; i++) {
            numbers.add(i);
            System.out.println(":"+i+" :"+i/8);
        }
//        parallel(numbers);
//        inSeries(numbers);

  //      System.out.println("time parallel:" + parallel(numbers) + ", in series:" + inSeries(numbers));

    }

    PFunction func = new PFunction();

    long parallel(List<Integer> numbers) {
        long start = System.currentTimeMillis();
       // numbers.stream().parallel().map(func).collect(Collectors.joining(", "));
        numbers.stream().parallel().map(func).collect(Collectors.joining(", "));
         //numbers.stream().parallel().map(Integer :: doubleValue).count();

        long stop = System.currentTimeMillis();
        return stop - start;
    }

    long inSeries(List<Integer> numbers) {
        long start = System.currentTimeMillis();
//        numbers.stream().map(func).collect(Collectors.joining(", "));
        numbers.stream().map(Integer :: doubleValue).count();

        long stop = System.currentTimeMillis();
        return stop - start;
    }

    private static class PFunction implements Function<Integer, String> {

        @Override
        public String apply(Integer t) {
            return "[" + t + "]";
        }
    }
}
