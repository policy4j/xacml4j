package com.artagon.xacml.v3.policy.function;

import java.util.regex.Pattern;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DNSNameType.DNSNameValue;
import com.artagon.xacml.v3.types.IPAddressType.IPAddressValue;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.X500NameType.X500NameValue;

@XacmlFunctionProvider
public class RegularExpressionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue stringRegexpMatch(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue regexp, 
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue anyURIRegexpMatch(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue regexp, 
			@XacmlParam(type=XacmlDataTypes.ANYURI)AnyURIValue input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue ipAddressRegexpMatch(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue regexp, 
			@XacmlParam(type=XacmlDataTypes.IPADDRESS)IPAddressValue input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue dnsNameRegexpMatch(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue regexp, 
			@XacmlParam(type=XacmlDataTypes.DNSNAME)DNSNameValue input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue rfc822NameRegexpMatch(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue regexp, 
			@XacmlParam(type=XacmlDataTypes.RFC822NAME)RFC822NameValue input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue x500NameRegexpMatch(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue regexp, 
			@XacmlParam(type=XacmlDataTypes.X500NAME)X500NameValue input)
	{
		 return matches(regexp, input);
	}
	
	
	
	private static BooleanValue matches(StringValue regexp, AttributeValue input){
		 return XacmlDataTypes.BOOLEAN.create(Pattern.matches(
				 covertXacmlToJavaSyntax(regexp.getValue()), 
				 input.toXacmlString()));
	}
	
	/*
	 *
	 * Copyright 2003-2006 Sun Microsystems, Inc. All Rights Reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 *   1. Redistribution of source code must retain the above copyright notice,
	 *      this list of conditions and the following disclaimer.
	 * 
	 *   2. Redistribution in binary form must reproduce the above copyright
	 *      notice, this list of conditions and the following disclaimer in the
	 *      documentation and/or other materials provided with the distribution.
	 *
	 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
	 * be used to endorse or promote products derived from this software without
	 * specific prior written permission.
	 * 
	 * This software is provided "AS IS," without a warranty of any kind. ALL
	 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
	 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
	 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
	 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
	 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
	 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
	 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
	 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
	 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
	 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
	 *
	 * You acknowledge that this software is not designed or intended for use in
	 * the design, construction, operation or maintenance of any nuclear facility.
	 */
	public static String covertXacmlToJavaSyntax(String xpr) 
	{
	       // the regular expression syntax required by XACML differs
	       // from the syntax supported by java.util.regex.Pattern
	       // in several ways; the next several code blocks transform
	       // the XACML syntax into a semantically equivalent Pattern syntax

	       StringBuffer buf = new StringBuffer(xpr.trim());
	       
	       // in order to handle the requirement that the string is
	       // considered to match the pattern if any substring matches
	       // the pattern, we prepend ".*" and append ".*" to the reg exp,
	       // but only if there isn't an anchor (^ or $) in place

	       if (xpr.charAt(0) != '^')
	           buf = buf.insert(0, ".*");

	       if (xpr.charAt(xpr.length() - 1) != '$')
	           buf = buf.insert(buf.length(), ".*");

	       // in order to handle Unicode blocks, we replace all 
	       // instances of "\p{Is" with "\p{In" in the reg exp

	       int idx = -1;
	       idx = buf.indexOf("\\p{Is", 0);
	       while (idx != -1){
	           buf = buf.replace(idx, idx+5, "\\p{In");
	           idx = buf.indexOf("\\p{Is", idx);
	       }

	       // in order to handle Unicode blocks, we replace all instances 
	       // of "\P{Is" with "\P{In" in the reg exp

	       idx = -1;
	       idx = buf.indexOf("\\P{Is", 0);
	       while (idx != -1){
	           buf = buf.replace(idx, idx+5, "\\P{In");
	           idx = buf.indexOf("\\P{Is", idx);
	       }
	       
	       // converts to class subtraction
	       // in order to handle character class subtraction, we
	       // replace nested instances of "-[" with "&&[^" in the reg exp
	       idx = -1;
	       idx = buf.indexOf("-[", 0);
	       while (idx != -1){
	    	   if (calculateNestLevel(buf, idx-1) > 0) {
	    		   buf = buf.replace(idx, idx+2, "&&[^");
	    	   }
	    	   idx = buf.indexOf("-[", idx+1);
	       }
	       
	       return buf.toString();
	  }

	private static int calculateNestLevel(StringBuffer buf, int idx) {
		int level = 0;
		while(idx >= 0) {
			char ch = buf.charAt(idx--);
			if (ch == '[') level++;
			if (ch == ']') level --;
		}
		return level;
	}
}
