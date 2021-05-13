package org.xacml4j.v30.spi.function;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.InvocationFactory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class JavaMethodToFunctionSpecConverter
{
	private final static Logger log = LoggerFactory.getLogger(JavaMethodToFunctionSpecConverter.class);

	private final InvocationFactory invocationFactory;

	private final static TypeCapability.Index<TypeToString> TYPE_TO_STRING_INDEX = TypeCapability.Index.<TypeToString>build(TypeToString.Types.values());

	public JavaMethodToFunctionSpecConverter(
			InvocationFactory invocationFactory)
	{
		Preconditions.checkNotNull(invocationFactory);
		this.invocationFactory = invocationFactory;
	}

	public FunctionSpec createFunctionSpec(Method m) throws XacmlSyntaxException
	{
		return createFunctionSpec(m, null);
	}

	public FunctionSpec createFunctionSpec(Method m, Object instance) throws XacmlSyntaxException
	{
		Preconditions.checkArgument(m != null, "Method can not be null");
		if(m.getReturnType().equals(Void.TYPE)){
			throw new XacmlSyntaxException("Method=\"%s\" must " +
					"have other then void return type", m.getName());
		}
		if(!Expression.class.isAssignableFrom(m.getReturnType())){
			throw new XacmlSyntaxException("Method=\"%s\" must " +
					"return XACML expression", m.getName());
		}
		XacmlFuncSpec funcId = m.getAnnotation(XacmlFuncSpec.class);
		if(funcId == null){
			throw new XacmlSyntaxException("Method=\"%s\" must be " +
					"annotated via XacmlFunc annotation", m.getName());
		}
		if(!(instance != null ^ Modifier.isStatic(m.getModifiers()))){
			throw new XacmlSyntaxException("Only static method can be annotated " +
					"via XacmlFunc annotation, method=\"%s\" "
					+ "in the stateless function provider, " +
					"declaring class=\"%s\" is not static", m.getName(), m.getDeclaringClass());
		}
		XacmlLegacyFunc legacyFuncId = m.getAnnotation(XacmlLegacyFunc.class);
		XacmlFuncReturnType returnType = m.getAnnotation(XacmlFuncReturnType.class);
		XacmlFuncReturnTypeResolver returnTypeResolver = m.getAnnotation(XacmlFuncReturnTypeResolver.class);
		XacmlFuncParamValidator validator = m.getAnnotation(XacmlFuncParamValidator.class);
		validateMethodReturnType(m);
		FunctionSpecBuilder b = FunctionSpecBuilder.builder(funcId.id(),
				(legacyFuncId == null) ? null : legacyFuncId.id());
		Annotation[][] params = m.getParameterAnnotations();
		Class<?>[] types = m.getParameterTypes();
		boolean evalContextParamFound = false;
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null) {
				throw new IllegalArgumentException(String.format(
						"Method=\"%s\" contains parameter without annotation",
						m.getName()));
			}
			if (params[i][0] instanceof XacmlFuncParamEvaluationContext) {
				if (!types[i].isInstance(EvaluationContext.class)) {
					// FIXME: why is the exception not thrown?
					new IllegalArgumentException("XACML evaluation context "
											+ "annotation annotates wrong parameter type");
				}
				if (i > 0) {
					// FIXME: why is the exception not thrown?
					new IllegalArgumentException(String.format(
							"XACML evaluation context parameter must "
									+ "be a first parameter "
									+ "in the method=\"%s\" signature", m.getName()));
				}
				evalContextParamFound = true;
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParam) {
				XacmlFuncParam param = (XacmlFuncParam) params[i][0];
				Optional<AttributeExpType> type = XacmlTypes.getType(param.typeId());
				if(!type.isPresent()){
					throw new XacmlSyntaxException(
							"Unknown XACML type id=\"%s\"", param.typeId());
				}
				if (param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
					log.debug("Expecting bag at index=\"{}\", actual type type=\"{}\"",
							i, types[i].getName());
					throw new IllegalArgumentException(String.format(
							"Parameter type annotates bag of=\"%s\" "
									+ "but method=\"%s\" is of class=\"%s\"",
							type, m.getName(), types[i]));
				}
				if (!param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
					log.debug("Expecting attribute value at index=\"{}\", "
							+ "actual type type=\"{}\"", i, types[i].getName());
					throw new IllegalArgumentException(
							String.format(
									"Parameter type annotates attribute value of "
											+ "type=\"%s\" but method=\"%s\" parameter is type of=\"%s\"",
									type, m.getName(), types[i]));
				}

				b.param(param.isBag() ? type.get().bagType() : type.get(), null, false);
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParamOptional) {
				XacmlFuncParamOptional param = (XacmlFuncParamOptional) params[i][0];
				Optional<AttributeExpType> type = XacmlTypes.getType(param.typeId());
				if(!type.isPresent()){
					throw new XacmlSyntaxException(
							"Unknown XACML type id=\"%s\"", param.typeId());
				}
				if (param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
					log.debug("Expecting bag at index=\"{}\", actual type type=\"{}\"",
							i, types[i].getName());
					throw new IllegalArgumentException(String.format(
							"Parameter type annotates bag of=\"%s\" "
									+ "but method=\"%s\" is of class=\"%s\"",
							type, m.getName(), types[i]));
				}
				if (!param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
					log.debug("Expecting attribute value at index=\"{}\", "
							+ "actual type type=\"{}\"", i, types[i].getName());
					throw new IllegalArgumentException(
							String.format(
									"Parameter type annotates attribute value of "
											+ "type=\"%s\" but method=\"%s\" parameter is type of=\"%s\"",
									type, m.getName(), types[i]));
				}

				b.param(param.isBag() ? type.get().bagType() : type.get(),
						createDefaultValue(type.get(), param.isBag(), param.value()), true);
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParamVarArg) {
				if (!m.isVarArgs()) {
					throw new IllegalArgumentException(
							String.format("Found varArg parameter "
									+ "declaration but actual method=\"%s\" is not varArg",
									m.getName()));
				}
				if (m.isVarArgs() && i < params.length - 1) {
					throw new IllegalArgumentException(
							String.format("Found varArg parameter "
									+ "declaration in incorrect place, "
									+ "varArg parameter must be a last parameter in the method"));
				}
				XacmlFuncParamVarArg param = (XacmlFuncParamVarArg) params[i][0];
				Optional<AttributeExpType> type = XacmlTypes.getType(param.typeId());
				if(!type.isPresent()){
					throw new XacmlSyntaxException(
							"Unknown XACML type id=\"%s\"", param.typeId());
				}
				b.varArg(param.isBag() ? type.get().bagType() : type.get(), param.min(),
						param.max());
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParamFunctionReference) {
				b.funcRefParam();
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParamAnyBag) {
				b.anyBag();
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParamAnyAttribute) {
				//XacmlFuncParamAnyAttribute param = (XacmlFuncParamAnyAttribute)params[i][0];
				b.anyAttribute();
				continue;
			}
			if (params[i][0] == null) {
				throw new IllegalArgumentException(String.format(
						"Found method=\"%s\" parameter at "
								+ "index=\"%s\" with no annotation", m
								.getName(), i));
			}
			throw new IllegalArgumentException(String.format(
					"Found method=\"%s\" parameter at "
							+ "index=\"%s\" with unknown annotation=\"%s\"", m
							.getName(), i, params[i][0]));
		}
		if (returnType != null) {
			Optional<AttributeExpType> type = XacmlTypes.getType(returnType.typeId());
			if(!type.isPresent()){
				throw new XacmlSyntaxException(
						"Unknown XACML type id=\"%s\"", returnType.typeId());
			}
			return b.build(returnType.isBag() ? type.get().bagType() : type.get(),
					(validator != null) ? createValidator(validator
							.validatorClass()) : null,
					new DefaultFunctionInvocation(invocationFactory
							.<ValueExpression>create(instance, m),
							evalContextParamFound));
		}
		if (returnTypeResolver != null) {
			return b.build(createResolver(returnTypeResolver.resolverClass()),
					(validator != null) ? createValidator(validator
							.validatorClass()) : null,
					new DefaultFunctionInvocation(invocationFactory
							.<ValueExpression>create(instance, m),
							evalContextParamFound));
		}
		throw new IllegalArgumentException(
				"Either static return type or return type resolver must be specified");
	}

	private void validateMethodReturnType(Method m) {
		XacmlFuncReturnType returnType = m
				.getAnnotation(XacmlFuncReturnType.class);
		XacmlFuncReturnTypeResolver returnTypeResolver = m
				.getAnnotation(XacmlFuncReturnTypeResolver.class);
		if (!(returnType == null ^ returnTypeResolver == null)) {
			throw new XacmlSyntaxException(
					"Either \"XacmlFuncReturnTypeResolver\" or "
							+ "\"XacmlFuncReturnType\" annotation must be specified, not both");
		}
		if (returnTypeResolver != null) {
			return;
		}
		if (returnType.isBag() &&
					!BagOfAttributeExp.class.isAssignableFrom(m.getReturnType())) {
			throw new XacmlSyntaxException(
					"Method=\"%s\" return type declared XACML " +
					"bag of=\"%s\" but method returns type=\"%s\"",
					m.getName(), returnType.typeId(), m.getReturnType());
		}
		if(!returnType.isBag() && BagOfAttributeExp.class.isAssignableFrom(m.getReturnType())) {
			throw new XacmlSyntaxException("Method=\"%s\" return type declared XACML attribute type=\"%s\" "
							+ "but method returns=\"%s\"", m.getName(),
					returnType.typeId(), m.getReturnType());
		}
	}

	private FunctionReturnTypeResolver createResolver(
			Class<? extends FunctionReturnTypeResolver> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format(
					"Failed with error=\"%s\" to build instance of "
							+ "function return type resolver, class=\"%s\"", e
							.getMessage(), clazz.getName()));
		}
	}

	private FunctionParametersValidator createValidator(
			Class<? extends FunctionParametersValidator> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format(
					"Failed with error=\"%s\" to build instance of "
							+ "function parameter validator, class=\"%s\"", e
							.getMessage(), clazz.getName()));
		}
	}

	private ValueExpression createDefaultValue(AttributeExpType type,
			boolean isBag, String[] values)
	{
		if((values == null || values.length == 0)){
			return null;
		}
		Optional<TypeToString> fromString = TYPE_TO_STRING_INDEX.get(type);
		if(!fromString.isPresent()){
			throw new XacmlSyntaxException("Xacml type=\"%s\" does support default values in annotation",
					type.getDataTypeId());
		}
		List<AttributeExp> defaultValues = new ArrayList<AttributeExp>(values.length);
		for(String v : values){
			defaultValues.add(fromString.get().fromString(v));
		}
		return isBag?type.bagOf(defaultValues):defaultValues.iterator().next();
	}
}
