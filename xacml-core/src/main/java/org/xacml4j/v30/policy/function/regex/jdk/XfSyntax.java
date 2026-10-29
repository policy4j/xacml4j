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

import java.util.BitSet;

import org.xacml4j.v30.policy.function.regex.CodePointSet;
import org.xacml4j.v30.policy.function.regex.RegexCompileException;
import org.xacml4j.v30.policy.function.regex.XacmlPattern;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Alternation;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Anchor;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.BackReference;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.BlockEscape;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.CategoryEscape;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.CharClass;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.ClassGroup;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.ClassItem;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.CodePointRange;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Group;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Literal;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.MultiCharEscape;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Node;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Repeat;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Sequence;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Wildcard;

/**
 * Emits an {@link XacmlPattern} in {@code java.util.regex} syntax with the meaning of the
 * {@code xf} profile (change add-regex-engine-spi, design D2): {@code ^} and {@code \z} anchor
 * the string, {@code .} excludes the line feed only, {@code \s} is the four XML spaces,
 * {@code \d} is {@code \p{Nd}}, {@code \w} and {@code \p{C}} leave the surrogates out as XML
 * Schema does, {@code \i} and {@code \c} are the XML name characters, and a class subtraction
 * is an intersection with a negated class. Categories keep their Java names, whose data is the
 * data of the flat sets; a block, a name-character escape and a literal are emitted as code
 * points. Groups are named {@code g1, g2, ...} so that a back-reference is never ambiguous.
 */
final class XfSyntax
{
	private XfSyntax(){
	}

	static String toJavaSyntax(XacmlPattern pattern){
		if(pattern.hasBackReferences()) checkReferences(pattern.root(), new BitSet());
		StringBuilder out = new StringBuilder(pattern.source().length() * 2 + 16);
		emit(pattern.root(), out);
		return out.toString();
	}

	private static void checkReferences(Node node, BitSet certain){
		switch(node){
			case Sequence sequence -> sequence.pieces().forEach(piece -> checkReferences(piece, certain));
			case Alternation alternation -> {
				BitSet common = null;
				for(Node branch : alternation.branches()){
					BitSet branchGroups = (BitSet)certain.clone();
					checkReferences(branch, branchGroups);
					if(common == null) common = branchGroups;
					else common.and(branchGroups);
				}
				if(common != null) certain.or(common);
			}
			case Group group -> {
				checkReferences(group.body(), certain);
				certain.set(group.number());
			}
			case Repeat repeat -> {
				BitSet repeated = (BitSet)certain.clone();
				checkReferences(repeat.atom(), repeated);
				if(repeat.min() > 0) certain.or(repeated);
			}
			case BackReference reference -> {
				if(!certain.get(reference.group())){
					throw RegexCompileException.unsupported("back-reference to an optional group", JdkRegexEngine.NAME);
				}
			}
			default -> { }
		}
	}

	private static void emit(Node node, StringBuilder out){
		switch(node){
			case Alternation alternation -> {
				boolean first = true;
				for(Node branch : alternation.branches()){
					if(!first){
						out.append('|');
					}
					first = false;
					emit(branch, out);
				}
			}
			case Sequence sequence -> {
				for(Node piece : sequence.pieces()){
					emit(piece, out);
				}
			}
			case Repeat repeat -> {
				emit(repeat.atom(), out);
				quantifier(repeat, out);
			}
			case Group group -> {
				out.append("(?<g").append(group.number()).append('>');
				emit(group.body(), out);
				out.append(')');
			}
			case BackReference reference -> out.append("\\k<g").append(reference.group()).append('>');
			case Literal literal -> codePoint(literal.codePoint(), out);
			case Anchor anchor -> out.append(anchor == Anchor.START ? "^" : "\\z");
			case CharClass charClass -> charClass(charClass.group(), out);
		}
	}

	private static void quantifier(Repeat repeat, StringBuilder out){
		int min = repeat.min();
		int max = repeat.max();
		if(min == 0 && max == 1){
			out.append('?');
		}else if(min == 0 && max == XacmlPattern.UNBOUNDED){
			out.append('*');
		}else if(min == 1 && max == XacmlPattern.UNBOUNDED){
			out.append('+');
		}else if(max == XacmlPattern.UNBOUNDED){
			out.append('{').append(min).append(",}");
		}else if(min == max){
			out.append('{').append(min).append('}');
		}else{
			out.append('{').append(min).append(',').append(max).append('}');
		}
		if(repeat.reluctant()){
			out.append('?');
		}
	}

	/** A class node: a bare escape or {@code .} without brackets, a class expression with them */
	private static void charClass(ClassGroup group, StringBuilder out){
		if(!group.negated() && group.subtracted().isEmpty() && group.items().size() == 1
				&& !(group.items().get(0) instanceof CodePointRange)){
			bareItem(group.items().get(0), out);
			return;
		}
		javaClass(group, out);
	}

	/** An item outside brackets, in its shortest Java form */
	private static void bareItem(ClassItem item, StringBuilder out){
		switch(item){
			case CategoryEscape category -> category(category, out);
			case MultiCharEscape escape -> {
				switch(escape.letter()){
					case 'd' -> out.append("\\p{Nd}");
					case 'D' -> out.append("\\P{Nd}");
					default -> javaClass(new ClassGroup(false, java.util.List.of(item), java.util.Optional.empty()), out);
				}
			}
			case Wildcard wildcard -> out.append("[^\\n]");
			default -> javaClass(new ClassGroup(false, java.util.List.of(item), java.util.Optional.empty()), out);
		}
	}

	/**
	 * A class expression in brackets. A subtraction {@code [G-[S]]} is {@code [G&&[^S]]}, and with
	 * a negated group {@code [^G-[S]]} it is {@code [[^G]&&[^S]]}: XML Schema subtracts after it
	 * negates.
	 */
	private static void javaClass(ClassGroup group, StringBuilder out){
		out.append('[');
		if(group.subtracted().isEmpty()){
			if(group.negated()){
				out.append('^');
			}
			items(group, out);
		}else{
			if(group.negated()){
				out.append("[^");
				items(group, out);
				out.append(']');
			}else{
				items(group, out);
			}
			out.append("&&[^");
			javaClass(group.subtracted().get(), out);
			out.append(']');
		}
		out.append(']');
	}

	/** The items of a group as the content of a Java class */
	private static void items(ClassGroup group, StringBuilder out){
		for(ClassItem item : group.items()){
			switch(item){
				case CodePointRange range -> {
					codePoint(range.lo(), out);
					if(range.hi() != range.lo()){
						out.append('-');
						codePoint(range.hi(), out);
					}
				}
				case CategoryEscape category -> category(category, out);
				case BlockEscape block -> ranges(XacmlPattern.codePoints(block), out);
				case MultiCharEscape escape -> multiCharEscape(escape, out);
				case Wildcard wildcard -> out.append("[^\\n]");
			}
		}
	}

	/** A category in its Java name; {@code C} without the surrogates, as XML Schema defines it */
	private static void category(CategoryEscape category, StringBuilder out){
		if(category.name().equals("C")){
			out.append(category.complement() ? "[^" : "[").append("\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]");
			return;
		}
		out.append(category.complement() ? "\\P{" : "\\p{").append(category.name()).append('}');
	}

	/** A multi-character escape as the content of a class */
	private static void multiCharEscape(MultiCharEscape escape, StringBuilder out){
		switch(escape.letter()){
			case 's' -> out.append(" \\t\\n\\r");
			case 'S' -> out.append("[^ \\t\\n\\r]");
			case 'd' -> out.append("\\p{Nd}");
			case 'D' -> out.append("\\P{Nd}");
			case 'w' -> out.append("[^\\p{P}\\p{Z}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]");
			case 'W' -> out.append("[\\p{P}\\p{Z}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]");
			case 'i', 'c' -> ranges(XacmlPattern.codePoints(escape), out);
			case 'I', 'C' -> {
				out.append("[^");
				ranges(XacmlPattern.codePoints(new MultiCharEscape(Character.toLowerCase(escape.letter()))), out);
				out.append(']');
			}
			default -> throw new IllegalArgumentException("not a multi-character escape: " + escape.letter());
		}
	}

	/** The ranges of a set as the content of a class */
	private static void ranges(CodePointSet set, StringBuilder out){
		for(int i = 0; i < set.rangeCount(); i++){
			codePoint(set.lo(i), out);
			if(set.hi(i) != set.lo(i)){
				out.append('-');
				codePoint(set.hi(i), out);
			}
		}
	}

	/** A code point: a letter or digit as it is, anything else as {@code \x{H}}, inside and outside a class */
	private static void codePoint(int codePoint, StringBuilder out){
		if((codePoint >= 'a' && codePoint <= 'z') || (codePoint >= 'A' && codePoint <= 'Z')
				|| (codePoint >= '0' && codePoint <= '9')){
			out.append((char)codePoint);
			return;
		}
		out.append("\\x{").append(Integer.toHexString(codePoint)).append('}');
	}
}
