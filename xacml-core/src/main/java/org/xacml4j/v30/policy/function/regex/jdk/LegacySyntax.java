package org.xacml4j.v30.policy.function.regex.jdk;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code legacy} profile of the {@code jdk} engine (change add-regex-engine-spi, design D2):
 * the translation of an XACML pattern to {@code java.util.regex} syntax and the bound on nested
 * counted repetitions that the regexp functions had before the SPI, moved here unchanged.
 */
final class LegacySyntax
{
	private static final Logger log = LoggerFactory.getLogger(LegacySyntax.class);

	private LegacySyntax(){
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
	static String toJavaSyntax(String xpr)
	{
	       // XML Schema / XPath regular expressions differ from
	       // java.util.regex.Pattern. Substring matching is Matcher.find
	       // (XF 7.6.2). Do not wrap the pattern in ".*": that breaks
	       // branch-local ^ and $ and makes an empty pattern miss "\n".
	       // An empty pattern stays empty and matches every input.

	       StringBuffer buf = new StringBuffer(xpr);

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

	       String regexp = translateEndAnchors(buf.toString());
	       if(log.isDebugEnabled()){
	    	   log.debug("XACML regexp=\"{}\", " +
	    	   		"Java regexp=\"{}\"", xpr, regexp);
	       }
	       return regexp;
	  }

	private static String translateEndAnchors(String regexp)
	{
		StringBuilder translated = new StringBuilder(regexp.length());
		int characterClassDepth = 0;
		boolean escaped = false;
		for (int index = 0; index < regexp.length(); index++) {
			char current = regexp.charAt(index);
			if (escaped) {
				translated.append(current);
				escaped = false;
			} else if (current == '\\') {
				translated.append(current);
				escaped = true;
			} else if (current == '[') {
				characterClassDepth++;
				translated.append(current);
			} else if (current == ']') {
				if (characterClassDepth > 0) {
					characterClassDepth--;
				}
				translated.append(current);
			} else if (current == '$' && characterClassDepth == 0) {
				// XPath '$' is strict end-of-input; Java '$' also accepts before a final terminator.
				translated.append("\\z");
			} else {
				translated.append(current);
			}
		}
		return translated.toString();
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

	/**
	 * The largest product of the minimum counts of counted repetitions
	 * that are nested in each other. {@code (?:(?:){1000}){1000}} gives
	 * 1,000,000. A group that can match the empty string is repeated its
	 * minimum count without a read of the input, so the deadline of
	 * {@link DeadlineInput} does not see that work; this number bounds it.
	 * Repetitions with {@code *}, {@code +} and {@code ?} have a minimum of
	 * 0 or 1 and do not count.
	 * <p>
	 * This bounds counted repetitions only. A pattern can also make the
	 * matcher work without a read of the input in other ways (many groups
	 * with more than one alternative that matches the empty string); that
	 * work is not bounded here.
	 *
	 * @param regexp a pattern in {@code java.util.regex} syntax
	 * @return the weight, at least 1; {@link Long#MAX_VALUE} when it is
	 * larger than a {@code long} or when the pattern can not be scanned.
	 * Such a pattern is rejected whatever the limit is
	 */
	static long repetitionWeight(String regexp)
	{
		return new RepetitionScanner(withoutQuoting(regexp)).weight();
	}

	/**
	 * Replaces quoted text ({@code \Q..\E}) by escaped characters, in the
	 * way {@code java.util.regex} does it before it parses a pattern (its
	 * private method {@code Pattern.RemoveQEQuoting}). An empty quote
	 * disappears, so that a quantifier after it belongs to the atom before
	 * it.
	 *
	 * @param regexp a pattern in {@code java.util.regex} syntax
	 * @return the pattern without quoted text
	 */
	static String withoutQuoting(String regexp)
	{
		int length = regexp.length();
		int i = 0;
		while(i < length - 1 && (regexp.charAt(i) != '\\' || regexp.charAt(i + 1) != 'Q')){
			i += (regexp.charAt(i) == '\\')?2:1;
		}
		if(i >= length - 1){
			return regexp;
		}
		StringBuilder out = new StringBuilder(length).append(regexp, 0, i);
		i += 2;
		boolean inQuote = true;
		boolean beginQuote = true;
		while(i < length){
			char c = regexp.charAt(i++);
			char next = (i < length)?regexp.charAt(i):0;
			if(c > 127 || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
				out.append(c);
			}else if(c >= '0' && c <= '9'){
				// A digit at the start of a quote must not join an escape before the quote.
				out.append(beginQuote?"\\x3":"").append(c);
			}else if(c != '\\'){
				out.append(inQuote?"\\":"").append(c);
			}else if(inQuote && next == 'E'){
				i++;
				inQuote = false;
			}else if(inQuote){
				out.append("\\\\");
			}else if(next == 'Q'){
				i++;
				inQuote = true;
				beginQuote = true;
				continue;
			}else{
				out.append(c);
				if(i < length){
					out.append(next);
					i++;
				}
			}
			beginQuote = false;
		}
		return out.toString();
	}

	/**
	 * Reads a pattern without quoted text far enough to find its groups,
	 * character classes and counted quantifiers. It is not a parser of the
	 * whole syntax. Where it can not follow the pattern (comments mode) it
	 * reports the largest weight, so that a quantifier can not be hidden
	 * from it.
	 */
	private static final class RepetitionScanner
	{
		private final String regexp;
		private final int length;
		private int at;

		RepetitionScanner(String regexp){
			this.regexp = regexp;
			this.length = regexp.length();
		}

		long weight()
		{
			// One entry per open group: the largest weight in the enclosing group so far.
			java.util.ArrayDeque<Long> open = new java.util.ArrayDeque<>();
			long inside = 1;
			// The weight of what a counted quantifier at this place repeats: the
			// group that was just closed, or 1. It is 1 at the start of a group
			// and after a quantifier too: java.util.regex repeats an empty atom there.
			long atom = 1;
			while(at < length){
				char c = regexp.charAt(at);
				if(c == '{'){
					inside = Math.max(inside, repeated(atom));
					atom = 1;
				}else if(c == '('){
					if(turnsOnCommentsMode()){
						return Long.MAX_VALUE;
					}
					open.push(inside);
					inside = 1;
					atom = 1;
					at++;
				}else if(c == ')'){
					atom = inside;
					inside = Math.max(atom, open.isEmpty()?1:open.pop());
					at++;
				}else{
					skipAtom(c);
					atom = 1;
				}
			}
			while(!open.isEmpty()){
				inside = Math.max(inside, open.pop());
			}
			return inside;
		}

		/**
		 * Moves past the brace that starts here.
		 *
		 * @return the weight of the repetition when the brace starts a
		 * counted quantifier, 1 when it does not
		 */
		private long repeated(long atom)
		{
			long min = countedMinimum();
			if(min < 0){
				at++;
				return 1;
			}
			at = regexp.indexOf('}', at) + 1;
			return saturatedMultiply(atom, Math.max(1, min));
		}

		private void skipAtom(char c)
		{
			if(c == '['){
				skipCharacterClass();
			}else if(c == '\\'){
				skipEscape();
			}else{
				at++;
			}
		}

		/**
		 * Tells whether the group that starts here is a flag group with
		 * {@code x}, as in {@code (?x)} or {@code (?ix:}. In comments mode
		 * a {@code #} hides the rest of a line from the pattern, which
		 * this scanner does not follow.
		 */
		private boolean turnsOnCommentsMode()
		{
			if(at + 1 >= length || regexp.charAt(at + 1) != '?'){
				return false;
			}
			for(int i = at + 2; i < length && Character.isLetter(regexp.charAt(i)); i++){
				if(regexp.charAt(i) == 'x'){
					return true;
				}
			}
			return false;
		}

		/**
		 * Moves past the escape that starts here. The character after
		 * {@code \c}, the braces of {@code \p{..}}, {@code \P{..}},
		 * {@code \x{..}}, {@code \N{..}} and the brackets of
		 * {@code \k<..>} belong to the escape.
		 */
		private void skipEscape()
		{
			char escaped = (at + 1 < length)?regexp.charAt(at + 1):0;
			at = Math.min(length, at + ((escaped == 'c')?3:2));
			char close = 0;
			if(at < length && regexp.charAt(at) == '{' && "pPxN".indexOf(escaped) >= 0){
				close = '}';
			}else if(at < length && regexp.charAt(at) == '<' && escaped == 'k'){
				close = '>';
			}
			if(close != 0){
				int end = regexp.indexOf(close, at);
				at = (end < 0)?length:end + 1;
			}
		}

		/**
		 * Moves past the character class that starts here, with the classes
		 * nested in it. As in {@code java.util.regex}, a {@code ]} that is
		 * the first character of a class (after {@code [} or {@code [^})
		 * is a literal and does not close the class.
		 */
		private void skipCharacterClass()
		{
			int depth = 0;
			boolean first = false;
			do{
				char c = regexp.charAt(at);
				if(c == '\\'){
					skipEscape();
					first = false;
				}else if(c == '['){
					depth++;
					at++;
					if(at < length && regexp.charAt(at) == '^'){
						at++;
					}
					first = true;
				}else{
					if(c == ']' && !first){
						depth--;
					}
					first = false;
					at++;
				}
			}while(depth > 0 && at < length);
		}

		/** The minimum count of the counted quantifier that starts here, or -1 when there is none. */
		private long countedMinimum()
		{
			int i = at + 1;
			long min = 0;
			int digits = 0;
			for(; i < length && isDigit(regexp.charAt(i)); i++, digits++){
				// A count above Integer.MAX_VALUE is not valid; it only has to stay large.
				min = (min > Integer.MAX_VALUE)?min:min * 10 + (regexp.charAt(i) - '0');
			}
			if(digits == 0){
				return -1;
			}
			if(i < length && regexp.charAt(i) == ','){
				for(i++; i < length && isDigit(regexp.charAt(i)); i++){
					// the maximum count does not matter
				}
			}
			return (i < length && regexp.charAt(i) == '}')?min:-1;
		}

		private static boolean isDigit(char c)
		{
			return c >= '0' && c <= '9';
		}

		private static long saturatedMultiply(long a, long b)
		{
			return (a > Long.MAX_VALUE / b)?Long.MAX_VALUE:a * b;
		}
	}
}
