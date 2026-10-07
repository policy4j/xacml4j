package org.xacml4j.v30.policy;

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
import org.xacml4j.v30.types.Value;

/**
 * @author Valdas Sevelis
 */
public class CombinerParameterTest {
	@Test
	public void testObjectMethods() {
		Value exp1 = org.xacml4j.v30.types.BooleanVal.of(true);
		Value exp2 = org.xacml4j.v30.types.BooleanVal.of(false);

		CombinerParameter p1 = new CombinerParameter("name1", exp1);
		CombinerParameter p2 = new CombinerParameter("name1", exp1);
		CombinerParameter p3 = new CombinerParameter("name1", exp2);


		assertTrue(p1.equals(p2));
		assertFalse(p1.equals(p3));
		assertFalse(p1.equals(exp1));
		assertFalse(p1.equals(null));

		assertEquals(p1.hashCode(), p2.hashCode());
		assertEquals(p1.toString(), p2.toString());

	}
}
