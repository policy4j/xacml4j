package com.artagon.xacml.v30.spi.function;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.util.DefaultInvocationFactory;

public class JavaMethodToFunctionSpecTest 
{
	private JavaMethodToFunctionSpecConverter builder;
	
	@Before
	public void init(){
		this.builder = new JavaMethodToFunctionSpecConverter(new DefaultInvocationFactory());
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
