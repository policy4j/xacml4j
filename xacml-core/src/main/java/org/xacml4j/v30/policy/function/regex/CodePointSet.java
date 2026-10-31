package org.xacml4j.v30.policy.function.regex;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * An immutable set of Unicode code points, kept as sorted ranges that do not touch (change
 * add-regex-engine-spi, design D4): the flat form of a character class, which every engine can
 * emit, and the unit of the size estimate of a pattern.
 */
public final class CodePointSet
{
	public static final int MIN_CODE_POINT = Character.MIN_CODE_POINT;
	public static final int MAX_CODE_POINT = Character.MAX_CODE_POINT;

	public static final CodePointSet EMPTY = new CodePointSet(new int[0]);
	public static final CodePointSet ALL = new CodePointSet(new int[]{MIN_CODE_POINT, MAX_CODE_POINT});

	/** The ranges, {@code lo, hi} pairs inclusive, sorted and with a gap between each two */
	private final int[] ranges;

	private CodePointSet(int[] ranges){
		this.ranges = ranges;
	}

	public static CodePointSet of(int codePoint){
		return of(codePoint, codePoint);
	}

	/** The range from {@code lo} to {@code hi}, both inclusive */
	public static CodePointSet of(int lo, int hi){
		checkCodePoint(lo);
		checkCodePoint(hi);
		Preconditions.checkArgument(lo <= hi, "lo must not be above hi");
		return new CodePointSet(new int[]{lo, hi});
	}

	public static Builder builder(){
		return new Builder();
	}

	/** Whether the set holds a code point */
	public boolean contains(int codePoint){
		int low = 0;
		int high = ranges.length / 2 - 1;
		while(low <= high){
			int mid = (low + high) >>> 1;
			if(codePoint < ranges[2 * mid]){
				high = mid - 1;
			}else if(codePoint > ranges[2 * mid + 1]){
				low = mid + 1;
			}else{
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty(){
		return ranges.length == 0;
	}

	/** The number of ranges, which an engine emits one by one */
	public int rangeCount(){
		return ranges.length / 2;
	}

	/** The first code point of a range, by the index of the range */
	public int lo(int range){
		return ranges[2 * range];
	}

	/** The last code point of a range, by the index of the range */
	public int hi(int range){
		return ranges[2 * range + 1];
	}

	/** The number of code points in the set */
	public long size(){
		long size = 0;
		for(int i = 0; i < ranges.length; i += 2){
			size += ranges[i + 1] - ranges[i] + 1L;
		}
		return size;
	}

	public CodePointSet union(CodePointSet other){
		if(other.isEmpty()){
			return this;
		}
		if(isEmpty()){
			return other;
		}
		int[] joined = Arrays.copyOf(ranges, ranges.length + other.ranges.length);
		System.arraycopy(other.ranges, 0, joined, ranges.length, other.ranges.length);
		return new CodePointSet(normalized(joined));
	}

	/** Every code point that is not in this set */
	public CodePointSet complement(){
		Builder b = new Builder();
		int next = MIN_CODE_POINT;
		for(int i = 0; i < ranges.length; i += 2){
			if(ranges[i] > next){
				b.add(next, ranges[i] - 1);
			}
			next = ranges[i + 1] + 1;
		}
		if(next <= MAX_CODE_POINT){
			b.add(next, MAX_CODE_POINT);
		}
		return b.build();
	}

	public CodePointSet intersect(CodePointSet other){
		Builder b = new Builder();
		int i = 0;
		int j = 0;
		while(i < ranges.length && j < other.ranges.length){
			int lo = Math.max(ranges[i], other.ranges[j]);
			int hi = Math.min(ranges[i + 1], other.ranges[j + 1]);
			if(lo <= hi){
				b.add(lo, hi);
			}
			if(ranges[i + 1] < other.ranges[j + 1]){
				i += 2;
			}else{
				j += 2;
			}
		}
		return b.build();
	}

	/** The code points of this set that are not in the other */
	public CodePointSet minus(CodePointSet other){
		return intersect(other.complement());
	}

	@Override
	public boolean equals(Object o){
		return o == this || (o instanceof CodePointSet s && Arrays.equals(ranges, s.ranges));
	}

	@Override
	public int hashCode(){
		return Arrays.hashCode(ranges);
	}

	@Override
	public String toString(){
		StringBuilder b = new StringBuilder("[");
		for(int i = 0; i < ranges.length; i += 2){
			if(i > 0){
				b.append(' ');
			}
			b.append(String.format("%04X", ranges[i]));
			if(ranges[i + 1] != ranges[i]){
				b.append('-').append(String.format("%04X", ranges[i + 1]));
			}
		}
		return b.append(']').toString();
	}

	private static void checkCodePoint(int codePoint){
		Preconditions.checkArgument(codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT,
				"not a code point: %s", codePoint);
	}

	/** Sorts ranges and merges the ones that overlap or touch */
	private static int[] normalized(int[] pairs){
		int count = pairs.length / 2;
		long[] keyed = new long[count];
		for(int i = 0; i < count; i++){
			keyed[i] = ((long)pairs[2 * i] << 32) | (pairs[2 * i + 1] & 0xFFFFFFFFL);
		}
		Arrays.sort(keyed);
		int[] merged = new int[pairs.length];
		int size = 0;
		for(long key : keyed){
			int lo = (int)(key >>> 32);
			int hi = (int)key;
			if(size > 0 && lo <= merged[size - 1] + 1){
				merged[size - 1] = Math.max(merged[size - 1], hi);
			}else{
				merged[size++] = lo;
				merged[size++] = hi;
			}
		}
		return size == merged.length ? merged : Arrays.copyOf(merged, size);
	}

	/** Collects ranges in any order; {@link #build()} sorts and merges them */
	public static final class Builder
	{
		private final List<int[]> pairs = new ArrayList<>();

		private Builder(){
		}

		public Builder add(int codePoint){
			return add(codePoint, codePoint);
		}

		public Builder add(int lo, int hi){
			checkCodePoint(lo);
			checkCodePoint(hi);
			Preconditions.checkArgument(lo <= hi, "lo must not be above hi");
			pairs.add(new int[]{lo, hi});
			return this;
		}

		public Builder addAll(CodePointSet set){
			for(int i = 0; i < set.ranges.length; i += 2){
				pairs.add(new int[]{set.ranges[i], set.ranges[i + 1]});
			}
			return this;
		}

		public CodePointSet build(){
			if(pairs.isEmpty()){
				return EMPTY;
			}
			int[] flat = new int[pairs.size() * 2];
			for(int i = 0; i < pairs.size(); i++){
				flat[2 * i] = pairs.get(i)[0];
				flat[2 * i + 1] = pairs.get(i)[1];
			}
			return new CodePointSet(normalized(flat));
		}
	}
}
