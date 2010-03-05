package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.policy.AttributeSelector;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultAttributeSelectorTest
{
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	"xmlns:md=\"urn:example:med:schemas:record\">" +
	"<md:patient>" +
	"<md:patientDoB>1992-03-21</md:patientDoB>" +
	"<md:patient-number>555555</md:patient-number>" +
	"</md:patient>" +
	"</md:record>";

	private EvaluationContext context;
	private Node content;
	
	@Before
	public void init() throws Exception
	{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.context = createStrictMock(EvaluationContext.class);
		this.content = builder.parse(new InputSource(new StringReader(testXml)));
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueAndContentNodeDoesNotExists() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, "/test", DataTypes.INTEGER.getType(), true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(null);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresentCategoryContentExistXPathReturnsNonEmptySetInteger() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number/text()", 
				DataTypes.INTEGER.getType(), true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(content);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(DataTypes.INTEGER.bag(DataTypes.INTEGER.create(555555)), v);
		verify(context);
	}
	
	@Test
	public void testMustBePresentCategoryContentExistXPathReturnsNonEmptySetDate() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DataTypes.DATE.getType(), true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(content);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(DataTypes.DATE.bag(DataTypes.DATE.fromXacmlString("1992-03-21")), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentCategoryContentExistXPathReturnsNonEmptySetValueCantBeConverted() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patientDoB/text()", 
				DataTypes.INTEGER.getType(), true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(content);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentCategoryContentExistXPathReturnsNonEmptySetWithWrongNodeType() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/md:record/md:patient/md:patient-number", 
				DataTypes.INTEGER.getType(), true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(content);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	@Test
	public void testMustBePresentFalseCategoryContentExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/test", 
				DataTypes.INTEGER.getType(), false);
		expect(context.getContent(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(content);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(DataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
	
	@Test(expected=AttributeReferenceEvaluationException.class)
	public void testMustBePresentTrueCategoryContentExistXPathReturnsEmptySet() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, 
				"/test", 
				DataTypes.INTEGER.getType(), true);
		expect(context.getContent(AttributeCategoryId.SUBJECT_RECIPIENT)).andReturn(content);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		desig.evaluate(context);
		verify(context);
	}
	
	public void testMustBePresentFalseAttributeDoesNotExist() throws EvaluationException
	{
		AttributeSelector desig = new DefaultAttributeSelector(
				AttributeCategoryId.SUBJECT_RECIPIENT, "/test", DataTypes.INTEGER.getType(), true);
		expect(context.getXPathProvider()).andReturn(new JDKXPathProvider());
		replay(context);
		Expression v = desig.evaluate(context);
		assertEquals(DataTypes.INTEGER.emptyBag(), v);
		verify(context);
	}
	
}

