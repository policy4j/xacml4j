Task 4.3 should lock sixty function cases. Fifteen of them fail against XACML 3.0 today. The published `any-of-all` example still returns true, so it will not catch the quantifier bug.

`passes today` is from reading the implementation and from JDK `jshell` probes. No Surefire run was made.

## A. Defects

Known and not re-filed: 4.6, 4.12, 4.14, 4.15, 4.16, 4.21, 4.25, 4.26, 4.28, 4.29, 4.30, 4.31, rv038. Task 4.5 is open; the current subtract path applies the duration sign once, so it is not a new defect.

| Id | URN | Input | Spec-required result | Current result | Severity |
| --- | --- | --- | --- | --- | --- |
| NEW-1 | `urn:oasis:names:tc:xacml:1.0:function:any-of-all` | `integer-equal`, first bag `{1,2}`, second bag `{1,2}` | True. A.3.12 L10705–10721: the predicate is true between each element of the second bag and any element of the first, evaluated as `any-of` over the second bag and then `and`. The published example (L10726–10761) applies `integer-greater-than(element of first, element of second)`. | False. `HigherOrderFunctions.java:148-155` returns true when one element of the first bag satisfies `allOf` against the whole second bag (`∃ first ∀ second`). The published `{3,5}` greater-than `{1,2,3,4}` example still returns true because `5` is greater than every second element. Smallest fix: for each element of the second bag, `anyOf(predicate, firstBag, thatElement)`, then `and`. | High |
| NEW-2 | `urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match`, `ipAddress-regexp-match`, `dnsName-regexp-match`, `rfc822Name-regexp-match`, `x500Name-regexp-match` | Lookup of the 2.0 URN | Function is present. Identifier table L9169–9173 and A.3.13 L10883–10940. `string-regexp-match` stays 1.0 (L9168). | Absent. `RegExpFunctions.java:55,65,75,85,95` register the 1.0 URNs only, with no legacy alias. `RegularExpressionFunctionsTest.java:61-63` asserts the 1.0 `anyURI` and `dnsName` URNs. Smallest fix: register the 2.0 ids. | High |
| NEW-3 | `urn:oasis:names:tc:xacml:3.0:function:string-from-double` and the same gap on `string-from-time`, `date`, `dateTime`, `anyURI`, `dayTimeDuration`, `yearMonthDuration`, `x500Name`, `rfc822Name`, `ipAddress`, `dnsName` | Lookup of the canonical URN | Present. Table L9130–9150. A.3.13 L10891–10939 calls these URNs before `string-regexp-match`. | Absent. `StringFunctions.java:204,222,240,258,276,294,312,330,348,366,384` omit `:function:`. `string-from-boolean` (L168) and `string-from-integer` (L186) are correct. Smallest fix: insert `:function:` in those eleven ids. | High |
| NEW-4 | `urn:oasis:names:tc:xacml:1.0:function:double-to-integer` and `integer-to-double` | `double-to-integer(3000000000.9)`; `integer-to-double(9007199254740993)` | `xs:integer` 3000000000 (A.3.4 L9738–9741). The wide integer is Indeterminate with `processing-error` because it is not the same numeric value as a double (L9745–9750). | Annotations are swapped: `NumericConversionFunctions.java:44-49` declares return `xs:double` and returns `IntegerVal`; L52–57 declares `xs:integer` and returns `DoubleVal`. `DoubleVal.java:87-89` uses `intValue()`, so `3000000000.9` becomes `2147483647` and `NaN` becomes `0`. `IntegerVal.java:93-95` does `DoubleVal.of(long)` with no range check. jshell: `(long)(double)9007199254740993L == 9007199254740993L` is false; the printed double is `9.007199254740992E15`. Smallest fix: swap the annotations, convert through `long` with a range check, and return Indeterminate/`processing-error` outside that range. | High |
| NEW-5 | `urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match` | Pattern `sun.com` against `user@east.sun.com` and `user@notsun.com` | False for both. A.3.14 L10984–10988: a domain pattern matches that domain only. A leading dot is the subdomain form (L10990–10995). | True for both. `SpecialMatchFunctions.java:46` calls `RegExpFunctions.matches`, which wraps the pattern as `.*sun.com.*`. jshell: `Pattern.matches(".*sun.com.*", "user@east.sun.com")` is true. `RFC822Name.matches` (L189–199) already implements the structural rule and is unused. The constructor lowercases the domain with the default locale (`RFC822Name.java:184`). Smallest fix: call `rfc822Name.matches(pattern)` and use `Locale.ROOT`. | High |
| NEW-6 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-count`, `xpath-node-equal`, `xpath-node-match` | XPath evaluation throws `EvaluationException` | Indeterminate. A.3.15 allows 0 / False / False only when `<Content>` is absent (L11013–11015, L11025–11027, L11040–11042). | `0`, false, false. `XPathFunctions.java:71-73`, `112-114`, `175-177`. Missing `<Content>` is handled before the catch and matches the spec. Smallest fix: do not catch `EvaluationException`. | High |
| NEW-7 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-match` | Two distinct elements that share one equal attribute, and neither is an ancestor of the other | False. Equality is node identity (L11039–11040). Condition (2) is a node below a node from the first set (L11037–11039). | True. `XPathFunctions.java:154-166` returns true on `isEqualNode` of the elements and on `isEqualNode` of any attribute. `compareChildNodes` (L201–213) uses `isSameNode` and matches condition (2). Smallest fix: delete the `isEqualNode` and attribute loops. | High |
| NEW-8 | `urn:oasis:names:tc:xacml:1.0:function:string-regexp-match` | Pattern `foo\|bar`, input `xbar` | True. A.3.13 L10880–10881: `xf:matches` with the arguments reversed. | False. `RegExpFunctions.java:188-192` inserts `.*` without grouping, so the pattern becomes `.*foo\|bar.*`. jshell: `Pattern.matches(".*foo\|bar.*", "xbar")` is false. An empty pattern throws at `charAt(0)`. Smallest fix: group the pattern before adding the unanchored match, or match with an XPath engine. XF 7.6.2 was not opened (PARTIAL). | Medium |
| NEW-9 | `urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal` and `double-less-than-or-equal` | `(NaN, NaN)` and `(-0.0, 0.0)` | False for NaN; true for `-0.0 >= 0.0`. A.3.6 L9811–9812 requires IEEE 754. jshell: `NaN >= NaN` is false, `-0.0 >= 0.0` is true. Sibling of 4.29, different functions. | True for NaN; false for signed zero. `NumericComparisonFunctions.java:101` and `:110` use `Double.equals` or the relational operator. jshell: `Double.equals(NaN, NaN)` is true and `(-0.0).equals(0.0)` is false. Strict `>` / `<` at L83 and L92 use the primitive operator. Smallest fix: use primitive `>=` and `<=` only. | Medium |
| NEW-10 | `urn:oasis:names:tc:xacml:3.0:function:boolean-from-string` (same wrapper for every `*-from-string`) | `"TRUE"` | Indeterminate with `urn:oasis:names:tc:xacml:1.0:status:syntax-error` (A.3.9 L10098–10100). | Indeterminate with `processing-error`. `BooleanVal.java:74` throws `SyntaxException`, and `SyntaxException.java:40` carries `syntax-error`. `DefaultFunctionInvocation.java:136-138` catches every exception and `FunctionInvocationException.java:53` always builds `processing-error`. `Condition.java:93-95` stores that status. Smallest fix: if the cause is an `EvaluationException`, propagate its status. | Medium |
| NEW-11 | `urn:oasis:names:tc:xacml:1.0:function:integer-abs` and `integer-add` | `abs(Long.MIN_VALUE)`; `add(Long.MAX_VALUE, 1)` | A positive magnitude, or Indeterminate when the result does not fit. `xs:integer` is unbounded. A.3.2 L9702 does not name a status code. Task 5.4 already plans non-wrapping arithmetic. | Negative `Long.MIN_VALUE`, and `Long.MIN_VALUE` for the sum. `IntegerVal.java:97-120` uses wrapping `long` arithmetic. jshell: `Math.abs(Long.MIN_VALUE)` stays negative; `Long.MAX_VALUE + 1` wraps. Smallest fix: check the operation and throw `EvaluationException` with `processing-error`, or use arbitrary precision in 5.4. | Medium |
| NEW-12 | `urn:oasis:names:tc:xacml:1.0:function:x500Name-match` | A terminal RDN sequence that is not a string suffix of the JDK canonical form | True when the first name is a terminal RDN sequence of the second under `x500Name-equal` (A.3.14 L10952–10954; RFC 3280 §4.1.2.4 via A.3.1 L9600–9603). | `canonicalSecond.endsWith(canonicalFirst)` at `SpecialMatchFunctions.java:55-57`. The published example `O=Medico Corp,C=US` against `cn=John Smith,o=Medico Corp,c=US` is true in jshell. A multi-valued terminal RDN `O=Acme+OU=Engineering,C=US` against `O=Acme,C=US` is false. `OU=SoftwareEngineering` against `OU=Engineering` is also false, so that is not a false positive. Smallest fix: compare a terminal RDN sequence with `x500Name-equal`. | Medium |
| NEW-13 | `urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case` | `"I"` when the default locale is Turkish | `"i"`. A.3.3 L9726–9728: `fn:lower-case` with no language tailoring. | `"ı"` (U+0131) under `Locale.forLanguageTag("tr")`. `StringConversionFunctions.java:51` calls `StringVal.toLowerCase()` (`StringVal.java:104-105`), which uses `String.toLowerCase()`. jshell: Turkish `"I"` is `"ı"`; `Locale.ROOT` is `"i"`. A US default locale hides this. Smallest fix: `toLowerCase(Locale.ROOT)`. | Medium |
| NEW-14 | `urn:oasis:names:tc:xacml:1.0:function:round` | `NaN`, `+Inf` | See C4. A.3.2 L9649–9651 binds IEEE 754 to the two-argument double functions. `round` is specified only by signature at L9697–9700. | `0` and `9223372036854775807`. `ArithmeticFunctions.java:138` uses `Math.round`. jshell confirms both values. `floor(NaN)` stays NaN. Smallest fix, if IEEE is adopted: preserve NaN and infinities. | Medium |
| NEW-15 | `urn:oasis:names:tc:xacml:1.0:function:string-normalize-space` | `"\u000Ba"` | The vertical tab remains. A.3.3 L9718–9720 strips only XML `S` (`#x20`, `#x9`, `#xD`, `#xA`). | `"a"`. `StringConversionFunctions.java:43` calls `StringVal.trim()` (`StringVal.java:88-89`), which is `String.trim()`. jshell: `"\u000Ba".trim()` is `"a"`. `" a  b ".trim()` is `"a  b"`, which matches the spec because internal spaces stay. Smallest fix: strip only the four XML space characters. | Low |

## B. Test cases for 4.3

Sixty rows. `Y`/`N` means the current code matches the Expected column by inspection or by `jshell`, not that a test already passed. Bags are written as multisets. Addresses in the `rfc822Name` rows are constructed from the domain rule at L10986–10988. The HTML examples are email-obfuscated.

| # | URN | Input | Expected | Passes today? |
| --- | --- | --- | --- | --- |
| 1 | `urn:oasis:names:tc:xacml:1.0:function:any-of-all` | `integer-equal`, `{1,2}`, `{1,2}` | true | N, NEW-1 |
| 2 | `urn:oasis:names:tc:xacml:1.0:function:any-of-all` | `integer-greater-than`, `{3,5}`, `{1,2,3,4}` (L10726–10761) | true | Y (does not catch NEW-1) |
| 3 | `urn:oasis:names:tc:xacml:1.0:function:all-of-any` | `integer-greater-than`, `{3,5}`, `{1,2,3,4}` (L10699–10701) | true | Y |
| 4 | `urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match` | Registry lookup | function present | N, NEW-2 |
| 5 | `urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match` | Registry lookup | function present | N, NEW-2 |
| 6 | `urn:oasis:names:tc:xacml:1.0:function:string-regexp-match` | Registry lookup | function present | Y |
| 7 | `urn:oasis:names:tc:xacml:3.0:function:string-from-double` | Registry lookup | function present | N, NEW-3 |
| 8 | `urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI` | Registry lookup | function present | N, NEW-3 |
| 9 | `urn:oasis:names:tc:xacml:3.0:function:string-from-boolean` | Registry lookup | function present | Y |
| 10 | `urn:oasis:names:tc:xacml:1.0:function:double-to-integer` | `3000000000.9` | `xs:integer` 3000000000 | N, NEW-4 |
| 11 | `urn:oasis:names:tc:xacml:1.0:function:double-to-integer` | `5.9` | `xs:integer` 5, declared return type `xs:integer` | N, NEW-4 (value is 5; declared type is `xs:double`) |
| 12 | `urn:oasis:names:tc:xacml:1.0:function:double-to-integer` | `NaN` | Indeterminate (C5) | N, NEW-4 (returns integer 0) |
| 13 | `urn:oasis:names:tc:xacml:1.0:function:integer-to-double` | `9007199254740993` | Indeterminate, `processing-error` | N, NEW-4 |
| 14 | `urn:oasis:names:tc:xacml:1.0:function:integer-to-double` | `2` | `xs:double` 2.0 | N, NEW-4 (value is 2.0; declared type is `xs:integer`) |
| 15 | `urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match` | `"sun.com"`, `user@east.sun.com` | false | N, NEW-5 |
| 16 | `urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match` | `"sun.com"`, `user@notsun.com` | false | N, NEW-5 |
| 17 | `urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match` | `"sun.com"`, `user@sun.com` | true | Y |
| 18 | `urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match` | `".east.sun.com"`, `user@east.sun.com` | true | Y (regex also matches) |
| 19 | `urn:oasis:names:tc:xacml:1.0:function:string-regexp-match` | `"foo\|bar"`, `"xbar"` | true | N, NEW-8 |
| 20 | `urn:oasis:names:tc:xacml:1.0:function:string-regexp-match` | `"^foo$"`, `"foo"` | true | Y |
| 21 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-count` | expression whose evaluation throws | Indeterminate | N, NEW-6 |
| 22 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal` | expression whose evaluation throws | Indeterminate | N, NEW-6 |
| 23 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-count` | category has no `<Content>` | integer 0 | Y |
| 24 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal` | either category has no `<Content>` | false | Y |
| 25 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-match` | either category has no `<Content>` | false | Y |
| 26 | `urn:oasis:names:tc:xacml:3.0:function:xpath-node-match` | distinct elements sharing one equal attribute | false | N, NEW-7 |
| 27 | `urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal` | `NaN`, `NaN` | false | N, NEW-9 |
| 28 | `urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal` | `-0.0`, `0.0` | true | N, NEW-9 |
| 29 | `urn:oasis:names:tc:xacml:1.0:function:double-greater-than` | `NaN`, `NaN` | false | Y |
| 30 | `urn:oasis:names:tc:xacml:1.0:function:double-equal` | `NaN`, `NaN` | false under IEEE; see C1 before encoding | N, 4.29 |
| 31 | `urn:oasis:names:tc:xacml:1.0:function:double-equal` | `-0.0`, `0.0` | true | N, 4.29 |
| 32 | `urn:oasis:names:tc:xacml:3.0:function:boolean-from-string` | `"TRUE"` | Indeterminate, `syntax-error` | N, NEW-10 (Indeterminate with `processing-error`) |
| 33 | `urn:oasis:names:tc:xacml:3.0:function:boolean-from-string` | `"1"` | true | Y |
| 34 | `urn:oasis:names:tc:xacml:3.0:function:boolean-from-string` | `" true "` | true | Y (`BooleanVal.java:61-74`) |
| 35 | `urn:oasis:names:tc:xacml:1.0:function:integer-abs` | `Long.MIN_VALUE` | positive value, or Indeterminate (C3) | N, NEW-11 (stays negative) |
| 36 | `urn:oasis:names:tc:xacml:1.0:function:integer-add` | `Long.MAX_VALUE`, `1` | not `Long.MIN_VALUE` (C3) | N, NEW-11 |
| 37 | `urn:oasis:names:tc:xacml:1.0:function:integer-divide` | `5`, `2` | `xs:integer` 2 | N, 4.6 (double 2.0) |
| 38 | `urn:oasis:names:tc:xacml:1.0:function:integer-divide` | `7`, `-3` | `xs:integer` -2 toward zero, after C2 | N, 4.6 (double -2.0; quotient matches toward zero) |
| 39 | `urn:oasis:names:tc:xacml:1.0:function:integer-divide` | `1`, `0` | Indeterminate | Y (`ArithmeticFunctions.java:166`, then `processing-error`) |
| 40 | `urn:oasis:names:tc:xacml:1.0:function:double-divide` | `1.0`, `0.0` | Indeterminate, not Infinity | Y (`ArithmeticFunctions.java:156`) |
| 41 | `urn:oasis:names:tc:xacml:1.0:function:double-divide` | `1.0`, `-0.0` | Indeterminate | Y (`!= 0` is false for `-0.0`) |
| 42 | `urn:oasis:names:tc:xacml:1.0:function:integer-mod` | `-5`, `3` | `xs:integer` -2 (C2) | Y (`IntegerVal.java:114-116`; jshell `(-5)%3=-2`) |
| 43 | `urn:oasis:names:tc:xacml:1.0:function:integer-mod` | `5`, `0` | Indeterminate (C6) | Y (`ArithmeticException` becomes `processing-error`) |
| 44 | `urn:oasis:names:tc:xacml:1.0:function:integer-multiply` | `2`, `3`, `4` | `xs:integer` 24 | Y (`ArithmeticFunctions.java:70-80`) |
| 45 | `urn:oasis:names:tc:xacml:1.0:function:double-multiply` | `2.0`, `3.0`, `4.0` | `xs:double` 24.0 | Y (`ArithmeticFunctions.java:96-106`) |
| 46 | `urn:oasis:names:tc:xacml:1.0:function:integer-union` | `{1,1,2}`, `{2,3}` | `{1,2,3}` | Y (`BagOfValues.java:153-159`) |
| 47 | `urn:oasis:names:tc:xacml:1.0:function:integer-intersection` | `{1,1,2}`, `{2,2,3}` | `{2}` | Y (`BagOfValues.java:170-176`) |
| 48 | `urn:oasis:names:tc:xacml:1.0:function:double-union` | `{+0.0}`, `{-0.0}` | one element once 4.29 makes them equal | N, 4.29 (two elements) |
| 49 | `urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only` | `{}` | Indeterminate | Y (`BagFunctions.java:80`) |
| 50 | `urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only` | `{1,1}` | Indeterminate | Y (`BagFunctions.java:81`) |
| 51 | `urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only` | `{7}` | integer 7 | Y |
| 52 | `urn:oasis:names:tc:xacml:1.0:function:integer-union` | integer bag, string bag | Indeterminate | Y (`BagOfValues.java:155`, `IllegalArgumentException`) |
| 53 | `urn:oasis:names:tc:xacml:1.0:function:integer-subset` | `{1}`, `{1,2}` | true | Y (`SetFunctions.java:69`: `b.containsAll(a)`) |
| 54 | `urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of` | `{9,2}`, `{1,2}` | true | Y (`SetFunctions.java:78`) |
| 55 | `urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration` | `2002-03-22T08:00:00Z`, `PT1H` | `2002-03-22T07:00:00Z` (L9856–9858) | Y by code read of the sign path (`DateTimeArithmeticFunctions.java:56-60`, `DateTimeDurationUtils.java:238`). Not executed. Cite 4.5 as a regression lock. |
| 56 | `urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration` | `2002-03-22T08:00:00Z`, `-PT1H` | `2002-03-22T09:00:00Z` (L9858–9861) | Y by the same code read. Not executed. |
| 57 | `urn:oasis:names:tc:xacml:1.0:function:x500Name-match` | `O=Medico Corp,C=US`, `cn=John Smith,o=Medico Corp,c=US` | true (L10956–10958) | Y (jshell canonical `endsWith`) |
| 58 | `urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case` | `"I"` with Turkish default locale | `"i"` | N, NEW-13 |
| 59 | `urn:oasis:names:tc:xacml:1.0:function:string-normalize-space` | `"\u000Ba"` | `"\u000Ba"` | N, NEW-15 |
| 60 | `urn:oasis:names:tc:xacml:1.0:function:round` | `NaN` | NaN if C4 adopts IEEE | N, NEW-14 (returns 0.0) |

Rows 21–26, 37–54, and 55–56 cover every item named in `tasks.md:45`. `floor(NaN)` stays NaN (`Math.floor`) and is a useful passing companion to row 60. `string-normalize-space(" a  b ")` stays `"a  b"` and should be added beside row 59.

## C. Maintainer decisions

| Id | Question | Options |
| --- | --- | --- |
| C1 | What should `double-equal(NaN, NaN)` lock in the 4.3 tests? | (a) Keep true, as memo 032 recommends while IIC350 and IIC358 expect Permit. (b) Return false under IEEE 754-1985 §5.7 and retag those conformance rows. `+0.0` equal to `-0.0` is not this choice: both IEEE and memo 032 say true, and today it is false (4.29). Do not implement either comparison with `Double.compare`. |
| C2 | Rounding and remainder for integer divide and mod. A.3.2 L9684–9695 says "divided" and "remainder" and does not name a mode. | (a) Toward-zero quotient and a remainder with the sign of the dividend: `5/2=2`, `7/-3=-2`, `-5%3=-2`. This matches memo 032 option 1 and the current Java operators. (b) Floor division, where `7/-3` is `-3`. Task 4.6 already says a truncated `xs:integer`. |
| C3 | Integer overflow, including `abs(Long.MIN_VALUE)`. | (a) Indeterminate with `processing-error` when the result does not fit in a signed 64-bit value. (b) Arbitrary precision, which task 5.4 already names. A.3.2 does not name the status code. |
| C4 | `round` and `floor` for NaN, infinity, and halfway cases. The IEEE sentence at L9649–9651 is in the two-argument paragraph. L9697–9700 only gives the signature. | (a) IEEE: NaN stays NaN, infinities stay infinite, halfway ties to even. (b) Lock `Math.round`: `NaN→0`, `+Inf→Long.MAX_VALUE`, `2.5→3`, `-1.5→-1`. `floor` already preserves NaN and infinity. |
| C5 | `double-to-integer` of NaN and ±Inf. L9738–9741 says truncate to a whole number. | (a) Indeterminate. (b) Define a saturation or zero result. Today `intValue()` yields `0`, `2147483647`, and `-2147483648`. |
| C6 | `integer-mod` by zero. L9653–9654 names the divide functions. | (a) Indeterminate with `processing-error`, which is what the current `ArithmeticException` becomes. (b) Leave it unspecified and do not assert a status. |
| C7 | `ipAddress` set functions are registered at 1.0 (`SetFunctions.java:687-730`). The normative table through L9246 has no `ipAddress-*` or `dnsName-*` set function. No `dnsName` set method was found in `SetFunctions.java`. An empty search is not proof of absence. | (a) Keep the 1.0 `ipAddress` functions as extensions. (b) Add them under a version the maintainer chooses. (c) Remove them. WD 19 (L13172) removed higher-order functions for these types, not set functions. |
| C8 | `x500Name-match` on a multi-valued terminal RDN. JDK canonical `endsWith` is false for `O=Acme+OU=Engineering,C=US` against `O=Acme,C=US`. | (a) Compare a terminal RDN sequence with `x500Name-equal`. (b) Keep string suffix and accept that only the published single-RDN example is guaranteed. |

## Verification

SUPPORTED:

- OASIS XACML 3.0 Core OS, 22 January 2013, https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html, lines cited above.
- Source reads: `HigherOrderFunctions.java`, `RegExpFunctions.java`, `StringFunctions.java`, `NumericConversionFunctions.java`, `SpecialMatchFunctions.java`, `XPathFunctions.java`, `NumericComparisonFunctions.java`, `ArithmeticFunctions.java`, `IntegerVal.java`, `DoubleVal.java`, `StringVal.java`, `BooleanVal.java`, `RFC822Name.java`, `BagFunctions.java`, `BagOfValues.java`, `SetFunctions.java`, `DateTimeArithmeticFunctions.java`, `DateTimeDurationUtils.java`, `StringConversionFunctions.java`, `DefaultFunctionInvocation.java`, `FunctionInvocationException.java`, `SyntaxException.java`, `Condition.java`, `tasks.md:45`, `RegularExpressionFunctionsTest.java:61-63`.
- jshell, exit 0: `9007199254740993` round-trips false; `(-5)%3=-2`; `Math.abs(Long.MIN_VALUE)` stays negative; `Math.round(NaN)=0`; `Math.round(+Inf)=Long.MAX_VALUE`; `NaN>=NaN` is false; `Double.equals(NaN,NaN)` is true; `-0.0>=0.0` is true; `(-0.0).equals(0.0)` is false; Turkish `"I"` lowercases to `"ı"`; `"\u000Ba".trim()` is `"a"`; `Pattern.matches(".*foo|bar.*","xbar")` is false; `Pattern.matches(".*sun.com.*","user@east.sun.com")` is true.

PARTIAL:

- `dateTime-subtract-dayTimeDuration` rows. The sign is applied once in `DateTimeDurationUtils.java:238`. `ISO8601DateTime.minus` was not executed, so the lexical form of the result is unread.
- XF 7.6.2. The XACML citation of `xf:matches` is SUPPORTED. The XPath specification text was not opened.
- `x500Name-match` multi-valued behavior is a jshell probe of `X500Principal.CANONICAL`, not a call through `SpecialMatchFunctions`.
- Registry presence for NEW-2 and NEW-3 is from the `@XacmlFuncSpec` ids. The provider was not loaded.

Not run, so UNAVAILABLE for this lane: Maven, Surefire, `ast-grep scan`, `ripwire --impact`, `ripwire --quality-delta`. This is a spec audit of the existing tree, not a diff review.

Residual risk: DNS trailing-dot parsing, IPv4 mask-versus-CIDR parsing, `integer-from-string` whitespace, and full Unicode case mapping beyond Turkish `"I"` were not probed. No project test was executed, so an invocation path that differs from the method body would move a `Y` row.

FINAL REPORT (policy4j_code_review (Grok 4.7, adversarial lane), Grok 4.7):
Table A has 15 new defects (7 high, 7 medium, 1 low). Table B has 60 test rows and covers every item in tasks.md:45. Table C has 8 decisions. Top 5: (1) any-of-all is ∃first ∀second, so integer-equal on {1,2}×{1,2} is false while A.3.12 requires true, and the published greater-than example still passes; (2) the five typed regexp-match URNs are registered as 1.0 while the normative table says 2.0; (3) eleven string-from-* URNs omit :function:, so the canonical ids used by A.3.13 do not resolve; (4) double-to-integer and integer-to-double have swapped declared types, int saturation, and silent rounding of 9007199254740993; (5) rfc822Name-match is an unanchored regex, so sun.com matches user@east.sun.com and user@notsun.com. Close behind: XPath evaluation errors become 0 or false, and xpath-node-match treats deep-equal attributes as identity. Known bugs 4.6, 4.12, 4.14–4.16, 4.21, 4.25, 4.26, 4.28–4.31, and rv038 were cited and not re-filed. The dateTime subtract sign path is correct and is a regression lock for open task 4.5. No files were edited and Maven was not run.
