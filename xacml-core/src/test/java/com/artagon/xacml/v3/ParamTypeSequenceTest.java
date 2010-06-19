package com.artagon.xacml.v3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class ParamTypeSequenceTest
{
	private StringType t1;
	private DoubleType t2;
	private ParamValueTypeSequenceSpec specAttrZeroOrMore;
	private ParamValueTypeSequenceSpec specAttrOneOrMore;
	private ParamValueTypeSequenceSpec specAttrFromOneToFour;
	
	@Before
	public void setUp() throws Exception
	{
		this.t1 = XacmlDataTypes.STRING.getType();
		this.t2 = XacmlDataTypes.DOUBLE.getType();
		this.specAttrZeroOrMore = new ParamValueTypeSequenceSpec(0, Integer.MAX_VALUE, t1); 
		this.specAttrOneOrMore = new ParamValueTypeSequenceSpec(1, Integer.MAX_VALUE, t1);
		this.specAttrFromOneToFour = new ParamValueTypeSequenceSpec(1, 4, t1);	
	}
	
	@Test
	public void testVarArgWithZeroOrMore() throws Exception
	{
		List<Expression> p = new LinkedList<Expression>();
		assertTrue(specAttrZeroOrMore.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t2.create(0.1));
		assertFalse(specAttrZeroOrMore.validate(p.listIterator()));	
		p = new LinkedList<Expression>();
		p.add(t1.create("1"));
		assertTrue(specAttrZeroOrMore.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t1.create("2"));
		p.add(t1.create("3"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t1.create("2"));
		p.add(t1.create("3"));
		p.add(t1.create("4"));
		p.add(t1.create("5"));
		p.add(t1.create("6"));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
	}
	
	@Test
	public void testWithOneToFour() throws Exception
	{
		List<Expression> p = new LinkedList<Expression>();
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t2.create(0.1));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));	
		p = new LinkedList<Expression>();
		p.add(t1.create("1"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t1.create("2"));
		p.add(t1.create("3"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t1.create("2"));
		p.add(t1.create("3"));
		p.add(t1.create("4"));
		p.add(t1.create("5"));
		p.add(t1.create("6"));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
	}
	
	@Test
	public void testVarArgWithOneOrMore() throws Exception
	{
		List<Expression> p = new LinkedList<Expression>();
		assertFalse(specAttrOneOrMore.validate(p.listIterator()));
		p.add(t1.create("1"));
		p.add(t1.create("2"));
		p.add(t1.create("3"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));	
		p = new LinkedList<Expression>();
		p.add(t1.create("1"));
		p.add(t1.create("2"));
		p.add(t2.create(0.1));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t1.create("1"));
		p.add(t1.create("2"));
		p.add(t1.create("3"));
		p.add(t1.create("4"));
		assertTrue(specAttrFromOneToFour.validate(p.listIterator()));
		p = new LinkedList<Expression>();
		p.add(t1.create("1"));
		p.add(t1.create("2"));
		p.add(t1.create("3"));
		p.add(t1.create("4"));
		p.add(t1.create("5"));
		assertFalse(specAttrFromOneToFour.validate(p.listIterator()));
	}
}
