package org.xacml4j.v30.spi.function;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;


public class ParamTypeSequenceTest
{
	private FunctionParamValueTypeSequenceSpec specAttrZeroOrMore;
	private FunctionParamValueTypeSequenceSpec specAttrOneOrMore;
	private FunctionParamValueTypeSequenceSpec specAttrFromOneToFour;

	@Before
	public void setUp() throws Exception
	{
		this.specAttrZeroOrMore = new FunctionParamValueTypeSequenceSpec(0, Integer.MAX_VALUE, XacmlTypes.STRING);
		this.specAttrOneOrMore = new FunctionParamValueTypeSequenceSpec(1, Integer.MAX_VALUE, XacmlTypes.STRING);
		this.specAttrFromOneToFour = new FunctionParamValueTypeSequenceSpec(1, 4, XacmlTypes.STRING);
	}

	@Test
	public void testVarArgWithZeroOrMore() throws Exception
	{
		List<Expression> p = new LinkedList<Expression>();
		assertTrue(specAttrZeroOrMore.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(DoubleExp.valueOf(0.1));
		assertFalse(specAttrZeroOrMore.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("1"));
		assertTrue(specAttrZeroOrMore.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("2"));
		p.add(StringExp.valueOf("3"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("2"));
		p.add(StringExp.valueOf("3"));
		p.add(StringExp.valueOf("4"));
		p.add(StringExp.valueOf("5"));
		p.add(StringExp.valueOf("6"));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
	}

	@Test
	public void testWithOneToFour() throws Exception
	{
		List<Expression> p = new LinkedList<Expression>();
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(DoubleExp.valueOf(0.1));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("1"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("2"));
		p.add(StringExp.valueOf("3"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("2"));
		p.add(StringExp.valueOf("3"));
		p.add(StringExp.valueOf("4"));
		p.add(StringExp.valueOf("5"));
		p.add(StringExp.valueOf("6"));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
	}

	@Test
	public void testVarArgWithOneOrMore() throws Exception
	{
		List<Expression> p = new LinkedList<Expression>();
		assertFalse(specAttrOneOrMore.validate(p.listIterator()));
		p.add(StringExp.valueOf("1"));
		p.add(StringExp.valueOf("2"));
		p.add(StringExp.valueOf("3"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("1"));
		p.add(StringExp.valueOf("2"));
		p.add(DoubleExp.valueOf(0.1));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("1"));
		p.add(StringExp.valueOf("2"));
		p.add(StringExp.valueOf("3"));
		p.add(StringExp.valueOf("4"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(StringExp.valueOf("1"));
		p.add(StringExp.valueOf("2"));
		p.add(StringExp.valueOf("3"));
		p.add(StringExp.valueOf("4"));
		p.add(StringExp.valueOf("5"));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
	}
}
