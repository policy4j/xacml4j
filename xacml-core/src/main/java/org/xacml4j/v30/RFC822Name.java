package org.xacml4j.v30;

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

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public final class RFC822Name implements Serializable
{
	private static final long serialVersionUID = -5984520030703446283L;

	/*
	 * Copyright 2008 Les Hazlewood
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *     http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */

	/**
	 * This constant states that domain literals are allowed in the email address, e.g.:
	 *
	 * <p><tt>someone@[192.168.1.100]</tt> or <br/>
	 * <tt>john.doe@[23:33:A2:22:16:1F]</tt> or <br/>
	 * <tt>me@[my computer]</tt></p>
	 *
	 * <p>The RFC says these are valid email addresses, but most people don't like allowing them.
	 * If you don't want to allow them, and only want to allow valid domain names
	 * (<a href="http://www.ietf.org/rfc/rfc1035.txt">RFC 1035</a>, x.y.z.com, etc),
	 * change this constant to <tt>false</tt>.
	 *
	 * <p>Its default value is <tt>true</tt> to remain RFC 2822 compliant, but
	 * you should set it depending on what you need for your application.
	 */
	private static final boolean ALLOW_DOMAIN_LITERALS = true;

	/**
	 * This constant states that quoted identifiers are allowed
	 * (using quotes and angle brackets around the raw address) are allowed, e.g.:
	 *
	 * <p><tt>"John Smith" &lt;john.smith@somewhere.com&gt;</tt>
	 *
	 * <p>The RFC says this is a valid mailbox.  If you don't want to
	 * allow this, because for example, you only want users to enter in
	 * a raw address (<tt>john.smith@somewhere.com</tt> - no quotes or angle
	 * brackets), then change this constant to <tt>false</tt>.
	 *
	 * <p>Its default value is <tt>true</tt> to remain RFC 2822 compliant, but
	 * you should set it depending on what you need for your application.
	 */
	private static final boolean ALLOW_QUOTED_IDENTIFIERS = false;

	// RFC 2822 2.2.2 Structured Header Field Bodies
	private static final String WSP = "[ \\t]"; //space or tab
	private static final String FWSP = WSP + "*";

	//RFC 2822 3.2.1 Primitive tokens
	private static final String DQUOTE = "\\\"";
	//ASCII Control characters excluding white space:
	private static final String NO_WS_CTL = "\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F";
	//all ASCII characters except CR and LF:
	private static final String ASCII_TEXT = "[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]";

	// RFC 2822 3.2.2 Quoted characters:
	//single backslash followed by a text char
	private static final String QUOTED_PAIR = "(\\\\" + ASCII_TEXT + ")";

	//RFC 2822 3.2.4 Atom:
	private static final String ATEXT = "[a-zA-Z0-9\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~]";
	private static final String ATOM = FWSP + ATEXT + "+" + FWSP;
	private static final String DOT_ATOM_TEXT = ATEXT + "+" + "(" + "\\." + ATEXT + "+)*";
	private static final String DOT_ATOM = FWSP + "(" + DOT_ATOM_TEXT + ")" + FWSP;

	//RFC 2822 3.2.5 Quoted strings:
	//noWsCtl and the rest of ASCII except the doublequote and backslash characters:
	private static final String QTEXT = "[" + NO_WS_CTL + "\\x21\\x23-\\x5B\\x5D-\\x7E]";
	private static final String QCONTENT = "(" + QTEXT + "|" + QUOTED_PAIR + ")";
	private static final String QUOTED_STRING = DQUOTE + "(" + FWSP + QCONTENT + ")*" + FWSP + DQUOTE;

	//RFC 2822 3.2.6 Miscellaneous tokens
	private static final String WORD = "((" + ATOM + ")|(" + QUOTED_STRING + "))";
	private static final String PHRASE = WORD + "+"; //one or more words.

	//RFC 1035 tokens for domain names:
	private static final String LETTER = "[a-zA-Z]";
	private static final String LET_DIG = "[a-zA-Z0-9]";
	private static final String LET_DIG_HYP = "[a-zA-Z0-9-]";
	private static final String RFC_LABEL = LET_DIG + "(" + LET_DIG_HYP + "{0,61}" + LET_DIG + ")?";
	private static final String RFC_1035_DOMAIN_NAME = RFC_LABEL + "(\\." + RFC_LABEL + ")*\\." + LETTER + "{2,6}";

	//RFC 2822 3.4 Address specification
	//domain text - non white space controls and the rest of ASCII chars not including [, ], or \:
	private static final String DTEXT = "[" + NO_WS_CTL + "\\x21-\\x5A\\x5E-\\x7E]";
	private static final String DCONTENT = DTEXT + "|" + QUOTED_PAIR;
	private static final String DOMAIN_LITERAL = "\\[" + "(" + FWSP + DCONTENT + "+)*" + FWSP + "\\]";
	private static final String RFC_2822_DOMAIN = "(" + DOT_ATOM + "|" + DOMAIN_LITERAL + ")";


	private static final String DOMAIN;
	static {
		if (ALLOW_DOMAIN_LITERALS) {
			DOMAIN = RFC_2822_DOMAIN;
		} else {
			DOMAIN = RFC_1035_DOMAIN_NAME;
		}
	}

	private static final String LOCAL_PART = "((" + DOT_ATOM + ")|(" + QUOTED_STRING + "))";
	private static final String ADDR_SPEC = LOCAL_PART + "@" + DOMAIN;
	private static final String ANGLE_ADDR = "<" + ADDR_SPEC + ">";
	private static final String NAME_ADDR = "(" + PHRASE + ")?" + FWSP + ANGLE_ADDR;
	private static final String MAILBOX = NAME_ADDR + "|" + ADDR_SPEC;

	//now compile a pattern for efficient re-use:
	//if we're allowing quoted identifiers or not:
	private static final String PATTERN_STRING;

	public static final Pattern VALID_PATTERN;

	static {
		if (ALLOW_QUOTED_IDENTIFIERS) {
			PATTERN_STRING = MAILBOX;
		} else {
			PATTERN_STRING = ADDR_SPEC;
		}

		try {
			VALID_PATTERN = Pattern.compile(PATTERN_STRING);
		} catch(PatternSyntaxException e) {
			LoggerFactory.getLogger(RFC822Name.class).error("Can not parse Email address pattern", e);
			e.printStackTrace();
			throw e;
		}
	}

	private String localPart;
	private String domainPart;
	private String fqName;

	public RFC822Name(String localPart,
			String domainPart){
		Preconditions.checkNotNull(localPart);
		Preconditions.checkNotNull(domainPart);
		this.domainPart = domainPart.toLowerCase();
		this.localPart = localPart;
		this.fqName = this.localPart + '@' + this.domainPart;
	}

	public boolean matches(String pattern){
		int pos = pattern.indexOf('@');
		if(pos != -1){
			RFC822Name n = RFC822Name.parse(pattern);
			return n.equals(this);
		}
		if (pattern.charAt(0) == '.') {
             return domainPart.endsWith(pattern.toLowerCase());
		}
		return domainPart.equalsIgnoreCase(pattern);
	}

	public String getLocalPart(){
		return localPart;
	}

	public String getDomainPart(){
		return domainPart;
	}

	@Override
	public String toString(){
		return fqName;
	}

	public String toXacmlString(){
		return fqName;
	}

	public static RFC822Name parse(Object name){
		Preconditions.checkNotNull(name);
		String trimmedName = ((String)name).trim();
		Preconditions.checkArgument(VALID_PATTERN.matcher(trimmedName).matches(),
				"Given value=\"%s\" is invalid RFC822 name", trimmedName);
		String [] parts = trimmedName.split("@");
		return new RFC822Name(parts[0], parts[1]);
	}

	@Override
	public int hashCode(){
		return fqName.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if (o == null) {
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof RFC822Name)){
			return false;
		}
		RFC822Name n = (RFC822Name)o;
		return n.getLocalPart().equals(getLocalPart()) &&
		n.getDomainPart().equalsIgnoreCase(getDomainPart());
	}
}
