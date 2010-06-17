package com.artagon.xacml.v3.spi.function;

import com.artagon.xacml.v3.types.BooleanType.BooleanValue;

public class JavaMethodToFunctionSpecTestFunctions 
{
	
	@XacmlFunc(id="returnsVoid")
	public void returnsVoid(){
	}
	
	@XacmlFunc(id="returnsNonXacmlExpression")
	public Integer returnsNonXacmlExpression(){
		return null;
	}
	
	public BooleanValue missingXacmlFuncAnnotation(){
		return null;
	}
	
	@XacmlFunc(id="missingReturnTypeDeclaration1")
	public BooleanValue missingReturnTypeDeclaration1(){
		return null;
	}
}
