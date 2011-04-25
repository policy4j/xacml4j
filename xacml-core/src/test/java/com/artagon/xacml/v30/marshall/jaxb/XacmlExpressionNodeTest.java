package com.artagon.xacml.v30.marshall.jaxb;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Apply;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeSelector;
import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.marshall.XacmlExpressionNode;
import com.artagon.xacml.v30.types.DayTimeDurationType;
import com.artagon.xacml.v30.types.StringType;
import com.artagon.xacml.v30.types.XPathExpressionType;
import com.artagon.xacml.v30.types.XPathExpressionValue;

public class XacmlExpressionNodeTest 
{
	private XacmlPolicyParsingContext context;
	private IMocksControl c;
	
	@Before
	public void init(){
		this.c = createControl();
		this.context = c.createMock(XacmlPolicyParsingContext.class);
	}
	
	@Test
	public void testParseAttributeValueXacml30WD17() throws Exception
	{
		expect(context.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration")).andReturn(DayTimeDurationType.DAYTIMEDURATION);
		c.replay();
		String data = "<AttributeValue xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" DataType=\"http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration\">P5DT2H0M0S</AttributeValue>";
		XMLInputFactory f = XMLInputFactory.newFactory();
		XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(data.getBytes()));
		r.nextTag();
		AttributeValue attr = (AttributeValue)XacmlExpressionNode.parse(r, context);
		assertEquals(DayTimeDurationType.DAYTIMEDURATION, attr.getType());
		assertEquals("P5DT2H0M0S", attr.toXacmlString());
		c.verify();	
	}
	
	@Test
	public void testParseAttributeValueXacml30() throws Exception
	{
		expect(context.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration")).andReturn(DayTimeDurationType.DAYTIMEDURATION);
		c.replay();
		String data = "<AttributeValue xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema\" DataType=\"http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration\">P5DT2H0M0S</AttributeValue>";
		XMLInputFactory f = XMLInputFactory.newFactory();
		XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(data.getBytes()));
		r.nextTag();
		AttributeValue attr = (AttributeValue)XacmlExpressionNode.parse(r, context);
		assertEquals(DayTimeDurationType.DAYTIMEDURATION, attr.getType());
		assertEquals("P5DT2H0M0S", attr.toXacmlString());
		c.verify();	
	}
	
	@Test
	public void testParseXPathAtributeValueXacml30WithCategoryAttribute() throws Exception
	{
		expect(context.getType(XPathExpressionType.XPATHEXPRESSION.getDataTypeId())).andReturn(XPathExpressionType.XPATHEXPRESSION);
		c.replay();
		String data = "<AttributeValue xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema\" Category=\"test\" DataType=\"urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression\">/test</AttributeValue>";
		XMLInputFactory f = XMLInputFactory.newFactory();
		XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(data.getBytes()));
		r.nextTag();
		XPathExpressionValue attr = (XPathExpressionValue)XacmlExpressionNode.parse(r, context);
		assertEquals(XPathExpressionType.XPATHEXPRESSION, attr.getType());
		assertEquals("/test", attr.toXacmlString());
		assertEquals(AttributeCategories.parse("test"), attr.getCategory());
		c.verify();	
	}
	
	@Test
	public void testParseXPathAtributeValueXacml30WD17WithCategoryAttribute() throws Exception
	{
		expect(context.getType(XPathExpressionType.XPATHEXPRESSION.getDataTypeId())).andReturn(XPathExpressionType.XPATHEXPRESSION);
		c.replay();
		String data = "<AttributeValue xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" Category=\"test\" DataType=\"urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression\">/test</AttributeValue>";
		XMLInputFactory f = XMLInputFactory.newFactory();
		XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(data.getBytes()));
		r.nextTag();
		XPathExpressionValue attr = (XPathExpressionValue)XacmlExpressionNode.parse(r, context);
		assertEquals(XPathExpressionType.XPATHEXPRESSION, attr.getType());
		assertEquals("/test", attr.toXacmlString());
		assertEquals(AttributeCategories.parse("test"), attr.getCategory());
		c.verify();	
	}
	
	@Test
	public void testXacml20AttributeSelector() throws Exception
	{
		 String xml = "<AttributeSelector xmlns=\"urn:oasis:names:tc:xacml:2.0:policy:schema:os\" RequestContextPath=\"./xacml-context:Resource/xacml-context:ResourceContent/md:" +
		 		"record/md:diagnosis_info/md:pathological_diagnosis/md:malignancy/@type\" " +
		 		"MustBePresent=\"true\" " +
		 		"DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>";
		expect(context.getType(StringType.STRING.getDataTypeId())).andReturn(StringType.STRING);
		c.replay();
		XMLInputFactory f = XMLInputFactory.newFactory();
		XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
		r.nextTag();
		AttributeSelector exp = (AttributeSelector)XacmlExpressionNode.parse(r, context);
		assertEquals(AttributeCategories.RESOURCE, exp.getCategory());
		assertEquals(true, exp.isMustBePresent());
		assertEquals("./md:record/md:diagnosis_info/md:pathological_diagnosis/md:malignancy/@type", exp.getPath());
	}
	
	@Test
	public void testXacml30AttributeSelector() throws Exception
	{
		String xml = "<AttributeSelector xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" MustBePresent=\"false\" " +
				"Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" " +
				"ContextSelectorId=\"test\" " +
				"Path=\"md:record/md:primaryCarePhysician/md:registrationID/text()\" " +
				"DataType=\"http://www.w3.org/2001/XMLSchema#string\"/>";
		expect(context.getType(StringType.STRING.getDataTypeId())).andReturn(StringType.STRING);
		c.replay();
		XMLInputFactory f = XMLInputFactory.newFactory();
		XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
		r.nextTag();
		AttributeSelector exp = (AttributeSelector)XacmlExpressionNode.parse(r, context);
		assertEquals(AttributeCategories.RESOURCE, exp.getCategory());
		assertEquals(false, exp.isMustBePresent());
		assertEquals("test", exp.getContextSelectorId());
		assertEquals("md:record/md:primaryCarePhysician/md:registrationID/text()", exp.getPath());
	}
	
	@Test
	public void testXacml30WD17Apply() throws Exception
	{
		String xml = "<Apply xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\">" +
				"<AttributeSelector MustBePresent=\"false\" " +
				"Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" " +
				"Path=\"md:record/md:primaryCarePhysician/md:registrationID/text()\" " +
						"DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply>";
		expect(context.getType(StringType.STRING.getDataTypeId())).andReturn(StringType.STRING);
		FunctionSpec spec = c.createMock(FunctionSpec.class);
		expect(context.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")).andReturn(spec);
		Capture<List<Expression>> params = new Capture<List<Expression>>();
		expect(spec.validateParameters(capture(params))).andReturn(true);
		c.replay();
		XMLInputFactory f = XMLInputFactory.newFactory();
		XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
		r.nextTag();
		Apply exp = (Apply)XacmlExpressionNode.parse(r, context);
		assertEquals(exp.getArguments(), params.getValue());
	}
	
	@Test
	public void testXacml30Apply() throws Exception
	{
		String xml = "<Apply xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema\" FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\">" +
				"<AttributeSelector MustBePresent=\"false\" " +
				"Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" " +
				"Path=\"md:record/md:primaryCarePhysician/md:registrationID/text()\" " +
						"DataType=\"http://www.w3.org/2001/XMLSchema#string\"/></Apply>";
		expect(context.getType(StringType.STRING.getDataTypeId())).andReturn(StringType.STRING);
		FunctionSpec spec = c.createMock(FunctionSpec.class);
		expect(context.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only")).andReturn(spec);
		Capture<List<Expression>> params = new Capture<List<Expression>>();
		expect(spec.validateParameters(capture(params))).andReturn(true);
		expect(spec.getId()).andReturn("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only");
		c.replay();
		XMLInputFactory fIn = XMLInputFactory.newFactory();
		XMLStreamReader r = fIn.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
		r.nextTag();
		Apply exp = (Apply)XacmlExpressionNode.parse(r, context);
		assertEquals(exp.getArguments(), params.getValue());		
	}
}
