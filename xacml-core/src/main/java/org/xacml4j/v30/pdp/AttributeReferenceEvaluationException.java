package org.xacml4j.v30.pdp;

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

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;

import com.google.common.base.Preconditions;


public class AttributeReferenceEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 4604041385261391333L;

	private AttributeReferenceKey ref;
	
	public AttributeReferenceEvaluationException(AttributeDesignatorKey ref, 
			String format, Object ... args){
		this(Status.missingAttribute(ref).build(), ref, String.format(format, args));
	}
	
	public AttributeReferenceEvaluationException(AttributeDesignatorKey ref){
		this(ref, ref.getAttributeId());
	}
	
	public AttributeReferenceEvaluationException(AttributeSelectorKey ref, 
			String format, Object ... args){
		this(Status.missingAttribute(ref).build(), ref, String.format(format, args));
	}
	
	public AttributeReferenceEvaluationException(
			Status status, 
			AttributeReferenceKey key, 
			String format, Object ... args){
		super(status, String.format(format, args));
		Preconditions.checkNotNull(key);
		this.ref = key;
	}

	public AttributeReferenceEvaluationException(
			AttributeSelectorKey ref){
		this(ref, ref.getPath());
	}

	public AttributeReferenceKey getReference(){
		return ref;
	}
}
