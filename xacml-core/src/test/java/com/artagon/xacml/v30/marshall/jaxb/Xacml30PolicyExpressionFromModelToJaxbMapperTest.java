package com.artagon.xacml.v30.marshall.jaxb;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.v30.jaxb.ApplyType;
import org.oasis.xacml.v30.jaxb.AttributeDesignatorType;
import org.oasis.xacml.v30.jaxb.AttributeSelectorType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.pdp.Apply;
import com.artagon.xacml.v30.pdp.AttributeDesignator;
import com.artagon.xacml.v30.pdp.AttributeSelector;
import com.artagon.xacml.v30.pdp.Expression;
import com.artagon.xacml.v30.pdp.FunctionSpec;
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
		Capture<List<Expression>> args = new Capture<List<Expression>>();
		expect(func.validateParameters(capture(args))).andReturn(true);
		expect(func.getId()).andReturn("TestFunc");
		
		ctrl.replay();
		Expression exp0 = new AttributeDesignator(AttributeCategories.SUBJECT_ACCESS, "TestId", "Issuer", StringType.STRING, false);
		Expression exp1 = StringType.STRING.create("Test");
		Expression exp2 = new Apply(func, exp0, exp1);
		JAXBElement<?> jaxb = mapper.create(exp2);
		ctrl.verify();
		
		ApplyType v = (ApplyType)jaxb.getValue();
		assertEquals("TestFunc", v.getFunctionId());
		
	}
	
	@Test
	public void testApplyMapTwoLevel() throws Exception
	{
		Capture<List<Expression>> args0 = new Capture<List<Expression>>();
		Capture<List<Expression>> args1 = new Capture<List<Expression>>();
		expect(func.validateParameters(capture(args0))).andReturn(true);
		expect(func.getId()).andReturn("TestFunc1");
		
		expect(func.validateParameters(capture(args1))).andReturn(true);
		expect(func.getId()).andReturn("TestFunc2");
		
		ctrl.replay();
		
		Expression exp0 = new AttributeDesignator(AttributeCategories.SUBJECT_ACCESS, "TestId", "Issuer", StringType.STRING, false);
		Expression exp1 = StringType.STRING.create("Test1");
		Expression exp2 = new Apply(func, exp0, exp1);
		
		Expression exp3 = StringType.STRING.create("Test2");
		Expression exp4 = new Apply(func, exp3, exp2);
		
		JAXBElement<?> jaxb = mapper.create(exp4);
		ctrl.verify();
		
		ApplyType v1 = (ApplyType)jaxb.getValue();
		assertEquals("TestFunc1", v1.getFunctionId());
		
		assertEquals("http://www.w3.org/2001/XMLSchema#string", ((AttributeValueType)v1.getExpression().get(0).getValue()).getDataType());
		assertEquals("Test2", ((AttributeValueType)v1.getExpression().get(0).getValue()).getContent().get(0));
		
		ApplyType v2 = ((ApplyType)v1.getExpression().get(1).getValue());
		assertEquals("TestFunc2", v2.getFunctionId());
		
		AttributeDesignatorType jaxbExp0 = (AttributeDesignatorType)v2.getExpression().get(0).getValue(); 
		assertEquals("http://www.w3.org/2001/XMLSchema#string", jaxbExp0.getDataType());
		assertEquals("TestId", jaxbExp0.getAttributeId());
		assertEquals(AttributeCategories.SUBJECT_ACCESS.getId(), jaxbExp0.getCategory());
		
		AttributeValueType jaxbExp1 = (AttributeValueType)v2.getExpression().get(1).getValue();

		assertEquals("http://www.w3.org/2001/XMLSchema#string", jaxbExp1.getDataType());
		assertEquals("Test1", jaxbExp1.getContent().get(0));
			
		
	}
}
