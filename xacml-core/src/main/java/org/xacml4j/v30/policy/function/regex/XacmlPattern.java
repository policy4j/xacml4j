package org.xacml4j.v30.policy.function.regex;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * An XACML pattern read with the grammar of the {@code xf} profile (change add-regex-engine-spi,
 * designs D1 and D4): XF 1.0 section 7.6.1 as published on 23 January 2007, which XACML 3.0
 * A.3.13 cites. That is the XML Schema Part 2 Appendix F grammar with {@code ^} and {@code $}
 * as anchors, reluctant quantifiers and back-references; braces, {@code ^} and {@code $} are
 * metacharacters; there are no flags and no non-capturing groups. A pattern that the grammar
 * rejects is {@link RegexException.Kind#MALFORMED}.
 * <p>
 * The tree carries every character class twice: as it was written, for an engine that can
 * express categories and blocks itself, and as one flat {@link CodePointSet} from the Unicode
 * data of the JDK, for an engine that cannot. The size estimate is the one of D4: pieces and
 * branches add, a counted repetition multiplies by its largest count (its smallest count plus
 * one when it has no largest), and a class counts its ranges.
 */
public final class XacmlPattern
{
	/** The largest count of a repetition without one, as {@code *}, {@code +} and {@code {n,}} have */
	public static final int UNBOUNDED = -1;

	static{
		UnicodeSets.prepare();
	}

	private final String source;
	private final Node root;
	private final int groups;
	private final int largestCount;
	private final long sizeEstimate;
	private final boolean backReferences;

	private XacmlPattern(String source, Parser parser, Node root){
		this.source = source;
		this.root = root;
		this.groups = parser.groups;
		this.largestCount = parser.largestCount;
		this.backReferences = parser.backReferences;
		this.sizeEstimate = estimate(root);
	}

	/**
	 * Reads a pattern
	 *
	 * @param pattern an XACML pattern
	 * @return the pattern as a tree
	 * @throws RegexCompileException of kind {@link RegexException.Kind#MALFORMED} when the
	 * grammar of XF 1.0 section 7.6.1 rejects the pattern
	 */
	public static XacmlPattern parse(String pattern){
		Preconditions.checkNotNull(pattern, "pattern");
		Parser parser = new Parser(pattern);
		Node root = parser.parseAll();
		return new XacmlPattern(pattern, parser, root);
	}

	/** The pattern as it was written */
	public String source(){
		return source;
	}

	public Node root(){
		return root;
	}

	/** The number of capturing groups */
	public int groups(){
		return groups;
	}

	/** The largest count of a counted repetition, 0 when there is none */
	public int largestCount(){
		return largestCount;
	}

	/** The estimate of the expanded program (design D4), saturating at {@link Long#MAX_VALUE} */
	public long sizeEstimate(){
		return sizeEstimate;
	}

	public boolean hasBackReferences(){
		return backReferences;
	}

	@Override
	public String toString(){
		return "XacmlPattern[" + root + "]";
	}

	/** A node of the tree */
	public sealed interface Node permits Alternation, Sequence, Repeat, Group, BackReference, CharClass, Literal, Anchor
	{
	}

	/** Branches separated by {@code |}; a pattern with one branch is a {@link Sequence} */
	public record Alternation(List<Node> branches) implements Node
	{
		public Alternation{
			branches = ImmutableList.copyOf(branches);
		}
	}

	/** Pieces one after another; an empty sequence matches the empty string */
	public record Sequence(List<Node> pieces) implements Node
	{
		public Sequence{
			pieces = ImmutableList.copyOf(pieces);
		}
	}

	/**
	 * A quantified atom
	 *
	 * @param min the smallest count
	 * @param max the largest count, or {@link #UNBOUNDED}
	 * @param reluctant whether the quantifier ends with {@code ?}
	 */
	public record Repeat(Node atom, int min, int max, boolean reluctant) implements Node
	{
	}

	/** A parenthesized sub-expression, which captures; numbered by its opening parenthesis from 1 */
	public record Group(int number, Node body) implements Node
	{
	}

	/** {@code \N}: the string the group matched */
	public record BackReference(int group) implements Node
	{
	}

	/** One character */
	public record Literal(int codePoint) implements Node
	{
	}

	/** {@code ^} and {@code $}: the start and the end of the string */
	public enum Anchor implements Node
	{
		START, END
	}

	/**
	 * A character class: a class expression in brackets, a multi-character or category escape
	 * outside brackets, or {@code .}
	 *
	 * @param group the class as it was written
	 * @param codePoints the code points of the class, from the Unicode data of the JDK
	 */
	public record CharClass(ClassGroup group, CodePointSet codePoints) implements Node
	{
	}

	/**
	 * A character group: its items, whether {@code ^} negates them, and the class expression that
	 * a {@code -} subtracts from the result
	 */
	public record ClassGroup(boolean negated, List<ClassItem> items, Optional<ClassGroup> subtracted)
	{
		public ClassGroup{
			items = ImmutableList.copyOf(items);
		}
	}

	/** An item of a character group */
	public sealed interface ClassItem permits CodePointRange, CategoryEscape, BlockEscape, MultiCharEscape, Wildcard
	{
	}

	/** One character, or a range {@code s-e} */
	public record CodePointRange(int lo, int hi) implements ClassItem
	{
	}

	/** {@code \p{X}} or, when {@code complement}, {@code \P{X}} for a category X of Appendix F */
	public record CategoryEscape(String name, boolean complement) implements ClassItem
	{
	}

	/** {@code \p{IsX}} or, when {@code complement}, {@code \P{IsX}} for a block X; the name is without {@code Is} */
	public record BlockEscape(String name, boolean complement) implements ClassItem
	{
	}

	/** {@code \s \S \i \I \c \C \d \D \w \W}, by the letter */
	public record MultiCharEscape(char letter) implements ClassItem
	{
	}

	/** {@code .}: every character but the line feed (XF 1.0 section 7.6.1, without the {@code s} flag) */
	public enum Wildcard implements ClassItem
	{
		DOT
	}

	/** The code points of a character group, from the Unicode data of the JDK */
	public static CodePointSet codePoints(ClassGroup group){
		CodePointSet.Builder b = CodePointSet.builder();
		for(ClassItem item : group.items()){
			b.addAll(codePoints(item));
		}
		CodePointSet set = b.build();
		if(group.negated()){
			set = set.complement();
		}
		if(group.subtracted().isPresent()){
			set = set.minus(codePoints(group.subtracted().get()));
		}
		return set;
	}

	/** The code points of one item of a character group */
	public static CodePointSet codePoints(ClassItem item){
		if(item instanceof CodePointRange(int lo, int hi)){
			return CodePointSet.of(lo, hi);
		}
		if(item instanceof CategoryEscape(String name, boolean complement)){
			CodePointSet set = UnicodeSets.category(name);
			return complement ? set.complement() : set;
		}
		if(item instanceof BlockEscape(String name, boolean complement)){
			CodePointSet set = UnicodeSets.block(name)
					.orElseThrow(() -> new IllegalArgumentException("not a block: " + name));
			return complement ? set.complement() : set;
		}
		if(item instanceof MultiCharEscape(char letter)){
			return switch(letter){
				case 's' -> UnicodeSets.whitespace();
				case 'S' -> UnicodeSets.whitespace().complement();
				case 'i' -> UnicodeSets.nameStartChars();
				case 'I' -> UnicodeSets.nameStartChars().complement();
				case 'c' -> UnicodeSets.nameChars();
				case 'C' -> UnicodeSets.nameChars().complement();
				case 'd' -> UnicodeSets.digits();
				case 'D' -> UnicodeSets.digits().complement();
				case 'w' -> UnicodeSets.wordChars();
				case 'W' -> UnicodeSets.wordChars().complement();
				default -> throw new IllegalArgumentException("not a multi-character escape: " + letter);
			};
		}
		return UnicodeSets.all().minus(CodePointSet.of('\n'));
	}

	/** The estimate of design D4 */
	private static long estimate(Node node){
		if(node instanceof Alternation alternation){
			long sum = 0;
			for(Node branch : alternation.branches()){
				sum = saturated(sum + estimate(branch));
			}
			return sum;
		}
		if(node instanceof Sequence sequence){
			long sum = 0;
			for(Node piece : sequence.pieces()){
				sum = saturated(sum + estimate(piece));
			}
			return sum;
		}
		if(node instanceof Repeat repeat){
			long times = repeat.max() == UNBOUNDED ? repeat.min() + 1L : repeat.max();
			return saturated(times(saturated(estimate(repeat.atom()) + 1), times) + 1);
		}
		if(node instanceof Group group){
			return saturated(estimate(group.body()) + 1);
		}
		if(node instanceof CharClass charClass){
			return Math.max(1, charClass.codePoints().rangeCount());
		}
		return 1;
	}

	private static long saturated(long value){
		return value < 0 ? Long.MAX_VALUE : value;
	}

	private static long times(long a, long b){
		if(a == 0 || b == 0){
			return 0;
		}
		return (a > Long.MAX_VALUE / b) ? Long.MAX_VALUE : a * b;
	}

	/** A recursive-descent reader of the grammar, over the code points of the pattern */
	private static final class Parser
	{
		private final int[] cps;
		private int at;
		private int groups;
		private boolean[] closed = new boolean[8];
		private int largestCount;
		private boolean backReferences;

		Parser(String pattern){
			this.cps = pattern.codePoints().toArray();
		}

		Node parseAll(){
			Node root = parseRegExp();
			if(at < cps.length){
				// Only an unmatched ")" stops the top-level expression early
				throw RegexCompileException.malformed("unmatched )");
			}
			return root;
		}

		private Node parseRegExp(){
			List<Node> branches = new ArrayList<>();
			branches.add(parseBranch());
			while(at < cps.length && cps[at] == '|'){
				at++;
				branches.add(parseBranch());
			}
			return branches.size() == 1 ? branches.get(0) : new Alternation(branches);
		}

		private Node parseBranch(){
			List<Node> pieces = new ArrayList<>();
			while(at < cps.length && cps[at] != '|' && cps[at] != ')'){
				pieces.add(parsePiece());
			}
			return new Sequence(pieces);
		}

		private Node parsePiece(){
			Node atom = parseAtom();
			if(at >= cps.length){
				return atom;
			}
			int min;
			int max;
			switch(cps[at]){
				case '?' -> { min = 0; max = 1; at++; }
				case '*' -> { min = 0; max = UNBOUNDED; at++; }
				case '+' -> { min = 1; max = UNBOUNDED; at++; }
				case '{' -> {
					at++;
					min = parseCount();
					max = min;
					if(peek() == ','){
						at++;
						max = isDigit(peek()) ? parseCount() : UNBOUNDED;
					}
					if(peek() != '}'){
						throw RegexCompileException.malformed("invalid quantifier");
					}
					at++;
					if(max != UNBOUNDED && max < min){
						throw RegexCompileException.malformed("invalid quantifier: the largest count is below the smallest");
					}
					largestCount = Math.max(largestCount, Math.max(min, max));
				}
				default -> {
					return atom;
				}
			}
			boolean reluctant = peek() == '?';
			if(reluctant){
				at++;
			}
			return new Repeat(atom, min, max, reluctant);
		}

		/** QuantExact: one or more digits; a count beyond an int stays at Integer.MAX_VALUE */
		private int parseCount(){
			if(!isDigit(peek())){
				throw RegexCompileException.malformed("invalid quantifier");
			}
			long count = 0;
			while(isDigit(peek())){
				count = Math.min(Integer.MAX_VALUE, count * 10 + (cps[at++] - '0'));
			}
			return (int)count;
		}

		private Node parseAtom(){
			int c = cps[at];
			switch(c){
				case '(' -> {
					at++;
					int number = ++groups;
					if(number >= closed.length){
						closed = java.util.Arrays.copyOf(closed, closed.length * 2);
					}
					Node body = parseRegExp();
					if(peek() != ')'){
						throw RegexCompileException.malformed("missing )");
					}
					at++;
					closed[number] = true;
					return new Group(number, body);
				}
				case '[' -> {
					ClassGroup group = parseClassExpr();
					return new CharClass(group, codePoints(group));
				}
				case '.' -> {
					at++;
					return charClass(Wildcard.DOT);
				}
				case '^' -> {
					at++;
					return Anchor.START;
				}
				case '$' -> {
					at++;
					return Anchor.END;
				}
				case '\\' -> {
					return parseEscape();
				}
				case '?', '*', '+', '{' -> throw RegexCompileException.malformed("quantifier without an atom");
				case '}', ']' -> throw RegexCompileException.malformed("unescaped metacharacter");
				default -> {
					at++;
					return new Literal(c);
				}
			}
		}

		private static CharClass charClass(ClassItem item){
			ClassGroup group = new ClassGroup(false, List.of(item), Optional.empty());
			return new CharClass(group, codePoints(group));
		}

		/** An escape outside a class: a literal, a class, or a back-reference */
		private Node parseEscape(){
			at++;
			int c = peek();
			if(c >= '1' && c <= '9'){
				return parseBackReference();
			}
			ClassItem item = parseClassEscape(c);
			if(item instanceof CodePointRange range){
				return new Literal(range.lo());
			}
			return charClass(item);
		}

		/**
		 * XF 1.0 section 7.6.1: {@code \N} with one digit is a back-reference; more digits belong
		 * to it while the number stays within the groups opened so far; the group must be closed
		 * before the back-reference
		 */
		private Node parseBackReference(){
			int group = cps[at++] - '0';
			while(isDigit(peek())){
				int wider = group * 10 + (cps[at] - '0');
				if(wider > groups){
					break;
				}
				group = wider;
				at++;
			}
			if(group > groups || !closed[group]){
				throw RegexCompileException.malformed("invalid back-reference");
			}
			backReferences = true;
			return new BackReference(group);
		}

		/**
		 * The escape after a backslash, inside or outside a class: a single character as a range
		 * of one, a multi-character escape, a category or a block
		 */
		private ClassItem parseClassEscape(int c){
			if(c == -1){
				throw RegexCompileException.malformed("invalid escape at the end of the pattern");
			}
			at++;
			switch(c){
				case 'n' -> { return new CodePointRange('\n', '\n'); }
				case 'r' -> { return new CodePointRange('\r', '\r'); }
				case 't' -> { return new CodePointRange('\t', '\t'); }
				case '\\', '|', '.', '-', '^', '?', '*', '+', '{', '}', '(', ')', '[', ']', '$' -> {
					return new CodePointRange(c, c);
				}
				case 's', 'S', 'i', 'I', 'c', 'C', 'd', 'D', 'w', 'W' -> { return new MultiCharEscape((char)c); }
				case 'p', 'P' -> { return parseProperty(c == 'P'); }
				default -> throw RegexCompileException.malformed("invalid escape");
			}
		}

		/** {@code {X}} after {@code \p} or {@code \P}: a category, or a block as {@code IsX} */
		private ClassItem parseProperty(boolean complement){
			if(peek() != '{'){
				throw RegexCompileException.malformed("invalid property escape");
			}
			at++;
			int start = at;
			while(at < cps.length && cps[at] != '}'){
				at++;
			}
			if(at >= cps.length){
				throw RegexCompileException.malformed("invalid property escape");
			}
			String name = new String(cps, start, at - start);
			at++;
			if(UnicodeSets.isCategory(name)){
				return new CategoryEscape(name, complement);
			}
			if(name.startsWith("Is") && name.length() > 2 && isBlockName(name.substring(2))
					&& UnicodeSets.block(name.substring(2)).isPresent()){
				return new BlockEscape(name.substring(2), complement);
			}
			throw RegexCompileException.malformed("unknown category or block");
		}

		/** IsBlock: letters, digits and hyphens */
		private static boolean isBlockName(String name){
			for(int i = 0; i < name.length(); i++){
				char c = name.charAt(i);
				if(!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-')){
					return false;
				}
			}
			return true;
		}

		/** A class expression in brackets, with its negation and its subtraction */
		private ClassGroup parseClassExpr(){
			if(peek() != '['){
				throw RegexCompileException.malformed("invalid character class");
			}
			at++;
			boolean negated = peek() == '^';
			if(negated){
				at++;
			}
			List<ClassItem> items = parsePosCharGroup();
			Optional<ClassGroup> subtracted = Optional.empty();
			if(peek() == '-'){
				at++;
				subtracted = Optional.of(parseClassExpr());
			}
			if(peek() != ']'){
				throw RegexCompileException.malformed("invalid character class");
			}
			at++;
			return new ClassGroup(negated, items, subtracted);
		}

		/**
		 * The items of a group up to its end: a hyphen stands for itself only first or last, and
		 * before {@code [} it starts a subtraction; a range needs a character or single-character
		 * escape at both ends, in order
		 */
		private List<ClassItem> parsePosCharGroup(){
			List<ClassItem> items = new ArrayList<>();
			while(true){
				int c = peek();
				if(c == -1){
					throw RegexCompileException.malformed("unclosed character class");
				}
				if(c == ']'){
					break;
				}
				if(c == '['){
					throw RegexCompileException.malformed("unescaped [ in a character class");
				}
				if(c == '-'){
					int next = peekAt(1);
					if(next == '['){
						break;
					}
					if(next == ']' || items.isEmpty()){
						at++;
						items.add(new CodePointRange('-', '-'));
						continue;
					}
					throw RegexCompileException.malformed("a hyphen in a character class must come first, last, or in a range");
				}
				ClassItem item;
				if(c == '\\'){
					at++;
					if(peek() >= '1' && peek() <= '9'){
						parseBackReference();
						throw RegexCompileException.unsupported("back-reference in a character class", "xf parser");
					}
					item = parseClassEscape(peek());
				}else{
					at++;
					item = new CodePointRange(c, c);
				}
				if(item instanceof CodePointRange start && peek() == '-' && peekAt(1) != ']' && peekAt(1) != '['){
					at++;
					int end = peek();
					if(end == -1){
						throw RegexCompileException.malformed("unclosed character class");
					}
					if(end == '\\'){
						at++;
						if(!(parseClassEscape(peek()) instanceof CodePointRange escaped)){
							throw RegexCompileException.malformed("invalid range");
						}
						end = escaped.lo();
					}else if(end == '-'){
						throw RegexCompileException.malformed("invalid range");
					}else{
						at++;
					}
					if(end < start.lo()){
						throw RegexCompileException.malformed("invalid range: the end is before the start");
					}
					item = new CodePointRange(start.lo(), end);
				}
				items.add(item);
			}
			if(items.isEmpty()){
				throw RegexCompileException.malformed("empty character class");
			}
			return items;
		}

		private int peek(){
			return at < cps.length ? cps[at] : -1;
		}

		private int peekAt(int offset){
			return at + offset < cps.length ? cps[at + offset] : -1;
		}

		private static boolean isDigit(int c){
			return c >= '0' && c <= '9';
		}
	}
}
