package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeExp;


public class XPathExpressionTypeTest
{
	private Types types;
	
	@Before
	public void init(){
		this.types = Types.builder().defaultTypes().create();
	}

	@Test
	public void testCreateXPathAttribute() throws Exception
	{
		XPathExp v = XPathExpType.XPATHEXPRESSION.create("/test", AttributeCategories.SUBJECT_RECIPIENT);
		assertEquals("/test", v.getPath());
		assertEquals(AttributeCategories.SUBJECT_RECIPIENT, v.getCategory());
	}
	
	@Test
	public void testToXacml30()
	{
		TypeToXacml30 toXacml = types.getCapability(XPathExpType.XPATHEXPRESSION, TypeToXacml30.class);
		XPathExp xpath0 = XPathExpType.XPATHEXPRESSION.create("/test", AttributeCategories.SUBJECT_RECIPIENT);
		AttributeValueType xacml = toXacml.toXacml30(types, xpath0);
		assertEquals("/test", xacml.getContent().get(0));
		assertEquals(AttributeCategories.SUBJECT_RECIPIENT.getId(), xacml.getOtherAttributes().get(XPathExpType.XPATH_CATEGORY_ATTR_NAME));
		AttributeExp xpath1 = toXacml.fromXacml30(types, xacml);
		assertEquals(xpath0, xpath1);
	}
}
