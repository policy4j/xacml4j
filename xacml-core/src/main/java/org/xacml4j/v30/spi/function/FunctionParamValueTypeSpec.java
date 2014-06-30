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

import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

final class FunctionParamValueTypeSpec extends BaseFunctionParamSpec
{
	private final static Logger log = LoggerFactory.getLogger(FunctionParamValueTypeSpec.class);

	private ValueType type;

	public FunctionParamValueTypeSpec(
			ValueType paramType, 
			ValueExpression defaultValue, 
			boolean optional){
		super(optional, false, defaultValue);
		Preconditions.checkNotNull(paramType);
		if(defaultValue != null){
			Preconditions.checkArgument(paramType.equals(defaultValue.getEvaluatesTo()));
		}
		this.type = paramType;
	}
	
	public FunctionParamValueTypeSpec(ValueType type){
		this(type, null, false);
	}

	public ValueType getParamType(){
		return type;
	}

	@Override
	public boolean validate(ListIterator<Expression> it) {
		if(!it.hasNext()){
			log.debug("Parameter iterator at index=\"{}\" does not " +
					"have any elements left to iterate", it.previousIndex());
			return false;
		}
		Expression exp = it.next();
		if(exp == null){
			return isOptional();
		}
		ValueType expType = exp.getEvaluatesTo();
		boolean valid = type.equals(expType);
		if(log.isDebugEnabled()){
			log.debug("Expecting parameter of type=\"{}\" " +
					"at index=\"{}\" found=\"{}\" valid=\"{}\"",
					new Object[]{type, it.previousIndex(), expType, valid});
		}
		return valid;
	}
	
	@Override
	public boolean isValidParamType(ValueType type) {
		return this.type.equals(type);
	}

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("type", type)
				.add("optional", isOptional())
				.add("defaultValue", getDefaultValue())
				.add("variadic", isVariadic())
				.toString();
	}

	@Override
	public int hashCode(){
		return type.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof FunctionParamValueTypeSpec)){
			return false;
		}
		FunctionParamValueTypeSpec s = (FunctionParamValueTypeSpec)o;
		return type.equals(s.type);
	}

	public void accept(FunctionParamSpecVisitor v){
		v.visit(this);
	}
}
