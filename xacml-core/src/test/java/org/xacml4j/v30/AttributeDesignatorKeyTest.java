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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.XacmlTypes;

public class AttributeDesignatorKeyTest
{
	@Test
	public void builderAndConstructorPreserveValuesAndHash(){
		AttributeDesignatorKey.Builder builder = AttributeDesignatorKey.builder("urn:test:attribute")
				.category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.STRING)
				.issuer("issuer");
		AttributeDesignatorKey built = builder.build();
		AttributeDesignatorKey constructed = new AttributeDesignatorKey(builder);

		assertEquals(CategoryId.RESOURCE, built.getCategory());
		assertEquals(XacmlTypes.STRING, built.getDataType());
		assertEquals("urn:test:attribute", built.getAttributeId());
		assertEquals("issuer", built.getIssuer());
		assertEquals(constructed, built);
		assertEquals(built, constructed);
		assertEquals(built, built.toBuilder().build());
		assertEquals(built, built.withIssuer("issuer"));
		assertEquals(Objects.hash("urn:test:attribute", "issuer"), built.hashCode());
		assertEquals(built.hashCode(), constructed.hashCode());
	}

	@Test
	public void eachKeyFieldSeparatesValues(){
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("urn:test:attribute")
				.category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.STRING)
				.issuer("issuer")
				.build();
		AttributeDesignatorKey category = key.toBuilder().category(CategoryId.ACTION).build();
		AttributeDesignatorKey type = key.toBuilder().dataType(XacmlTypes.INTEGER).build();
		AttributeDesignatorKey id = key.toBuilder().attributeId("urn:test:other").build();
		AttributeDesignatorKey issuer = key.withIssuer("other");

		assertEquals(CategoryId.ACTION, category.getCategory());
		assertEquals(XacmlTypes.INTEGER, type.getDataType());
		assertEquals("urn:test:other", id.getAttributeId());
		assertEquals("other", issuer.getIssuer());
		for(AttributeDesignatorKey different : new AttributeDesignatorKey[]{category, type, id, issuer}){
			assertNotEquals(key, different);
			assertNotEquals(different, key);
		}
		assertEquals(key.hashCode(), category.hashCode());
		assertEquals(key.hashCode(), type.hashCode());
	}

	@Test
	public void emptyIssuerRemainsAbsent(){
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("urn:test:attribute")
				.category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.STRING)
				.issuer("issuer")
				.build();
		AttributeDesignatorKey withoutIssuer = key.withIssuer("");
		AttributeDesignatorKey constructed = new AttributeDesignatorKey(AttributeDesignatorKey.builder("urn:test:attribute")
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING));

		assertNull(withoutIssuer.getIssuer());
		assertEquals(constructed, withoutIssuer);
		assertEquals(Objects.hash("urn:test:attribute", null), withoutIssuer.hashCode());
	}

	@Test
	public void keysWithoutCategoryKeepTheirValues(){
		AttributeDesignatorKey.Builder builder = AttributeDesignatorKey.builder("urn:test:attribute")
				.dataType(XacmlTypes.INTEGER)
				.issuer("issuer");
		for(AttributeDesignatorKey key : new AttributeDesignatorKey[]{builder.build(), builder.build(), new AttributeDesignatorKey(builder)}){
			assertTrue(key.getCategoryOpt().isEmpty());
			assertEquals(XacmlTypes.INTEGER, key.getDataType());
			assertEquals("urn:test:attribute", key.getAttributeId());
			assertEquals("issuer", key.getIssuer());
			assertEquals(Objects.hash("urn:test:attribute", "issuer"), key.hashCode());
			assertTrue(key.toString().contains("Category=null"));
		}
		assertEquals(builder.build(), builder.build());
	}

	@Test
	public void builderStillValidatesRequiredFields(){
		assertThrows(IllegalArgumentException.class, () -> AttributeDesignatorKey.builder("")
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build());
		assertThrows(NullPointerException.class, () -> AttributeDesignatorKey.builder("urn:test:attribute")
				.category(CategoryId.RESOURCE).build());
	}
}
