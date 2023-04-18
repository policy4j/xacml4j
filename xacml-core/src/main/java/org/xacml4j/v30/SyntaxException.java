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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.w3c.dom.Element;
import org.xacml4j.util.DOMUtil;


public class SyntaxException extends EvaluationException
{
	private static final long serialVersionUID = 5208193385563540743L;

	public SyntaxException(String message) {
		super(Status.syntaxError()
		            .message(message)
		            .build(), message);
	}

	public SyntaxException(Throwable cause) {
		super(Status.syntaxError()
		            .message(cause.getMessage())
		            .error(cause)
		            .build(),
				cause);
	}

	public SyntaxException(String message, Throwable cause) {
		super(Status.syntaxError()
		            .message(message)
		            .error(cause)
		            .build(),
		      cause);
	}

	public SyntaxException(String message, Object ...p) {
		super(Status.syntaxError()
		            .message(message != null && p != null?String.format(message, p):message)
		            .build(), (Throwable) null);
	}

	public static SyntaxException invalidAttributeValue(Object v, ValueType expectedType){
		return new SyntaxException(
				format("Invalid XACML type=\"%s\" attribute value=\"%s\"", expectedType.getShortTypeId(), v));
	}

	public static SyntaxException invalidDataTypeId(Object v){
		return new SyntaxException(
				format("Invalid XACML type identifier=\"%s\"", v));
	}
	public static SyntaxException invalidStatusCode(String code){
		return new SyntaxException(format("Invalid XACML type identifier=\"%s\"", code));
	}

	public static SyntaxException invalidCategoryId(Object categoryId){
		return new SyntaxException(
				format("Invalid XACML categoryId=\"%s\"",
				categoryId));
	}

	public static SyntaxException invalidCategoryId(Object categoryId, String info, Object...params){
		return new SyntaxException(format(
				"Invalid XACML categoryId=\"%s\", - s%",
				categoryId, String.format(info, params)));
	}

	public static SyntaxException noContentFound(String message, Object ...p){
		return new SyntaxException(format(message == null?
				"No content found":message, p));
	}

	public static SyntaxException noContentFound(){
		return noContentFound(null);
	}

	public static SyntaxException invalidXml(String message, Throwable t){
		return new SyntaxException(format(
				"Invalid XML source=\"{}\"", message), t);
	}

	public static SyntaxException invalidXmlDomNode(String message, Element source){
		return new SyntaxException(format("Invalid XML node=\"{}\" error=\"{}\"",
		                                  DOMUtil.toString(source), message));
	}

	public static SyntaxException failedToParseXml(String message, Throwable t){
		return new SyntaxException(
				format("Failed to parse XML error=\"{}\"", message, t), t);
	}

	public static SyntaxException invalidAttributeValue(Object v, ValueType...expectedTypes){
		return new SyntaxException(format(
				"Invalid XACML attribute value for type=\"%s\" attribute " +
						"value=\"%s\"", Arrays.toString(expectedTypes), v));
	}

	public static SyntaxException invalidResolverMethod(Method m, String message){
		return new SyntaxException(format("Invalid XACML resolver method=\"%s\" class=\"%s\", details=\"%s\"",
		                           m.getName(), m.getDeclaringClass().getName(), message));
	}

	public static SyntaxException invalidResolverAttributeId(String resolverId, String message, Object ...p){
		return new SyntaxException(format("Invalid resolver id=\"%s\" details=\"%s\"",
		                           resolverId, format(message, p)));
	}

	public static SyntaxException invalidResolverReferenceSelfRef(String resolverId, String message, Object ...p){
		return new SyntaxException(format("Invalid resolver id=\"%s\" details=\"%s\"",
		                           resolverId, format(message, p)));
	}
}
