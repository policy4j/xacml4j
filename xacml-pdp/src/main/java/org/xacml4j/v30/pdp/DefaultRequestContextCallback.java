package org.xacml4j.v30.pdp;

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

import com.google.common.base.Preconditions;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.RequestContext;

public class DefaultRequestContextCallback
	implements RequestContextCallback
{
	private RequestContext request;

	public DefaultRequestContextCallback(RequestContext request)
	{
		Preconditions.checkNotNull(request);
		Preconditions.checkArgument(
				!request.containsRepeatingCategories(),
				"RequestContext has repeating category categories");
		Preconditions.checkArgument(
				!request.containsRequestReferences(),
				"RequestContext contains multiple request references");
		this.request = request;
	}

	@Override
	public Entity getEntity(CategoryId category) {
		return request.getOnlyEntity(category);
	}
}
