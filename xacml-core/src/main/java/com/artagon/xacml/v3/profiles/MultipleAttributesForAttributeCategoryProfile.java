package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributeCategoryId;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextException;

public class MultipleAttributesForAttributeCategoryProfile extends BaseRequestContextProfile
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:multiple-resource-elements";

	public MultipleAttributesForAttributeCategoryProfile() {
		super(ID);
	}

	@Override
	public Collection<RequestContext> process(RequestContext context)
			throws RequestContextException {
		Collection<RequestContext> requests = new LinkedList<RequestContext>();
		Map<AttributeCategoryId, Collection<Attributes>> attributes = context.getAttributes();
		for(AttributeCategoryId categoryId : attributes.keySet()){
			Collection<Attributes> byCategory = attributes.get(categoryId);
			if(byCategory.size() <= 1){
				continue;
			}
			attributes.remove(categoryId);
		}
		return requests;
	}
}
