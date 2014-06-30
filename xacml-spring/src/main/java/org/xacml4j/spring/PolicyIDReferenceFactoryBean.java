package org.xacml4j.spring;

/*
 * #%L
 * Xacml4J Spring 3.x Support Module
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

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.pdp.PolicyIDReference;


public class PolicyIDReferenceFactoryBean extends
	AbstractFactoryBean<PolicyIDReference>
{
	private PolicyIDReference.Builder ref = PolicyIDReference.builder();

	public void setId(String id){
		this.ref.id(id);
	}

	public void setEarliest(String earliest){
		this.ref.earliest(earliest);
	}

	public void setVersion(String version){
		this.ref.versionAsString(version);
	}

	public void setLatest(String latest){
		this.ref.latest(latest);
	}

	@Override
	public Class<PolicyIDReference> getObjectType() {
		return PolicyIDReference.class;
	}

	@Override
	protected PolicyIDReference createInstance() throws Exception
	{
		return ref.build();
	}
}

