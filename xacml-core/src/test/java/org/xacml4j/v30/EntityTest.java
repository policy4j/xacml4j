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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.pdp.XPathEvaluationException;
import org.xacml4j.v30.spi.xpath.DefaultXPathProvider;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;
import org.xml.sax.InputSource;

import com.google.common.base.Predicate;

public class EntityTest 
{
	
	private String testXml = "<md:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
			"xmlns:md=\"urn:example:med:schemas:record\">" +
			"<md:patient>" +
			"<md:patientDoB>1992-03-21</md:patientDoB>" +
			"<md:patient-number>555555</md:patient-number>" +
			"</md:patient>" +
			"</md:record>";
	
	private Entity entity;
	
	@Before
	public void init() throws Exception{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		this.entity = Entity
				.builder()
				.content(builder.parse
						(new InputSource(new StringReader(testXml)))).build();
	}
	@Test
	public void testBuildEntity(){
		Entity e0 = Entity
				.builder()
				.attribute(
					Attribute.
						builder("testId1")
						.value(StringExp.valueOf("a"), StringExp.valueOf("bb"))
						.build(),
					Attribute
						.builder("testId2")
						.value(StringExp.valueOf("aa"), StringExp.valueOf("bbb"))
						.build(),
					Attribute
						.builder("testId3")
						.value(IntegerExp.valueOf(10), DoubleExp.valueOf(0.1))
						.build())
				.build();
		Entity e1 = Entity.builder().copyOf(e0).build();
		assertEquals(e0,  e1);
		assertTrue(e1.getAttributeValues("testId1", XacmlTypes.STRING).contains(StringExp.valueOf("a")));
		assertTrue(e1.getAttributeValues("testId1", XacmlTypes.STRING).contains(StringExp.valueOf("bb")));
		Entity e2 = Entity.builder().copyOf(e0, new Predicate<Attribute>(){
			public boolean apply(Attribute a){
				return a.getAttributeId().equals("testId2");
			}
			
		} ).build();
		assertFalse(e2.getAttributeValues("testId1", XacmlTypes.STRING).contains(StringExp.valueOf("bb")));
		assertTrue(e2.getAttributeValues("testId2", XacmlTypes.STRING).contains(StringExp.valueOf("aa")));
		assertTrue(e2.getAttributeValues("testId2", XacmlTypes.STRING).contains(StringExp.valueOf("bbb")));
	}
	
	@Test
	public void testEntityXPathCorrectType(){
		BagOfAttributeExp values = entity.getAttributeValues("/md:record/md:patient/md:patient-number/text()", 
				new DefaultXPathProvider(), XacmlTypes.INTEGER, null);
		assertTrue(values.contains(IntegerExp.valueOf(555555)));
	}
	
	@Test(expected=XPathEvaluationException.class)
	public void testXPathReturnUnsupportedNodeType(){
		
		entity.getAttributeValues("/md:record/md:patient", 
				new DefaultXPathProvider(), XacmlTypes.INTEGER, null);
	}
	
	@Test
	public void testEntityEquals(){
		Entity e0 = Entity
				.builder()
				.attribute(Attribute.builder("testId1").value(StringExp.valueOf("aa"), StringExp.valueOf("bb")).build())
				.attribute(Attribute.builder("testId2").value(StringExp.valueOf("cc"), StringExp.valueOf("dd")).build())
				.build();
		
		Entity e1 = Entity
				.builder()
				.attribute(Attribute.builder("testId2").value(StringExp.valueOf("dd"), StringExp.valueOf("cc")).build())
				.attribute(Attribute.builder("testId1").value(StringExp.valueOf("bb"), StringExp.valueOf("aa")).build())
				.build();
		assertEquals(e0, e1);
	}
}


