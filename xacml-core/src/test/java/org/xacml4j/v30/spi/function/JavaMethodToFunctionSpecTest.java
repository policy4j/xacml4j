package org.xacml4j.v30.spi.function;

import static org.easymock.EasyMock.createControl;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.xacml4j.util.DefaultInvocationFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.Types;


public class JavaMethodToFunctionSpecTest
{
	private EvaluationContext context;
	private JavaMethodToFunctionSpecConverter builder;
	private IMocksControl c;
	@Before
	public void init(){
		this.c = createControl();
		this.context = c.createMock(EvaluationContext.class);
		this.builder = new JavaMethodToFunctionSpecConverter(Types.builder().defaultTypes().create(), new DefaultInvocationFactory());
	}

	@Test(expected=IllegalArgumentException.class)
	public void missingXacmlFuncAnnotation() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("missingXacmlFuncAnnotation"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void missingReturnTypeDeclaration1() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("missingReturnTypeDeclaration1"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void returnsVoid() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnsVoid"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void returnsNonXacmlExpression() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnsNonXacmlExpression"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void returnTypeDeclarationExistButWrongMethodReturnType1() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnTypeDeclarationExistButWrongMethodReturnType1"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void returnTypeDeclarationExistButWrongMethodReturnType2() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnTypeDeclarationExistButWrongMethodReturnType2"));
	}
	
	@Test
	public void optionalParametersTest() throws Exception
	{
		FunctionSpec test = builder.createFunctionSpec(getTestMethod("optionalParametersTest"));
		test.invoke(context, Arrays.<Expression>asList(null, null));
		assertTrue(test.validateParameters(Arrays.<Expression>asList(null, null)));
	}

	private static Method getTestMethod(String name)
	{
		for(Method m : JavaMethodToFunctionSpecTestFunctions.class.getDeclaredMethods()){
			if(m.getName().equals(name)){
				return m;
			}
		}
		return null;
	}
}
