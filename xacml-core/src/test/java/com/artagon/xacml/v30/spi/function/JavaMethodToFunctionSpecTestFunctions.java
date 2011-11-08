package com.artagon.xacml.v30.spi.function;

import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.types.BooleanValueExp;

public class JavaMethodToFunctionSpecTestFunctions 
{
	
	@XacmlFuncSpec(id="returnsVoid")
	public void returnsVoid(){
	}
	
	@XacmlFuncSpec(id="returnsNonXacmlExpression")
	public Integer returnsNonXacmlExpression(){
		return null;
	}
	
	public BooleanValueExp missingXacmlFuncAnnotation(){
		return null;
	}
	
	@XacmlFuncSpec(id="missingReturnTypeDeclaration1")
	public static BooleanValueExp missingReturnTypeDeclaration1(){
		return null;
	}
	
	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BooleanValueExp returnTypeDeclarationExistButWrongMethodReturnType1(){
		return null;
	}
	
	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BagOfAttributesExp returnTypeDeclarationExistButWrongMethodReturnType2(){
		return null;
	}
	
}
