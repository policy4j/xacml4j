package com.artagon.xacml.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.CategoryId;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.DataTypes;

public class AttributeDesignatorTest extends XacmlPolicyTestCase
{
	private String issuer;
	private String attributeId;
	private String badAttributeId;
	private IntegerType type;
	
	@Before
	public void init(){
		this.issuer = "urn:test:issuer";
		this.attributeId = "urn:test:attrId";
		this.badAttributeId = "urn:test:attrId:bad";
		this.type = DataTypes.INTEGER.getType();
	}
	
	@Test(expected=PolicyEvaluationIndeterminateException.class)
	public void testMustBePresentTrueAttributeDoesNotExist() throws PolicyEvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(
				CategoryId.SUBJECT_RECIPIENT, attributeId, issuer, type, true);
		desig.evaluate(context);
	}
	
	@Test
	public void testMustBePresentTrueAttributeDoesExist() throws PolicyEvaluationException
	{
		Collection<Attribute> attributes = new LinkedList<Attribute>();
		Attribute attr = type.create(10l);
		attributes.add(attr);
		attributeService.addAttribute(CategoryId.SUBJECT_RECIPIENT,
				issuer, attributeId, type, attributes);
		AttributeDesignator desig = new AttributeDesignator(CategoryId.SUBJECT_RECIPIENT, issuer, attributeId, 
				type, true);
		Value v = desig.evaluate(context);
		assertNotNull(v);
		assertTrue(((BagOfAttributes<?>)v).contains(attr));
	}
	
	@Test
	public void testMustBePresentFalseAttributeDoesNotExistWithId() throws PolicyEvaluationException
	{
		AttributeDesignator desig = new AttributeDesignator(CategoryId.SUBJECT_RECIPIENT,
				badAttributeId, issuer,  type, false);
		Value v = desig.evaluate(context);
		assertNotNull(v);
		assertEquals(type.bagOf(), v.getEvaluatesTo());
	}
}
