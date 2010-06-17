package com.artagon.xacml.v3.spi.function;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.types.XacmlDataTypes;
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
	public static BooleanValue missingReturnTypeDeclaration1(){
		return null;
	}
	
	@XacmlFunc(id="returnTypeDeclarationExistButWrongMethodReturnType1")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BooleanValue returnTypeDeclarationExistButWrongMethodReturnType1(){
		return null;
	}
	
	@XacmlFunc(id="returnTypeDeclarationExistButWrongMethodReturnType2")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BagOfAttributeValues<BooleanValue> returnTypeDeclarationExistButWrongMethodReturnType2(){
		return null;
	}
	
}
