package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Today's function call behavior, which cycle D of change optimize-pdp-evaluation must keep
 * (design D8, review rv005): argument order, optional defaults, variadic arguments, lazy
 * functions that receive unevaluated arguments, validation at runtime, and failure handling.
 */
public class FunctionInvocationCharacterizationTest
{
	private FunctionInvocation impl;
	private EvaluationContext context;

	@BeforeEach
	public void init()
	{
		impl = mock(FunctionInvocation.class);
		context = mock(EvaluationContext.class);
	}

	@SuppressWarnings("unchecked")
	private List<Expression> argumentsSeen(FunctionSpec spec) throws EvaluationException
	{
		ArgumentCaptor<List<Expression>> captor = ArgumentCaptor.forClass(List.class);
		verify(impl).invoke(eq(spec), eq(context), captor.capture());
		return captor.getValue();
	}

	@Test
	public void fixedArityEagerArgumentsArriveEvaluatedAndInOrder() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:f2")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, impl);
		when(impl.invoke(eq(spec), eq(context), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		Object result = spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2));

		assertThat(result).isEqualTo(XacmlTypes.BOOLEAN.ofAny(true));
		assertThat(argumentsSeen(spec)).containsExactly(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).inOrder();
	}

	@Test
	public void optionalParameterGetsItsDefault() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:optional")
				.param(XacmlTypes.INTEGER).optional(XacmlTypes.STRING, XacmlTypes.STRING.ofAny("aaa"))
				.build(XacmlTypes.BOOLEAN, impl);
		when(impl.invoke(eq(spec), eq(context), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		spec.invoke(context, XacmlTypes.INTEGER.ofAny(1));

		assertThat(argumentsSeen(spec)).containsExactly(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.STRING.ofAny("aaa")).inOrder();
	}

	@Test
	public void variadicParameterKeepsEveryArgument() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:variadic")
				.param(XacmlTypes.INTEGER).varArg(XacmlTypes.INTEGER, 0, 3).build(XacmlTypes.BOOLEAN, impl);
		when(impl.invoke(eq(spec), eq(context), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2), XacmlTypes.INTEGER.ofAny(3));

		assertThat(argumentsSeen(spec)).containsExactly(
				XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2), XacmlTypes.INTEGER.ofAny(3)).inOrder();
	}

	@Test
	public void twoArgumentCallsOfOptionalAndVariadicFunctionsNormalizeAsBefore() throws EvaluationException
	{
		FunctionSpec optional = FunctionSpecBuilder.builder("urn:xacml4j:test:optional2")
				.param(XacmlTypes.INTEGER).optional(XacmlTypes.STRING, XacmlTypes.STRING.ofAny("aaa"))
				.build(XacmlTypes.BOOLEAN, impl);
		FunctionSpec variadic = FunctionSpecBuilder.builder("urn:xacml4j:test:variadic2")
				.param(XacmlTypes.INTEGER).varArg(XacmlTypes.INTEGER, 0, 3).build(XacmlTypes.BOOLEAN, impl);
		when(impl.invoke(any(), eq(context), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		optional.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.STRING.ofAny("b"));
		variadic.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2));

		assertThat(argumentsSeen(optional)).containsExactly(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.STRING.ofAny("b")).inOrder();
		assertThat(argumentsSeen(variadic)).containsExactly(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).inOrder();
	}

	@Test
	public void lazyFixedArityFunctionGetsItsArgumentsUnevaluated() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:lazy").lazyArgEval()
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, impl);
		Expression failing = mock(Expression.class);
		when(failing.evaluate(any())).thenThrow(new IllegalStateException("must stay unevaluated"));
		when(impl.invoke(eq(spec), eq(context), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));

		spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), failing);

		assertThat(argumentsSeen(spec)).containsExactly(XacmlTypes.INTEGER.ofAny(1), failing).inOrder();
		verify(failing, never()).evaluate(any());
	}

	@Test
	public void runtimeValidationRejectsAnArgumentOfTheWrongType() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:validated")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, impl);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);

		assertThrows(FunctionInvocationException.class,
				() -> spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.STRING.ofAny("x")));
		verify(impl, never()).invoke(any(), any(), anyList());
	}

	@Test
	public void evaluationFailuresPassThroughAndOtherFailuresAreWrapped() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:failing")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, impl);
		FunctionInvocationException own = new FunctionInvocationException(spec, "own failure");
		when(impl.invoke(eq(spec), eq(context), anyList()))
				.thenThrow(own)
				.thenThrow(new IllegalStateException("bug"));

		EvaluationException first = assertThrows(EvaluationException.class,
				() -> spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)));
		FunctionInvocationException second = assertThrows(FunctionInvocationException.class,
				() -> spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)));

		assertSame(own, first);
		assertThat(second).hasCauseThat().isInstanceOf(IllegalStateException.class);
	}

	/** Reviews RVC1 and RVC2: a function implementation may change its argument list, as before. */
	@Test
	public void eagerPairInvocationMayChangeItsArgumentList() throws EvaluationException
	{
		FunctionInvocation consuming = new FunctionInvocation(){
			@Override
			public org.xacml4j.v30.ValueExp invoke(FunctionSpec spec, EvaluationContext context, Expression... arguments){
				return invoke(spec, context, new java.util.ArrayList<>(List.of(arguments)));
			}

			@Override
			public org.xacml4j.v30.ValueExp invoke(FunctionSpec spec, EvaluationContext context, List<Expression> arguments){
				Expression first = arguments.remove(0);
				arguments.add(first);
				return XacmlTypes.BOOLEAN.ofAny(arguments.size() == 2);
			}
		};
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:consuming")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, consuming);

		Object pair = spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2));
		Object list = spec.invokeWithList(context, List.of(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)));

		assertThat(pair).isEqualTo(XacmlTypes.BOOLEAN.ofAny(true));
		assertThat(list).isEqualTo(XacmlTypes.BOOLEAN.ofAny(true));
	}

	/** Review rv009: with validation at runtime, a validator that keeps its list does not see the call change it. */
	@Test
	public void validatorListIsNotChangedByTheCall() throws EvaluationException
	{
		List<List<Expression>> seen = new java.util.ArrayList<>();
		FunctionParametersValidator keeping = (spec, arguments) -> {
			seen.add(arguments);
			return true;
		};
		FunctionInvocation consuming = new FunctionInvocation(){
			@Override
			public org.xacml4j.v30.ValueExp invoke(FunctionSpec spec, EvaluationContext context, Expression... arguments){
				return invoke(spec, context, new java.util.ArrayList<>(List.of(arguments)));
			}

			@Override
			public org.xacml4j.v30.ValueExp invoke(FunctionSpec spec, EvaluationContext context, List<Expression> arguments){
				arguments.remove(1);
				return XacmlTypes.BOOLEAN.ofAny(true);
			}
		};
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:validated-pair")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER)
				.build(new StaticFunctionReturnTypeResolver(XacmlTypes.BOOLEAN), keeping, consuming);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);

		spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2));
		spec.invokeWithList(context, List.of(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)));

		assertThat(seen).hasSize(2);
		for(List<Expression> kept : seen){
			assertThat(kept).containsExactly(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).inOrder();
		}
	}

	/** Reviews RVC1 and RVC2: a custom spec that overrides the variadic invoke keeps it for two arguments. */
	@Test
	public void twoArgumentCallKeepsAnOverriddenVariadicInvoke() throws Throwable
	{
		FunctionSpec real = FunctionSpecBuilder.builder("urn:xacml4j:test:custom")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, impl);
		FunctionSpec custom = (FunctionSpec) java.lang.reflect.Proxy.newProxyInstance(
				FunctionSpec.class.getClassLoader(), new Class<?>[]{FunctionSpec.class}, (proxy, method, args) -> {
					if(method.getName().equals("invoke") && method.getParameterCount() == 2){
						return XacmlTypes.BOOLEAN.ofAny(true);
					}
					if(method.getName().equals("invokeWithList")){
						return XacmlTypes.BOOLEAN.ofAny(false);
					}
					if(method.isDefault()){
						return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
					}
					return method.invoke(real, args);
				});

		Object direct = custom.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2));
		org.xacml4j.v30.policy.Match match = org.xacml4j.v30.policy.Match.builder()
				.predicate(custom)
				.attribute(XacmlTypes.INTEGER.ofAny(1))
				.attrRef(org.xacml4j.v30.policy.AttributeDesignator.builder()
						.key(org.xacml4j.v30.AttributeDesignatorKey.builder("urn:xacml4j:test:id")
								.category(CategoryId.RESOURCE).dataType(XacmlTypes.INTEGER).build())
						.mustBePresent(false).build())
				.build();
		when(context.resolve(any(org.xacml4j.v30.AttributeReferenceKey.class)))
				.thenReturn(java.util.Optional.of(XacmlTypes.INTEGER.bagBuilder().attribute(XacmlTypes.INTEGER.ofAny(2)).build()));

		assertThat(direct).isEqualTo(XacmlTypes.BOOLEAN.ofAny(true));
		assertThat(match.match(context)).isEqualTo(org.xacml4j.v30.MatchResult.MATCH);
	}

	/** Cycle J (design D16): normalization gives the same arguments for a list without random access. */
	@Test
	public void linkedListArgumentsNormalizeAsArrayListArguments() throws EvaluationException
	{
		FunctionSpec optional = FunctionSpecBuilder.builder("urn:xacml4j:test:optional-linked")
				.param(XacmlTypes.INTEGER).optional(XacmlTypes.STRING, XacmlTypes.STRING.ofAny("aaa"))
				.build(XacmlTypes.BOOLEAN, impl);
		FunctionSpec variadic = FunctionSpecBuilder.builder("urn:xacml4j:test:variadic-linked")
				.param(XacmlTypes.INTEGER).varArg(XacmlTypes.INTEGER, 0, 3).build(XacmlTypes.BOOLEAN, impl);
		when(impl.invoke(any(), eq(context), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		List<Expression> one = List.of(XacmlTypes.INTEGER.ofAny(1));
		List<Expression> three = List.of(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2), XacmlTypes.INTEGER.ofAny(3));

		optional.invokeWithList(context, new java.util.LinkedList<>(one));
		optional.invokeWithList(context, new java.util.ArrayList<>(one));
		variadic.invokeWithList(context, new java.util.LinkedList<>(three));
		variadic.invokeWithList(context, new java.util.ArrayList<>(three));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Expression>> captor = ArgumentCaptor.forClass(List.class);
		verify(impl, org.mockito.Mockito.times(2)).invoke(eq(optional), eq(context), captor.capture());
		assertThat(captor.getAllValues().get(0)).isEqualTo(captor.getAllValues().get(1));
		assertThat(captor.getAllValues().get(0)).containsExactly(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.STRING.ofAny("aaa")).inOrder();
		ArgumentCaptor<List<Expression>> variadicCaptor = ArgumentCaptor.forClass((Class) List.class);
		verify(impl, org.mockito.Mockito.times(2)).invoke(eq(variadic), eq(context), variadicCaptor.capture());
		assertThat(variadicCaptor.getAllValues().get(0)).isEqualTo(variadicCaptor.getAllValues().get(1));
		assertThat(variadicCaptor.getAllValues().get(0)).containsExactlyElementsIn(three).inOrder();
	}

	/**
	 * Review RCJ2: normalization reads a caller's list as its iterator would; a random-access view over a
	 * {@code CopyOnWriteArrayList} that changes while it is read gives the snapshot of the backing list.
	 */
	@Test
	public void callerListIsNormalizedAsItsSnapshot() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:three")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER)
				.build(XacmlTypes.BOOLEAN, impl);
		when(impl.invoke(eq(spec), eq(context), anyList())).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		for(boolean remove : new boolean[]{false, true}){
			java.util.concurrent.CopyOnWriteArrayList<Integer> backing = new java.util.concurrent.CopyOnWriteArrayList<>(List.of(1, 2, 3));
			List<Expression> arguments = com.google.common.collect.Lists.transform(backing, i -> {
				if(i == 1){
					if(remove){
						backing.remove(2);
					}else{
						backing.set(2, 4);
					}
				}
				return XacmlTypes.INTEGER.ofAny(i);
			});

			spec.invokeWithList(context, arguments);
		}

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Expression>> captor = ArgumentCaptor.forClass(List.class);
		verify(impl, org.mockito.Mockito.times(2)).invoke(eq(spec), eq(context), captor.capture());
		for(List<Expression> seen : captor.getAllValues()){
			assertThat(seen).containsExactly(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2), XacmlTypes.INTEGER.ofAny(3)).inOrder();
		}
	}

	@Test
	public void wrappedFailureKeepsTheStatusOfANestedEvaluationException() throws EvaluationException
	{
		FunctionSpec spec = FunctionSpecBuilder.builder("urn:xacml4j:test:status")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, impl);
		AttributeDesignatorKey key = AttributeDesignatorKey.builder()
				.category(CategoryId.RESOURCE).attributeId("urn:xacml4j:test:id").dataType(XacmlTypes.INTEGER).build();
		when(impl.invoke(eq(spec), eq(context), anyList()))
				.thenThrow(new IllegalStateException(AttributeReferenceEvaluationException.forSyntaxError(key, new IllegalArgumentException("bad"))))
				.thenThrow(new IllegalStateException("bug"));

		FunctionInvocationException nested = assertThrows(FunctionInvocationException.class,
				() -> spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)));
		FunctionInvocationException plain = assertThrows(FunctionInvocationException.class,
				() -> spec.invoke(context, XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)));

		assertThat(nested.getStatus().getStatusCode().getValue()).isEqualTo(StatusCodeId.SYNTAX_ERROR);
		assertThat(plain.getStatus().isProcessingError()).isTrue();
	}
}
