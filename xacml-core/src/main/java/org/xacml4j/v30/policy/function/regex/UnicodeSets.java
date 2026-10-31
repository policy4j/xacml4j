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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The code-point sets of the {@code xf} profile, from the Unicode data of the running JDK
 * (change add-regex-engine-spi, design D2): the categories and blocks of XML Schema Part 2
 * Appendix F, the name characters of XML 1.0 Second Edition, and the multi-character escapes.
 * Each table is scanned once, when it is first needed.
 */
final class UnicodeSets
{
	/** The XML Schema categories by name, each the types of {@link Character#getType(int)} it holds */
	private static final Map<String, int[]> CATEGORIES = Map.ofEntries(
			Map.entry("Lu", new int[]{Character.UPPERCASE_LETTER}),
			Map.entry("Ll", new int[]{Character.LOWERCASE_LETTER}),
			Map.entry("Lt", new int[]{Character.TITLECASE_LETTER}),
			Map.entry("Lm", new int[]{Character.MODIFIER_LETTER}),
			Map.entry("Lo", new int[]{Character.OTHER_LETTER}),
			Map.entry("L", new int[]{Character.UPPERCASE_LETTER, Character.LOWERCASE_LETTER, Character.TITLECASE_LETTER,
					Character.MODIFIER_LETTER, Character.OTHER_LETTER}),
			Map.entry("Mn", new int[]{Character.NON_SPACING_MARK}),
			Map.entry("Mc", new int[]{Character.COMBINING_SPACING_MARK}),
			Map.entry("Me", new int[]{Character.ENCLOSING_MARK}),
			Map.entry("M", new int[]{Character.NON_SPACING_MARK, Character.COMBINING_SPACING_MARK, Character.ENCLOSING_MARK}),
			Map.entry("Nd", new int[]{Character.DECIMAL_DIGIT_NUMBER}),
			Map.entry("Nl", new int[]{Character.LETTER_NUMBER}),
			Map.entry("No", new int[]{Character.OTHER_NUMBER}),
			Map.entry("N", new int[]{Character.DECIMAL_DIGIT_NUMBER, Character.LETTER_NUMBER, Character.OTHER_NUMBER}),
			Map.entry("Pc", new int[]{Character.CONNECTOR_PUNCTUATION}),
			Map.entry("Pd", new int[]{Character.DASH_PUNCTUATION}),
			Map.entry("Ps", new int[]{Character.START_PUNCTUATION}),
			Map.entry("Pe", new int[]{Character.END_PUNCTUATION}),
			Map.entry("Pi", new int[]{Character.INITIAL_QUOTE_PUNCTUATION}),
			Map.entry("Pf", new int[]{Character.FINAL_QUOTE_PUNCTUATION}),
			Map.entry("Po", new int[]{Character.OTHER_PUNCTUATION}),
			Map.entry("P", new int[]{Character.CONNECTOR_PUNCTUATION, Character.DASH_PUNCTUATION, Character.START_PUNCTUATION,
					Character.END_PUNCTUATION, Character.INITIAL_QUOTE_PUNCTUATION, Character.FINAL_QUOTE_PUNCTUATION,
					Character.OTHER_PUNCTUATION}),
			Map.entry("Zs", new int[]{Character.SPACE_SEPARATOR}),
			Map.entry("Zl", new int[]{Character.LINE_SEPARATOR}),
			Map.entry("Zp", new int[]{Character.PARAGRAPH_SEPARATOR}),
			Map.entry("Z", new int[]{Character.SPACE_SEPARATOR, Character.LINE_SEPARATOR, Character.PARAGRAPH_SEPARATOR}),
			Map.entry("Sm", new int[]{Character.MATH_SYMBOL}),
			Map.entry("Sc", new int[]{Character.CURRENCY_SYMBOL}),
			Map.entry("Sk", new int[]{Character.MODIFIER_SYMBOL}),
			Map.entry("So", new int[]{Character.OTHER_SYMBOL}),
			Map.entry("S", new int[]{Character.MATH_SYMBOL, Character.CURRENCY_SYMBOL, Character.MODIFIER_SYMBOL,
					Character.OTHER_SYMBOL}),
			Map.entry("Cc", new int[]{Character.CONTROL}),
			Map.entry("Cf", new int[]{Character.FORMAT}),
			Map.entry("Co", new int[]{Character.PRIVATE_USE}),
			Map.entry("Cn", new int[]{Character.UNASSIGNED}),
			// XML Schema Part 2, Appendix F: C holds Cc, Cf, Co and Cn; the surrogate category Cs
			// does not occur at the level of characters that XML works on
			Map.entry("C", new int[]{Character.CONTROL, Character.FORMAT, Character.PRIVATE_USE, Character.UNASSIGNED}));

	private static final Map<String, CodePointSet> COMPOSED = new ConcurrentHashMap<>();

	private UnicodeSets(){
	}

	static CodePointSet all(){
		return CodePointSet.ALL;
	}

	static void prepare(){
		for(String category : CATEGORIES.keySet()){
			category(category);
		}
		wordChars();
		block("BasicLatin");
		nameChars();
		whitespace();
	}

	/** Whether a name is one of the categories of XML Schema Part 2 Appendix F */
	static boolean isCategory(String name){
		return CATEGORIES.containsKey(name);
	}

	/**
	 * The code points of a category of XML Schema Part 2 Appendix F, from the Unicode data of
	 * the JDK
	 *
	 * @throws IllegalArgumentException for a name that is not a category
	 */
	static CodePointSet category(String name){
		int[] types = CATEGORIES.get(name);
		if(types == null){
			throw new IllegalArgumentException("not a category: " + name);
		}
		return COMPOSED.computeIfAbsent(name, n -> {
			CodePointSet.Builder b = CodePointSet.builder();
			for(int type : types){
				b.addAll(Types.BY_TYPE[type]);
			}
			return b.build();
		});
	}

	/**
	 * The code points of a block by its canonical XML Schema Appendix F name
	 *
	 * @return the block, or nothing for a name that is not a block
	 */
	static Optional<CodePointSet> block(String name){
		return Optional.ofNullable(Blocks.BY_NAME.get(name));
	}

	/** {@code \s}: space, tab, line feed and carriage return */
	static CodePointSet whitespace(){
		return Escapes.WHITESPACE;
	}

	/** {@code \i}: the name start characters of XML 1.0 Second Edition. */
	static CodePointSet nameStartChars(){
		return Escapes.NAME_START;
	}

	/** {@code \c}: the name characters of XML 1.0 Second Edition. */
	static CodePointSet nameChars(){
		return Escapes.NAME;
	}

	/** {@code \d}: the decimal digits, {@code \p{Nd}} */
	static CodePointSet digits(){
		return category("Nd");
	}

	/** {@code \w}: every code point but the punctuation, separators and others, {@code [^\p{P}\p{Z}\p{C}]} */
	static CodePointSet wordChars(){
		return Word.CHARS;
	}

	private static final class Word
	{
		private static final CodePointSet CHARS = CodePointSet.ALL.minus(category("P"))
				.minus(category("Z")).minus(category("C"));
	}

	/** The ranges of every type of {@link Character#getType(int)}, scanned once */
	private static final class Types
	{
		private static final CodePointSet[] BY_TYPE = scan();

		private static CodePointSet[] scan(){
			CodePointSet.Builder[] builders = new CodePointSet.Builder[Character.FINAL_QUOTE_PUNCTUATION + 1];
			for(int i = 0; i < builders.length; i++){
				builders[i] = CodePointSet.builder();
			}
			int start = Character.MIN_CODE_POINT;
			int type = Character.getType(start);
			for(int cp = start + 1; cp <= Character.MAX_CODE_POINT; cp++){
				int next = Character.getType(cp);
				if(next != type){
					builders[type].add(start, cp - 1);
					start = cp;
					type = next;
				}
			}
			builders[type].add(start, Character.MAX_CODE_POINT);
			CodePointSet[] sets = new CodePointSet[builders.length];
			for(int i = 0; i < builders.length; i++){
				sets[i] = builders[i].build();
			}
			return sets;
		}
	}

	/** The canonical block names and ranges of XML Schema Appendix F. */
	private static final class Blocks
	{
		private static final Map<String, CodePointSet> BY_NAME = Map.ofEntries(
				Map.entry("BasicLatin", CodePointSet.of(0x0000, 0x007F)),
				Map.entry("Latin-1Supplement", CodePointSet.of(0x0080, 0x00FF)),
				Map.entry("LatinExtended-A", CodePointSet.of(0x0100, 0x017F)),
				Map.entry("LatinExtended-B", CodePointSet.of(0x0180, 0x024F)),
				Map.entry("IPAExtensions", CodePointSet.of(0x0250, 0x02AF)),
				Map.entry("SpacingModifierLetters", CodePointSet.of(0x02B0, 0x02FF)),
				Map.entry("CombiningDiacriticalMarks", CodePointSet.of(0x0300, 0x036F)),
				Map.entry("Greek", CodePointSet.of(0x0370, 0x03FF)),
				Map.entry("Cyrillic", CodePointSet.of(0x0400, 0x04FF)),
				Map.entry("Armenian", CodePointSet.of(0x0530, 0x058F)),
				Map.entry("Hebrew", CodePointSet.of(0x0590, 0x05FF)),
				Map.entry("Arabic", CodePointSet.of(0x0600, 0x06FF)),
				Map.entry("Syriac", CodePointSet.of(0x0700, 0x074F)),
				Map.entry("Thaana", CodePointSet.of(0x0780, 0x07BF)),
				Map.entry("Devanagari", CodePointSet.of(0x0900, 0x097F)),
				Map.entry("Bengali", CodePointSet.of(0x0980, 0x09FF)),
				Map.entry("Gurmukhi", CodePointSet.of(0x0A00, 0x0A7F)),
				Map.entry("Gujarati", CodePointSet.of(0x0A80, 0x0AFF)),
				Map.entry("Oriya", CodePointSet.of(0x0B00, 0x0B7F)),
				Map.entry("Tamil", CodePointSet.of(0x0B80, 0x0BFF)),
				Map.entry("Telugu", CodePointSet.of(0x0C00, 0x0C7F)),
				Map.entry("Kannada", CodePointSet.of(0x0C80, 0x0CFF)),
				Map.entry("Malayalam", CodePointSet.of(0x0D00, 0x0D7F)),
				Map.entry("Sinhala", CodePointSet.of(0x0D80, 0x0DFF)),
				Map.entry("Thai", CodePointSet.of(0x0E00, 0x0E7F)),
				Map.entry("Lao", CodePointSet.of(0x0E80, 0x0EFF)),
				Map.entry("Tibetan", CodePointSet.of(0x0F00, 0x0FFF)),
				Map.entry("Myanmar", CodePointSet.of(0x1000, 0x109F)),
				Map.entry("Georgian", CodePointSet.of(0x10A0, 0x10FF)),
				Map.entry("HangulJamo", CodePointSet.of(0x1100, 0x11FF)),
				Map.entry("Ethiopic", CodePointSet.of(0x1200, 0x137F)),
				Map.entry("Cherokee", CodePointSet.of(0x13A0, 0x13FF)),
				Map.entry("UnifiedCanadianAboriginalSyllabics", CodePointSet.of(0x1400, 0x167F)),
				Map.entry("Ogham", CodePointSet.of(0x1680, 0x169F)),
				Map.entry("Runic", CodePointSet.of(0x16A0, 0x16FF)),
				Map.entry("Khmer", CodePointSet.of(0x1780, 0x17FF)),
				Map.entry("Mongolian", CodePointSet.of(0x1800, 0x18AF)),
				Map.entry("LatinExtendedAdditional", CodePointSet.of(0x1E00, 0x1EFF)),
				Map.entry("GreekExtended", CodePointSet.of(0x1F00, 0x1FFF)),
				Map.entry("GeneralPunctuation", CodePointSet.of(0x2000, 0x206F)),
				Map.entry("SuperscriptsandSubscripts", CodePointSet.of(0x2070, 0x209F)),
				Map.entry("CurrencySymbols", CodePointSet.of(0x20A0, 0x20CF)),
				Map.entry("CombiningMarksforSymbols", CodePointSet.of(0x20D0, 0x20FF)),
				Map.entry("LetterlikeSymbols", CodePointSet.of(0x2100, 0x214F)),
				Map.entry("NumberForms", CodePointSet.of(0x2150, 0x218F)),
				Map.entry("Arrows", CodePointSet.of(0x2190, 0x21FF)),
				Map.entry("MathematicalOperators", CodePointSet.of(0x2200, 0x22FF)),
				Map.entry("MiscellaneousTechnical", CodePointSet.of(0x2300, 0x23FF)),
				Map.entry("ControlPictures", CodePointSet.of(0x2400, 0x243F)),
				Map.entry("OpticalCharacterRecognition", CodePointSet.of(0x2440, 0x245F)),
				Map.entry("EnclosedAlphanumerics", CodePointSet.of(0x2460, 0x24FF)),
				Map.entry("BoxDrawing", CodePointSet.of(0x2500, 0x257F)),
				Map.entry("BlockElements", CodePointSet.of(0x2580, 0x259F)),
				Map.entry("GeometricShapes", CodePointSet.of(0x25A0, 0x25FF)),
				Map.entry("MiscellaneousSymbols", CodePointSet.of(0x2600, 0x26FF)),
				Map.entry("Dingbats", CodePointSet.of(0x2700, 0x27BF)),
				Map.entry("BraillePatterns", CodePointSet.of(0x2800, 0x28FF)),
				Map.entry("CJKRadicalsSupplement", CodePointSet.of(0x2E80, 0x2EFF)),
				Map.entry("KangxiRadicals", CodePointSet.of(0x2F00, 0x2FDF)),
				Map.entry("IdeographicDescriptionCharacters", CodePointSet.of(0x2FF0, 0x2FFF)),
				Map.entry("CJKSymbolsandPunctuation", CodePointSet.of(0x3000, 0x303F)),
				Map.entry("Hiragana", CodePointSet.of(0x3040, 0x309F)),
				Map.entry("Katakana", CodePointSet.of(0x30A0, 0x30FF)),
				Map.entry("Bopomofo", CodePointSet.of(0x3100, 0x312F)),
				Map.entry("HangulCompatibilityJamo", CodePointSet.of(0x3130, 0x318F)),
				Map.entry("Kanbun", CodePointSet.of(0x3190, 0x319F)),
				Map.entry("BopomofoExtended", CodePointSet.of(0x31A0, 0x31BF)),
				Map.entry("EnclosedCJKLettersandMonths", CodePointSet.of(0x3200, 0x32FF)),
				Map.entry("CJKCompatibility", CodePointSet.of(0x3300, 0x33FF)),
				Map.entry("CJKUnifiedIdeographsExtensionA", CodePointSet.of(0x3400, 0x4DB5)),
				Map.entry("CJKUnifiedIdeographs", CodePointSet.of(0x4E00, 0x9FFF)),
				Map.entry("YiSyllables", CodePointSet.of(0xA000, 0xA48F)),
				Map.entry("YiRadicals", CodePointSet.of(0xA490, 0xA4CF)),
				Map.entry("HangulSyllables", CodePointSet.of(0xAC00, 0xD7A3)),
				Map.entry("PrivateUse", CodePointSet.of(0xE000, 0xF8FF)),
				Map.entry("CJKCompatibilityIdeographs", CodePointSet.of(0xF900, 0xFAFF)),
				Map.entry("AlphabeticPresentationForms", CodePointSet.of(0xFB00, 0xFB4F)),
				Map.entry("ArabicPresentationForms-A", CodePointSet.of(0xFB50, 0xFDFF)),
				Map.entry("CombiningHalfMarks", CodePointSet.of(0xFE20, 0xFE2F)),
				Map.entry("CJKCompatibilityForms", CodePointSet.of(0xFE30, 0xFE4F)),
				Map.entry("SmallFormVariants", CodePointSet.of(0xFE50, 0xFE6F)),
				Map.entry("ArabicPresentationForms-B", CodePointSet.of(0xFE70, 0xFEFE)),
				Map.entry("Specials", CodePointSet.of(0xFEFF, 0xFEFF).union(CodePointSet.of(0xFFF0, 0xFFFD))),
				Map.entry("HalfwidthandFullwidthForms", CodePointSet.of(0xFF00, 0xFFEF)));
	}

	/** The multi-character escapes with fixed ranges */
	private static final class Escapes
	{
		private static final CodePointSet WHITESPACE = CodePointSet.builder()
				.add(0x20).add(0x09).add(0x0A).add(0x0D).build();

		/** XML 1.0 Second Edition, productions [5], [85] and [86]. */
		private static final CodePointSet NAME_START = ranges(
				0x0041, 0x005A, 0x0061, 0x007A, 0x00C0, 0x00D6, 0x00D8, 0x00F6, 0x00F8, 0x00FF,
				0x0100, 0x0131, 0x0134, 0x013E, 0x0141, 0x0148, 0x014A, 0x017E, 0x0180, 0x01C3,
				0x01CD, 0x01F0, 0x01F4, 0x01F5, 0x01FA, 0x0217, 0x0250, 0x02A8, 0x02BB, 0x02C1,
				0x0386, 0x0386, 0x0388, 0x038A, 0x038C, 0x038C, 0x038E, 0x03A1, 0x03A3, 0x03CE,
				0x03D0, 0x03D6, 0x03DA, 0x03DA, 0x03DC, 0x03DC, 0x03DE, 0x03DE, 0x03E0, 0x03E0,
				0x03E2, 0x03F3, 0x0401, 0x040C, 0x040E, 0x044F, 0x0451, 0x045C, 0x045E, 0x0481,
				0x0490, 0x04C4, 0x04C7, 0x04C8, 0x04CB, 0x04CC, 0x04D0, 0x04EB, 0x04EE, 0x04F5,
				0x04F8, 0x04F9, 0x0531, 0x0556, 0x0559, 0x0559, 0x0561, 0x0586, 0x05D0, 0x05EA,
				0x05F0, 0x05F2, 0x0621, 0x063A, 0x0641, 0x064A, 0x0671, 0x06B7, 0x06BA, 0x06BE,
				0x06C0, 0x06CE, 0x06D0, 0x06D3, 0x06D5, 0x06D5, 0x06E5, 0x06E6, 0x0905, 0x0939,
				0x093D, 0x093D, 0x0958, 0x0961, 0x0985, 0x098C, 0x098F, 0x0990, 0x0993, 0x09A8,
				0x09AA, 0x09B0, 0x09B2, 0x09B2, 0x09B6, 0x09B9, 0x09DC, 0x09DD, 0x09DF, 0x09E1,
				0x09F0, 0x09F1, 0x0A05, 0x0A0A, 0x0A0F, 0x0A10, 0x0A13, 0x0A28, 0x0A2A, 0x0A30,
				0x0A32, 0x0A33, 0x0A35, 0x0A36, 0x0A38, 0x0A39, 0x0A59, 0x0A5C, 0x0A5E, 0x0A5E,
				0x0A72, 0x0A74, 0x0A85, 0x0A8B, 0x0A8D, 0x0A8D, 0x0A8F, 0x0A91, 0x0A93, 0x0AA8,
				0x0AAA, 0x0AB0, 0x0AB2, 0x0AB3, 0x0AB5, 0x0AB9, 0x0ABD, 0x0ABD, 0x0AE0, 0x0AE0,
				0x0B05, 0x0B0C, 0x0B0F, 0x0B10, 0x0B13, 0x0B28, 0x0B2A, 0x0B30, 0x0B32, 0x0B33,
				0x0B36, 0x0B39, 0x0B3D, 0x0B3D, 0x0B5C, 0x0B5D, 0x0B5F, 0x0B61, 0x0B85, 0x0B8A,
				0x0B8E, 0x0B90, 0x0B92, 0x0B95, 0x0B99, 0x0B9A, 0x0B9C, 0x0B9C, 0x0B9E, 0x0B9F,
				0x0BA3, 0x0BA4, 0x0BA8, 0x0BAA, 0x0BAE, 0x0BB5, 0x0BB7, 0x0BB9, 0x0C05, 0x0C0C,
				0x0C0E, 0x0C10, 0x0C12, 0x0C28, 0x0C2A, 0x0C33, 0x0C35, 0x0C39, 0x0C60, 0x0C61,
				0x0C85, 0x0C8C, 0x0C8E, 0x0C90, 0x0C92, 0x0CA8, 0x0CAA, 0x0CB3, 0x0CB5, 0x0CB9,
				0x0CDE, 0x0CDE, 0x0CE0, 0x0CE1, 0x0D05, 0x0D0C, 0x0D0E, 0x0D10, 0x0D12, 0x0D28,
				0x0D2A, 0x0D39, 0x0D60, 0x0D61, 0x0E01, 0x0E2E, 0x0E30, 0x0E30, 0x0E32, 0x0E33,
				0x0E40, 0x0E45, 0x0E81, 0x0E82, 0x0E84, 0x0E84, 0x0E87, 0x0E88, 0x0E8A, 0x0E8A,
				0x0E8D, 0x0E8D, 0x0E94, 0x0E97, 0x0E99, 0x0E9F, 0x0EA1, 0x0EA3, 0x0EA5, 0x0EA5,
				0x0EA7, 0x0EA7, 0x0EAA, 0x0EAB, 0x0EAD, 0x0EAE, 0x0EB0, 0x0EB0, 0x0EB2, 0x0EB3,
				0x0EBD, 0x0EBD, 0x0EC0, 0x0EC4, 0x0F40, 0x0F47, 0x0F49, 0x0F69, 0x10A0, 0x10C5,
				0x10D0, 0x10F6, 0x1100, 0x1100, 0x1102, 0x1103, 0x1105, 0x1107, 0x1109, 0x1109,
				0x110B, 0x110C, 0x110E, 0x1112, 0x113C, 0x113C, 0x113E, 0x113E, 0x1140, 0x1140,
				0x114C, 0x114C, 0x114E, 0x114E, 0x1150, 0x1150, 0x1154, 0x1155, 0x1159, 0x1159,
				0x115F, 0x1161, 0x1163, 0x1163, 0x1165, 0x1165, 0x1167, 0x1167, 0x1169, 0x1169,
				0x116D, 0x116E, 0x1172, 0x1173, 0x1175, 0x1175, 0x119E, 0x119E, 0x11A8, 0x11A8,
				0x11AB, 0x11AB, 0x11AE, 0x11AF, 0x11B7, 0x11B8, 0x11BA, 0x11BA, 0x11BC, 0x11C2,
				0x11EB, 0x11EB, 0x11F0, 0x11F0, 0x11F9, 0x11F9, 0x1E00, 0x1E9B, 0x1EA0, 0x1EF9,
				0x1F00, 0x1F15, 0x1F18, 0x1F1D, 0x1F20, 0x1F45, 0x1F48, 0x1F4D, 0x1F50, 0x1F57,
				0x1F59, 0x1F59, 0x1F5B, 0x1F5B, 0x1F5D, 0x1F5D, 0x1F5F, 0x1F7D, 0x1F80, 0x1FB4,
				0x1FB6, 0x1FBC, 0x1FBE, 0x1FBE, 0x1FC2, 0x1FC4, 0x1FC6, 0x1FCC, 0x1FD0, 0x1FD3,
				0x1FD6, 0x1FDB, 0x1FE0, 0x1FEC, 0x1FF2, 0x1FF4, 0x1FF6, 0x1FFC, 0x2126, 0x2126,
				0x212A, 0x212B, 0x212E, 0x212E, 0x2180, 0x2182, 0x3041, 0x3094, 0x30A1, 0x30FA,
				0x3105, 0x312C, 0xAC00, 0xD7A3, 0x4E00, 0x9FA5, 0x3007, 0x3007, 0x3021, 0x3029,
				0x003A, 0x003A, 0x005F, 0x005F);

		/** XML 1.0 Second Edition, productions [4] and [87] to [89]. */
		private static final CodePointSet NAME = NAME_START.union(ranges(
				0x0300, 0x0345, 0x0360, 0x0361, 0x0483, 0x0486, 0x0591, 0x05A1, 0x05A3, 0x05B9,
				0x05BB, 0x05BD, 0x05BF, 0x05BF, 0x05C1, 0x05C2, 0x05C4, 0x05C4, 0x064B, 0x0652,
				0x0670, 0x0670, 0x06D6, 0x06DC, 0x06DD, 0x06DF, 0x06E0, 0x06E4, 0x06E7, 0x06E8,
				0x06EA, 0x06ED, 0x0901, 0x0903, 0x093C, 0x093C, 0x093E, 0x094C, 0x094D, 0x094D,
				0x0951, 0x0954, 0x0962, 0x0963, 0x0981, 0x0983, 0x09BC, 0x09BC, 0x09BE, 0x09BE,
				0x09BF, 0x09BF, 0x09C0, 0x09C4, 0x09C7, 0x09C8, 0x09CB, 0x09CD, 0x09D7, 0x09D7,
				0x09E2, 0x09E3, 0x0A02, 0x0A02, 0x0A3C, 0x0A3C, 0x0A3E, 0x0A3E, 0x0A3F, 0x0A3F,
				0x0A40, 0x0A42, 0x0A47, 0x0A48, 0x0A4B, 0x0A4D, 0x0A70, 0x0A71, 0x0A81, 0x0A83,
				0x0ABC, 0x0ABC, 0x0ABE, 0x0AC5, 0x0AC7, 0x0AC9, 0x0ACB, 0x0ACD, 0x0B01, 0x0B03,
				0x0B3C, 0x0B3C, 0x0B3E, 0x0B43, 0x0B47, 0x0B48, 0x0B4B, 0x0B4D, 0x0B56, 0x0B57,
				0x0B82, 0x0B83, 0x0BBE, 0x0BC2, 0x0BC6, 0x0BC8, 0x0BCA, 0x0BCD, 0x0BD7, 0x0BD7,
				0x0C01, 0x0C03, 0x0C3E, 0x0C44, 0x0C46, 0x0C48, 0x0C4A, 0x0C4D, 0x0C55, 0x0C56,
				0x0C82, 0x0C83, 0x0CBE, 0x0CC4, 0x0CC6, 0x0CC8, 0x0CCA, 0x0CCD, 0x0CD5, 0x0CD6,
				0x0D02, 0x0D03, 0x0D3E, 0x0D43, 0x0D46, 0x0D48, 0x0D4A, 0x0D4D, 0x0D57, 0x0D57,
				0x0E31, 0x0E31, 0x0E34, 0x0E3A, 0x0E47, 0x0E4E, 0x0EB1, 0x0EB1, 0x0EB4, 0x0EB9,
				0x0EBB, 0x0EBC, 0x0EC8, 0x0ECD, 0x0F18, 0x0F19, 0x0F35, 0x0F35, 0x0F37, 0x0F37,
				0x0F39, 0x0F39, 0x0F3E, 0x0F3E, 0x0F3F, 0x0F3F, 0x0F71, 0x0F84, 0x0F86, 0x0F8B,
				0x0F90, 0x0F95, 0x0F97, 0x0F97, 0x0F99, 0x0FAD, 0x0FB1, 0x0FB7, 0x0FB9, 0x0FB9,
				0x20D0, 0x20DC, 0x20E1, 0x20E1, 0x302A, 0x302F, 0x3099, 0x3099, 0x309A, 0x309A,
				0x0030, 0x0039, 0x0660, 0x0669, 0x06F0, 0x06F9, 0x0966, 0x096F, 0x09E6, 0x09EF,
				0x0A66, 0x0A6F, 0x0AE6, 0x0AEF, 0x0B66, 0x0B6F, 0x0BE7, 0x0BEF, 0x0C66, 0x0C6F,
				0x0CE6, 0x0CEF, 0x0D66, 0x0D6F, 0x0E50, 0x0E59, 0x0ED0, 0x0ED9, 0x0F20, 0x0F29,
				0x00B7, 0x00B7, 0x02D0, 0x02D0, 0x02D1, 0x02D1, 0x0387, 0x0387, 0x0640, 0x0640,
				0x0E46, 0x0E46, 0x0EC6, 0x0EC6, 0x3005, 0x3005, 0x3031, 0x3035, 0x309D, 0x309E,
				0x30FC, 0x30FE, 0x002D, 0x002E));

		/** A set from pairs of first and last code points */
		private static CodePointSet ranges(int... pairs){
			CodePointSet.Builder b = CodePointSet.builder();
			for(int i = 0; i < pairs.length; i += 2){
				b.add(pairs[i], pairs[i + 1]);
			}
			return b.build();
		}
	}
}
