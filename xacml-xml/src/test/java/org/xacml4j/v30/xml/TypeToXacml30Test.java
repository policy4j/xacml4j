package org.xacml4j.v30.xml;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.Path;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.types.ISO8601Time;

import com.google.common.collect.Iterables;

import java.time.Duration;

public class TypeToXacml30Test
{
	@Test
	public void xpathRoundTripPreservesPathAndCategory()
	{
		Path xpath = Path.xpath("//example:item", CategoryId.ACTION);
		AttributeValueType xml = TypeToXacml30.Types.XPATH.toXacml30(xpath);

		assertEquals("urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression", xml.getDataType());
		assertEquals("//example:item", Iterables.getOnlyElement(xml.getContent()));
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:action",
				xml.getOtherAttributes().get(TypeToXacml30.Types.XPATH_CATEGORY_ATTR_NAME));

		Path restored = (Path)TypeToXacml30.Types.XPATH.fromXacml30(xml);
		assertEquals(xpath, restored);
		assertEquals("//example:item", restored.getPath());
		assertEquals(CategoryId.ACTION, restored.getCategory().get());
		assertEquals(Content.PathType.XPATH, restored.getPathType());
	}

	@Test
	public void xpathWithoutCategoryOmitsCategoryAttribute()
	{
		Path xpath = Path.of("//example:item", Content.PathType.XPATH, null);
		AttributeValueType xml = TypeToXacml30.Types.XPATH.toXacml30(xpath);

		assertEquals("//example:item", Iterables.getOnlyElement(xml.getContent()));
		assertTrue(xml.getOtherAttributes().isEmpty());
	}

	@Test
	public void unzonedTimeRoundTripPreservesLexicalForm()
	{
		ISO8601Time time = ISO8601Time.of("23:00:00");
		AttributeValueType xml = TypeToXacml30.Types.TIME.toXacml30(time);
		assertEquals("23:00:00", Iterables.getOnlyElement(xml.getContent()));
		ISO8601Time restored = (ISO8601Time)TypeToXacml30.Types.TIME.fromXacml30(xml);
		assertEquals("23:00:00", restored.toXacmlString());
		assertFalse(restored.isTimeZoneDefined());
	}

	@Test
	public void entityRoundTripPreservesIssuersAndDuplicateValues()
	{
		Entity entity = Entity.builder().attribute(
				Attribute.builder("urn:example:label").issuer("issuer-a")
						.includeInResult(true)
						.value(XacmlTypes.STRING.ofAny("alpha"), XacmlTypes.STRING.ofAny("alpha"))
						.build(),
				Attribute.builder("urn:example:label").issuer("issuer-b")
						.value(XacmlTypes.STRING.ofAny("beta"))
						.build()).build();

		AttributeValueType xml = TypeToXacml30.Types.ENTITY.toXacml30(entity);
		Entity restored = (Entity)TypeToXacml30.Types.ENTITY.fromXacml30(xml);

		assertEquals(entity, restored);
		assertEquals(2, restored.attributes().size());
		assertEquals(2, restored.findValues("urn:example:label", XacmlTypes.STRING, "issuer-a").size());
		assertEquals((Object)XacmlTypes.STRING.ofAny("beta"),
				(Object)Iterables.getOnlyElement(restored.findValues("urn:example:label", XacmlTypes.STRING, "issuer-b")));
		assertTrue(Iterables.getOnlyElement(restored.find("urn:example:label", "issuer-a")).isIncludeInResult());
	}

	@Test
	public void dayTimeDurationRoundTripPreservesWholeSecondDuration()
	{
		ISO8601DayTimeDuration duration = ISO8601DayTimeDuration.of(Duration.ofSeconds(10));
		AttributeValueType xml = TypeToXacml30.Types.DAYTIMEDURATION.toXacml30(duration);

		assertEquals("P0DT0H0M10S", Iterables.getOnlyElement(xml.getContent()));
		assertEquals(duration, TypeToXacml30.Types.DAYTIMEDURATION.fromXacml30(xml));
	}

	@Test
	public void integerUsesXmlSchemaLexicalForm()
	{
		AttributeValueType xml = TypeToXacml30.Types.INTEGER.toXacml30(XacmlTypes.INTEGER.ofAny(56));
		assertEquals("56", Iterables.getOnlyElement(xml.getContent()));
		assertEquals(XacmlTypes.INTEGER.ofAny(56), TypeToXacml30.Types.INTEGER.fromXacml30(xml));
	}
}
