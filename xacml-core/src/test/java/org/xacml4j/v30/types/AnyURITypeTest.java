package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.collect.ImmutableList;


public class AnyURITypeTest
{
	private AnyURIType t1;
	private Types types;
	
	@Before
	public void init(){
		this.types = Types.builder().defaultTypes().create();
		this.t1 = AnyURIType.ANYURI;
	}

	@Test
	public void testEquals()
	{
		AttributeExp v0 = t1.fromAny("http://www.test.org");
		AttributeExp v1 = t1.fromAny("http://www.test.org");
		assertEquals(v0, v1);
		assertEquals(AnyURIType.ANYURI, new AnyURIExp(URI.create("test")).getType());
	}

	@Test
	public void toStringTest(){
		assertEquals("http://www.w3.org/2001/XMLSchema#anyURI", t1.toString());		
	}
	
	@Test
	public void testCreate(){
		AttributeExp v0 = t1.fromAny("http://www.test.org");
		AttributeExp v1 = t1.fromAny(URI.create("http://www.test.org"));
		assertEquals(v0, v1);
	}
	
	@Test
	public void testBagOf(){
		AttributeExp v0 = t1.fromAny("http://www.test.org");
		AttributeExp v1 = t1.fromAny(URI.create("http://www.test1.org"));
		BagOfAttributeExp b0 = AnyURIType.ANYURI.bagOf(v0, v1);
		BagOfAttributeExp b1 = AnyURIType.ANYURI.bagOf(ImmutableList.<AttributeExp>of(v0, v1));
		BagOfAttributeExp b2 = AnyURIType.ANYURI.bagOf("http://www.test.org", "http://www.test1.org");
		BagOfAttributeExp b3 = AnyURIType.ANYURI.bagOf(URI.create("http://www.test.org"), 
				URI.create("http://www.test1.org"));
		assertEquals(b0, b1);
		assertEquals(b1, b2);
		assertEquals(b2, b3);
		
	}
	
	@Test
	public void typeToStringCapability(){
		TypeToString capability = types.getCapability(AnyURIType.ANYURI, TypeToString.class);	
		AttributeExp v0 = t1.fromAny("http://www.test.org");
		assertEquals("http://www.test.org", capability.toString(v0));
		assertEquals(v0, capability.fromString("http://www.test.org"));
	}
	
	@Test
	public void testTypeAlias(){
		AttributeExpType t0 = types.getType("anyURI");
		assertEquals(AnyURIType.ANYURI, t0);
		assertNotNull(types.getCapability("anyURI", TypeToString.class));
		assertNotNull(types.getCapability("anyURI", TypeToXacml30.class));
	}
	
	@Test
	public void typeToXacml30Capability(){
		TypeToXacml30 capability = types.getCapability(AnyURIType.ANYURI, TypeToXacml30.class);	
		AttributeExp v0 = t1.fromAny("http://www.test.org");
		AttributeValueType xml = capability.toXacml30(types, v0);
		assertEquals(AnyURIType.ANYURI.getDataTypeId(), xml.getDataType());
		assertEquals("http://www.test.org", xml.getContent().get(0));
	}
}
