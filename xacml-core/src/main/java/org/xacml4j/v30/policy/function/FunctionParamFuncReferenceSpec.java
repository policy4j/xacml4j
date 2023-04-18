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
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.PolicySyntaxException;

import com.google.common.base.MoreObjects;

final class FunctionParamFuncReferenceSpec extends BaseFunctionParamSpec
{
	@Override
	public boolean isValidParamType(ValueExpTypeInfo type) {
		return false;
	}

	@Override
	public boolean validate(ListIterator<Expression> it, boolean suppressException) {
		Expression exp = it.next();
		if(!(exp instanceof FunctionReference)){
			if(suppressException) {
				return false;
			}
			throw PolicySyntaxException.invalidParam(this, it.previousIndex(),
			                                         String.format("expression class=\"%s\" is not function reference",
			                                         exp.getClass().getSimpleName()));
		}
		return true;
	}

	@Override
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
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		return (o instanceof FunctionParamFuncReferenceSpec);
	}

	public void accept(FunctionParamSpecVisitor v){
		v.visit(this);
	}
}
