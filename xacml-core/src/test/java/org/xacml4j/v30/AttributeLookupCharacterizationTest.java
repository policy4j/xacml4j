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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The results of the attribute lookups that cycle B of change optimize-pdp-evaluation
 * rewrites without streams (design D6): same values, same order, duplicates kept, the same
 * issuer filtering, and the same collection behavior.
 */
public class AttributeLookupCharacterizationTest
{
	private static final String ID = "urn:xacml4j:test:label";

	/** Two attributes with one id: issuer a (string, integer, string, string again) and issuer b. */
	private static Entity entity()
	{
		return Entity.builder()
				.attribute(Attribute.builder(ID).issuer("issuer-a")
						.value(XacmlTypes.STRING.ofAny("x"))
						.value(XacmlTypes.INTEGER.ofAny(7))
						.value(XacmlTypes.STRING.ofAny("y"))
						.value(XacmlTypes.STRING.ofAny("x"))
						.build())
				.attribute(Attribute.builder(ID).issuer("issuer-b")
						.value(XacmlTypes.STRING.ofAny("z"))
						.build())
				.attribute(Attribute.builder("urn:xacml4j:test:other")
						.value(XacmlTypes.STRING.ofAny("other"))
						.build())
				.build();
	}

	@Test
	public void findValuesKeepsOrderAndDuplicatesAcrossAttributesOfAnyIssuer()
	{
		Collection<Value<?>> values = entity().findValues(ID, XacmlTypes.STRING);

		assertThat(values).containsExactly(XacmlTypes.STRING.ofAny("x"), XacmlTypes.STRING.ofAny("y"),
				XacmlTypes.STRING.ofAny("x"), XacmlTypes.STRING.ofAny("z")).inOrder();
	}

	@Test
	public void findValuesFiltersByIssuerAndType()
	{
		assertThat(entity().findValues(ID, XacmlTypes.STRING, "issuer-b")).containsExactly(XacmlTypes.STRING.ofAny("z"));
		assertThat(entity().findValues(ID, XacmlTypes.INTEGER, "issuer-a")).containsExactly(XacmlTypes.INTEGER.ofAny(7));
		assertThat(entity().findValues(ID, XacmlTypes.INTEGER, "issuer-b")).isEmpty();
		assertThat(entity().findValues("urn:xacml4j:test:missing", XacmlTypes.STRING)).isEmpty();
	}

	@Test
	public void findValuesReturnsAnUnmodifiableCollection()
	{
		Collection<Value<?>> values = entity().findValues(ID, XacmlTypes.STRING);
		Collection<Value<?>> single = entity().findValues(ID, XacmlTypes.STRING, "issuer-b");
		Collection<Value<?>> none = entity().findValues(ID, XacmlTypes.BOOLEAN);

		assertThrows(UnsupportedOperationException.class, () -> values.add(XacmlTypes.STRING.ofAny("w")));
		assertThrows(UnsupportedOperationException.class, () -> single.add(XacmlTypes.STRING.ofAny("w")));
		assertThrows(UnsupportedOperationException.class, () -> none.add(XacmlTypes.STRING.ofAny("w")));
	}

	@Test
	public void resolveBuildsTheBagOfTheFoundValues()
	{
		assertThat(entity().resolve(ID, XacmlTypes.STRING, null))
				.isEqualTo(Optional.of(XacmlTypes.STRING.bagBuilder().attributes(List.of(XacmlTypes.STRING.ofAny("x"),
						XacmlTypes.STRING.ofAny("y"), XacmlTypes.STRING.ofAny("x"), XacmlTypes.STRING.ofAny("z"))).build()));
		assertThat(entity().resolve(ID, XacmlTypes.BOOLEAN, null)).isEqualTo(Optional.empty());
	}

	/** The bag of a designator is built once per entity (cycle AF): resolving it again gives an equal bag, as the lookup by id, type, and issuer does */
	@Test
	public void resolvingADesignatorTwiceGivesEqualBags()
	{
		Entity entity = entity();
		AttributeDesignatorKey strings = designator(XacmlTypes.STRING, null);
		AttributeDesignatorKey integersOfA = designator(XacmlTypes.INTEGER, "issuer-a");
		AttributeDesignatorKey booleans = designator(XacmlTypes.BOOLEAN, null);

		Optional<BagOfValues> first = entity.resolve(strings);

		assertThat(first).isEqualTo(Optional.of(XacmlTypes.STRING.bagBuilder().attributes(List.of(XacmlTypes.STRING.ofAny("x"),
				XacmlTypes.STRING.ofAny("y"), XacmlTypes.STRING.ofAny("x"), XacmlTypes.STRING.ofAny("z"))).build()));
		assertThat(entity.resolve(strings)).isEqualTo(first);
		assertThat(entity.resolve(ID, XacmlTypes.STRING, null)).isEqualTo(first);
		// The category of the designator plays no part in the lookup
		assertThat(entity.resolve(designator(CategoryId.RESOURCE, XacmlTypes.STRING, null))).isEqualTo(first);
		assertThat(entity.resolve(integersOfA)).isEqualTo(Optional.of(XacmlTypes.INTEGER.ofAny(7).toBag()));
		assertThat(entity.resolve(integersOfA)).isEqualTo(Optional.of(XacmlTypes.INTEGER.ofAny(7).toBag()));
		assertThat(entity.resolve(booleans)).isEqualTo(Optional.empty());
		assertThat(entity.resolve(booleans)).isEqualTo(Optional.empty());
	}

	/** A category answers a designator of its own category with the bag its entity keeps, and nothing for another category (cycle AK) */
	@Test
	public void categoryResolvesADesignatorOfItsCategoryThroughItsEntity()
	{
		Entity entity = entity();
		Category category = Category.builder(CategoryId.SUBJECT_ACCESS).entity(entity).build();
		AttributeDesignatorKey strings = designator(XacmlTypes.STRING, null);
		AttributeDesignatorKey ofResource = AttributeDesignatorKey.builder().category(CategoryId.RESOURCE)
				.attributeId(ID).dataType(XacmlTypes.STRING).build();

		assertThat(category.resolve(strings)).isEqualTo(entity.resolve(strings));
		assertThat(category.resolve(strings)).isEqualTo(Optional.of(XacmlTypes.STRING.bagBuilder().attributes(List.of(
				XacmlTypes.STRING.ofAny("x"), XacmlTypes.STRING.ofAny("y"), XacmlTypes.STRING.ofAny("x"), XacmlTypes.STRING.ofAny("z"))).build()));
		assertThat(category.resolve(ofResource)).isEqualTo(Optional.empty());
	}

	private static AttributeDesignatorKey designator(ValueType type, String issuer)
	{
		return designator(CategoryId.SUBJECT_ACCESS, type, issuer);
	}

	private static AttributeDesignatorKey designator(CategoryId category, ValueType type, String issuer)
	{
		return AttributeDesignatorKey.builder().category(category)
				.attributeId(ID).dataType(type).issuer(issuer).build();
	}

	@Test
	public void hasValuesNeedsAValueOfTheTypeFromAnAttributeOfTheIssuer()
	{
		assertThat(entity().hasValues(ID, XacmlTypes.INTEGER, null)).isTrue();
		assertThat(entity().hasValues(ID, XacmlTypes.INTEGER, "issuer-b")).isFalse();
		assertThat(entity().hasValues(ID, XacmlTypes.STRING, "issuer-b")).isTrue();
		assertThat(entity().hasValues("urn:xacml4j:test:missing", XacmlTypes.STRING, null)).isFalse();
	}

	@Test
	public void findValueReturnsTheFirstMatchInAttributeOrder()
	{
		Optional<Value<?>> any = entity().findValue(ID, XacmlTypes.STRING, null);
		Optional<Value<?>> filtered = entity().findValue(ID, XacmlTypes.STRING, null, v -> v.equals(XacmlTypes.STRING.ofAny("z")));

		assertThat(any).isEqualTo(Optional.of(XacmlTypes.STRING.ofAny("x")));
		assertThat(filtered).isEqualTo(Optional.of(XacmlTypes.STRING.ofAny("z")));
		assertThat(entity().findValue(ID, XacmlTypes.BOOLEAN, null)).isEqualTo(Optional.empty());
	}

	@Test
	public void findByIssuerStillReturnsAModifiableList()
	{
		Collection<Attribute> found = entity().find(ID, "issuer-a");

		assertThat(found).hasSize(1);
		found.clear();
		assertThat(entity().find(ID, "issuer-a")).hasSize(1);
	}

	@Test
	public void requestEntityIsTheFirstEntityOfTheCategory()
	{
		Entity first = Entity.builder().attribute(Attribute.builder("a").value(XacmlTypes.STRING.ofAny("1")).build()).build();
		Entity second = Entity.builder().attribute(Attribute.builder("a").value(XacmlTypes.STRING.ofAny("2")).build()).build();
		RequestContext request = RequestContext.builder()
				.attributes(Category.builder(CategoryId.RESOURCE).entity(first).build(),
						Category.builder(CategoryId.RESOURCE).entity(second).build())
				.build();

		assertThat(request.getEntity(CategoryId.RESOURCE)).isEqualTo(Optional.of(first));
		assertThat(request.getEntity(CategoryId.ACTION)).isEqualTo(Optional.empty());
	}
}
