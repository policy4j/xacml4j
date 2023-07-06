package org.xacml4j.v30.policy.function.impl;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncParamOptional;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;


public class JavaMethodToFunctionSpecTestFunctions
{

	@XacmlFuncSpec(id="returnsVoid")
	public void returnsVoid(){
	}

	@XacmlFuncSpec(id="returnsNonXacmlExpression")
	public Integer returnsNonXacmlExpression(){
		return null;
	}

	public BooleanVal missingXacmlFuncAnnotation(){
		return null;
	}

	@XacmlFuncSpec(id="missingReturnTypeDeclaration1")
	public static BooleanVal missingReturnTypeDeclaration1(){
		return null;
	}

	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BooleanVal returnTypeDeclarationExistButWrongMethodReturnType1(){
		return null;
	}
	
	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal optionalParametersTest(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanVal a,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true, value={"false", "true"}) String b,
			@XacmlFuncParamOptional(typeId="http://www.w3.org/2001/XMLSchema#string", value="false") String c){
		return null;
	}

	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType2")
	public static BagOfValues returnTypeDeclarationExistButWrongMethodReturnType2(){
		return null;
	}
}
