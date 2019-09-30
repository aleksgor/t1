package com.nomad.cache.ServerManager;

import static org.junit.Assert.*;

import com.nomad.cache.servermanager.management.IntegerRange;

public class FirstTest {

	/*
	 * test session isolation
	 */
	@org.junit.Test
	public void test1() {

		IntegerRange ir = new IntegerRange(16);
		int result = ir.getIntRange((Integer.MAX_VALUE / 2) - 1);
		assertEquals(result, 11);

		result = ir.getIntRange((Integer.MAX_VALUE / 2) + 5);
		assertEquals(result, 12);

		result = ir.getIntRange((Integer.MIN_VALUE) + 1);
		assertEquals(result, 0);

		result = ir.getIntRange((Integer.MAX_VALUE) - 1);
		assertEquals(result, 15);

		result = ir.getIntRange((Integer.MAX_VALUE) );
		assertEquals(result, 15);
		
		result = ir.getIntRange((Integer.MIN_VALUE) );
		assertEquals(result, 0);

		ir = new IntegerRange(1);
		 result = ir.getIntRange((Integer.MAX_VALUE / 2) - 1);
		assertEquals(result, 0);
	}

}
