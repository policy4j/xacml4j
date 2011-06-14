package com.artagon.xacml.v30.marshall.jaxb;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import javax.xml.bind.JAXBElement;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.v30.jaxb.ApplyType;
import org.oasis.xacml.v30.jaxb.AttributeDesignatorType;
import org.oasis.xacml.v30.jaxb.AttributeSelectorType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;

import com.artagon.xacml.v30.Apply;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignator;
import com.artagon.xacml.v30.AttributeSelector;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.types.StringType;

public class Xacml30PolicyExpressionFromModelToJaxbMapperTest 
{
	private Xacml30PolicyExpressionFromModelToJaxbMapper mapper;
	
	private FunctionSpec func;
	private IMocksControl ctrl;
	
	@Before
	public void init(){
		this.ctrl = createControl();
		this.func = ctrl.createMock(FunctionSpec.class);
		this.mapper = new Xacml30PolicyExpressionFromModelToJaxbMapper();
	}
	
	@Test
	public void testMapAttributeValue()
	{
		JAXBElement<?> jaxb = mapper.create(StringType.STRING.create("Test"));
		AttributeValueType v = (AttributeValueType)jaxb.getValue();
		assertEquals("http://www.w3.org/2001/XMLSchema#string", v.getDataType());
		assertEquals("Test", v.getContent().get(0));
	}
	
	@Test
	public void testAttributeSelector()
	{
		JAXBElement<?> jaxb = mapper.create(
				new AttributeSelector(AttributeCategories.SUBJECT_ACCESS, "/Test", StringType.STRING, false));
		AttributeSelectorType v = (AttributeSelectorType)jaxb.getValue();
		assertEquals("http://www.w3.org/2001/XMLSchema#string", v.getDataType());
		assertEquals(AttributeCategories.SUBJECT_ACCESS.getId(), v.getCategory());
		assertEquals("/Test", v.getPath());
		assertFalse(v.isMustBePresent());
	}
	
	@Test
	public void testAttributeDesignator()
	{
		JAXBElement<?> jaxb = mapper.create(
				new AttributeDesignator(AttributeCategories.SUBJECT_ACCESS, "TestId", "Issuer", StringType.STRING, false));
		AttributeDesignatorType v = (AttributeDesignatorType)jaxb.getValue();
		assertEquals("http://www.w3.org/2001/XMLSchema#string", v.getDataType());
		assertEquals("TestId", v.getAttributeId());
		assertEquals(AttributeCategories.SUBJECT_ACCESS.getId(), v.getCategory());
		assertFalse(v.isMustBePresent());
	}
	
	@Test
	public void testApplyMapOneLevel() throws Exception
	{
		Expression exp0 = new AttributeDesignator(AttributeCategories.SUBJECT_ACCESS, "TestId", "Issuer", StringType.STRING, false);
		Expression exp1 = StringType.STRING.create("Test");
		expect(func.validateParameters(Arrays.asList(exp0, exp1))).andReturn(true);
		expect(func.getId()).andReturn("TestFunc");
		ctrl.replay();
		Expression exp2 = new Apply(func, exp0, exp1);
		JAXBElement<?> jaxb = mapper.create(exp2);
		ApplyType v = (ApplyType)jaxb.getValue();
		assertEquals("TestFunc", v.getFunctionId());
		ctrl.verify();
	}
}
