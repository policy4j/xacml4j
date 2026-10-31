package org.xacml4j.jmh.value;

/*
 * #%L
 * Xacml4J JMH Benchmarks
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.xacml4j.v30.types.StringVal;

/** Compares cached-hash and first-use string values at the D49 threshold. */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class StringValEqualsBenchmark
{
	@Param({"16", "32", "57"})
	public int length;

	@Param({"identical", "equal", "differsFirst", "differsLast"})
	public String relation;

	private char[] leftCharacters;
	private char[] rightCharacters;
	private boolean identical;
	private StringVal cachedLeft;
	private StringVal cachedRight;

	@Setup(Level.Trial)
	public void setUp(){
		leftCharacters = new char[length];
		Arrays.fill(leftCharacters, 'a');
		rightCharacters = leftCharacters.clone();
		identical = false;
		boolean equal;
		switch(relation){
		case "identical":
			identical = true;
			equal = true;
			break;
		case "equal":
			equal = true;
			break;
		case "differsFirst":
			rightCharacters[0] = 'b';
			equal = false;
			break;
		case "differsLast":
			rightCharacters[length - 1] = 'b';
			equal = false;
			break;
		default:
			throw new IllegalArgumentException("Unknown relation: " + relation);
		}

		String left = new String(leftCharacters);
		String right = identical ? left : new String(rightCharacters);
		left.hashCode();
		right.hashCode();
		cachedLeft = StringVal.of(left);
		cachedRight = identical ? cachedLeft : StringVal.of(right);
		if((cachedLeft == cachedRight) != identical ||
				(!identical && left == right) || cachedLeft.equals(cachedRight) != equal){
			throw new IllegalStateException("Invalid input: " + length + ", " + relation);
		}
	}

	@Benchmark
	public boolean cachedHash(){
		return cachedLeft.equals(cachedRight);
	}

	@Benchmark
	public boolean firstUse(FreshValues values){
		return values.left.equals(values.right);
	}

	/**
	 * Each invocation receives new Strings whose hashes have not been requested.
	 * Invocation setup is outside the timed method, but JMH's per-invocation
	 * timing and allocation effects can dominate such a short operation.
	 */
	@State(Scope.Thread)
	public static class FreshValues
	{
		private StringVal left;
		private StringVal right;

		@Setup(Level.Invocation)
		public void setUp(StringValEqualsBenchmark inputs){
			left = StringVal.of(new String(inputs.leftCharacters));
			right = inputs.identical ? left : StringVal.of(new String(inputs.rightCharacters));
		}
	}
}
