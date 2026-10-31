package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Today's behavior of {@link BagOfValues}, which cycle C of change optimize-pdp-evaluation
 * must keep (design D7, review rv005): the iteration order groups equal values in the order of
 * their first occurrence, {@code values()} is a {@link Multiset}, and bag and set operations,
 * equality, hash, and serialization behave as below.
 */
public class BagOfValuesCharacterizationTest
{
	private static final Value<?> A = XacmlTypes.STRING.ofAny("a");
	private static final Value<?> B = XacmlTypes.STRING.ofAny("b");
	private static final Value<?> C = XacmlTypes.STRING.ofAny("c");

	private static BagOfValues bag(Value<?>... values)
	{
		return XacmlTypes.STRING.bagBuilder().attribute(values).build();
	}

	private static List<Value<?>> iteration(BagOfValues bag)
	{
		List<Value<?>> out = new ArrayList<>();
		for(Object v : bag.values()){
			out.add((Value<?>) v);
		}
		return out;
	}

	@Test
	public void iterationGroupsEqualValuesInFirstOccurrenceOrder()
	{
		BagOfValues bag = bag(A, B, A, C, B);

		assertThat(iteration(bag)).containsExactly(A, A, B, B, C).inOrder();
		assertThat(bag.stream().toList()).containsExactly(A, A, B, B, C).inOrder();
		assertThat((Object) bag.value()).isEqualTo(A);
		assertThat(bag.single().get()).isEqualTo(A);
		assertThat(bag.size()).isEqualTo(5);
	}

	@Test
	public void valuesIsAnUnmodifiableMultiset()
	{
		BagOfValues bag = bag(A, B, A);

		assertThat(bag.values()).isInstanceOf(Multiset.class);
		assertThat(((Multiset<?>) bag.values()).count(A)).isEqualTo(2);
		assertThat(bag.values().equals(HashMultiset.create(List.of(A, A, B)))).isTrue();
		assertThat(bag.values().equals(HashMultiset.create(List.of(A, B)))).isFalse();
		assertThrows(UnsupportedOperationException.class, () -> bag.values().add(C));
	}

	@Test
	public void equalityAndHashIgnoreOrderAndCountDuplicates()
	{
		assertThat(bag(A, B, A)).isEqualTo(bag(B, A, A));
		assertThat(bag(A, B, A).hashCode()).isEqualTo(bag(B, A, A).hashCode());
		assertThat(bag(A, B, A)).isNotEqualTo(bag(A, B));
		assertThat(bag()).isEqualTo(XacmlTypes.STRING.emptyBag());
		assertThat(bag(A)).isNotEqualTo(XacmlTypes.INTEGER.bagBuilder().attribute(XacmlTypes.INTEGER.ofAny(1)).build());
	}

	@Test
	public void setOperationsDropDuplicatesAndContainsAllIgnoresCounts()
	{
		assertThat(bag(A, A, B).union(bag(B, C))).isEqualTo(bag(A, B, C));
		assertThat(bag(A, A, B).intersection(bag(B, B, C))).isEqualTo(bag(B));
		assertThat(bag(A).containsAll(bag(A, A))).isTrue();
		assertThat(bag(A, B).containsAtLeastOneOf(bag(C, B))).isTrue();
		assertThat(bag(A, B).containsAtLeastOneOf(bag(C))).isFalse();
		assertThat(bag(A, B).contains(B)).isTrue();
		assertThat(bag().isEmpty()).isTrue();
	}

	@Test
	public void doubleBagsKeepTodaysNaNAndSignedZeroBehavior()
	{
		BagOfValues nan = XacmlTypes.DOUBLE.bagBuilder().attribute(XacmlTypes.DOUBLE.ofAny(Double.NaN)).build();
		BagOfValues zeros = XacmlTypes.DOUBLE.bagBuilder()
				.attribute(XacmlTypes.DOUBLE.ofAny(0.0d), XacmlTypes.DOUBLE.ofAny(-0.0d)).build();

		assertThat(nan.contains(XacmlTypes.DOUBLE.ofAny(Double.NaN))).isEqualTo(
				XacmlTypes.DOUBLE.ofAny(Double.NaN).equals(XacmlTypes.DOUBLE.ofAny(Double.NaN)));
		assertThat(zeros.size()).isEqualTo(2);
		assertThat(zeros.union(zeros).size()).isEqualTo(
				XacmlTypes.DOUBLE.ofAny(0.0d).equals(XacmlTypes.DOUBLE.ofAny(-0.0d)) ? 1 : 2);
	}

	@Test
	public void serializationRoundTripKeepsEqualityAndOrder() throws Exception
	{
		BagOfValues bag = bag(A, B, A, C);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try(ObjectOutputStream out = new ObjectOutputStream(bytes)){
			out.writeObject(bag);
		}
		BagOfValues copy;
		try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){
			copy = (BagOfValues) in.readObject();
		}

		assertThat(copy).isEqualTo(bag);
		assertThat(iteration(copy)).containsExactly(A, A, B, C).inOrder();
	}
}
