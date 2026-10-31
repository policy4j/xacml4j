## RX1: Q1. Which XML Schema regex features RE2/J can express, translate, or must reject

Read-only lane, 2026-10-27. I wrote no files and ran no Maven, tests, or benchmarks.

### Answer

1. **XACML uses `fn:matches`, not the XSD `pattern` facet.** XACML 3.0 A.3.13 defines `string-regexp-match` as "xf:matches … with the arguments reversed [XF] Section 7.6.2" [S5 §A.3.13]. The other five functions convert their argument with `string-from-*`, then apply `string-regexp-match` [S5].
   - [XF] is **F&O 1.0 of 23 January 2007**, not F&O 3.1 [S5 §1.4]. The packet's premise "3.1 §5.6.1, which A.3.13 cites" is drift. §5.6.1 of 3.1 is the successor text.
   - So a match is a substring search, `^`/`$` are anchors, and no flags apply [S3 §5.6.3]. The XSD rule that "implicitly anchors all regular expressions" applies only to the facet [S1 F; S2 G].
   - No anchoring translation is needed. RE2/J `Matcher.find()` and its default one-line mode give these semantics (rows 1–3).
2. **RE2/J expresses most of the grammar directly.** These need a translation: `.`, `\s \S \d \D \w \W`, `\i \I \c \C`, block escapes, `\p{Cn}`, `\p{C}`, and class subtraction.
   - RE2 has no block escapes, no `Cn`, no class subtraction, and ASCII-only `\d \s \w` [S7; S8].
3. **The dangerous cases fail silently.** RE2/J accepts all of these with no error but with another meaning:
   - `[a-z-[aeiou]]`
   - `\12` (read as octal U+000A, not a back-reference)
   - `x{,3}` (a literal)
   - `(?i)`
   - `\p{Greek}` (a script, not a block)

   The translator must be a full validating parser of the F&O grammar. A text rewrite is not enough.
4. **Must reject with re2j:**
   - back-references
   - repeat counts above 1000
   - every construct outside the F&O grammar (list R1/R2 below)
5. **The Unicode data differ.** RE2/J 1.7 and 1.8 tables say "Unicode version 6.0.0.0" [S8 UnicodeTables.java:7; S9 raw file at tag re2j-1.8]. Their `Sc` table ends at U+20B9 [S8 UnicodeTables.java:1726].
   - JDK 25 uses Unicode 16.0 [S10 Character.java:66].
   - If re2j uses RE2/J's own `\p{..}`, the two engines disagree on every code point assigned after 6.0.
6. **Today's `jdk` translation** is correct for substring matching, `^`, `$`→`\z`, `\$`, positive subtraction, and categories.
   - Reading the source, it has silent drifts: `\c`, `\d \s \w`, `.`, negative-group subtraction, and `&&` (D1–D8). These are predictions; I did not run them.
7. **The conformance fixtures exercise almost none of this.** Their patterns use only literals, spaces, `.`, `*`, and `|`. Examples:
   - `J.* Hibbert` (xacml3.0-ct-v.0.4/IIC056Policy.xml:15)
   - `read|write` (IIB008Policy.xml:32)
   - `   This  is n*o*t* *IT!  ` (IIC168Policy.xml:12)

   A dedicated pattern corpus is needed.

**Sources** (all retrieved 2026-10-27):
- S1: XML Schema Part 2, Second Edition, W3C REC 28 Oct 2004, Appendix F. https://www.w3.org/TR/xmlschema-2/#regexs
- S2: XSD 1.1 Part 2, W3C REC 5 Apr 2012, Appendix G. https://www.w3.org/TR/xmlschema11-2/#regexs
- S3: F&O 3.1, W3C REC 21 Mar 2017, §5.6.1–§5.6.3. https://www.w3.org/TR/xpath-functions-31/#regex-syntax
- S4: F&O 1.0, W3C REC 23 Jan 2007, §7.6.1 (https://www.w3.org/TR/2007/REC-xpath-functions-20070123/), and its Second Edition, 14 Dec 2010, revised 13 Apr 2015 (https://www.w3.org/TR/2010/REC-xpath-functions-20101214/)
- S5: XACML 3.0 Plus Errata 01, OASIS Standard incorporating Approved Errata, 12 Jul 2017, §1.4 and §A.3.13. https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html
- S6: XML 1.0 Fifth Edition §2.3, productions [4] and [4a]. https://www.w3.org/TR/xml/
- S7: RE2 Syntax wiki, revision of 11 Aug 2023. https://github.com/google/re2/wiki/Syntax
- S8: RE2/J 1.7 sources jar from the local Maven cache, read with `unzip -p`
- S9: RE2/J 1.7→1.8 compare diff (https://github.com/google/re2j/compare/re2j-1.7...re2j-1.8.diff), the releases page, README and `UnicodeTables.java` at tag `re2j-1.8`
- S10: JDK 25 `src.zip` of Oracle GraalVM 25.0.4 (`25.0.4+7-LTS-jvmci-b01`)

Repository paths are under `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/` and its test mirror.

#### Translation table

| # | XSD / F&O construct and meaning | RE2/J 1.7/1.8 as is | re2j translation | Today's `jdk` engine |
|---|---|---|---|---|
| 1 | Match mode: true if any substring matches; no implicit anchors [S3 §5.6.3; S5 A.3.13] | `find()` is unanchored | None. Call `find()`; never wrap in `^…$` or `.*`. | `find()` (RegExpFunctions.java:402-403). OK |
| 2 | `^` = start of string (no `m` flag) [S3 §5.6.1.1] | `RE2.PERL` includes ONE_LINE, so `^` = begin of text (RE2.java:94; Parser.java:831) | Emit `\A` | Unchanged; Java has no MULTILINE. OK |
| 3 | `$` = end of string only, not before a final `\n` | `$` = end of text, like `\z` (Parser.java:840-841; S7) | Emit `\z` | `$`→`\z` (RegExpFunctions.java:793-795). OK |
| 4 | Single-character escapes `\n \r \t \\ \| \. \? \* \+ \( \) \{ \} \- \[ \] \^`, plus F&O `\$` [S1 [24]; S3 §5.6.1.1] | Accepted; an escaped punctuation mark is itself (Parser.java:1337-1450) | Emit `\x{…}` or the escaped mark | Unchanged. OK |
| 5 | `.` = `[^\n\r]` [S1 F [37a]; S3 §5.6.2]. F&O 1.0 (both editions) says only `#x0A` [S4 §7.6.1.1] | Excludes only `\n`, so it matches `\r` (Parser.java:849) | Emit `[^\n\r]` | Java `.` also excludes U+0085, U+2028, U+2029 (S10 Pattern.java:5897-5901). **DRIFT** |
| 6 | Quantifiers `? * + {n} {n,} {n,m}` with n ≤ m, no size limit [S1 [4]-[8]] | Counts above 1000 throw "invalid repeat count" (Parser.java:1030-1032; S7) | Pass counts ≤ 1000. Reject larger (R2). | Any count. The product of minimum counts is bounded (RegExpFunctions.java:415-437). |
| 7 | Reluctant `?? *? +? {…}?`; no effect on `fn:matches` [S3 §5.6.1.2] | Supported | Drop the `?` | OK |
| 8 | `(…)`; `(?:…)` from F&O 3.0 on (not in S4) [S3 §5.6.1.3] | Supported | Emit `(?:…)` for every group | OK |
| 9 | Back-reference `\N`, outside classes only [S3 §5.6.1.4] | Not supported [S7]. `\1`–`\7` alone: "invalid escape". `\1` followed by an octal digit (`\12`) parses as octal U+000A. `\8`, `\9`: error (Parser.java:1337-1376) | Reject before RE2/J (R2) | Passed to Java as a back-reference (valid per spec) |
| 10 | `\s` = `[#x20\t\n\r]`; `\S` [S1 F; S2 G.4.2.5] | `[\t\n\f\r ]` (CharGroup.java:31) | Emit `[\t\n\r ]` / `[^\t\n\r ]` | Java ASCII `\s` adds VT and FF (S10 Pattern.java:2738-2741; ASCII.java:77-81). **DRIFT** |
| 11 | `\d` = `\p{Nd}`; `\D` | `[0-9]` (CharGroup.java:26) | Nd range set (see row 14) | Java `[0-9]`; no UNICODE_CHARACTER_CLASS flag (RegExpFunctions.java:285, :396). **DRIFT** |
| 12 | `\w` = all − (P ∪ Z ∪ C), which equals L ∪ M ∪ N ∪ S; `\W` | `[0-9A-Za-z_]` (CharGroup.java:36) | L∪M∪N∪S range set; `\W` = its complement | Java `[a-zA-Z_0-9]`: matches `_` (Pc), misses `+ $ ^` (S) and non-ASCII letters. **DRIFT** |
| 13 | `\i` / `\c`. XSD 1.0: `Letter\|_\|:` / `NameChar`. XSD 1.1: `NameStartChar` / `NameChar` [S2 G.4.2.5; S6 [4], [4a]]. `\I`, `\C` are complements | `\i` and `\c` are invalid escapes; `\C` rejected (Parser.java:916) | Explicit ranges from S6. XACML's [XML] is XML 1.0 Fifth Edition [S5 §1.4]. Complements computed. | `\i \I \C`: Java error "Illegal/unsupported escape" (Pattern.java:2580-2581, 2605-2610, 2707-2709, 2786). `\c` is Java's control escape `\cX` = X xor 64 (Pattern.java:2684-2685, 3498-3502), so `\c+` matches only `k`. **SILENT DRIFT** |
| 14 | `\p{X}`/`\P{X}` for X ∈ L Lu Ll Lt Lm Lo M Mn Mc Me N Nd Nl No P Pc Pd Ps Pe Pi Pf Po Z Zs Zl Zp S Sm Sc Sk So C Cc Cf Co Cn [S1 [27]-[35]; S2 [87]-[95]]. The Unicode version is implementation-defined [S2 G.4.2.2] | All except `Cn`. Its `C` = Cc∪Cf∪Co∪Cs, without Cn (UnicodeTables.java:2242-2254, 3930-3966; S7). Data are Unicode 6.0 | **Recommended:** range sets from `java.lang.Character`. **Minimum:** `\p{Cn}`→`[^\pL\pM\pN\pP\pS\pZ\p{Cc}\p{Cf}\p{Co}\p{Cs}]`; `\p{C}`→`[^\pL\pM\pN\pP\pS\pZ]` | Unchanged. The JDK has `Cn`, and its `C` includes Cn (CharPredicates.java:272, 325-329). OK, at Unicode 16.0 |
| 15 | Category forms that XSD lacks: `\pL`, `\p{^L}`, `\p{Cs}`, `\p{Any}`, scripts such as `\p{Greek}` | Accepted, except `LC`/`L&` (Parser.java:1524-1608; S7) | Reject (R1) | Java accepts several. Lenient |
| 16 | `\p{IsX}`: block by normalized, case-sensitive name [S2 G.4.2.3]. XSD 1.0 names Greek, CombiningMarksforSymbols, PrivateUse (E000–F8FF, F0000–FFFFD, 100000–10FFFD) are superseded but encouraged. An unknown name is **invalid** for `fn:matches` [S3 §5.6.1.5]; the XSD 1.1 default "all characters" [S2 G.4.2.4] does not apply | No blocks: "invalid character class range" (Parser.java:1590-1592). Stripping `Is` gives a script, a different set | Name→ranges table (scan `Character.UnicodeBlock`, plus the legacy names). Unknown name: reject (R1) | `Is`→`In`, then `UnicodeBlock.forName` (RegExpFunctions.java:732-750; CharPredicates.java:255-262). Greek and CombiningMarksforSymbols work. `IsPrivateUse` is an error (no alias, Character.java:1243-1246). Names match case-insensitively |
| 17 | Class syntax: `[` and `]` must be escaped; `[^]` invalid; hyphen rules, e.g. `[--z]` invalid [S2 G.4.1] | `[` literal; `[:alpha:]` is POSIX; a leading `]` is literal; `-` allowed anywhere; `[^…]` matches `\n` (Parser.java:1615-1690) | Emit escaped members or ranges; reject invalid forms | Java `[a[b]]` = union. Lenient |
| 18 | Subtraction `[G-[C]]`, nestable; `[^G-[C]]` = (¬G) − C [S1 [16]; S2 [76]] | Not supported. `[a-z-[aeiou]]` parses as {a–z, `-`, `[`, a, e, i, o, u} plus a literal `]`. **SILENT** | Compute the set difference as ranges | `-[`→`&&[^` (RegExpFunctions.java:752-762). Positive groups OK (RegularExpressionFunctionsTest.java:73-74). Negative groups wrong: JDK 25 negates the whole intersection (Pattern.java:2880-2881), so `[^a-z-[aeiou]]` matches `a`. **DRIFT**; fix: `[[^a-z]&&[^aeiou]]` |
| 19 | `&` in a class is a literal | Literal | None | Java `&&` = intersection: `[a&&b]` matches nothing. **DRIFT** |
| 20 | No inline flags: `(?` is valid only as `(?:` [S3 [72]]. `$flags` is "" [S3 §5.6.3] | Accepts `(?i) (?m) (?s) (?U) (?flags:…) (?P<n>…)`, and in 1.8 also `(?<n>…)` [S9]. Rejects lookaround [S7] | Reject (R1) | Java accepts lookaround, `(?<n>`, `(?i)` (RegExpLimitsTest.java:424). `(?x)` is rejected only by the weight scanner (RegExpLimitsTest.java:337-354) |
| 21 | Escapes outside XSD/F&O: `\A \z \b \B \Q…\E \xHH \x{H} \0 \ooo \a \f \v \e \cX \uHHHH` | Accepts `\A \z \b \B \Q…\E \x \x{} octal \a \f \v` (Parser.java:898-916, 1337-1450) | Reject (R1) | Java accepts most. Lenient |
| 22 | Unicode case (`i` flag): case variants via `fn:lower-case`/`fn:upper-case`; `\p{Lu}` is unaffected [S3 §5.6.2]. Unreachable: XACML passes no flags | `(?i)` folds with Unicode 6.0 orbits plus JDK case mapping [S9]; it also folds `\p{..}` (Parser.java:1598-1606) | Nothing to translate; reject `(?i)` (row 20) | Java `(?i)` is ASCII-only without `u` |
| 23 | Characters are code points [S3 §5.6.3] | Decodes UTF-16 to code points (MachineInput.java:186-190) | Emit supplementary characters as `\x{…}` | OK |

#### Rejection list

**R1: invalid under the F&O 3.1 grammar.** This is F&O error FORX0002 [S3 §5.6.3]. Both engines should reject these the same way:
- `(?` other than `(?:`: flags, named groups, lookaround
- escapes outside [S1 [24]] / [S3]
- `\pX` without braces
- `\p{Cs}`, script names, `\p{Any}`, `\p{^…}`
- an unknown block name [S3 §5.6.1.5]
- POSIX `[:…:]`
- an unescaped `[` or `]` inside a class, except for subtraction
- `[^]`, `[--z]`, and other hyphen misuse [S2 G.4.1]
- `\` followed by a digit inside a class [S3 §5.6.1.4 note]
- malformed or dangling quantifiers: `{`, `{,m}`, `{3,2}`, `*a`, `a**`, possessive forms
- a back-reference to a group that is missing or not yet closed
- a trailing `\`

**R2: valid F&O, but re2j cannot run it.** These need an engine-specific status:
- back-references
- counts above 1000 (Parser.java:1030-1032)
- an expanded program above a size bound. RE2/J has no compile-time memory budget; 1.8's `programSize()` works only after compiling [S9].

#### Today's `jdk` drifts to pin with characterization tests

Each case shows the result per spec against today's result as predicted from source. None was executed.

- **D1.** `\c+` on "a": spec true, today false.
- **D2.** `\i` throws `PatternSyntaxException`. It is not caught at RegExpFunctions.java:396.
- **D3.** Classes:
  - `\d` on U+0663: spec true, today false.
  - `\w` on `_`: spec false, today true.
  - `\w` on `+`: spec true, today false.
  - `\s` on U+000B: spec false, today true.
- **D4.** `a.c` on "a\u2028c": spec true, today false.
- **D5.** `[^a-z-[aeiou]]` on "a": spec false, today true.
- **D6.** `[a&&b]` on "&": spec true, today false.
- **D7.** `\p{IsPrivateUse}` on U+E000: spec true, today an error.
- **D8.** `(?i)a` on "A": spec says invalid pattern, today true.

### Options

**A. Extend today's text rewrite.**
- Cost: small.
- Risk: high. Text rewrites already miss D1, D3–D6. They cannot compute subtraction or ranges for RE2, and they do not validate syntax, so the silent cases in Answer item 3 go through.
- A test proves only the cases it lists.
- Not recommended.

**B. One strict parser (XSD 1.1 [64]–[98] plus the F&O 3.1 changes) into a shared AST, with an emitter per engine.** The RE2 emitter would use RE2/J's own `\p{..}`.
- Cost: medium.
- Risk: medium. The engines would disagree on post-6.0 code points (6.0 against 16.0). Subtraction that involves categories, blocks, or `\W`/`Cn` cannot be computed from RE2/J's package-private tables, so it would have to be rejected.
- A test proves: an accept/reject corpus, and a differential test minus a documented Unicode list.

**C. (Recommended) The same parser, plus range-set algebra.**
- Every class (escape, category, block, `\i`/`\c`, `.`, negation, subtraction) becomes one code-point range set built from the JDK's data: `Character.getType` and `UnicodeBlock`, scanned once over U+0000–U+10FFFF and cached.
- It is emitted as `[\x{..}-\x{..}…]`. RE2/J never sees `\p`, `\d`, `\s`, `\w`, `.`, or a block name.
- Cost: medium-plus. Patterns get longer; `\p{L}` becomes hundreds of ranges (UNVERIFIED count). This is mitigated by the compile-once cache.
- Risk: low semantic risk, because both engines use one Unicode version. I assume a class compiles to a single RE2/J instruction (UNVERIFIED; I did not read Compiler.java).
- Tests that would prove it:
  1. Each emitted set equals its predicate over all 1,114,112 code points.
  2. A jdk-vs-re2j differential corpus with inputs `\r`, U+0085, U+2028, U+000B, `_`, `+`, U+0663, U+20BF, and a lone surrogate.
  3. The R1 corpus gives the same status in both engines.
  4. `(?<n>a)` is rejected on RE2/J 1.7 and 1.8.
  5. The OASIS conformance suite passes with both engines.

**Recommendation: C.** Use F&O 3.1 §5.6 with XSD 1.1 Appendix G as the reference grammar, and record two deviations from the cited F&O 1.0 in `design.md`: `(?:` is accepted, and `.` excludes `\r`.

The maintainer decides whether the `jdk` engine also adopts the strict parser and fixes D1–D8. That is BREAKING for lenient policies; a legacy switch is one option.

### Confidence and drift risk

**Confidence:**
- High for the spec texts (fetched).
- High for RE2/J 1.7 behavior (source read) and for the 1.8 changes (official diff).
- High for JDK 25 behavior (its `src.zip`).
- Medium for D1–D8: derived from source, not executed.

**Drift:**
- The packet cites F&O 3.1, but XACML's [XF] is F&O 1.0. Two consequences:
  - `.` and `\r`: F&O 1.0, both editions, excludes only `#x0A` [S4]. XSD 1.0 and F&O 3.1 exclude `#x0D` too.
  - `(?:` exists only from F&O 3.0 on.
- The XSD 1.1 note that Java's `C` lacks `Cn` [S2 G.4.2.2] is stale for JDK 25 (CharPredicates.java:325-329).
- RE2/J 1.8 accepts `(?<n>…)`; 1.7 does not [S9].
- The RE2 C++ wiki differs from RE2/J (for example `\C`). I relied on RE2/J's source.
- The XSD 1.0 block table is BMP-only and Unicode 3.x [S1].
- RE2/J release dates: 1.8 on 9 Jan 2025 and 1.7 on 30 Jun 2022. The years are inferred from the cached Maven metadata (lastUpdated 20250109) and the jar file dates.

**Tool lanes:**
- GitHub MCP: UNAVAILABLE (`401 Bad credentials`). Fallback: DuckDuckGo fetches of github.com and raw.githubusercontent.com.
- Tavily search: one "no verdict" error from the safety check; not retried.
- DuckDuckGo: several transient "no verdict" errors from the safety check; each succeeded on retry.

### Not checked

- No code was run. D1–D8 need characterization tests.
- How a `PatternSyntaxException` becomes an XACML status (Q5, lane RX2).
- The exact XSD 1.0 `\i`/`\c` sets (XML 1.0 2nd-edition Appendix B).
- XSD and F&O errata lists.
- RE2/J compiler memory and recursion depth for nested repetition.
- Lone surrogates and U+FFFE/U+FFFF in inputs and patterns.
- The range counts of expanded categories.
- The Unicode version of the example characters U+20BA and U+20BF (UNVERIFIED).
- Fixtures beyond the count: `rtk rg -c 'regexp-match'` finds hits only in `oasis-xacml20-compat-test/` and `xacml3.0-ct-v.0.4/` (22 files).

### Verification run

- `rtk rg` over the regex sources, tests, and fixtures.
- Read `RegExpFunctions.java`, `RegExpLimitsTest.java`, `RegularExpressionFunctionsTest.java`, and `FunctionSafetyNet43Test.java`.
- `rtk proxy unzip -p`, with `rtk proxy sed -n` ranges and `rtk rg`, on the RE2/J 1.7 sources jar and the JDK 25 `src.zip`.
- Fetched S1–S7 and S9 with DuckDuckGo `fetch_content`.

**Residual risk:** the drift predictions and RE2/J internals are unexecuted. The Unicode-version examples are unverified.
