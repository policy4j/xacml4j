## 1. Answer

### Executive Summary

Across the open-source Java XACML engines examined (`SUPPORTED`), **none** provide a pluggable regular-expression engine SPI, **none** integrate a linear-time automaton engine (`RE2/J` or `brics`), and **none** implement ReDoS guardrails (pattern length, input length, repetition bounds, or match-time deadlines). Every external engine relies on a single hardcoded backtracking matcher:

1. **OW2 AuthzForce CE** (`core` `release-21.2.0` / `core-pdp-api` `release-22.2.0`) delegates to **Saxon-HE's XPath 2.0 regex engine** (`net.sf.saxon.regex.RegularExpression` with `"XP20"` syntax and `containsMatch(...)`). It natively supports XML Schema / XPath 2.0 regex syntax and **pre-compiles constant first-argument patterns once at policy-load time** via AST call specialization (`CompiledRegexMatchFunctionCall`), rejecting invalid static patterns during policy loading (`IllegalArgumentException`). However, dynamic patterns are recompiled on every call without caching, Saxon's matcher is a backtracking NFA with no ReDoS limits, and a dynamic invalid regex in `string-regexp-match` throws an uncaught `PatternSyntaxException` because `EqualTypeMatchFunction.CallFactory` omits the `try/catch` present in `NonEqualTypeMatchFunction.CallFactory`.
2. **AT&T XACML 3.0 / Apache OpenAZ / ONAP Policy XACML PDP** (`att/xacml-3.0` `v3.2` and `master`; `apache/incubator-retired-openaz` `master`; `onap/policy-xacml-pdp` `10.0.1-PF-I1`) calls `elementValueString.matches(regexpValue)` (`java.util.regex.Pattern`). It performs **zero XML Schema syntax translation**, erroneously enforces **full-string matching** instead of XPath `fn:matches` substring matching, performs **no pattern caching**, has **no ReDoS limits**, and lets `PatternSyntaxException` escape `ATTPDPEngine.decide(...)` uncaught.
3. **WSO2 Balana (and its SunXACML ancestor)** (`wso2/balana` `v1.2.29`) calls `Pattern.matches(buf.toString(), str)` (`java.util.regex.Pattern`) after a naive `StringBuffer` rewrite (`MatchFunction.regexpHelper`). It wraps patterns without leading `^` or trailing `$` in `.*...*` (which breaks on multiline inputs, corrupts alternations like `^a|b`, crashes with `StringIndexOutOfBoundsException` on `""`, and worsens backtracking ReDoS), replaces `\p{Is` / `\P{Is` with `\p{In` / `\P{In`, and replaces `-[` with `&&[^` unconditionally (even outside `[...]`). It has **no pattern cache**, **no ReDoS limits**, and does not catch `PatternSyntaxException`.
4. **SNE-XACML** (`canhnt/sne-xacml` `master`) compiles policies into Multi-data-type Interval Decision Diagrams (MIDDs) supporting only `[gt, ge, lt, le, eq]` and **does not implement `*-regexp-match`**.
5. **`xacml4j` (current baseline)** ([RegExpFunctions.java](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L1-L813)) is the only engine with ReDoS guardrails (bead 4.58: pattern length `10,000`, input length `100,000`, nested quantifier product `1,000,000`, and `1,000 ms` `DeadlineCharSequence` timeout returning `Indeterminate` / `processing-error`), an opt-in `ThreadLocal` LRU pattern cache (disabled by default), and `Matcher.find()` substring semantics with XML Schema translation (`\p{Is` -> `\p{In`, nested `-[` -> `&&[^`, unescaped `$` -> `\z`).

---

### Comparison Table

| Dimension | `xacml4j` (Today) | OW2 AuthzForce CE (`21.2.0` / `22.2.0`) | AT&T XACML / OpenAZ / ONAP (`v3.2`) | WSO2 Balana / SunXACML (`v1.2.29`) | SNE-XACML (`master`) |
| --- | --- | --- | --- | --- | --- |
| **Underlying regex engine** | `java.util.regex.Pattern` (`Matcher.find()`) | Saxon-HE `net.sf.saxon.regex.RegularExpression` (`"XP20"`, `containsMatch`) | `java.util.regex.Pattern` (`String.matches`) | `java.util.regex.Pattern` (`Pattern.matches`) | None (`*-regexp-match` unsupported) |
| **Pluggable engine SPI?** | No (proposed in [proposal.md](openspec/changes/add-regex-engine-spi/proposal.md#L1-L83)) | No (`StandardFunction` registry in `PdpEngineConfiguration`) | No (`StdFunctions` static reflection table) | No (`StandardFunctionFactory` hardcoded set) | No |
| **Substring vs. full match** | Substring (`Matcher.find()`) | Substring (`compiledRegex.containsMatch(...)`) | **Full-string** (`String.matches` — non-conformant) | Substring via `.*` prefix/suffix rewrite (flawed on `\n`, `""`, `^a\|b`) | N/A |
| **XML Schema syntax handling** | `\p{Is`->`\p{In`, `\P{Is`->`\P{In`, nested `-[`->`&&[^`, `$`->`\z`; no `\i`/`\c` | Native XPath 2.0 / XSD compiler in Saxon (`-[`, `\i`, `\c`, `\p{Is...}`, `$`) | **None** (raw Java regex) | `\p{Is`->`\p{In`, `\P{Is`->`\P{In`, unconditional `-[`->`&&[^` | N/A |
| **Pattern caching / compile-once** | Opt-in `ThreadLocal` LRU (`cacheRegExps=false` default, size `256`) | **Compile-once on AST** if arg #0 is static (`<AttributeValue>`); **none** if dynamic or 2-bag higher-order | **None** (recompiles on every evaluation) | **None** (recompiles on every evaluation) | N/A |
| **Invalid regex handling** | `PatternSyntaxException` caught by [`FunctionSpecImpl.doInvoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L428-L434) -> `FunctionInvocationException` (`processing-error`) | Static arg #0: rejected at policy load (`IllegalArgumentException`). Dynamic arg #0: `processing-error` in `NonEqualTypeMatchFunction`, **uncaught `PatternSyntaxException`** in `EqualTypeMatchFunction` (`string-regexp-match`) | **Uncaught `PatternSyntaxException`** (escapes `ATTPDPEngine.decide`) | **Uncaught `PatternSyntaxException`** (and `StringIndexOutOfBoundsException` on `""`) | Rejects policy at parse time (`MIDDParsingException`) |
| **ReDoS mitigations / limits** | Pattern len `10k`, input len `100k`, repetition product `1M`, `(?x)` rejected, `1000ms` `DeadlineCharSequence` -> `processing-error` | **None** (Saxon `REMatcher` is backtracking NFA; only AST/PolicyRef depth is limited) | **None** | **None** (`.*` wrapping worsens backtracking) | N/A |

---

### Detailed Findings by Engine

#### 0. Baseline: `xacml4j` (`SUPPORTED`)
- **Functions**: Six standard functions in [`RegExpFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L64-L127): `string-regexp-match` (`1.0`) and `anyURI-`, `ipAddress-`, `dnsName-`, `rfc822Name-`, `x500Name-regexp-match` (`2.0` with `@XacmlLegacyFunc` `1.0`).
- **Engine & Translation**: Uses `java.util.regex.Pattern.compile(...)` and `Matcher.find()` ([`RegExpFunctions.java:396-403`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L396-L403)). [`covertXacmlToJavaSyntax`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L722-L770) and [`translateEndAnchors`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L772-L801) translate `\p{Is` -> `\p{In`, `\P{Is` -> `\P{In`, nested `-[` (`calculateNestLevel > 0`, [`:758`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L758)) -> `&&[^`, and unescaped `$` outside `[...]` -> `\z`.
- **Caching**: `ThreadLocal<PatternCache>` backed by a `LinkedHashMap` LRU of size `256` ([`RegExpFunctions.java:173-180`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L173-L180), [`:265-304`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L265-L304)), disabled by default (`Boolean.parseBoolean(System.getProperty("org.xacml4j.v30.policy.function.impl.RegExpFunctions.cacheRegExps")) == false`). Even in [`Match.match`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L120-L129), `predicate.invoke(context, value, v)` passes `Value` on every bag element rather than pre-compiling `value`.
- **Limits & Failure Status**: Bead 4.58 ([`RegExpFunctions.java:150-259`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L150-L259), [`:365-687`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L365-L687)) enforces `maxPatternLength=10_000`, `maxInputLength=100_000`, `maxRepetition=1_000_000` (via `RepetitionScanner`), and `maxMatchMillis=1_000` (via `DeadlineCharSequence` polling `System.nanoTime()` every `1,024` `charAt` calls). Limit violations throw `EvaluationException(Status.processingError().message("Regular expression evaluation limit exceeded").build())` ([`:683-687`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L683-L687)). Invalid regex syntax throws `PatternSyntaxException` from `Pattern.compile`, which [`FunctionSpecImpl.doInvoke`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java#L428-L434) wraps in `FunctionInvocationException` (a subclass of `EvaluationException` carrying `Status.processingError()`).

---

#### 1. OW2 AuthzForce CE (`SUPPORTED`)
- **Sources (retrieved `2026-10-27`)**:
  - Repository `authzforce/core-pdp-api`, tag `release-22.2.0` (commit `da56877b2e2141875d033c0490c02e9bf449049e`, 2024-11-12):
    - `RegexpMatchFunctionHelper.java`: `https://raw.githubusercontent.com/authzforce/core-pdp-api/da56877b2e2141875d033c0490c02e9bf449049e/src/main/java/org/ow2/authzforce/core/pdp/api/func/RegexpMatchFunctionHelper.java`
    - `EqualTypeMatchFunction.java`: `https://raw.githubusercontent.com/authzforce/core-pdp-api/da56877b2e2141875d033c0490c02e9bf449049e/src/main/java/org/ow2/authzforce/core/pdp/api/func/EqualTypeMatchFunction.java`
    - `NonEqualTypeMatchFunction.java`: `https://raw.githubusercontent.com/authzforce/core-pdp-api/da56877b2e2141875d033c0490c02e9bf449049e/src/main/java/org/ow2/authzforce/core/pdp/api/func/NonEqualTypeMatchFunction.java`
    - `XmlUtils.java`: `https://raw.githubusercontent.com/authzforce/core-pdp-api/da56877b2e2141875d033c0490c02e9bf449049e/src/main/java/org/ow2/authzforce/core/pdp/api/XmlUtils.java`
  - Repository `authzforce/core`, tag `release-21.2.0` (commit `3cc0e988e1639da48184434cd5c918102ff5b499`, 2025-05-14; depends on `authzforce-ce-core-pdp-api` `22.2.0` and `Saxon-HE` `12.5` in `pom.xml`):
    - `StandardFunction.java`: `https://raw.githubusercontent.com/authzforce/core/3cc0e988e1639da48184434cd5c918102ff5b499/pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/func/StandardFunction.java`
    - `StandardEqualTypeMatchers.java`: `https://raw.githubusercontent.com/authzforce/core/3cc0e988e1639da48184434cd5c918102ff5b499/pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/func/StandardEqualTypeMatchers.java`
    - `StandardNonEqualTypeMatchers.java`: `https://raw.githubusercontent.com/authzforce/core/3cc0e988e1639da48184434cd5c918102ff5b499/pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/func/StandardNonEqualTypeMatchers.java`
    - `StandardHigherOrderBagFunctions.java`: `https://raw.githubusercontent.com/authzforce/core/3cc0e988e1639da48184434cd5c918102ff5b499/pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/func/StandardHigherOrderBagFunctions.java`
    - `MatchEvaluator.java`: `https://raw.githubusercontent.com/authzforce/core/3cc0e988e1639da48184434cd5c918102ff5b499/pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/MatchEvaluator.java`
    - `RuleEvaluators.java`: `https://raw.githubusercontent.com/authzforce/core/3cc0e988e1639da48184434cd5c918102ff5b499/pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/rule/RuleEvaluators.java`
    - `PdpEngineConfiguration.java`: `https://raw.githubusercontent.com/authzforce/core/3cc0e988e1639da48184434cd5c918102ff5b499/pdp-engine/src/main/java/org/ow2/authzforce/core/pdp/impl/PdpEngineConfiguration.java`
- **Engine & XML Schema Syntax**:
  - `RegexpMatchFunctionHelper` compiles patterns using Saxon-HE (`net.sf.saxon.Version.platform.compileRegularExpression(XmlUtils.SAXON_PROCESSOR.getUnderlyingConfiguration(), BMPString.of(regex), "", "XP20", null)`) and evaluates matches via `compiledRegex.containsMatch(BMPString.of(arg1.getUnderlyingValue()))`.
  - Because Saxon's `"XP20"` regex compiler implements the W3C XPath 2.0 / XML Schema regular-expression grammar directly in `net.sf.saxon.regex`, AuthzForce does not need string-rewrite heuristics for `[a-z-[aeiou]]`, `\i`, `\c`, `\p{IsBasicLatin}`, or `$` end-of-string semantics, and `containsMatch` implements XPath `fn:matches` unanchored substring matching.
- **Compile-Once AST Specialization vs. Dynamic Evaluation**:
  - AuthzForce has **no runtime pattern cache** (no `Map` or `ThreadLocal`). Instead, when building the AST for a function call (`FirstOrderFunction.newCall(argExpressions, remainingArgTypes)`), both `StandardEqualTypeMatchers.STRING_REGEXP_MATCH_CALL_FACTORY_BUILDER` (`string-regexp-match`) and `NonEqualTypeMatchFunction.RegexpMatchCallFactoryBuilder` (`anyURI-`, `ipAddress-`, `dnsName-`, `rfc822Name-`, `x500Name-regexp-match`) invoke `RegexpMatchFunctionHelper.getCompiledRegexMatchCall(...)`.
  - If `argExpressions` is non-empty and `argExpressions.get(0).getValue()` is present (i.e., arg #0 is a static constant `<AttributeValue>`), `getCompiledRegexMatchCall` compiles the `RegularExpression` **once at policy-load time** and returns a `CompiledRegexMatchFunctionCall` holding `final RegularExpression compiledRegex`.
  - This compile-once path covers:
    1. Direct `<Apply>` calls where arg #0 is a constant `<AttributeValue>`.
    2. `<Match>` elements in `<Target>`: `MatchEvaluator` transforms `<Match MatchId="F">` into a higher-order `any-of(F, attributeValueExpr, bagExpr)` call (`OneBagOnlyHigherOrderFunction`), which passes `primitiveInputsBeforeBag = [attributeValueExpr]` as `argExpressions` to `subFunction.newCall(...)`.
    3. Explicit `any-of`, `all-of`, and `map` higher-order calls where arg #0 is a constant primitive before the bag.
  - This compile-once path **misses**:
    1. Any call where arg #0 is a dynamic expression (`<AttributeDesignator>`, `<AttributeSelector>`, or non-constant `<VariableReference>` or `<Apply>`).
    2. All two-bag or N-bag higher-order functions (`any-of-all`, `all-of-any`, `all-of-all`, `any-of-any` in `StandardHigherOrderBagFunctions.java`), because their constructors call `subFunc.newCall(Collections.emptyList(), subFuncArgTypes)` with an empty `argExpressions` list. In those cases, `RegexpMatchFunctionHelper.match(arg0, arg1)` recompiles the regex on **every tuple** of the bag cross-product.
- **Status on Failure**:
  - **Static invalid regex at policy-load time**: `getCompiledRegexMatchCall` catches `net.sf.saxon.trans.XPathException` and throws `IllegalArgumentException(invalidRegexMsgPrefix + firstArg.get() + "'", e)`, rejecting the policy before the PDP can serve requests.
  - **Dynamic invalid regex at evaluation time**: `RegexpMatchFunctionHelper.match` catches `XPathException` and throws `java.util.regex.PatternSyntaxException(e.getMessage(), regex.getUnderlyingValue(), -1)`.
    - For the 5 non-string functions (`NonEqualTypeMatchFunction.CallFactory`), `evaluate(...)` catches `PatternSyntaxException` and throws `IndeterminateEvaluationException(..., XacmlStatusCode.PROCESSING_ERROR.value(), e)`.
    - For `string-regexp-match` (`EqualTypeMatchFunction.CallFactory`), `evaluate(...)` **does not catch `PatternSyntaxException`** (`BooleanValue.valueOf(matcher.match(typedArgs.get(0), typedArgs.get(1)))`), and `ConditionEvaluators` / `RuleEvaluators` only catch `IndeterminateEvaluationException`. Thus a dynamic invalid regex in `string-regexp-match` throws an uncaught `PatternSyntaxException` out of `BasePdpEngine.evaluate`.
- **ReDoS & Limits**:
  - **No regex limits or ReDoS protection**: Saxon's `ARegularExpression` / `REMatcher` is a backtracking NFA engine without input length, pattern length, quantifier product, or step/time limits. AuthzForce's `PdpEngineConfiguration` only bounds `maxVariableRefDepth` and `maxPolicyRefDepth`.
- **Engine / Function Selection**:
  - Configured per PDP instance via `PdpEngineConfiguration` (from XML/JAXB `Pdp` configuration): standard functions are enabled via `pdpJaxbConf.isStandardFunctionsEnabled()` (`StandardFunction.getRegistry(...)`) and custom `Function` extensions are looked up via `PdpExtensions.getExtension(Function.class, funcId)` (`ServiceLoader`-backed). There is no regex-engine selector.

---

#### 2. AT&T XACML 3.0 Engine / Apache OpenAZ / ONAP Policy XACML PDP (`SUPPORTED`)
- **Sources (retrieved `2026-10-27`)**:
  - Repository `att/xacml-3.0`, tag `v3.2` (commit `5cb98e5f70b067e04610b1d36a6e72d223f49ce9`) and `master` HEAD (commit `9ff91c5ba0ecc89be7199e2e93c16159accfe155`, 2023-09-05):
    - `FunctionDefinitionRegexpMatch.java`: `https://raw.githubusercontent.com/att/xacml-3.0/5cb98e5f70b067e04610b1d36a6e72d223f49ce9/xacml-pdp/src/main/java/com/att/research/xacmlatt/pdp/std/functions/FunctionDefinitionRegexpMatch.java`
    - `FunctionDefinitionRegexpMatchTest.java`: `https://raw.githubusercontent.com/att/xacml-3.0/5cb98e5f70b067e04610b1d36a6e72d223f49ce9/xacml-pdp/src/test/java/com/att/research/xacmlatt/pdp/std/functions/FunctionDefinitionRegexpMatchTest.java`
    - `StdFunctions.java`: `https://raw.githubusercontent.com/att/xacml-3.0/5cb98e5f70b067e04610b1d36a6e72d223f49ce9/xacml-pdp/src/main/java/com/att/research/xacmlatt/pdp/std/StdFunctions.java`
    - `Match.java`: `https://raw.githubusercontent.com/att/xacml-3.0/5cb98e5f70b067e04610b1d36a6e72d223f49ce9/xacml-pdp/src/main/java/com/att/research/xacmlatt/pdp/policy/Match.java`
    - `ATTPDPEngine.java`: `https://raw.githubusercontent.com/att/xacml-3.0/5cb98e5f70b067e04610b1d36a6e72d223f49ce9/xacml-pdp/src/main/java/com/att/research/xacmlatt/pdp/ATTPDPEngine.java`
  - Repository `apache/incubator-retired-openaz` (commit `d85056d41daefd3adba4af57fc5ca908cd107a48`, 2016-08-28):
    - `FunctionDefinitionRegexpMatch.java` (identical logic): `https://raw.githubusercontent.com/apache/incubator-retired-openaz/d85056d41daefd3adba4af57fc5ca908cd107a48/openaz-xacml-pdp/src/main/java/org/apache/openaz/xacml/pdp/std/functions/FunctionDefinitionRegexpMatch.java`
  - Repository `onap/policy-xacml-pdp`, tag `10.0.1-PF-I1` (commit `923b8047da57ae1866ff1816878164ceee8f3c1e`, 2025-10-16):
    - `pom.xml`: `https://raw.githubusercontent.com/onap/policy-xacml-pdp/923b8047da57ae1866ff1816878164ceee8f3c1e/pom.xml` (depends directly on `com.att.research.xacml:xacml-pdp`).
- **Engine & XML Schema Syntax**:
  - `FunctionDefinitionRegexpMatch.evaluate(EvaluationContext, List<FunctionArgument>)` converts arg #0 to `String regexpValue` and arg #1 to `String elementValueString`, then executes:
    ```java
    if (elementValueString.matches(regexpValue)) {
        return ER_TRUE;
    } else {
        return ER_FALSE;
    }
    ```
  - **No XML Schema syntax translation**: `\p{Is...}`, character class subtraction `[a-z-[aeiou]]`, `\i`, `\c`, and `$` end-of-string semantics are unimplemented.
  - **Full-string match bug**: `String.matches(regexpValue)` delegates to `Pattern.matches(regexpValue, elementValueString)` (`Matcher.matches()`), requiring the pattern to match the **entire input string** rather than a substring as required by XACML 3.0 Appendix A.3.13 / XQuery & XPath F&O `fn:matches`. Their unit test (`FunctionDefinitionRegexpMatchTest.java`) masks this by writing `"^.*123.*$"`, `".*abc.*"`, and `".*Duke.*"`.
- **Caching**:
  - **None**. `String.matches` compiles a fresh `java.util.regex.Pattern` on every evaluation, including inside `<Match>` loops over multi-valued attribute bags (`Match.java`).
- **Status on Failure**:
  - Null/type/DataTypeException errors return `ExpressionResult.newError(new StdStatus(StdStatusCode.STATUS_CODE_PROCESSING_ERROR, ...))`.
  - However, `elementValueString.matches(regexpValue)` is **not** wrapped in a `try/catch (PatternSyntaxException)`. Because `Apply.evaluate`, `Match.match`, `Condition.evaluate`, `Rule.evaluate`, and `ATTPDPEngine.processRequest` only catch checked `EvaluationException`, an invalid regex throws an **uncaught `PatternSyntaxException`** out of `ATTPDPEngine.decide(Request)`.
- **ReDoS & Limits**:
  - **None**. Unbounded `java.util.regex.Pattern` backtracking with no length, repetition, or time limits.

---

#### 3. WSO2 Balana (and SunXACML Lineage) (`SUPPORTED`)
- **Sources (retrieved `2026-10-27`)**:
  - Repository `wso2/balana`, tag `v1.2.29` (commit `282368515bc9f5be04587681e1d415abfe957584`, 2025-10-08):
    - `MatchFunction.java`: `https://raw.githubusercontent.com/wso2/balana/282368515bc9f5be04587681e1d415abfe957584/modules/balana-core/src/main/java/org/wso2/balana/cond/MatchFunction.java`
    - `Apply.java`: `https://raw.githubusercontent.com/wso2/balana/282368515bc9f5be04587681e1d415abfe957584/modules/balana-core/src/main/java/org/wso2/balana/cond/Apply.java`
    - `TargetMatch.java`: `https://raw.githubusercontent.com/wso2/balana/282368515bc9f5be04587681e1d415abfe957584/modules/balana-core/src/main/java/org/wso2/balana/TargetMatch.java`
    - `Rule.java`: `https://raw.githubusercontent.com/wso2/balana/282368515bc9f5be04587681e1d415abfe957584/modules/balana-core/src/main/java/org/wso2/balana/Rule.java`
- **Engine & XML Schema Syntax**:
  - `MatchFunction.regexpHelper(String xpr, String str)` (inherited verbatim from SunXACML `com.sun.xacml.cond.MatchFunction`) rewrites `xpr` in a `StringBuffer` and calls `Pattern.matches(buf.toString(), str)`:
    1. If `xpr.charAt(0) != '^'`, prepends `".*"`.
    2. If `xpr.charAt(xpr.length() - 1) != '$'`, appends `".*"`.
    3. Replaces `\p{Is` with `\p{In` and `\P{Is` with `\P{In`.
    4. Replaces `-[` with `&&[^` via `buf.indexOf("-[", idx)`.
  - **Defects in Balana/SunXACML's rewrite (which `xacml4j` fixed)**:
    - **Empty pattern crash**: `xpr.charAt(0)` throws `StringIndexOutOfBoundsException` when `xpr` is `""`.
    - **Newline mismatch**: `Pattern.matches` does not enable `Pattern.DOTALL`, so prepended/appended `.*` fails to match across `\n` or `\r` in multiline inputs.
    - **Alternation & escaped-anchor corruption**: `^foo|bar` has `charAt(0) == '^'` and `charAt(len-1) != '$'`, so it becomes `^foo|bar.*` (anchoring only the first branch and failing to match `foo` as a substring when `foo` is not at index 0); `foo\$` has `charAt(len-1) == '$'`, so `.*` is not appended even though `\$` is a literal dollar sign.
    - **Unconditional `-[` replacement**: Unlike [`xacml4j` (`RegExpFunctions.java:758`)](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L758), which checks `calculateNestLevel(...) > 0`, Balana replaces `-[` even outside character classes (turning `a-[b]` into `a&&[^b]`).
- **Caching**:
  - **None**. Every evaluation allocates a `StringBuffer`, rewrites the pattern string, and calls `Pattern.matches(...)`.
- **Status on Failure**:
  - `MatchFunction.regexpHelper` does not catch `PatternSyntaxException` (or `StringIndexOutOfBoundsException`). `Apply.evaluate`, `TargetMatch.match`, and `Rule.evaluate` do not catch `RuntimeException`, so an invalid regex escapes PDP evaluation uncaught.
- **ReDoS & Limits**:
  - **None**, and prepending/appending greedy `.*` (`.*<pattern>.*`) amplifies `java.util.regex` backtracking by forcing the NFA to retry `<pattern>` from every starting offset via backtracking over `.*`.

---

#### 4. SNE-XACML (`SUPPORTED`)
- **Source (retrieved `2026-10-27`)**:
  - Repository `canhnt/sne-xacml`, `master` HEAD (commit `7ffca16bf558d2c3ee16181d926f066ab1de75b2`, 2016-04-10):
    - `AllOfExpression.java`: `https://raw.githubusercontent.com/canhnt/sne-xacml/7ffca16bf558d2c3ee16181d926f066ab1de75b2/sne-xacml/src/main/java/nl/uva/sne/xacml/policy/parsers/AllOfExpression.java`
- **Finding**: Compiles XACML 3.0 policies into Multi-data-type Interval Decision Diagrams (MIDDs) supporting only interval comparison operators (`gt`, `ge`, `lt`, `le`, `eq`); `*-regexp-match` functions are not supported.

---

## 2. Options (Q4: Engine Selection & Compile-Once Pattern Reuse)

Q4 in [`openspec/changes/add-regex-engine-spi/proposal.md:55-56`](openspec/changes/add-regex-engine-spi/proposal.md#L55-L56) and [`:32-34`](openspec/changes/add-regex-engine-spi/proposal.md#L32-L34) raises two coupled design choices:
- **Decision A**: How the regex engine (`jdk` vs. `re2j` or custom SPI) is selected (global system property, `FunctionProviderBuilder` / `PolicyDecisionPointBuilder`, or `EvaluationContext`).
- **Decision B**: How compiled patterns (including constant first arguments in `<Match>` and `<Apply>`) are reused across evaluations without `ThreadLocal` leaks or repeated compilation.

### Decision A: Engine Selection Mechanism

| Option | Description | Cost | Risk | What a Test Would Prove |
| --- | --- | --- | --- | --- |
| **A1. Global System Property + `ServiceLoader` only** | [`RegExpFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L64-L66) remains static; selects engine via `-Dorg.xacml4j.v30.policy.function.impl.RegExpFunctions.engine=jdk\|re2j` (default `jdk`), discovering `RegExpEngineProvider` via `ServiceLoader`. | **Low**: zero changes to [`PolicyDecisionPointBuilder`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java#L44-L161) or [`EvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/EvaluationContext.java#L41-L385). | **Medium**: process-wide global state makes multi-tenant PDPs or parallel JMH/unit tests with different engines require a package-private setter or reset hook (like today's [`RegExpFunctions.configureLimits`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L198-L237)). | Setting the property (or calling the test hook) to `re2j` routes all six `*-regexp-match` functions through `re2j`; unknown engine name fails fast at startup/configuration; missing `xacml-re2j` JAR with `engine=re2j` fails clearly. |
| **A2. `EvaluationContext` / `PolicyDecisionPointBuilder` per-PDP wiring** | Add `RegExpEngine` (analogous to [`XPathProvider`](xacml-core/src/main/java/org/xacml4j/v30/content/XPathProvider.java#L52-L180)) to [`PolicyDecisionPointBuilder`](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java#L51-L140) and [`EvaluationContext`](xacml-core/src/main/java/org/xacml4j/v30/EvaluationContext.java#L41-L385), and pass `@XacmlEvaluationContextParam EvaluationContext` to the 6 methods in [`RegExpFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L69-L127). | **Medium**: touches `EvaluationContext`, `RootEvaluationContext`, `DelegatingEvaluationContext`, `PolicyDecisionPointBuilder`, and the 6 `@XacmlFuncSpec` method signatures in `RegExpFunctions`. | **Medium**: direct callers of `RegExpFunctions.regexpMatch(StringVal, StringVal)` in existing tests ([`RegularExpressionFunctionsTest.java`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L69-L73)) require two-arg overloads to remain binary-compatible; also prevents policy-parse-time compilation unless the parser also knows the engine. | Two PDP instances in the same JVM configured with `jdk` and `re2j` respectively evaluate requests concurrently with isolated engines and limits. |
| **A3. Instance-backed `RegExpFunctions` in `FunctionProviderBuilder` + System Property default (Recommended)** | Keep static two-arg methods in [`RegExpFunctions`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L69-L127) delegating to a default `RegExpEngine` resolved via system property + `ServiceLoader` (default `jdk`), AND allow constructing `new RegExpFunctions(RegExpEngine)` via [`FunctionProviderBuilder.fromInstance(...)`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProviderBuilder.java#L157-L166) (which `AnnotationBasedFunctionProvider` already supports!). | **Low**: zero changes to `EvaluationContext` or `Match`/`Apply`; reuses `FunctionProviderBuilder.fromInstance` and `ServiceLoader`. | **Low**: `FunctionProviderBuilder.builder().withDefaultFunctions()` registers `XacmlDefaultFunctions` first, so `FunctionProviderBuilder` or `XacmlDefaultFunctions(FunctionInvocationFactory, RegExpEngine)` should allow overriding the `RegExpFunctions` provider cleanly. | 1) Default PDP uses `jdk` (or system property override); 2) Programmatic `FunctionProviderBuilder` / `XacmlDefaultFunctions` with explicit `re2j` engine runs isolated from global property; 3) Existing two-arg static calls in tests continue to compile unchanged. |

**Recommendation for Decision A**: **Option A3**. It matches both `xacml4j`'s existing `ServiceLoader` + builder architecture ([`FunctionProviderBuilder.java:64-166`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProviderBuilder.java#L64-L166)) and `RegExpFunctions`'s existing system-property configuration ([`RegExpFunctions.java:155-180`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L155-L180)), without polluting `EvaluationContext` or breaking existing static callers.

---

### Decision B: Compile-Once Reuse of Constant First Arguments vs. Bounded Shared Cache

| Option | Description | Cost | Risk | What a Test Would Prove |
| --- | --- | --- | --- | --- |
| **B1. Bounded Thread-Safe Cache inside each `RegExpEngine` (Enabled by Default)** | Replace the disabled-by-default `ThreadLocal<PatternCache>` ([`RegExpFunctions.java:173-180`](xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java#L173-L180)) with a bounded thread-safe cache per `RegExpEngine` instance (enabled by default, e.g. capacity `256`–`1024`), storing the immutable compiled pattern (`java.util.regex.Pattern` or `com.google.re2j.Pattern`). | **Low**: localized to the regex engine implementation; both `java.util.regex.Pattern` and `com.google.re2j.Pattern` are thread-safe and immutable (`Matcher` is created per call). | **Low**: cache lookup adds a single concurrent hash lookup (`String` key) per evaluation; avoids `ThreadLocal` memory retention in thread pools. | JMH benchmark and unit test showing repeated evaluation of constant and dynamic patterns across multiple threads compiles each distinct pattern once, respects max capacity, and never leaks `ThreadLocal` entries. |
| **B2. AuthzForce-style AST Compile-Once Specialization in `Match` / `Apply`** | Like AuthzForce's `RegexpMatchFunctionHelper.getCompiledRegexMatchCall`, inspect `Match` ([`Match.java:44-78`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L44-L78)) and `Apply` ([`Apply.java:84-91`](xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java#L84-L91)) when built; if `spec` is a `*-regexp-match` function and arg #0 is a constant `Value`, pre-compile and bind a specialized `FunctionSpec` / call site. | **High**: requires changing `FunctionSpec` / `Apply` / `Match` / `HigherOrderFunctions` (`any-of`, `all-of`, `any-of-any`), and couples AST construction to the active `RegExpEngine`. | **High**: in `xacml4j`, an invalid regex in a branch that is never evaluated must **not** fail policy loading if XACML conformance tests expect runtime `Indeterminate` (`processing-error`) only when evaluated; also, as seen in AuthzForce, higher-order two-bag functions (`any-of-any`, `all-of-any`) bypass AST pre-compilation unless a runtime cache also exists. | Policy with an invalid regex in an unreachable `Rule` or short-circuited `or(true, regexp-match("[", x))` still loads and evaluates per XACML 3.0 spec. |
| **B3. B1 (Bounded Engine Cache on by default) + Optional Per-`StringVal` or Call-Site Memoization (Recommended)** | Make each `RegExpEngine` own a bounded, lock-free/concurrent compiled-pattern cache **enabled by default** (B1). Because XACML `<Match>` and `<Apply>` AST nodes hold the same immutable [`StringVal`](xacml-core/src/main/java/org/xacml4j/v30/policy/Match.java#L44) instance across all evaluations of a loaded policy, keying or memoizing on the pattern `String` in the engine's bounded cache gives compile-once reuse across `<Match>`, `<Apply>`, and all higher-order bag functions (`any-of-any`, etc.) without changing AST semantics or eager-failing at policy load time. | **Low**: keeps `Match`, `Apply`, and `FunctionSpec` completely decoupled from regex internals while delivering compile-once behavior for every constant (and repeated dynamic) pattern. | **Low**: preserves XACML lazy/runtime error semantics (`processing-error` on evaluation rather than policy-load crash). | Conformance suite + unit test where a `<Match>` over a 100-element bag and `any-of-any` compile the regex pattern exactly once (`compileCount == 1`), and unreachable invalid regexes do not break policy parsing. |

**Recommendation for Decision B**: **Option B3 (B1 enabled by default)**. AuthzForce's AST specialization (`getCompiledRegexMatchCall`) is clever, but it (a) changes policy-load vs. evaluation-time error semantics by throwing `IllegalArgumentException` at load time, and (b) completely misses `any-of-any`, `all-of-any`, `any-of-all`, and `all-of-all`. A bounded concurrent compiled-pattern cache inside `RegExpEngine` (enabled by default, replacing the disabled `ThreadLocal` LRU) achieves compile-once behavior for `<Match>`, `<Apply>`, and all higher-order bag functions alike, while preserving `xacml4j`'s runtime `Indeterminate` (`processing-error`) semantics.

---

## 3. Confidence and Disagreement / Drift Risk

- **Evidence Labels**:
  - `xacml4j` repository claims: **`SUPPORTED`** (verified directly in opened workspace files with exact line numbers).
  - External XACML engine claims (AuthzForce, AT&T XACML / OpenAZ / ONAP, WSO2 Balana / SunXACML, SNE-XACML): **`SUPPORTED`** (verified directly by fetching exact source files from GitHub at pinned release tags/commit SHAs on `2026-10-27` via `duckduckgo` `fetch_content`).
- **Pinned Versions & Retrieval Dates (`2026-10-27`)**:
  - `authzforce/core`: tag `release-21.2.0` (`3cc0e988e1639da48184434cd5c918102ff5b499`, 2025-05-14).
  - `authzforce/core-pdp-api`: tag `release-22.2.0` (`da56877b2e2141875d033c0490c02e9bf449049e`, 2024-11-12).
  - `att/xacml-3.0`: tag `v3.2` (`5cb98e5f70b067e04610b1d36a6e72d223f49ce9`) and `master` HEAD (`9ff91c5ba0ecc89be7199e2e93c16159accfe155`, 2023-09-05).
  - `apache/incubator-retired-openaz`: `master` HEAD (`d85056d41daefd3adba4af57fc5ca908cd107a48`, 2016-08-28).
  - `onap/policy-xacml-pdp`: tag `10.0.1-PF-I1` (`923b8047da57ae1866ff1816878164ceee8f3c1e`, 2025-10-16).
  - `wso2/balana`: tag `v1.2.29` (`282368515bc9f5be04587681e1d415abfe957584`, 2025-10-08).
  - `canhnt/sne-xacml`: `master` HEAD (`7ffca16bf558d2c3ee16181d926f066ab1de75b2`, 2016-04-10).
- **MCP Tool Status & Fallbacks Recorded (`2026-10-27`)**:
  - `github` MCP (`search_code`): **`UNAVAILABLE`** (failed with `HTTP 401 Bad credentials` on `2026-10-27`; not retried).
  - `tavily` MCP (`tavily_search`): **`PARTIAL`** (returned `Internal Server Error` on boolean `OR` / multi-`site:` queries, succeeded on simple queries).
  - `duckduckgo` MCP: `search` returned 0 results (bot block), whereas `fetch_content` against `https://api.github.com/...` and `https://raw.githubusercontent.com/...` succeeded for all inspected tags, trees, and source files (**`SUPPORTED`**).
- **Disagreement / Drift Risk**:
  - **Policy-load vs. evaluation-time invalid regex errors**: AuthzForce rejects static invalid regular expressions at policy-load time (`IllegalArgumentException`), whereas `xacml4j` evaluates `*-regexp-match` lazily at request time and returns `Indeterminate` (`urn:oasis:names:tc:xacml:1.0:status:processing-error`). Keep `xacml4j`'s evaluation-time `Indeterminate` (`processing-error`) behavior so policies with unevaluated branches do not fail to load.

---

## 4. Not Checked

- **Proprietary / closed-source XACML engines** (`UNVERIFIED`): Commercial engines (Axiomatics Policy Server, PingAuthorize / UnboundID, Oracle Entitlements Server) do not publish their `*-regexp-match` source code publicly and were not inspected.
- **Live runtime execution of external engines**: Per the read-only research lane constraints, external engines were analyzed strictly from their pinned source code and unit tests without cloning or executing their builds locally.
