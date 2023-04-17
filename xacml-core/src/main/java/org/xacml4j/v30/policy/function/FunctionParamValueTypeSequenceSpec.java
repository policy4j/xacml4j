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
import org.xacml4j.v30.ValueTypeInfo;
import org.xacml4j.v30.policy.PolicySyntaxException;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;


final class FunctionParamValueTypeSequenceSpec extends BaseFunctionParamSpec
{
	private Integer min = 0;
	private Integer max = Integer.MAX_VALUE;

	private ValueTypeInfo paramType;

	/**
	 * Constructs parameter specification 
	 * with a given minimum and maximum 
	 * number of parameters
	 * 
	 * @param min a minimum number
	 * @param max a maximum number
	 * @param paramType an argument type
	 */
	public FunctionParamValueTypeSequenceSpec(
			Integer min, 
			Integer max,
			ValueTypeInfo paramType){
		super((min == null || min == 0), true, null);
		this.min = min == null?0:min;
		this.max = max == null?Integer.MAX_VALUE:max;
		this.paramType = java.util.Objects.requireNonNull(paramType, "paramType");;
	}

	public FunctionParamValueTypeSequenceSpec(int min,
			ValueTypeInfo paramType){
		this(min, Integer.MAX_VALUE, paramType);
	}
	
	/**
	 * Gets parameter XACML type.
	 *
	 * @return parameter XACML type
	 */
	public ValueTypeInfo getParamType(){
		return paramType;
	}

	/**
	 * Gets minimum number of parameters
	 * in this sequence.
	 *
	 * @return a minimum number of parameters
	 */
	public Integer getMinParams(){
		return min;
	}

	public Integer getMaxParams(){
		return max;
	}

	@Override
	public boolean isValidParamType(ValueTypeInfo type){
		return this.paramType.equals(type);
	}

	@Override
	public boolean validate(ListIterator<Expression> it, boolean suppressExceptions) throws PolicySyntaxException {
		if(!it.hasNext() && min > 0){
			if(suppressExceptions){
				return false;
			}
			throw PolicySyntaxException
					.invalidParam(
							this, it.nextIndex(),
							String.format("expecting parameter params of type=\"%s\" " +
									              "to have least min=\"%d\" params, found=\"%d\"",
							              new Object[]{paramType, min, 0}));
		}
		int c = 0;
		while(it.hasNext()){
			Expression exp = it.next();
			if(exp == null){
				break;
			}
			ValueTypeInfo expType = exp.getEvaluatesTo();
			// variadic param is always last
			if(!expType.equals(paramType))
			{
				if(suppressExceptions){
					return false;
				}
				throw PolicySyntaxException
						.invalidParam(
								this, it.previousIndex(),
								String.format("expecting parameter params of type=\"%s\" at index=\"%s\" " +
										              "to have min=\"%d\" max=\"%d\" params, found=\"%d\"",
								              new Object[]{paramType, it.nextIndex(), min, max, c}));
			}
			c++;
		}
		if(c >= min && c <= max){
			return true;
		}
		if(suppressExceptions){
			return false;
		}
		throw PolicySyntaxException
				.invalidParam(
						this, it.previousIndex(),
						String.format("expecting parameter params of type=\"%s\" at index=\"%s\" " +
								              "to have min=\"%d\" max=\"%d\" params, found=\"%d\"",
						              new Object[]{paramType, it.nextIndex(), min, max, c}));
	}

	@Override
	public String toString(){
		return MoreObjects
				.toStringHelper(this)
				.add("min", min)
				.add("max", max)
				.add("type", paramType)
				.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(min, max, paramType);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof FunctionParamValueTypeSequenceSpec)){
			return false;
		}
		FunctionParamValueTypeSequenceSpec s = (FunctionParamValueTypeSequenceSpec)o;
		return Objects.equal(min, s.min) &&
				Objects.equal(max, s.max) &&
				paramType.equals(s.paramType);
	}
	
	public void accept(FunctionParamSpecVisitor v){
		v.visit(this);
	}
}
