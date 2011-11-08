package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;

public class AttributeResolverTestSupport {

	protected <T extends AttributeExpType> void checkHasAttribute(
			Map<String, BagOfAttributesExp> v, String attributeName,
			T type, Object ...values) {
		BagOfAttributesExp bag = v.get(attributeName);
		checkHasValues(bag, type, values);
	}

	protected <T extends AttributeExpType> void checkHasValues(BagOfAttributesExp bag,
			T type, Object ...values) {
		assertNotNull(bag);
		assertEquals(values.length, bag.size());
		for(Object value: values) {
			assertTrue(bag.contains(type.create(value)));
		}
	}
}
