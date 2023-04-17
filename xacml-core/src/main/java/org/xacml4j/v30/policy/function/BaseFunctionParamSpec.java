package org.xacml4j.v30.policy.function;

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

import java.util.ListIterator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.FunctionParamSpec;
import org.xacml4j.v30.policy.PolicySyntaxException;

abstract class BaseFunctionParamSpec implements FunctionParamSpec
{
	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	private final boolean optional;
	private final Optional<Expression> defaultValue;
	private final boolean variadic;

	protected BaseFunctionParamSpec(){
		this(false, false, null);
	}

	protected BaseFunctionParamSpec(
			boolean optional,
			boolean variadic,
			Expression defaultValue){
		if (!optional && defaultValue != null) {
			throw new SyntaxException("Function parameter can't be required " +
					"and have default value at the same time");
		}
		this.optional = optional;
		this.variadic = variadic;
		this.defaultValue = Optional.ofNullable(defaultValue);
	}

	@Override
	public final boolean isOptional(){
		return optional && defaultValue != null;
	}

	@Override
	public final boolean isVariadic(){
		return variadic;
	}

	@Override
	public final Optional<Expression> getDefaultValue(){
		return defaultValue;
	}
}
