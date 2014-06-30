package org.xacml4j.v30.spi.function;

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

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionParamSpec;

abstract class BaseFunctionParamSpec implements FunctionParamSpec
{
	private boolean optional = false;
	private Expression defaultValue;
	private boolean variadic;
	
	protected BaseFunctionParamSpec(){
	}
	
	protected BaseFunctionParamSpec(
			boolean optional, 
			boolean variadic,
			Expression defaultValue){
		if(!optional && defaultValue != null){
			throw new XacmlSyntaxException("Function parameter can't be required " +
					"and have default value at the same time");
		}
		this.optional = optional;
		this.variadic = variadic;
		this.defaultValue = defaultValue;
	}
	
	@Override
	public final boolean isOptional(){
		return optional;
	}
	
	@Override
	public final boolean isVariadic(){
		return variadic;
	}
	
	@Override
	public final Expression getDefaultValue(){
		return defaultValue;
	}
}
