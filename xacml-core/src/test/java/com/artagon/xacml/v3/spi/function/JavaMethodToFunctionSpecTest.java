package com.artagon.xacml.v3.spi.function;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class JavaMethodToFunctionSpecTest 
{
	private JavaMethodToFunctionSpecConverter builder;
	
	@Before
	public void init(){
		this.builder = new JavaMethodToFunctionSpecConverter();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void missingXacmlFuncAnnotation()
	{
		builder.createFunctionSpec(getTestMethod("missingXacmlFuncAnnotation"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void missingReturnTypeDeclaration1()
	{
		builder.createFunctionSpec(getTestMethod("missingReturnTypeDeclaration1"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void returnsVoid()
	{
		builder.createFunctionSpec(getTestMethod("returnsVoid"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void returnsNonXacmlExpression()
	{
		builder.createFunctionSpec(getTestMethod("returnsNonXacmlExpression"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void returnTypeDeclarationExistButWrongMethodReturnType1()
	{
		builder.createFunctionSpec(getTestMethod("returnTypeDeclarationExistButWrongMethodReturnType1"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void returnTypeDeclarationExistButWrongMethodReturnType2()
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
