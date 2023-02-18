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
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.PolicySyntaxException;
import org.xacml4j.v30.policy.function.DefaultFunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionParametersValidator;
import org.xacml4j.v30.policy.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.policy.function.XacmlEvaluationContextParam;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncParamAnyAttribute;
import org.xacml4j.v30.policy.function.XacmlFuncParamAnyBag;
import org.xacml4j.v30.policy.function.XacmlFuncParamFunctionReference;
import org.xacml4j.v30.policy.function.XacmlFuncParamOptional;
import org.xacml4j.v30.policy.function.XacmlFuncParamValidator;
import org.xacml4j.v30.policy.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncReturnTypeResolver;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.policy.function.XacmlLegacyFunc;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;

class JavaMethodToFunctionSpecConverter
{
	private final static Logger LOG = LoggerFactory.getLogger(JavaMethodToFunctionSpecConverter.class);

	private final FunctionInvocationFactory invocationFactory;

	public JavaMethodToFunctionSpecConverter(
			FunctionInvocationFactory invocationFactory)
	{
		Preconditions.checkNotNull(invocationFactory);
		this.invocationFactory = invocationFactory;
	}

	@Nullable
	public FunctionSpec apply(@Nullable Method method) {
		return createFunctionSpec(method);
	}

	public FunctionSpec createFunctionSpec(Method m) throws SyntaxException
	{
		return createFunctionSpec(m, null);
	}

	public FunctionSpec createFunctionSpec(Method m, Object instance) throws SyntaxException
	{
		Preconditions.checkArgument(m != null, "Method can not be null");
		if(m.getReturnType().equals(Void.TYPE)){
			throw new PolicySyntaxException(
					"Method=\"%s\" must have other then void return type", m.getName());
		}

		if(!Expression.class.isAssignableFrom(m.getReturnType())){
			throw new PolicySyntaxException(
					"Method=\"%s\" must return XACML expression", m.getName());
		}
		XacmlFuncSpec funcId = m.getAnnotation(XacmlFuncSpec.class);
		if(funcId == null){
			throw new PolicySyntaxException("Method=\"%s\" must be " +
					"annotated via XacmlFunc annotation", m.getName());
		}
		if(!(instance != null ^ Modifier.isStatic(m.getModifiers()))){
			if(Optional.ofNullable(instance)
			        .map(o->o.getClass().getAnnotation(XacmlFunctionProvider.class))
			        .map(a->a.nonStaticFunctions())
			        .orElse(false)){
				throw new PolicySyntaxException("Only static method can be annotated " +
						                          "via XacmlFunc annotation, method=\"%s\" "
						                          + "in the stateless function provider, " +
						                          "declaring class=\"%s\" is not static", m.getName(), m.getDeclaringClass());
			};

		}
		XacmlLegacyFunc legacyFuncId = m.getAnnotation(XacmlLegacyFunc.class);
		XacmlFuncReturnType returnType = m.getAnnotation(XacmlFuncReturnType.class);
		XacmlFuncReturnTypeResolver returnTypeResolver = m.getAnnotation(XacmlFuncReturnTypeResolver.class);
		XacmlFuncParamValidator validator = m.getAnnotation(XacmlFuncParamValidator.class);
		validateMethodReturnType(m);
		FunctionSpecBuilder b = FunctionSpecBuilder
				.builder(funcId.id(),
				         (legacyFuncId == null) ? null : legacyFuncId.id());
		Annotation[][] params = m.getParameterAnnotations();
		Class<?>[] types = m.getParameterTypes();
		boolean evalContextParamFound = false;
		for (int i = 0; i < params.length; i++)
		{
			if (params[i] == null) {
				throw new PolicySyntaxException(String.format(
						"Method=\"%s\" contains parameter without annotation",
						m.getName()));
			}
			if (params[i][0] instanceof XacmlEvaluationContextParam) {
				if (!types[i].isAssignableFrom(org.xacml4j.v30.EvaluationContext.class)) {
					throw new PolicySyntaxException("XACML evaluation context "
											+ "annotation annotates wrong parameter type");
				}
				if (i > 0) {
					throw new PolicySyntaxException(String.format(
							"XACML evaluation context parameter must "
									+ "be a first parameter "
									+ "in the method=\"%s\" signature", m.getName()));
				}
				evalContextParamFound = true;
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParam) {
				XacmlFuncParam param = (XacmlFuncParam) params[i][0];
				java.util.Optional<ValueType> type = XacmlTypes.getType(param.typeId());
				if(!type.isPresent()){
					throw PolicySyntaxException
							.invalidDataTypeId(param.typeId());
				}
				if (param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
					LOG.error("Expecting bag at index=\"{}\", actual type type=\"{}\"", i, types[i].getName());
					throw PolicySyntaxException
							.invalidFunctionParameter(funcId, param, m);
				}
				if (!param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
						LOG.error("Expecting attribute value at index=\"{}\", "
								+ "actual type type=\"{}\"", i, types[i].getName());
					throw PolicySyntaxException
							.invalidFunctionParameter(funcId, param, m);
				}
				b.param(param.isBag() ? type.get().bagType() : type.get(), null, false);
				continue;
			}
			if (params[i][0] instanceof XacmlFuncParamOptional) {
				XacmlFuncParamOptional param = (XacmlFuncParamOptional) params[i][0];
				java.util.Optional<ValueType> type = XacmlTypes.getType(param.typeId());
				if(!type.isPresent()){
					throw PolicySyntaxException
							.invalidDataTypeId(param.typeId());
				}
				if (param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
					LOG.error("Expecting bag at index=\"{}\", actual type type=\"{}\"",
					          i, types[i].getName());
					throw PolicySyntaxException
							.invalidFunctionParameter(funcId, param, m);
				}
				if (!param.isBag()
						&& !Expression.class.isAssignableFrom(types[i])) {
					LOG.error("Expecting attribute value at index=\"{}\", "
							+ "actual type type=\"{}\"", i, types[i].getName());
					throw PolicySyntaxException
							.invalidFunctionParameter(funcId, param, m);
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
							"Found varArg parameter "
									+ "declaration in incorrect place, "
									+ "varArg parameter must be a last parameter in the method");
				}
				XacmlFuncParamVarArg param = (XacmlFuncParamVarArg) params[i][0];
				java.util.Optional<ValueType> type = XacmlTypes.getType(param.typeId());
				if(!type.isPresent()){
					throw new SyntaxException(
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
				throw new PolicySyntaxException(String.format(
						"Found method=\"%s\" parameter at "
								+ "index=\"%s\" with no annotation", m
								.getName(), i));
			}
			throw new PolicySyntaxException(String.format(
					"Found method=\"%s\" parameter at "
							+ "index=\"%s\" with unknown annotation=\"%s\"", m
							.getName(), i, params[i][0]));
		}
		if (returnType != null) {
			java.util.Optional<ValueType> type = XacmlTypes.getType(returnType.typeId());
			if(!type.isPresent()){
				throw new PolicySyntaxException(
						"Unknown XACML type id=\"%s\"", returnType.typeId());
			}
			return b.build(returnType.isBag() ? type.get().bagType() : type.get(),
					(validator != null) ? createValidator(validator
							.validatorClass()) : null,
					new DefaultFunctionInvocation(invocationFactory.create(instance, m),
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
		throw new PolicySyntaxException(
				"Either static return type or return type resolver must be specified");
	}

	private void validateMethodReturnType(Method m) {
		XacmlFuncReturnType returnType = m
				.getAnnotation(XacmlFuncReturnType.class);
		XacmlFuncReturnTypeResolver returnTypeResolver = m
				.getAnnotation(XacmlFuncReturnTypeResolver.class);
		if (!(returnType == null ^ returnTypeResolver == null)) {
			throw new SyntaxException(
					"Either \"XacmlFuncReturnTypeResolver\" or "
							+ "\"XacmlFuncReturnType\" annotation must be specified, not both");
		}
		if (returnTypeResolver != null) {
			return;
		}
		if (returnType.isBag() &&
					!BagOfValues.class.isAssignableFrom(m.getReturnType())) {
			throw new PolicySyntaxException(
					"Method=\"%s\" return type declared XACML " +
					"bag of=\"%s\" but method returns type=\"%s\"",
					m.getName(), returnType.typeId(), m.getReturnType());
		}
		if(!returnType.isBag() && BagOfValues.class.isAssignableFrom(m.getReturnType())) {
			throw new PolicySyntaxException("Method=\"%s\" return type declared XACML attribute type=\"%s\" "
							+ "but method returns=\"%s\"", m.getName(),
					returnType.typeId(), m.getReturnType());
		}
	}

	private FunctionReturnTypeResolver createResolver(
			Class<? extends FunctionReturnTypeResolver> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new SyntaxException(
					String.format(
						"Failed to build defaultProvider of function return type resolver, class=\"%s\"",
						clazz.getName()),
					e);
		}
	}

	private FunctionParametersValidator createValidator(
			Class<? extends FunctionParametersValidator> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new PolicySyntaxException(
					String.format(
							"Failed to build defaultProvider of function parameter validator, class=\"%s\"",
							clazz.getName()),
					e);
		}
	}

	private ValueExpression createDefaultValue(
			ValueType type,
			boolean isBag, String[] values)
	{
		if((values == null || values.length == 0)){
			return null;
		}
		Optional<TypeToString> fromString = TypeToString.forType(type);
		if(!fromString.isPresent()){
			throw new SyntaxException("Xacml type=\"%s\" does support default values in annotation",
					type.getDataTypeId());
		}
		List<Value> defaultValues = new ArrayList<Value>(values.length);
		for(String v : values){
			defaultValues.add(fromString.get().fromString(v));
		}
		return isBag?type.bag().attributes(defaultValues).build():defaultValues.iterator().next();
	}
}
