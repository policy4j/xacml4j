package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2022 Xacml4J.org
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

import java.util.ServiceLoader;

import org.junit.Test;
import org.xacml4j.v30.TypeCapability;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;

public class TypeToStringCapabilityTest 
{
	@Test
	public void testGetCapability() {
		Truth8.assertThat(TypeToString.forType(XacmlTypes.BOOLEAN)).isPresent();
		Truth8.assertThat(TypeToString.forType(XacmlTypes.ENTITY)).isEmpty();

	}
}
