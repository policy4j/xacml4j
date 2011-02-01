package com.artagon.xacml.v30.spi.function;

import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.types.BooleanValue;

public class JavaMethodToFunctionSpecTestFunctions 
{
	
	@XacmlFuncSpec(id="returnsVoid")
	public void returnsVoid(){
	}
	
	@XacmlFuncSpec(id="returnsNonXacmlExpression")
	public Integer returnsNonXacmlExpression(){
		return null;
	}
	
	public BooleanValue missingXacmlFuncAnnotation(){
		return null;
	}
	
	@XacmlFuncSpec(id="missingReturnTypeDeclaration1")
	public static BooleanValue missingReturnTypeDeclaration1(){
		return null;
	}
	
	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BooleanValue returnTypeDeclarationExistButWrongMethodReturnType1(){
		return null;
	}
	
	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BagOfAttributeValues returnTypeDeclarationExistButWrongMethodReturnType2(){
		return null;
	}
	
}
