package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;

public class AttributeResolverTestSupport {

	protected <T extends AttributeValueType> void checkHasAttribute(
			Map<String, BagOfAttributeValues> v, String attributeName,
			T type, Object ...values) {
		BagOfAttributeValues bag = v.get(attributeName);
		checkHasValues(bag, type, values);
	}

	protected <T extends AttributeValueType> void checkHasValues(BagOfAttributeValues bag,
			T type, Object ...values) {
		assertNotNull(bag);
		assertEquals(values.length, bag.size());
		for(Object value: values) {
			assertTrue(bag.contains(type.create(value)));
		}
	}
}
