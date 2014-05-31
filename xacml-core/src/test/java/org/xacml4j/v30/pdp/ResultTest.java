package org.xacml4j.v30.pdp;

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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.types.StringExp;

import com.google.common.collect.ImmutableList;


public class ResultTest
{
	private Obligation denyObligationWithId1;
	private Obligation denyObligationWithId2;
	private Obligation denyObligationWithSameId1;

	@Before
	public void init(){
		this.denyObligationWithId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringExp.valueOf("v1"))
				.build();
		this.denyObligationWithId2 = Obligation
				.builder("id2", Effect.DENY)
				.attribute("test1", StringExp.valueOf("v1"))
				.build();
		this.denyObligationWithSameId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringExp.valueOf("v2"))
				.build();
	}

	@Test
	public void testMergeObligationValues()
	{
		Result.Builder b = Result.ok(Decision.DENY);
		b.obligation(denyObligationWithId1);
		b.obligation(ImmutableList.of(denyObligationWithId2, denyObligationWithSameId1));
		Result r = b.build();
		assertEquals(Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringExp.valueOf("v1"))
				.attribute("test1", StringExp.valueOf("v2"))
				.build(), r.getObligation("id1"));
		assertEquals(Obligation
				.builder("id2", Effect.DENY)
				.attribute("test1", StringExp.valueOf("v1"))
				.build(), r.getObligation("id2"));

	}
}
