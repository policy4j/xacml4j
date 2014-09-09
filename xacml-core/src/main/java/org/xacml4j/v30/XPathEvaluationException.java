package org.xacml4j.v30;

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

import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;

import com.google.common.base.Preconditions;


public class XPathEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 1511624494955246280L;

	private String xpathExpression;

	public XPathEvaluationException(
			String xpath,
			Status status,
			Throwable cause, 
			String message, 
			Object... arguments) {
		super(status, cause, message, arguments);
		Preconditions.checkNotNull(xpath);
		this.xpathExpression = xpath;
	}
	
	public XPathEvaluationException(
			String xpath,
			String template, 
			Object... arguments) {
		this(xpath, Status.processingError().build(), 
				null, template, arguments);
		
	}
	
	public XPathEvaluationException(
			String xpath, 
			Status status,
			String template, 
			Object... arguments) {
		this(xpath, status, null, template, arguments);
		
	}

	public XPathEvaluationException(
			String xpath,
			Throwable cause, 
			String message, Object... arguments) {
		this(xpath, Status.processingError().build(),cause, message, arguments);
	}
	
	

	public XPathEvaluationException(String xpath,
			Throwable cause) {
		this(xpath, Status.processingError().build(), cause, null);
	}

	public String getXPathExpression(){
		return xpathExpression;
	}
}
