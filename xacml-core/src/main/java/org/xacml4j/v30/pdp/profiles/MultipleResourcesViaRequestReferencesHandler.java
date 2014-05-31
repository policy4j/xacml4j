package org.xacml4j.v30.pdp.profiles;

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

import java.util.Collection;
import java.util.LinkedList;

import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryReference;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.pdp.RequestSyntaxException;


final class MultipleResourcesViaRequestReferencesHandler extends AbstractRequestContextHandler
{
	private final static String FEATURE_ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:reference";

	public MultipleResourcesViaRequestReferencesHandler() {
		super(FEATURE_ID);
	}

	@Override
	public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context)
	{
		Collection<Result> results = new LinkedList<Result>();
		Collection<RequestReference> references = request.getRequestReferences();
		if(references.isEmpty()){
			return handleNext(request, context);
		}
		for(RequestReference ref : references){
			try{
				RequestContext resolvedRequest = resolveAttributes(request, ref);
				results.addAll(handleNext(resolvedRequest, context));
			}catch(RequestSyntaxException e){
				results.add(
						Result
						.indeterminate(e.getStatus())
						.includeInResultAttr(request.getIncludeInResultAttributes())
						.build());
			}
		}
		return results;
	}

	private RequestContext resolveAttributes(RequestContext req,
			RequestReference reqRef) throws RequestSyntaxException
	{
		Collection<Category> resolved = new LinkedList<Category>();
		for(CategoryReference ref : reqRef.getReferencedCategories()){
			Category attributes = req.getReferencedCategory(ref);
			if(attributes == null){
				throw new RequestSyntaxException(
						"Failed to resolve attribute reference",
						ref.getReferenceId());
			}
			resolved.add(attributes);
		}
		return new RequestContext(req.isReturnPolicyIdList(), resolved);
	}

}
