package org.xacml4j.v30.spi.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.InvocationFactory;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.Types;

import com.google.common.base.Preconditions;

class JavaMethodToFunctionSpecConverter
{
	private final static Logger log = LoggerFactory.getLogger(JavaMethodToFunctionSpecConverter.class);

	private final InvocationFactory invocationFactory;
	private final Types xacmlTypes;

	public JavaMethodToFunctionSpecConverter(
			Types types,
			InvocationFactory invocationFactory)
	{
		Preconditions.checkNotNull(invocationFactory);
		Preconditions.checkNotNull(types);
		this.xacmlTypes = types;
		this.invocationFactory = invocationFactory;
	}

	public FunctionSpec createFunctionSpec(Method m) throws XacmlSyntaxException
	{
		return createFunctionSpec(m, null);
	}

	public FunctionSpec createFunctionSpec(Method m, Object instance) throws XacmlSyntaxException
	{
		Preconditions.checkArgument(m != null, "Method can not be null");
		Preconditions.checkArgument(!m.getReturnType().equals(Void.TYPE),
				"Method=\"%s\" must have other then void return type", m
						.getName());
		Preconditions.checkArgument(Expression.class.isAssignableFrom(m
				.getReturnType()),
				"Method=\"%s\" must return XACML expression", m.getName());
		XacmlFuncSpec funcId = m.getAnnotation(XacmlFuncSpec.class);
		Preconditions.checkArgument(funcId != null,
				"Method=\"%s\" must be annotated via XacmlFunc annotation", m.getName());

		Preconditions.checkArgument((instance != null ^ Modifier.isStatic(m.getModifiers())),
						"Only static method can be annotiated via XacmlFunc annotiation, method=\"%s\" "
								+ "in the stateless function provider, " +
										"declaring class=\"%s\" is not static",
						m.getName(), m.getDeclaringClass());
		XacmlLegacyFunc legacyFuncId = m.getAnnotation(XacmlLegacyFunc.class);
		XacmlFuncReturnType returnType = m
				.getAnnotation(XacmlFuncReturnType.class);
		XacmlFuncReturnTypeResolver returnTypeResolver = m.getAnnotation(XacmlFuncReturnTypeResolver.class);
		XacmlFuncParamValidator validator = m.getAnnotation(XacmlFuncParamValidator.class);
		validateMethodReturnType(m);
		FunctionSpecBuilder b = FunctionSpecBuilder.builder(funcId.id(),
				(legacyFuncId == null) ? null : legacyFuncId.id(), xacmlTypes);
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
				AttributeExpType type = xacmlTypes.getType(param.typeId());
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
				b.param(param.isBag() ? type.bagType() : type, param.optional());
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
				AttributeExpType type = xacmlTypes.getType(param.typeId());
				b.param(param.isBag() ? type.bagType() : type, param.min(),
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
				XacmlFuncParamAnyAttribute param = (XacmlFuncParamAnyAttribute)params[i][0];
				b.anyAttribute(param.optional());
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
			AttributeExpType type = xacmlTypes.getType(returnType.typeId());
			return b.build(returnType.isBag() ? type.bagType() : type,
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
			throw new IllegalArgumentException(
					"Either \"XacmlFuncReturnTypeResolver\" or "
							+ "\"XacmlFuncReturnType\" annotation must be specified, not both");
		}
		if (returnTypeResolver != null) {
			return;
		}
			if (returnType.isBag() &&
					!BagOfAttributeExp.class.isAssignableFrom(m.getReturnType())) {
				throw new IllegalArgumentException(
						String
								.format(
										"Method=\"%s\" return type declared XACML "
												+ "bag of=\"%s\" but method returns type=\"%s\"",
										m.getName(), returnType.typeId(), m
												.getReturnType()));
			}
		if(!returnType.isBag() && BagOfAttributeExp.class.isAssignableFrom(m.getReturnType())) {
			throw new IllegalArgumentException(String.format(
					"Method=\"%s\" return type declared XACML attribute type=\"%s\" "
							+ "but method returns=\"%s\"", m.getName(),
					returnType.typeId(), m.getReturnType()));
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
}
