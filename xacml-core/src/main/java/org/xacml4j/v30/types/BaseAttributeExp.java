package org.xacml4j.v30.types;

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
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.ValueType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public abstract class BaseAttributeExp<T>
	implements AttributeExp
{
	private static final long serialVersionUID = 4131180767511036271L;

	private final T value;
	private final AttributeExpType type;

	protected BaseAttributeExp(AttributeExpType attrType,
			T attrValue) {
		Preconditions.checkNotNull(attrType);
		Preconditions.checkNotNull(attrValue);
		this.type = attrType;
		this.value = attrValue;
	}

	@Override
	public final ValueType getEvaluatesTo(){
		return type;
	}

	@Override
	public final AttributeExpType getType(){
		return type;
	}

	@Override
	public final AttributeExp evaluate(
			EvaluationContext context) throws EvaluationException {
		return this;
	}

	@Override
	public final T getValue(){
		return value;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
		add("Value", value).
		add("Type", getType()).toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				getType(), value);
	}

	@Override
	public BagOfAttributeExp toBag(){
		return type.bagOf(this);
	}

	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof AttributeExp)){
			return false;
		}
		AttributeExp e = (AttributeExp)o;
		return type.equals(e.getType()) &&
				value.equals(e.getValue());
	}

	@Override
	public final void accept(ExpressionVisitor expv) {
		AttributeExpVisitor v = (AttributeExpVisitor)expv;
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
