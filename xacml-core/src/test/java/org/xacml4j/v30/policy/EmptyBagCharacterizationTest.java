package org.xacml4j.v30.policy;

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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.DynamicType;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Cycle H of change optimize-pdp-evaluation (design D14): the empty bag of a type, and the bag
 * that an attribute reference evaluates to, keep today's type, equality, hash, serialized form,
 * and failures; one empty bag is shared per type.
 */
public class EmptyBagCharacterizationTest
{
	private static final ValueType[] TYPES = {
			XacmlTypes.STRING, XacmlTypes.INTEGER, XacmlTypes.DOUBLE, XacmlTypes.BOOLEAN,
			XacmlTypes.ANYURI, XacmlTypes.DATETIME, XacmlTypes.ENTITY};

	@Test
	public void emptyBagsKeepTypeEqualityHashAndSerializedForm() throws Exception
	{
		for(ValueType type : TYPES){
			BagOfValues empty = type.emptyBag();
			BagOfValues built = type.bagBuilder().build();

			assertThat(empty.isEmpty()).isTrue();
			assertThat(empty.getBagValueType()).isEqualTo(type);
			assertThat(empty.getEvaluatesTo()).isEqualTo(type.bagType());
			assertThat(empty).isEqualTo(built);
			assertThat(empty.hashCode()).isEqualTo(built.hashCode());
			assertThat(type.bagType().emptyBag()).isEqualTo(built);
			assertThat(roundTrip(empty)).isEqualTo(built);
		}
		assertThat(XacmlTypes.STRING.emptyBag()).isNotEqualTo(XacmlTypes.INTEGER.emptyBag());
	}

	@Test
	public void dynamicTypeHasNoEmptyBag()
	{
		assertThrows(IllegalArgumentException.class, () -> DynamicType.INSTANCE.emptyBag());
		assertThrows(IllegalArgumentException.class, () -> DynamicType.INSTANCE.emptyBag());
	}

	@Test
	public void referenceEvaluatesToTheResolvedBagOrAnEmptyBag() throws EvaluationException
	{
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("urn:xacml4j:test:id")
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.INTEGER).build();
		AttributeDesignator optional = AttributeDesignator.builder().key(key).mustBePresent(false).build();
		AttributeDesignator required = AttributeDesignator.builder().key(key).mustBePresent(true).build();
		BagOfValues found = XacmlTypes.INTEGER.bagBuilder().attribute(XacmlTypes.INTEGER.ofAny(7)).build();
		EvaluationContext present = mock(EvaluationContext.class);
		when(present.resolve(key)).thenReturn(Optional.of(found));
		EvaluationContext absent = mock(EvaluationContext.class);
		when(absent.resolve(key)).thenReturn(Optional.empty());

		assertSame(found, optional.evaluate(present));
		assertThat(optional.evaluate(absent)).isEqualTo(XacmlTypes.INTEGER.bagBuilder().build());
		assertThat(optional.evaluate(absent).getBagValueType()).isEqualTo(XacmlTypes.INTEGER);
		assertThrows(AttributeReferenceEvaluationException.class, () -> required.evaluate(absent));
	}

	/** The optimization: one empty bag per type (failing before D14). */
	@Test
	public void oneEmptyBagIsSharedPerType()
	{
		for(ValueType type : TYPES){
			assertSame(type.emptyBag(), type.emptyBag());
			assertSame(type.emptyBag(), type.bagType().emptyBag());
		}
	}

	private static BagOfValues roundTrip(BagOfValues bag) throws Exception
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try(ObjectOutputStream out = new ObjectOutputStream(bytes)){
			out.writeObject(bag);
		}
		try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){
			return (BagOfValues) in.readObject();
		}
	}

	/** Review RVH1: a value type that is not built in keeps its own bagBuilder() and is not shared. */
	@Test
	public void customTypeKeepsItsBagBuilderAndIsNotShared()
	{
		ValueType rejecting = mock(ValueType.class, org.mockito.Mockito.withSettings().defaultAnswer(org.mockito.Mockito.CALLS_REAL_METHODS));
		org.mockito.Mockito.doThrow(new IllegalStateException("no bags of this type")).when(rejecting).bagBuilder();
		ValueType building = mock(ValueType.class, org.mockito.Mockito.withSettings().defaultAnswer(org.mockito.Mockito.CALLS_REAL_METHODS));
		org.mockito.Mockito.doAnswer(invocation -> XacmlTypes.STRING.bagBuilder()).when(building).bagBuilder();

		assertThrows(IllegalStateException.class, rejecting::emptyBag);
		assertThat(building.emptyBag()).isEqualTo(XacmlTypes.STRING.bagBuilder().build());
		org.junit.jupiter.api.Assertions.assertNotSame(building.emptyBag(), building.emptyBag());
	}

	/** Review RVH1: a present attribute never asks its type for an empty bag (no eager orElse). */
	@Test
	public void presentAttributeDoesNotBuildAnEmptyBag() throws EvaluationException
	{
		ValueType counting = mock(ValueType.class, org.mockito.Mockito.withSettings().defaultAnswer(org.mockito.Mockito.CALLS_REAL_METHODS));
		org.mockito.Mockito.doReturn("urn:xacml4j:test:counting-type").when(counting).getTypeId();
		org.mockito.Mockito.doThrow(new AssertionError("empty bag built for a present attribute")).when(counting).emptyBag();
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("urn:xacml4j:test:id")
				.category(CategoryId.RESOURCE).dataType(counting).build();
		AttributeDesignator designator = AttributeDesignator.builder().key(key).mustBePresent(false).build();
		BagOfValues found = XacmlTypes.STRING.bagBuilder().attribute(XacmlTypes.STRING.ofAny("x")).build();
		EvaluationContext present = mock(EvaluationContext.class);
		when(present.resolve(key)).thenReturn(Optional.of(found));

		assertSame(found, designator.evaluate(present));
	}

	/**
	 * Review RVH2: shared bags are hashed from many threads; every thread sees the hash of an equal
	 * bag. A stress test only: a data race cannot be forced to show, so the fix (one read of the
	 * cache field) is checked in review, not by a red run.
	 */
	@Test
	public void concurrentFirstHashesOfOneBagAgree() throws Exception
	{
		int expected = XacmlTypes.STRING.bagBuilder().attribute(XacmlTypes.STRING.ofAny("x")).build().hashCode();
		java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
		try{
			for(int i = 0; i < 2_000; i++){
				BagOfValues bag = XacmlTypes.STRING.bagBuilder().attribute(XacmlTypes.STRING.ofAny("x")).build();
				java.util.concurrent.CyclicBarrier start = new java.util.concurrent.CyclicBarrier(2);
				java.util.concurrent.Callable<Integer> hash = () -> {
					start.await();
					return bag.hashCode();
				};
				java.util.concurrent.Future<Integer> first = pool.submit(hash);
				java.util.concurrent.Future<Integer> second = pool.submit(hash);
				assertThat(first.get()).isEqualTo(expected);
				assertThat(second.get()).isEqualTo(expected);
			}
		}finally{
			pool.shutdownNow();
		}
	}
}
