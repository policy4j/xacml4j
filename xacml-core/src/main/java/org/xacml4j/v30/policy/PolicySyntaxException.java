package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2022 Xacml4J.org
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

import javax.xml.stream.Location;

import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncParamOptional;
import org.xacml4j.v30.policy.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;

public class PolicySyntaxException extends SyntaxException
{
	public PolicySyntaxException(String template, Object... arguments) {
		super(template, arguments);
	}

	public PolicySyntaxException(
			String policyId,
			Location location,
			String errorMessage,
			Object... errorMessageArgs) {
		super(String.format(
				"Policy=\"%s\" syntax error at " +
						"line=\"%s\" column=\"%s\", error: %s",
				policyId,
				location.getLineNumber(),
				location.getColumnNumber(),
				String.format(errorMessage,
				              errorMessageArgs)));
	}

	public PolicySyntaxException(Throwable cause) {
		super(cause);
	}

	public static SyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParam param, Method functionMethod){
		return new PolicySyntaxException(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName());
	}

	public static SyntaxException invalidFunctionParameter(String funcId, FunctionParamSpec paramSpec){
		return new PolicySyntaxException(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\"",
				funcId, paramSpec);
	}

	public static SyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParamOptional param, Method functionMethod){
		return new PolicySyntaxException(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName());
	}

	public static SyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParamVarArg param, Method functionMethod){
		return new PolicySyntaxException(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName());
	}

	public static SyntaxException invalidFunction(String id){
		return new SyntaxException("Invalid XACML functionId=\"%s\"",  id);
	}

	public static SyntaxException invalidFunctionParam(String id, FunctionParamSpec spec){
		return new SyntaxException("Invalid XACML functionId=\"%s\" param={}",  id, spec);
	}


}
