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

import org.xacml4j.v30.AttributeExp;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Conveys a single parameter for a policy- or rule-combining algorithm.
 *
 * @author Giedrius Trumpickas
 */
public class CombinerParameter
	implements PolicyElement
{
	private final String name;
	private final AttributeExp value;

	/**
	 * Constructs decision combining parameter
	 *
	 * @param name a parameter name
	 * @param value a parameter value
	 */
	public CombinerParameter(String name,
			AttributeExp value)
	{
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(value);
		this.name = name;
		this.value = value;
	}

	public final String getName(){
		return name;
	}

	public final AttributeExp getValue(){
		return value;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(name, value);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("name", name)
		.add("value", value)
		.toString();
	}

	@Override
	public boolean equals(Object  o){
		if(o == this){
			return true;
		}
		if(!(o instanceof CombinerParameter)){
			return false;
		}
		CombinerParameter c = (CombinerParameter)o;
		return name.equals(c.name)
			&& value.equals(c.value);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
