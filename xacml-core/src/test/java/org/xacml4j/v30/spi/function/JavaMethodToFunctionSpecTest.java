package org.xacml4j.v30.spi.function;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.util.DefaultInvocationFactory;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.StringExp;


public class JavaMethodToFunctionSpecTest
{
	private JavaMethodToFunctionSpecConverter builder;
	
	@Before
	public void init(){
		this.builder = new JavaMethodToFunctionSpecConverter(new DefaultInvocationFactory());
	}

	@Test(expected=XacmlSyntaxException.class)
	public void missingXacmlFuncAnnotation() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("missingXacmlFuncAnnotation"));
	}

	@Test(expected=XacmlSyntaxException.class)
	public void missingReturnTypeDeclaration1() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("missingReturnTypeDeclaration1"));
	}

	@Test(expected=XacmlSyntaxException.class)
	public void returnsVoid() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnsVoid"));
	}

	@Test(expected=XacmlSyntaxException.class)
	public void returnsNonXacmlExpression() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnsNonXacmlExpression"));
	}

	@Test(expected=XacmlSyntaxException.class)
	public void returnTypeDeclarationExistButWrongMethodReturnType1() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnTypeDeclarationExistButWrongMethodReturnType1"));
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void returnTypeDeclarationExistButWrongMethodReturnType2() throws Exception
	{
		builder.createFunctionSpec(getTestMethod("returnTypeDeclarationExistButWrongMethodReturnType2"));
	}
	
	@Test
	public void optionalParametersTest() throws Exception
	{
		FunctionSpec spec = builder.createFunctionSpec(getTestMethod("optionalParametersTest"));
		assertEquals(StringExp.bag().value("false", "true").build(), spec.getParamSpecAt(1).getDefaultValue());
		assertEquals(
				StringExp.valueOf("false"), spec.getParamSpecAt(2).getDefaultValue());
		
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
