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

import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamOptional;
import org.xacml4j.v30.spi.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;

import java.lang.reflect.Method;
import java.util.Arrays;


public class XacmlSyntaxException extends XacmlException
{
	private static final long serialVersionUID = 5208193385563540743L;

	public XacmlSyntaxException(String template, Object... arguments) {
		super(Status.syntaxError()
						.detail(String.format(template, arguments)).build(),
				String.format(template, arguments));
	}


	public XacmlSyntaxException(Throwable cause) {
		super(Status.syntaxError().detail(cause).build(),
				cause);
	}

	public static XacmlSyntaxException invalidAttributeValue(Object v, AttributeValueType expectedType){
		return new XacmlSyntaxException(
				"Invalid XACML type=\"%s\" attribute value=\"%s\"",
				expectedType.getAbbrevDataTypeId(), v);
	}

	public static XacmlSyntaxException invalidDataTypeId(Object v){
		return new XacmlSyntaxException(
				"Invalid XACML type identifier=\"%s\"",
				v);
	}

	public static XacmlSyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParam param, Method functionMethod){
		return new XacmlSyntaxException(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName());
	}

	public static XacmlSyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParamOptional param, Method functionMethod){
		return new XacmlSyntaxException(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName());
	}

	public static XacmlSyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParamVarArg param, Method functionMethod){
		return new XacmlSyntaxException(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName());
	}

	public static XacmlSyntaxException invalidCategoryId(Object categoryId){
		return new XacmlSyntaxException(
				"Invalid XACML categoryId=\"%s\"",
				categoryId);
	}

	public static XacmlSyntaxException invalidCategoryId(Object categoryId, String info, Object...params){
		return new XacmlSyntaxException(
				"Invalid XACML categoryId=\"%s\", - s%",
				categoryId, String.format(info, params));
	}

	public static XacmlSyntaxException noContentFound(String message, Object ...p){
		return new XacmlSyntaxException(message == null?
				"No content found":message, p);
	}

	public static XacmlSyntaxException noContentFound(){
		return noContentFound(null);
	}

	public static XacmlSyntaxException invalidXml(String message, Throwable t){
		return new XacmlSyntaxException(
				"Invalid XML file=\"{}\"", message, t);
	}

	public static XacmlSyntaxException syntaxError(String message, Object ...params){
		return new XacmlSyntaxException(message, params);
	}


	public static XacmlSyntaxException invalidAttributeValue(Object v, AttributeValueType...excpectedTypes){
		return new XacmlSyntaxException(
				"Invalid XACML attribute value for type=\"%s\" attribute " +
						"value=\"%s\"", Arrays.toString(excpectedTypes), v);
	}

	public static XacmlSyntaxException invalidFunction(String id){
		return new XacmlSyntaxException("Invalid XACML functionId=\"{}\"",  id);
	}

	public static XacmlSyntaxException invalidResolverMethod(Method m, String message, Object ...params){
		return new XacmlSyntaxException("Invalid XACML resolver method=\"%s\" class=\"%%s\", details - %s",
				m.getName(), m.getDeclaringClass().getName(), String.format(message, params));
	}

}
