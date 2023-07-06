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

import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncParamOptional;
import org.xacml4j.v30.policy.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;

public class PolicySyntaxException extends SyntaxException
{
	public PolicySyntaxException(String message, Object ...params) {
		super(String.format(message, params));
	}

	public PolicySyntaxException(Throwable cause) {
		super(cause);
	}

	public PolicySyntaxException(String message, Throwable cause) {
		super(message, cause);
	}

	public static PolicySyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParam param, Method functionMethod){
		return new PolicySyntaxException(format(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName()));
	}

	public static PolicySyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParam param,
																 int position,
	                                                             String error){
		return new PolicySyntaxException(format(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" at=\"%d\" type=\"%s\" - %s",
				funcSpec.id(), param.getClass().getSimpleName(), position, param.typeId() , error));
	}

	public static PolicySyntaxException invalidFunctionParameter(String functionId, XacmlFuncParam param,
	                                                             int position,
	                                                             String error){
		return new PolicySyntaxException(format(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" at=\"%d\" type=\"%s\" - %s",
				functionId, param.getClass().getSimpleName(), position, param.typeId() , error));
	}

	public static PolicySyntaxException invalidFunctionParameter(String funcId, FunctionParamSpec paramSpec){
		return new PolicySyntaxException(format(
				"Invalid XACML function=\"%s\", parameter=\"%s\"",
				funcId, paramSpec));
	}

	public static PolicySyntaxException invalidFunctionParameter(String funcId, FunctionParamSpec paramSpec, int index,
	                                                             String error){
		return new PolicySyntaxException(format(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" at=\"%d\" -  %s",
				funcId, paramSpec, index, error));
	}

	public static PolicySyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec, XacmlFuncParamOptional param, Method functionMethod){
		return new PolicySyntaxException(format(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName()));
	}

	public static PolicySyntaxException invalidFunctionParameter(XacmlFuncSpec funcSpec,
	                                                             XacmlFuncParamVarArg param, Method functionMethod){
		return new PolicySyntaxException(format(
				"Invalid XACML function=\"%s\", parameter=\"{%s}\" method=\"%s\"",
				funcSpec.id(), param.typeId(), functionMethod.getName()));
	}

	public static PolicySyntaxException invalidFunction(String id){
		return new PolicySyntaxException(format("Invalid XACML functionId=\"%s\"",  id));
	}

	public static PolicySyntaxException invalidFunction(String id, String message){
		return new PolicySyntaxException(format("Invalid XACML functionId=\"%s\" - %s",
		                                        id, message));
	}

	public static PolicySyntaxException invalidCombingAlgorithm(String id){
		return new PolicySyntaxException(format("Invalid XACML combingAlgorithmId=\"%s\"",  id));
	}

	public static PolicySyntaxException invalidFunctionParam(String id, FunctionParamSpec spec){
		return new PolicySyntaxException(format("Invalid XACML functionId=\"%s\" param=\"%s\"",  id, spec));
	}


	public static PolicySyntaxException invalidFunctionParam(String id, FunctionParamSpec spec, int index, String error){
		return new PolicySyntaxException(format(
				"Invalid XACML functionId=\"%s\" parameter=\"%s\" at=\"%d\" - %s",  id, spec, index, error));
	}

	public static PolicySyntaxException invalidParam(FunctionParamSpec spec, int index, String error){
		return new PolicySyntaxException(format(
				"Invalid XACML parameter=\"%s\" at=\"%d\" - %s",  spec, index, error));
	}


}
