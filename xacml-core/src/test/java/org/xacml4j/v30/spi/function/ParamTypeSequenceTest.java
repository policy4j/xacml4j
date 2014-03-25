package org.xacml4j.v30.spi.function;

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
