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
 * You should have received a copy of the GNU General Lesser Public
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
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.ISO8601DateTime;
import org.xacml4j.v30.types.ISO8601Time;
import org.xacml4j.v30.types.Value;

final class BagStorages
{
	static final int LINEAR_LOOKUP_LIMIT = 8;

	/** The kinds of storage; a kind holds the values it accepts in their primitive form */
	enum Kind {
		VALUES, INTEGERS, DOUBLES, BOOLEANS;

		boolean accepts(Value<?> value) {
			return switch (this) {
				case INTEGERS -> value instanceof IntegerVal;
				case DOUBLES -> value instanceof DoubleVal;
				case BOOLEANS -> value instanceof BooleanVal;
				case VALUES -> true;
			};
		}
	}

	private BagStorages() {}

	static BagStorage.Builder builder(Kind kind) {
		return new Builder(kind);
	}

	/**
	 * A positive engine size, -1 for an engine empty bag, or 0 for custom storage.
	 * Zero also denotes an uninitialized cache, so it must delegate to the storage.
	 */
	static int cardinality(BagStorage storage) {
		if (storage instanceof Empty) return -1;
		if (storage instanceof Single) return 1;
		if (storage instanceof Values values) return values.size;
		if (storage instanceof Integers integers) return integers.size;
		if (storage instanceof Doubles doubles) return doubles.size;
		if (storage instanceof Booleans booleans) return booleans.trueCount + booleans.falseCount;
		return 0;
	}

	/**
	 * Collects the values. A bag of no value or of one value gets a storage without arrays, and
	 * the values of a larger bag are grouped at {@link #build()} (change optimize-pdp-evaluation,
	 * cycles X and AF).
	 */
	private static final class Builder implements BagStorage.Builder {
		private final Kind kind;
		/** The first value, until a second one arrives */
		private Value<?> first;
		/** All the values from the second one on, else null */
		private Value<?>[] values;
		private boolean specialized = true;
		private int size;

		private Builder(Kind kind) { this.kind = kind; }

		@Override
		public Builder add(Value<?> value) {
			Objects.requireNonNull(value);
			int nextSize = Math.addExact(size, 1);
			if (size == 0) {
				first = value;
			} else {
				if (values == null) {
					values = new Value<?>[4];
					values[0] = first;
				} else if (nextSize > values.length) {
					values = Arrays.copyOf(values, Math.max(nextSize, values.length + (values.length >> 1)));
				}
				values[size] = value;
			}
			size = nextSize;
			specialized &= kind.accepts(value);
			return this;
		}

		@Override
		public BagStorage build() {
			if (size == 0) {
				return Empty.of(kind);
			}
			if (size == 1) {
				return new Single(first);
			}
			Value<?>[] distinct;
			int[] counts;
			int distinctSize = 0;
			if (size <= LINEAR_LOOKUP_LIMIT) {
				distinct = new Value<?>[size];
				counts = new int[size];
				for (int i = 0; i < size; i++) {
					Value<?> value = values[i];
					int index = 0;
					while (index < distinctSize && !value.equals(distinct[index])) index++;
					if (index == distinctSize) distinct[distinctSize++] = value;
					counts[index]++;
				}
			} else {
				Map<Value<?>, Integer> grouped = new LinkedHashMap<>();
				for (int i = 0; i < size; i++) grouped.merge(values[i], 1, Math::addExact);
				distinctSize = grouped.size();
				distinct = new Value<?>[distinctSize];
				counts = new int[distinctSize];
				int index = 0;
				for (var entry : grouped.entrySet()) {
					distinct[index] = entry.getKey();
					counts[index++] = entry.getValue();
				}
			}
			if (!specialized) {
				return new Values(distinct, counts, distinctSize, size);
			}
			return switch (kind) {
				case INTEGERS -> new Integers(distinct, counts, distinctSize, size);
				case DOUBLES -> new Doubles(distinct, counts, distinctSize, size);
				case BOOLEANS -> new Booleans(distinct, counts, distinctSize);
				case VALUES -> new Values(distinct, counts, distinctSize, size);
			};
		}
	}

	/**
	 * No value, one storage per kind (cycle AF). It answers as the empty grouped storage of its
	 * kind did: a typed storage scans nothing for a literal of another class, the general one
	 * reports no match.
	 */
	private static final class Empty implements BagStorage {
		private static final Empty[] OF_KIND = Arrays.stream(Kind.values()).map(Empty::new).toArray(Empty[]::new);
		private final Kind kind;

		private Empty(Kind kind) { this.kind = kind; }

		static Empty of(Kind kind) { return OF_KIND[kind.ordinal()]; }

		@Override public int size() { return 0; }
		@Override public Value<?> valueAt(int index) { throw new IndexOutOfBoundsException(index); }
		@Override public int distinctSize() { return 0; }
		@Override public Value<?> distinctValueAt(int index) { throw new IndexOutOfBoundsException(index); }
		@Override public int distinctCountAt(int index) { throw new IndexOutOfBoundsException(index); }
		@Override public int multisetHashCode() { return 0; }
		@Override public int count(Value<?> value) { return 0; }
		@Override public Scan equal(Value<?> literal) { return kind.accepts(literal) ? Scan.NO_MATCH : Scan.NONE; }
	}

	/**
	 * One value, held directly (cycle AF). The defaults of {@link BagStorage} answer for it as
	 * the grouped storages do for one value: {@code count} and {@code equal} compare with
	 * {@link Value#equals(Object)}, which is the long, {@code Double.equals}, and boolean
	 * comparison of the typed storages, and a literal of another class scans nothing.
	 */
	private static final class Single implements BagStorage {
		private final Value<?> value;

		Single(Value<?> value) { this.value = value; }

		@Override public int size() { return 1; }
		@Override public Value<?> valueAt(int index) {
			Objects.checkIndex(index, 1);
			return value;
		}
		@Override public int distinctSize() { return 1; }
		@Override public Value<?> distinctValueAt(int index) {
			Objects.checkIndex(index, 1);
			return value;
		}
		@Override public int distinctCountAt(int index) {
			Objects.checkIndex(index, 1);
			return 1;
		}
		@Override public int multisetHashCode() { return value.hashCode() ^ 1; }
	}

	private abstract static class Grouped implements BagStorage {
		final int size;
		final int[] offsets;
		final int hash;

		Grouped(Value<?>[] distinct, int[] counts, int distinctSize, int size) {
			this.size = size;
			this.offsets = new int[distinctSize + 1];
			int hash = 0;
			for (int i = 0; i < distinctSize; i++) {
				offsets[i + 1] = offsets[i] + counts[i];
				hash += distinct[i].hashCode() ^ counts[i];
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

		Values(Value<?>[] distinct, int[] counts, int distinctSize, int size) {
			super(distinct, counts, distinctSize, size);
			if (distinctSize == size) {
				// Every value once: the distinct values, in order, are the values (cycle AF)
				values = distinct;
			} else {
				values = new Value<?>[size];
				for (int i = 0; i < distinctSize; i++) {
					Arrays.fill(values, offsets[i], offsets[i + 1], distinct[i]);
				}
			}
		}

		@Override public Value<?> valueAt(int index) { return values[index]; }
	}

	private static final class Integers extends Grouped {
		private final long[] values;
		private final Value<?> first;

		Integers(Value<?>[] distinct, int[] counts, int distinctSize, int size) {
			super(distinct, counts, distinctSize, size);
			values = new long[size];
			first = distinctSize == 0 ? null : distinct[0];
			for (int i = 0; i < distinctSize; i++) {
				Arrays.fill(values, offsets[i], offsets[i + 1], ((IntegerVal) distinct[i]).get());
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

		Doubles(Value<?>[] distinct, int[] counts, int distinctSize, int size) {
			super(distinct, counts, distinctSize, size);
			values = new double[size];
			first = distinctSize == 0 ? null : distinct[0];
			for (int i = 0; i < distinctSize; i++) {
				Arrays.fill(values, offsets[i], offsets[i + 1], ((DoubleVal) distinct[i]).get());
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

		Booleans(Value<?>[] distinct, int[] counts, int distinctSize) {
			firstValue = distinctSize == 0 ? null : distinct[0];
			trueFirst = distinctSize != 0 && ((BooleanVal) firstValue).get();
			int trues = 0;
			int falses = 0;
			int computed = 0;
			for (int i = 0; i < distinctSize; i++) {
				if (((BooleanVal) distinct[i]).get()) trues = counts[i];
				else falses = counts[i];
				computed += distinct[i].hashCode() ^ counts[i];
			}
			trueCount = trues;
			falseCount = falses;
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

	static BagStorage.Scan compareTemporal(BagStorage bag, BagStorage.Comparison comparison, Value<?> literal) {
		try {
			if (literal instanceof ISO8601Date date) {
				return compareTemporal(bag, comparison, date, ISO8601Date.class);
			}
			if (literal instanceof ISO8601DateTime dateTime) {
				return compareTemporal(bag, comparison, dateTime, ISO8601DateTime.class);
			}
			if (literal instanceof ISO8601Time time && time.isTimeZoneDefined()) {
				return compareTemporal(bag, comparison, time, ISO8601Time.class);
			}
			return BagStorage.Scan.NONE;
		} catch (Throwable failure) {
			return BagStorage.Scan.NONE;
		}
	}

	private static <T extends Value<?> & Comparable<T>> BagStorage.Scan compareTemporal(
			BagStorage bag, BagStorage.Comparison comparison, T literal, Class<T> type) {
		int size = bag.size();
		for (int i = 0; i < size; i++) {
			Value<?> value = bag.valueAt(i);
			if (value.getClass() != type || value instanceof ISO8601Time time && !time.isTimeZoneDefined()) {
				return BagStorage.Scan.NONE;
			}
		}
		for (int i = 0; i < size; i++) {
			if (compare(comparison, literal.compareTo(type.cast(bag.valueAt(i))), 0)) return BagStorage.Scan.MATCH;
		}
		return BagStorage.Scan.NO_MATCH;
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
