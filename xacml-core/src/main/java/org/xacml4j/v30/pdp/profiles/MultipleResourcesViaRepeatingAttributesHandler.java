package org.xacml4j.v30.pdp.profiles;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;

import com.google.common.collect.Sets;

final class MultipleResourcesViaRepeatingAttributesHandler extends AbstractRequestContextHandler
{
	private final static String FEATURE_ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories";

	public MultipleResourcesViaRepeatingAttributesHandler(){
		super(FEATURE_ID);
	}

	@Override
	public Collection<Result> handle(RequestContext request,
			PolicyDecisionPointContext context)
	{
		if(!request.containsRepeatingCategories()){
			return handleNext(request, context);
		}
		List<Set<Category>> byCategory = new LinkedList<Set<Category>>();
		for(CategoryId categoryId : request.getCategories()){
			Collection<Category> attributes = request.getAttributes(categoryId);
			if(attributes == null ||
					attributes.isEmpty()){
				continue;
			}
			byCategory.add(new LinkedHashSet<Category>(attributes));
		}
		Collection<Result> results = new LinkedList<Result>();
		Set<List<Category>> cartesian = Sets.cartesianProduct(byCategory);
		for(List<Category> requestAttr : cartesian){
			results.addAll(handleNext(RequestContext
					.builder()
					.copyOf(request, requestAttr)
					.build(), context));
		}
		return results;
	}
}
