package org.xacml4j.v30.policy.function;

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

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamEvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DNSNameExp;
import org.xacml4j.v30.types.IPAddressExp;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.TypeCapability;
import org.xacml4j.v30.types.TypeToString;
import org.xacml4j.v30.types.X500NameExp;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;


@XacmlFunctionProvider(description="XACML regular expression functions")
public class RegularExpressionFunctions
{
	private final static Logger log = LoggerFactory.getLogger(RegularExpressionFunctions.class);

	/** Private constructor for utility class */
	private RegularExpressionFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
			BooleanExp stringRegexpMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
			BooleanExp anyURIRegexpMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
			BooleanExp ipAddressRegexpMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressExp input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
				BooleanExp dnsNameRegexpMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameExp input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
			BooleanExp rfc822NameRegexpMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp input)
	{
		 return matches(context, regexp, input);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static
			BooleanExp x500NameRegexpMatch(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp regexp,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp input)
	{
		 return matches(context, regexp, input);
	}



	private static BooleanExp matches(EvaluationContext context, StringExp regexp, AttributeExp value){
		if(log.isDebugEnabled()){
			log.debug("Matching input=\"{}\" via regexp=\"{}\"", value, regexp);
		}
		TypeCapability.Index<TypeToString> idx = TypeToString.Types.getIndex();
		Optional<TypeToString> toString = idx.get(value.getType());
		Preconditions.checkState(toString.isPresent());
		return BooleanExp.valueOf(Pattern.matches(
				 covertXacmlToJavaSyntax(regexp.getValue()),
				 toString.get().toString(value)));
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
