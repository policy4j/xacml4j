package org.xacml4j.v30.spi.function;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.StringExp;


public class JavaMethodToFunctionSpecTestFunctions
{

	@XacmlFuncSpec(id="returnsVoid")
	public void returnsVoid(){
	}

	@XacmlFuncSpec(id="returnsNonXacmlExpression")
	public Integer returnsNonXacmlExpression(){
		return null;
	}

	public BooleanExp missingXacmlFuncAnnotation(){
		return null;
	}

	@XacmlFuncSpec(id="missingReturnTypeDeclaration1")
	public static BooleanExp missingReturnTypeDeclaration1(){
		return null;
	}

	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BooleanExp returnTypeDeclarationExistButWrongMethodReturnType1(){
		return null;
	}
	
	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp optionalParametersTest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", optional=true)BooleanExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", optional=true)StringExp b){
		return null;
	}

	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType2")
	public static BagOfAttributeExp returnTypeDeclarationExistButWrongMethodReturnType2(){
		return null;
	}
}
