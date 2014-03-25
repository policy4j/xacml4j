package org.xacml4j.v30.policy.function;

import javax.security.auth.x500.X500Principal;

import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.X500NameExp;


@XacmlFunctionProvider(description="XACML special match functions")
public class SpecialMatchFunctions
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
			BooleanExp rfc822NameMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp pattern,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp rfc822Name)
	{
		 return BooleanExp.valueOf(rfc822Name.getValue().matches(pattern.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
			BooleanExp x500NameMatch(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp b)
	{
		 String n0 = a.getValue().getName(X500Principal.CANONICAL);
		 String n1 = b.getValue().getName(X500Principal.CANONICAL);
		 return BooleanExp.valueOf(n1.endsWith(n0));
	}
}
