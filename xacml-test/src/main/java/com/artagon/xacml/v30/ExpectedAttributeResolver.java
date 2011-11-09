package com.artagon.xacml.v30;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.pdp.BagOfAttributeExpType;
import com.artagon.xacml.v30.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v30.spi.pip.AttributeResolverDescriptorBuilder;
import com.artagon.xacml.v30.spi.pip.BaseAttributeResolver;
import com.artagon.xacml.v30.spi.pip.ResolverContext;

public class ExpectedAttributeResolver extends BaseAttributeResolver {

	private Collection<Attribute> expectedAttributes;

	public ExpectedAttributeResolver(AttributeCategory attrCategory, Attribute ...expectedAttributes) {
		super(createDescriptor(attrCategory, expectedAttributes));
		this.expectedAttributes = new LinkedList<Attribute>();
		for(Attribute attr: expectedAttributes) {
			this.expectedAttributes.add(attr);
		}
	}

	private static AttributeResolverDescriptor createDescriptor(AttributeCategory attrCategory, Attribute... attributes) {
		AttributeResolverDescriptorBuilder builder = AttributeResolverDescriptorBuilder.builder(
				"com:artagon:test:resolver:"+attrCategory.getId(), 
				"XACML Test Attributes Resolver", 
				attrCategory)
				.noCache();
		for(Attribute attr: attributes) {
			builder.attribute(attr.getAttributeId(), attr.getValues().iterator().next().getType());
		}
		return builder.build();
	}

	@Override
	protected Map<String, BagOfAttributeExp> doResolve(
			ResolverContext context) throws Exception {
		Map<String, BagOfAttributeExp> v = new HashMap<String, BagOfAttributeExp>();
		for(Attribute attr: expectedAttributes) {
			BagOfAttributeExp values = attr.getValues().iterator().next().getType().bagOf(attr.getValues());
			v.put(attr.getAttributeId(), values);
		}
		return v;
	}

}
