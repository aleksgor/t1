package com.nomad.cache.servermanager.management;

public class IntegerRange {

	private long quotient;
	private boolean able = false;

	public IntegerRange(int count) {

		if (count > 0) {
			long tmp = Integer.MAX_VALUE;
			tmp -= Integer.MIN_VALUE;
			quotient = (tmp / count) + 1;
			able=true;
		}
	}

	public int getIntRange(int value) {
		if (able) {
			long tt = value;
			tt -= Integer.MIN_VALUE;
			return (int) (tt / quotient);
		}
		return -1;
	}

}
