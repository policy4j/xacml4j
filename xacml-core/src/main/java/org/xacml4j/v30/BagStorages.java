package org.xacml4j.v30;

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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.Value;

final class BagStorages
{
	static final int LINEAR_LOOKUP_LIMIT = 8;
	enum Kind { VALUES, INTEGERS, DOUBLES, BOOLEANS }

	private BagStorages() {}

	static BagStorage.Builder builder(Kind kind) {
		return new Builder(kind);
	}

	private static final class Builder implements BagStorage.Builder {
		private final Kind kind;
		private final Map<Value<?>, Integer> counts = new LinkedHashMap<>();
		private boolean specialized = true;
		private int size;

		private Builder(Kind kind) { this.kind = kind; }

		@Override
		public Builder add(Value<?> value) {
			Objects.requireNonNull(value);
			counts.merge(value, 1, Math::addExact);
			size = Math.addExact(size, 1);
			specialized &= switch (kind) {
				case INTEGERS -> value instanceof IntegerVal;
				case DOUBLES -> value instanceof DoubleVal;
				case BOOLEANS -> value instanceof BooleanVal;
				case VALUES -> true;
			};
			return this;
		}

		@Override
		public BagStorage build() {
			if (!specialized) {
				return new Values(counts, size);
			}
			return switch (kind) {
				case INTEGERS -> new Integers(counts, size);
				case DOUBLES -> new Doubles(counts, size);
				case BOOLEANS -> new Booleans(counts);
				case VALUES -> new Values(counts, size);
			};
		}
	}

	private abstract static class Grouped implements BagStorage {
		final int size;
		final int[] offsets;
		final int hash;

		Grouped(Map<Value<?>, Integer> counts, int size) {
			this.size = size;
			this.offsets = new int[counts.size() + 1];
			int index = 0;
			int hash = 0;
			for (var entry : counts.entrySet()) {
				offsets[index + 1] = offsets[index] + entry.getValue();
				hash += entry.getKey().hashCode() ^ entry.getValue();
				index++;
			}
			this.hash = hash;
		}

		@Override public int size() { return size; }
		@Override public int distinctSize() { return offsets.length - 1; }
		@Override public Value<?> distinctValueAt(int index) {
			Objects.checkIndex(index, distinctSize());
			return valueAt(offsets[index]);
		}
		@Override public int distinctCountAt(int index) {
			Objects.checkIndex(index, distinctSize());
			return offsets[index + 1] - offsets[index];
		}
		@Override public int multisetHashCode() { return hash; }
	}

	private static final class Values extends Grouped {
		private final Value<?>[] values;

		Values(Map<Value<?>, Integer> counts, int size) {
			super(counts, size);
			values = new Value<?>[size];
			int index = 0;
			for (var entry : counts.entrySet()) {
				Arrays.fill(values, index, index + entry.getValue(), entry.getKey());
				index += entry.getValue();
			}
		}

		@Override public Value<?> valueAt(int index) { return values[index]; }
	}

	private static final class Integers extends Grouped {
		private final long[] values;
		private final Value<?> first;

		Integers(Map<Value<?>, Integer> counts, int size) {
			super(counts, size);
			values = new long[size];
			first = counts.isEmpty() ? null : counts.keySet().iterator().next();
			int index = 0;
			for (var entry : counts.entrySet()) {
				Arrays.fill(values, index, index + entry.getValue(), ((IntegerVal) entry.getKey()).get());
				index += entry.getValue();
			}
		}

		@Override public Value<?> valueAt(int index) {
			Objects.checkIndex(index, size);
			return index == 0 ? first : IntegerVal.of(values[index]);
		}
		@Override public int count(Value<?> value) {
			if (!(value instanceof IntegerVal integer)) return super.count(value);
			long target = integer.get();
			int count = 0;
			for (long candidate : values) if (candidate == target) count++;
			return count;
		}
		@Override public Scan equal(Value<?> literal) {
			return literal instanceof IntegerVal integer
					? compareIntegers(Comparison.EQUAL, integer.get()) : Scan.NONE;
		}
		@Override public Scan compareIntegers(Comparison comparison, long literal) {
			for (long value : values) {
				if (compare(comparison, literal, value)) return Scan.MATCH;
			}
			return Scan.NO_MATCH;
		}
	}

	private static final class Doubles extends Grouped {
		private final double[] values;
		private final Value<?> first;
		private volatile Set<Long> ieeeValues;

		Doubles(Map<Value<?>, Integer> counts, int size) {
			super(counts, size);
			values = new double[size];
			first = counts.isEmpty() ? null : counts.keySet().iterator().next();
			int index = 0;
			for (var entry : counts.entrySet()) {
				Arrays.fill(values, index, index + entry.getValue(), ((DoubleVal) entry.getKey()).get());
				index += entry.getValue();
			}
		}

		@Override public Value<?> valueAt(int index) {
			Objects.checkIndex(index, size);
			return index == 0 ? first : DoubleVal.of(values[index]);
		}
		@Override public int count(Value<?> value) {
			if (!(value instanceof DoubleVal number)) return super.count(value);
			long target = Double.doubleToLongBits(number.get());
			int count = 0;
			for (double candidate : values) if (Double.doubleToLongBits(candidate) == target) count++;
			return count;
		}
		@Override public Scan equal(Value<?> literal) {
			if (!(literal instanceof DoubleVal)) return Scan.NONE;
			return count(literal) != 0 ? Scan.MATCH : Scan.NO_MATCH;
		}
		@Override public Scan compareDoubles(Comparison comparison, double literal) {
			if (comparison == Comparison.EQUAL && size > LINEAR_LOOKUP_LIMIT) {
				if (Double.isNaN(literal)) return Scan.NO_MATCH;
				Set<Long> indexed = ieeeValues;
				if (indexed == null) {
					for (int i = 0; i < LINEAR_LOOKUP_LIMIT; i++) {
						if (literal == values[i]) return Scan.MATCH;
					}
					indexed = new HashSet<>();
					for (double value : values) if (!Double.isNaN(value)) indexed.add(doubleKey(value));
					ieeeValues = indexed;
				}
				return indexed.contains(doubleKey(literal)) ? Scan.MATCH : Scan.NO_MATCH;
			}
			for (double value : values) {
				if (compare(comparison, literal, value)) return Scan.MATCH;
			}
			return Scan.NO_MATCH;
		}
	}

	private static final class Booleans implements BagStorage {
		private final int trueCount;
		private final int falseCount;
		private final boolean trueFirst;
		private final Value<?> firstValue;
		private final int hash;

		Booleans(Map<Value<?>, Integer> counts) {
			var iterator = counts.entrySet().iterator();
			if (iterator.hasNext()) {
				var entry = iterator.next();
				firstValue = entry.getKey();
				trueFirst = ((BooleanVal) firstValue).get();
			} else {
				firstValue = null;
				trueFirst = false;
			}
			trueCount = counts.getOrDefault(BooleanVal.of(true), 0);
			falseCount = counts.getOrDefault(BooleanVal.of(false), 0);
			int computed = 0;
			for (var entry : counts.entrySet()) computed += entry.getKey().hashCode() ^ entry.getValue();
			hash = computed;
		}

		@Override public int size() { return trueCount + falseCount; }
		@Override public int distinctSize() { return (trueCount > 0 ? 1 : 0) + (falseCount > 0 ? 1 : 0); }
		@Override public Value<?> distinctValueAt(int index) {
			Objects.checkIndex(index, distinctSize());
			return valueAt(index == 0 ? 0 : (trueFirst ? trueCount : falseCount));
		}
		@Override public int distinctCountAt(int index) {
			Objects.checkIndex(index, distinctSize());
			return (index == 0) == trueFirst ? trueCount : falseCount;
		}
		@Override public int multisetHashCode() { return hash; }
		@Override public int count(Value<?> value) {
			if (!(value instanceof BooleanVal bool)) return BagStorage.super.count(value);
			return bool.get() ? trueCount : falseCount;
		}
		@Override public Value<?> valueAt(int index) {
			Objects.checkIndex(index, size());
			if (index == 0) return firstValue;
			int firstCount = trueFirst ? trueCount : falseCount;
			return BooleanVal.of(index < firstCount ? trueFirst : !trueFirst);
		}
		@Override public Scan equal(Value<?> literal) {
			if (!(literal instanceof BooleanVal value)) return Scan.NONE;
			boolean found = value.get() ? trueCount > 0 : falseCount > 0;
			return found ? Scan.MATCH : Scan.NO_MATCH;
		}
	}

	static long doubleKey(double value) {
		return value == 0.0 ? 0L : Double.doubleToLongBits(value);
	}

	static boolean compare(BagStorage.Comparison comparison, long left, long right) {
		return switch (comparison) {
			case EQUAL -> left == right;
			case LESS_THAN -> left < right;
			case LESS_OR_EQUAL -> left <= right;
			case GREATER_THAN -> left > right;
			case GREATER_OR_EQUAL -> left >= right;
		};
	}

	static boolean compare(BagStorage.Comparison comparison, double left, double right) {
		return switch (comparison) {
			case EQUAL -> left == right;
			case LESS_THAN -> left < right;
			case LESS_OR_EQUAL -> left <= right;
			case GREATER_THAN -> left > right;
			case GREATER_OR_EQUAL -> left >= right;
		};
	}
}
