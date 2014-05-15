package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import java.util.Map;
import java.util.Set;

import org.xacml4j.v30.AttributeDesignatorKey;

import com.google.common.base.Preconditions;

class AttributeResolverDescriptorDelegate
	extends ResolverDescriptorDelegate implements AttributeResolverDescriptor
{
	private AttributeResolverDescriptor d;

	AttributeResolverDescriptorDelegate(AttributeResolverDescriptor d){
		super(d);
		Preconditions.checkNotNull(d);
		this.d = d;
	}

	@Override
	public boolean canResolve(AttributeDesignatorKey key) {
		return d.canResolve(key);
	}

	@Override
	public AttributeDescriptor getAttribute(String attributeId) {
		return d.getAttribute(attributeId);
	}

	@Override
	public int getAttributesCount() {
		return d.getAttributesCount();
	}

	@Override
	public Set<String> getProvidedAttributeIds() {
		return d.getProvidedAttributeIds();
	}

	@Override
	public Map<String, AttributeDescriptor> getAttributesById() {
		return d.getAttributesById();
	}

	@Override
	public Map<AttributeDesignatorKey, AttributeDescriptor> getAttributesByKey() {
		return d.getAttributesByKey();
	}

	@Override
	public boolean isAttributeProvided(String attributeId) {
		return d.isAttributeProvided(attributeId);
	}

	@Override
	public String getIssuer() {
		return d.getIssuer();
	}
}
