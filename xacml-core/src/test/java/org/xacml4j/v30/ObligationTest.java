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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.xacml4j.v30.types.IntegerExp;

/**
 * @author Valdas Sevelis
 */
public class ObligationTest {

	private final Obligation o1;
	private final Obligation o2;
	private final Obligation o3;

	public ObligationTest() {
		AttributeAssignment attr1 = AttributeAssignment
				.builder()
				.category(Categories.ACTION)
				.id("actionId1")
				.value(IntegerExp.valueOf(1))
				.build();
		AttributeAssignment attr2 = AttributeAssignment
				.builder()
				.category(Categories.ACTION)
				.id("actionId1")
				.value(IntegerExp.valueOf(2))
				.build();
		AttributeAssignment attr3 = AttributeAssignment
				.builder()
				.category(Categories.ACTION)
				.id("actionId1")
				.value(IntegerExp.valueOf(3))
				.build();

		o1 = Obligation.builder("id1").attribute(attr1).attribute(attr2).build();
		o2 = Obligation.builder("id1").attribute(attr1).attribute(attr2).build();
		o3 = Obligation.builder("id1").attribute(attr3).build();
	}

	@Test
	public void testEquals() {
		assertThat(o1.equals(o1), is(true));
		assertThat(o1.equals(o2), is(true));
		assertThat(o1.equals(o3), is(false));
		assertThat(o1.equals(null), is(false));
	}

	@Test
	public void testHashCode() {
		assertThat(o1.hashCode(), equalTo(o2.hashCode()));
		assertThat(o1.hashCode(), not(equalTo(o3.hashCode())));
	}

	@Test
	public void testToString() {
		assertThat(o1.toString(), equalTo(o2.toString()));
		assertThat(o1.toString(), not(equalTo(o3.toString())));
	}
}
