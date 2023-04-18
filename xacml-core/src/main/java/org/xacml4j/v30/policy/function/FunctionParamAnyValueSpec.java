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

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.policy.PolicySyntaxException;

import com.google.common.base.MoreObjects;


final class FunctionParamAnyValueSpec extends BaseFunctionParamSpec
{
	public FunctionParamAnyValueSpec() {
		super(false, false, null);
	}
	@Override
	public boolean isValidParamType(ValueExpTypeInfo type) {
		return (type instanceof ValueType);
	}
	
	@Override
	public boolean validate(ListIterator<Expression> it, boolean suppressException) {
		if(!it.hasNext()){
			if(suppressException){
				return false;
			}
			throw PolicySyntaxException
					.invalidParam(this, it.previousIndex(),
					              "parameter list is too short");
		}
		Expression exp = it.next();
		if(exp == null){
			if(isOptional()){
				return true;
			}
			if(suppressException){
				return false;
			}
			throw PolicySyntaxException
					.invalidParam(this, it.previousIndex(),
					              String.format("expected param of type=\"%s\", found null",
					                            Value.class.getSimpleName()));
		}
		if(!isValidParamType(exp.getEvaluatesTo())){
			if(suppressException) {
				return false;
			}
			throw PolicySyntaxException
					.invalidParam(this, it.previousIndex(),
					              String.format("expected param of type=\"%s\", found type=\"%s\"",
					              ValueType.class.getSimpleName(), exp.getEvaluatesTo()));
		}
		return true;
	}

	public String toString(){
		return MoreObjects.
				toStringHelper(this)
				.add("optional", isOptional())
				.add("defaultValue", getDefaultValue())
				.add("variadic", isVariadic())
				.toString();
	}

	@Override
	public int hashCode(){
		return 0;
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		return (o instanceof FunctionParamAnyValueSpec);
	}
	
	public void accept(FunctionParamSpecVisitor v){
		v.visit(this);
	}

}
