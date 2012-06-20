package com.artagon.xacml.v30.policy.function;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.AnyURIExp;
import com.artagon.xacml.v30.types.BooleanExp;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.DNSNameExp;
import com.artagon.xacml.v30.types.IPAddressExp;
import com.artagon.xacml.v30.types.RFC822NameExp;
import com.artagon.xacml.v30.types.StringExp;
import com.artagon.xacml.v30.types.X500NameExp;

@XacmlFunctionProvider(description="XACML regular expression functions")
public class RegularExpressionFunctions 
{
	private final static Logger log = LoggerFactory.getLogger(RegularExpressionFunctions.class);
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp stringRegexpMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp anyURIRegexpMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp ipAddressRegexpMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressExp input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp dnsNameRegexpMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameExp input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp rfc822NameRegexpMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp input)
	{
		 return matches(regexp, input);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanExp x500NameRegexpMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp input)
	{
		 return matches(regexp, input);
	}
	
	
	
	private static BooleanExp matches(StringExp regexp, AttributeExp input){
		if(log.isDebugEnabled()){
			log.debug("Matching input=\"{}\" via regexp=\"{}\"", input, regexp);
		}
		 return BooleanType.BOOLEAN.create(Pattern.matches(
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

	       StringBuffer buf = new StringBuffer(xpr);
	       
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
	       
	       String regexp = buf.toString();
	       if(log.isDebugEnabled()){
	    	   log.debug("XACML regexp=\"{}\", " +
	    	   		"Java regexp=\"{}\"", xpr, regexp);
	       }
	       return regexp;
	  }

	private static int calculateNestLevel(
			StringBuffer buf, int idx) {
		int level = 0;
		while(idx >= 0) {
			char ch = buf.charAt(idx--);
			if (ch == '[') level++;
			if (ch == ']') level --;
		}
		return level;
	}
}
