package org.xacml4j.v30.spi.function;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true, defaultValue={"false", "true"})StringExp b,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", defaultValue="false")StringExp c){
		return null;
	}

	@XacmlFuncSpec(id="returnTypeDeclarationExistButWrongMethodReturnType2")
	public static BagOfAttributeExp returnTypeDeclarationExistButWrongMethodReturnType2(){
		return null;
	}
}
