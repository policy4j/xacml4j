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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.policy.function.impl.BagFunctions;
import org.xacml4j.v30.policy.function.impl.SetFunctions;

class BagStorageCompatibilityTest
{
	private static final String FIXTURES = "/org/xacml4j/v30/bag-storage/";

	static Stream<Arguments> bags() {
		return Stream.of(
				Arguments.of("string", XacmlTypes.STRING, List.of(
						XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.ofAny("b"), XacmlTypes.STRING.ofAny("a"))),
				Arguments.of("integer", XacmlTypes.INTEGER, List.of(
						XacmlTypes.INTEGER.ofAny(2L), XacmlTypes.INTEGER.ofAny(3L), XacmlTypes.INTEGER.ofAny(2L))),
				Arguments.of("double", XacmlTypes.DOUBLE, List.of(
						XacmlTypes.DOUBLE.ofAny(0.0), XacmlTypes.DOUBLE.ofAny(-0.0),
						XacmlTypes.DOUBLE.ofAny(Double.NaN), XacmlTypes.DOUBLE.ofAny(0.0))),
				Arguments.of("boolean-false-first", XacmlTypes.BOOLEAN, List.of(
						XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(false))),
				Arguments.of("boolean-true-first", XacmlTypes.BOOLEAN, List.of(
						XacmlTypes.BOOLEAN.ofAny(true), XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(false))));
	}

	/** The seed bags cut or repeated to no value, one value, two values, and the sizes around the grouping threshold */
	@SuppressWarnings("unchecked")
	static Stream<Arguments> bagsOfTheSmallAndThresholdSizes() {
		return bags().flatMap(arguments -> {
			Object[] fixture = arguments.get();
			List<Value<?>> seed = (List<Value<?>>) fixture[2];
			return Stream.of(0, 1, 2, 8, 9).map(size -> {
				List<Value<?>> input = new ArrayList<>(size);
				for (int i = 0; i < size; i++) input.add(seed.get(i % seed.size()));
				return Arguments.of(fixture[0] + "-" + size, fixture[1], input);
			});
		});
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource({"bags", "bagsOfTheSmallAndThresholdSizes"})
	void groupedValuesAndHashMatchThePreviousMultiset(String name, ValueType type, List<Value<?>> input) {
		BagOfValues bag = type.bagBuilder().attributes(input).build();
		var expected = ImmutableMultiset.copyOf(input);
		assertEquals(expected.size(), bag.size());
		assertEquals(Objects.hash(expected, bag.getEvaluatesTo()), bag.hashCode());
		assertEquals(List.copyOf(expected), bag.stream().toList());
		assertEquals(expected, bag.values());
		for (Value<?> value : expected.elementSet()) {
			assertTrue(bag.contains(value));
			assertEquals(expected.count(value), ((com.google.common.collect.Multiset<?>) bag.values()).count(value));
		}
		if (input.isEmpty()) {
			assertTrue(bag.isEmpty());
			assertThrows(NoSuchElementException.class, bag::value);
			assertEquals(Optional.empty(), bag.single());
		} else {
			assertSame(input.get(0), bag.value());
			assertSame(input.get(0), bag.single().orElseThrow());
		}
		assertEquals(bag, type.bagBuilder().attributes(input.reversed()).build());
		assertEquals(bag, type.bagBuilder().attributes(ImmutableList.copyOf(input)).build());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource({"bags", "bagsOfTheSmallAndThresholdSizes"})
	void roundTripKeepsTheValuesAndTheHash(String name, ValueType type, List<Value<?>> input) throws Exception {
		BagOfValues expected = type.bagBuilder().attributes(input).build();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
			out.writeObject(expected);
		}
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			BagOfValues actual = (BagOfValues) in.readObject();
			assertEquals(expected, actual);
			assertEquals(expected.stream().toList(), actual.stream().toList());
			assertEquals(expected.hashCode(), actual.hashCode());
		}
	}

	@Test
	void aValueTypeCanSupplyItsOwnStorageBuilder() {
		Value<?> first = CustomType.INSTANCE.ofAny("first");
		Value<?> second = CustomType.INSTANCE.ofAny("second");
		BagOfValues bag = CustomType.INSTANCE.bagBuilder().attribute(first, second, second).build();
		assertEquals(List.of(second, second, first), bag.stream().toList());
		assertSame(second, bag.value());
		assertTrue(bag.contains(first));
	}

	@Test
	void largeBagsPreserveSetResults() {
		BagOfValues.Builder left = XacmlTypes.DOUBLE.bagBuilder();
		BagOfValues.Builder right = XacmlTypes.DOUBLE.bagBuilder();
		for (int i = 0; i < 256; i++) {
			left.value((double) i);
			right.value((double) (i + 128));
		}
		left.value(-0.0, Double.NaN);
		right.value(-0.0, Double.NaN);
		BagOfValues a = left.build();
		BagOfValues b = right.build();
		assertEquals(386, a.union(b).size());
		assertEquals(130, a.intersection(b).size());
		assertEquals(a, XacmlTypes.DOUBLE.bagBuilder().attributes(a).build());
		assertFalse(a.containsAll(b));
		assertTrue(a.containsAtLeastOneOf(b));
		assertEquals(386, SetFunctions.doubleUnion(a, b).size());
		assertEquals(129, SetFunctions.doubleIntersection(a, b).size());
		assertTrue(BagFunctions.doubleIsIn(XacmlTypes.DOUBLE.ofAny(0.0), b).get());
		assertFalse(BagFunctions.doubleIsIn(XacmlTypes.DOUBLE.ofAny(Double.NaN), b).get());
		assertTrue(SetFunctions.doubleAtLeastOneMemberOf(a, b).get());
		assertFalse(SetFunctions.doubleSubset(a, b).get());
		assertFalse(SetFunctions.doubleSetEquals(a, b).get());
	}

	private enum CustomType implements ValueType {
		INSTANCE;
		@Override public String getTypeId() { return "urn:test:custom"; }
		@Override public String getShortTypeId() { return "custom"; }
		@Override public Set<String> getTypeIdAliases() { return Set.of("custom"); }
		@SuppressWarnings("unchecked")
		@Override public <T extends Value<?>> T ofAny(Object value, Object... params) {
			return (T) new CustomValue(value.toString());
		}
		@Override public BagStorage.Builder bagStorageBuilder() {
			BagStorage.Builder delegate = BagStorage.values();
			return new BagStorage.Builder() {
				@Override public BagStorage.Builder add(Value<?> value) { delegate.add(value); return this; }
				@Override public BagStorage build() {
					BagStorage original = delegate.build();
					return new BagStorage() {
						@Override public int size() { return original.size(); }
						@Override public Value<?> valueAt(int i) { return original.valueAt(size() - 1 - i); }
						@Override public int distinctSize() { return original.distinctSize(); }
						@Override public Value<?> distinctValueAt(int i) { return original.distinctValueAt(distinctSize() - 1 - i); }
						@Override public int distinctCountAt(int i) { return original.distinctCountAt(distinctSize() - 1 - i); }
					};
				}
			};
		}
	}

	private static final class CustomValue extends Value.OtherValue<String> {
		private CustomValue(String value) { super(CustomType.INSTANCE, value); }
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("bags")
	void readsThePreviousClassStream(String name, ValueType type, List<Value<?>> input) throws Exception {
		BagOfValues expected = type.bagBuilder().attributes(input).build();
		try (InputStream fixture = getClass().getResourceAsStream(FIXTURES + name + ".ser");
				ObjectInputStream in = new ObjectInputStream(Objects.requireNonNull(fixture))) {
			BagOfValues actual = (BagOfValues) in.readObject();
			assertEquals(expected, actual);
			assertEquals(expected.stream().toList(), actual.stream().toList());
			assertEquals(expected.hashCode(), actual.hashCode());
		}
	}

	/** The previous-class fixture was compiled from commit 21858796 with Oracle GraalVM JDK 25.0.4. */
	@ParameterizedTest(name = "{0}")
	@MethodSource({"bags", "bagsOfTheSmallAndThresholdSizes"})
	void previousClassReadsTheCurrentStream(String name, ValueType type, List<Value<?>> input) throws Exception {
		BagOfValues expected = type.bagBuilder().attributes(input).build();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
			out.writeObject(expected);
		}
		byte[] oldClass;
		try (InputStream in = getClass().getResourceAsStream(FIXTURES + "BagOfValues-head.class.bin")) {
			oldClass = Objects.requireNonNull(in).readAllBytes();
		}
		ClassLoader loader = new ClassLoader(BagOfValues.class.getClassLoader()) {
			@Override
			protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
				if (!className.equals(BagOfValues.class.getName())) return super.loadClass(className, resolve);
				synchronized (getClassLoadingLock(className)) {
					Class<?> found = findLoadedClass(className);
					if (found == null) found = defineClass(className, oldClass, 0, oldClass.length);
					if (resolve) resolveClass(found);
					return found;
				}
			}
		};
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())) {
			@Override
			protected Class<?> resolveClass(ObjectStreamClass descriptor) throws java.io.IOException, ClassNotFoundException {
				return descriptor.getName().equals(BagOfValues.class.getName())
						? loader.loadClass(descriptor.getName()) : super.resolveClass(descriptor);
			}
		}) {
			Object old = in.readObject();
			assertEquals(expected.values(), old.getClass().getMethod("values").invoke(old));
			assertEquals(expected.getEvaluatesTo(), old.getClass().getMethod("getEvaluatesTo").invoke(old));
			assertEquals(expected.hashCode(), old.hashCode());
		}
	}
}
