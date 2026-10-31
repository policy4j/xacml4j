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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.Value;

/**
 * Immutable bag contents, grouped by equal value in order of first occurrence.
 * Indexes for {@link #valueAt(int)} include every occurrence; distinct indexes
 * address one value and its count. A type can supply its own builder through
 * {@link org.xacml4j.v30.types.ValueType#bagStorageBuilder()}.
 */
public interface BagStorage extends Iterable<Value<?>>
{
	int size();
	Value<?> valueAt(int index);
	int distinctSize();
	Value<?> distinctValueAt(int index);
	int distinctCountAt(int index);

	default int count(Value<?> value) {
		for (int i = 0; i < distinctSize(); i++) {
			if (distinctValueAt(i).equals(value)) {
				return distinctCountAt(i);
			}
		}
		return 0;
	}

	default int multisetHashCode() {
		int hash = 0;
		for (int i = 0; i < distinctSize(); i++) {
			hash += distinctValueAt(i).hashCode() ^ distinctCountAt(i);
		}
		return hash;
	}

	/** Compares values with {@link Value#equals(Object)}, with the literal first. */
	default Scan equal(Value<?> literal) {
		for (int i = 0; i < size(); i++) {
			if (valueAt(i).getClass() != literal.getClass()) return Scan.NONE;
		}
		for (int i = 0; i < size(); i++) {
			if (literal.equals(valueAt(i))) return Scan.MATCH;
		}
		return Scan.NO_MATCH;
	}

	/** Uses IEEE comparison for doubles, including NaN and signed zero. */
	default Scan compareDoubles(Comparison comparison, double literal) {
		for (int i = 0; i < size(); i++) {
			if (!(valueAt(i) instanceof DoubleVal)) return Scan.NONE;
		}
		for (int i = 0; i < size(); i++) {
			if (BagStorages.compare(comparison, literal, ((DoubleVal) valueAt(i)).get())) return Scan.MATCH;
		}
		return Scan.NO_MATCH;
	}

	default Scan compareIntegers(Comparison comparison, long literal) {
		for (int i = 0; i < size(); i++) {
			if (!(valueAt(i) instanceof IntegerVal)) return Scan.NONE;
		}
		for (int i = 0; i < size(); i++) {
			if (BagStorages.compare(comparison, literal, ((IntegerVal) valueAt(i)).get())) return Scan.MATCH;
		}
		return Scan.NO_MATCH;
	}

	/** Uses the temporal value's comparison, literal first; unsupported values or failures return NONE. */
	default Scan compareTemporal(Comparison comparison, Value<?> literal) {
		return BagStorages.compareTemporal(this, comparison, literal);
	}

	@Override
	default Iterator<Value<?>> iterator() {
		return new Iterator<>() {
			private int index;
			public boolean hasNext() { return index < size(); }
			public Value<?> next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return valueAt(index++);
			}
		};
	}

	enum Comparison { EQUAL, LESS_THAN, LESS_OR_EQUAL, GREATER_THAN, GREATER_OR_EQUAL }
	enum Scan { MATCH, NO_MATCH, NONE }

	interface Builder {
		/** Adds a value and returns this builder. */
		Builder add(Value<?> value);
		default Builder addAll(Iterable<? extends Value<?>> values) {
			for (Value<?> value : values) add(value);
			return this;
		}
		/** Returns an immutable snapshot; later additions do not change it. */
		BagStorage build();
	}

	static Builder values() { return BagStorages.builder(BagStorages.Kind.VALUES); }
	static Builder integers() { return BagStorages.builder(BagStorages.Kind.INTEGERS); }
	static Builder doubles() { return BagStorages.builder(BagStorages.Kind.DOUBLES); }
	static Builder booleans() { return BagStorages.builder(BagStorages.Kind.BOOLEANS); }
}
