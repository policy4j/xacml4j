package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.SyntaxException;

class ValueTypeApplyTest
{
	@Test
	void oneElementStringArrayConvertsItsContainedValue(){
		assertEquals(StringVal.of("value"), XacmlTypes.STRING.apply(new Object[]{"value"}));
	}

	@Test
	void oneElementIntegerArrayConvertsItsContainedValue(){
		assertEquals(IntegerVal.of(42L), XacmlTypes.INTEGER.apply(new Object[]{42L}));
	}

	@Test
	void oneElementBooleanArrayConvertsItsContainedValue(){
		assertEquals(BooleanVal.of(true), XacmlTypes.BOOLEAN.apply(new Object[]{true}));
	}

	@Test
	void oneElementArrayConvertsAContainedValueInstance(){
		StringVal value = StringVal.of("value");

		assertSame(value, XacmlTypes.STRING.apply(new Object[]{value}));
	}

	@Test
	void scalarInputKeepsTheFactoryConversion(){
		assertEquals(XacmlTypes.STRING.ofAny("value"), XacmlTypes.STRING.apply("value"));
	}

	@Test
	void emptyArrayRemainsInvalid(){
		assertThrows(SyntaxException.class, () -> XacmlTypes.STRING.apply(new Object[0]));
	}

	@Test
	void nullContainerRemainsInvalid(){
		assertThrows(SyntaxException.class, () -> XacmlTypes.STRING.apply(null));
	}

	@Test
	void multiArgumentArrayStillForwardsFactoryParameters(){
		assertEquals(XacmlTypes.XPATH.ofAny("/test", CategoryId.SUBJECT_RECIPIENT),
				XacmlTypes.XPATH.apply(new Object[]{"/test", CategoryId.SUBJECT_RECIPIENT}));
	}
}
