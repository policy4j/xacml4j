<!-- Discovery lane DQ1 (task 1.1): Claude Opus 5.5, read-only; questions Q5 and design D1, D2, D4, D5. -->

FINAL REPORT (xacml4j JMH benchmark engineer, lane DQ1 research, Claude Opus 5.5)

## Summary

- **Q5 result:**
  - Keep the GNU location prefix (`file:line:col:`), with 1-based lines and columns.
  - Use SARIF 2.1.0 as the machine format.
  - GitHub code scanning reads a SARIF subset; GitHub Actions, VS Code tasks, Emacs, and Vim parse GCC-style text.
  - GitLab Code Quality needs its own JSON format.
  - LSP clients need 0-based UTF-16 ranges.
- **D1, D2, D4, D5 mostly fit compiler and validator practice.** The most important changes, first:
  1. **D4:** JAXB validation events are dropped today, so a misspelled element can vanish without an error. Route them into the collector.
  2. **D5:** put the code at the end of the line (`error: … [POL203]`). D5's form `error[POL203]:` fails the GCC regex that VS Code documents.
  3. **D1:** use a span instead of a point, typed children (note or help), and a typed logical location (XML path or JSON Pointer). The model must be Serializable.
  4. **D3/D5:** SAX and `-Xlocator` positions point to the end of the start tag. The caret needs a backward scan of the source text.
  5. **D5:** write SARIF with a small built-in writer, not the D8 JSON provider.
  6. **D1:** add the families VAL, RSP, and DIA. A first catalogue of 38 codes is below.
- **Defects in today's message templates** (report only): unfilled `%s` and `{}` placeholders, a template that throws, and wrong texts.

## Q5. Diagnostic formats and their consumers

### Answer

**Text conventions**

- **GNU Coding Standards §4.4:**
  - Forms: `sourcefile:lineno: message` or `sourcefile:lineno:column: message`.
  - Line and column numbers start at 1. Columns count tab stops every 8 and Unicode display widths.
  - Ranges: `line1.column1-line2.column2`.
  - A non-compiler tool prefixes its program name.
- **GCC:**
  - Each diagnostic shows the controlling option at the end, for example `… [-Wimplicit-function-declaration]` (`-fdiagnostics-show-option` is on by default).
  - The column unit defaults to `display` (bytes before 11.1). `-fdiagnostics-column-origin` defaults to 1.
  - `-fmax-errors` defaults to 0, which means unlimited.
  - `-Werror=` and `-Wno-error=` work per warning.
  - SARIF 2.1.0 output: `sarif-file`, `sarif-stderr`, and `-fdiagnostics-add-output=sarif:version=2.1` (2.2 is experimental).
- **Clang:**
  - `-fdiagnostics-format=clang|msvc|vi|sarif`. The default is `t.c:3:11: warning: …`, and Clang marks its SARIF output "currently unstable".
  - `-ferror-limit` defaults to 20.
  - Diagnostic classes: Ignored, Note, Remark, Warning, Error, Fatal. Fix-its are supported.
- **rustc:**
  - Each error has a code like E0123 with a Markdown explanation, shown by `--explain`.
  - Style guide: start with a lowercase letter, no trailing punctuation, plain English, "invalid" instead of "illegal".
  - JSON spans:
    - `line_start` and `column_start` are 1-based, counted in Unicode scalar values;
    - `byte_start` and `byte_end` are 0-based;
    - `is_primary`, `label`, and `suggested_replacement` with `suggestion_applicability` (MachineApplicable, MaybeIncorrect, HasPlaceholders, Unspecified);
    - `children` with level note or help.

**"Did you mean" in compilers**

| Compiler | Distance | Cutoff and order |
| --- | --- | --- |
| GCC | Restricted Damerau-Levenshtein (optimal string alignment); a case change costs half | About a third of the longer string; on a tie, the first candidate wins |
| rustc | Restricted Damerau-Levenshtein | First an exact case-insensitive match, then edit distance with limit `max(len,3)/3`, then a sorted-word match; it can also retry with a prefix-stripped lookup |
| Clang | Levenshtein | `UpperBound = (TypoStr.size()+2)/3`, plus a length-difference pre-check |

**Machine format: SARIF 2.1.0 (OASIS, plus Errata 01)**

- Rule `id` SHALL be stable and SHOULD be opaque (§3.49.3). `name` is a readable identifier, for example `SpecifyMarshalingForPInvokeStringArguments` (§3.49.7).
- `level` is one of error, warning, note, none (§3.27.10). When no level resolves, the default is "warning" (§3.50.3).
- Region (§3.30.5-8): `startLine` is positive, `startColumn` defaults to 1, and `endColumn` is one past the last character.
- `columnKind` SHALL be present when a producer of text artifacts writes results (OS text).
- `logicalLocation.kind` has XML values `element`, `attribute`, `text`, `comment`, `processingInstruction`, `dtd` (§3.33.7). The spec's own example for JSON uses JSON Pointers (`/orders/0/productIds/1`).
- A notification describes the tool, not the analyzed artifact; artifact conditions are results (§3.58.1). An error-level `toolExecutionNotification` means the run failed (§3.20.21).

**What CI and IDEs consume**

| Consumer | Input | Positions | Code | Limits and notes |
| --- | --- | --- | --- | --- |
| GitHub code scanning | SARIF 2.1.0 subset | `physicalLocation` (relative URI), region semantics as SARIF | `ruleId` (stable) | 10 MB gzip; 20 runs; 25,000 results per run (5,000 shown); only the first location is used; only the first sentence of `message.text` shows when space is short; `partialFingerprints`: only `primaryLocationLineHash` is used; `help.markdown` recommended |
| GitHub Actions | `::error file=…,line=…,col=…,endColumn=…,title=…::msg`, or problem matchers (regex with file, line, column, severity, code, message groups) | line and col start at 1 | `code` group | 10 error, 10 warning, 10 notice annotations per step; 50 per job; severity is only warning or error |
| GitLab merge requests | Code Quality JSON array (CodeClimate subset: description, check_name, fingerprint, location.path, location.lines.begin) | line | `check_name` | Not SARIF |
| VS Code tasks | problem-matcher regex; documented GCC example `^(.*):(\d+):(\d+):\s+(warning|error):\s+(.*)$`; the C/C++ extension ships GCC matchers | 1-based | optional group | Text only |
| LSP clients | Diagnostic with range | 0-based line and character; UTF-16 is mandatory, other encodings by negotiation since 3.17 | `code` | Future language server |
| Java tooling | `javax.tools.Diagnostic` | lines and columns 1-based; offsets 0-based; `NOPOS` | `getCode()` (implementation-defined) | The JDK model to mirror |

**What the XML stack gives us**

- **SAX ErrorHandler:** `error()` is recoverable and parsing continues. After `fatalError()`, the application "must assume that the parser can no longer perform normal processing".
- **SAX positions:** Locator and SAXParseException report the end of the event's text, 1-based, in Java `char` (UTF-16) units, as an approximation. The system id "must" be a fully resolved URL, for example `file:…`.
- **JAXB `ValidationEventHandler`:** return true to continue, false to stop. JAXB RI reports unexpected child elements and unexpected text as recoverable ERROR events with a locator.
- **JDK Xerces schema messages** start with the W3C constraint id, for example `cvc-complex-type.2.4.a: Invalid content was found starting with element '{0}'. One of '{1}' is expected.`

### Sources (all retrieved 2026-10-27)

- GNU Coding Standards §4.4: https://www.gnu.org/prep/standards/html_node/Errors.html
- GCC formatting options: https://gcc.gnu.org/onlinedocs/gcc/Diagnostic-Message-Formatting-Options.html
- GCC warning options: https://gcc.gnu.org/onlinedocs/gcc/Warning-Options.html
- GCC `spellcheck.cc`: https://raw.githubusercontent.com/gcc-mirror/gcc/master/gcc/spellcheck.cc
- Clang User's Manual: https://clang.llvm.org/docs/UsersManual.html
- Clang `SemaLookup.cpp`: https://raw.githubusercontent.com/llvm/llvm-project/main/clang/lib/Sema/SemaLookup.cpp
- rustc JSON output: https://doc.rust-lang.org/rustc/json.html
- rustc dev guide, errors and lints: https://rustc-dev-guide.rust-lang.org/diagnostics.html
- rustc dev guide, error codes: https://rustc-dev-guide.rust-lang.org/diagnostics/error-codes.html
- rustc `edit_distance.rs`: https://raw.githubusercontent.com/rust-lang/rust/master/compiler/rustc_span/src/edit_distance.rs
- SARIF 2.1.0 plus Errata 01: https://docs.oasis-open.org/sarif/sarif/v2.1.0/errata01/os/sarif-v2.1.0-errata01-os-complete.html
- SARIF 2.1.0 OS, columnKind sentence (seen in a search snippet of this page): https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html
- GitHub SARIF support: https://docs.github.com/en/code-security/code-scanning/integrating-with-code-scanning/sarif-support-for-code-scanning
- GitHub workflow commands: https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-commands
- GitHub problem matchers: https://raw.githubusercontent.com/actions/toolkit/main/docs/problem-matchers.md
- VS Code tasks: https://code.visualstudio.com/docs/debugtest/tasks
- GitLab Code Quality: https://docs.gitlab.com/ci/testing/code_quality/
- LSP 3.17: https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/
- JDK 25 API docs:
  - https://docs.oracle.com/en/java/javase/25/docs/api/java.compiler/javax/tools/Diagnostic.html
  - https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/org/xml/sax/ErrorHandler.html
  - https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/org/xml/sax/SAXParseException.html
  - https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/org/xml/sax/Locator.html
- Jakarta XML Binding 2.3 `ValidationEventHandler`: https://jakarta.ee/specifications/xml-binding/2.3/apidocs/javax/xml/bind/ValidationEventHandler.html
- JAXB RI `Loader.java` (master): https://raw.githubusercontent.com/eclipse-ee4j/jaxb-ri/master/jaxb-ri/runtime/impl/src/main/java/org/glassfish/jaxb/runtime/v2/runtime/unmarshaller/Loader.java
- JDK `XMLSchemaMessages.properties`: https://raw.githubusercontent.com/openjdk/jdk/master/src/java.xml/share/classes/com/sun/org/apache/xerces/internal/impl/msg/XMLSchemaMessages.properties
- RFC 6901 (JSON Pointer): https://www.rfc-editor.org/rfc/rfc6901.html

### Confidence

- **High:** the format facts above, each quoted from its primary source.
- **Medium:** the consumer-compatibility conclusions, which are inferred from the documented regexes and limits.
- **UNVERIFIED:**
  - the SARIF `columnKind` values (`utf16CodeUnits`, `unicodeCodePoints`) and the JSON `logicalLocation` kinds (Tavily hit its rate limit);
  - the exact regex of the C/C++ extension's GCC matcher;
  - JAXB RI behavior in the 2.3.x line in use (checked in master source only).

### Disagreement or drift risk

- **Columns have no single standard:** GNU and GCC count display columns; SAX and `javax.tools` count UTF-16; rustc counts Unicode scalar values; LSP is 0-based UTF-16. SARIF makes the producer declare the unit, so this must be declared.
- **Moving targets:** GCC's SARIF sinks are new and SARIF 2.2 is a draft; Clang marks its SARIF unstable; GitHub limits change over time; LSP 3.18 is current (3.17 cited).

## Recommendations for the design (most important first)

### R1 (D4, high): route JAXB events into the collector

- **Problem:** `BaseJAXBUnmarshaller.java:87-93` logs every event at debug level and returns true, also for FATAL_ERROR. JAXB RI turns an unexpected element into a recoverable ERROR (`Loader.reportUnexpectedChildElement`, `reportError`), and the API skips it when the handler returns true. So a misspelled `<Rule>` can silently drop a rule.
- **Mapping:**

  | JAXB event | Code | Severity | Parse |
  | --- | --- | --- | --- |
  | unexpected element | XML002 | error | continue; note with "expected one of …" from the event text; hint over the expected names |
  | unexpected text | XML003 | warning | continue |
  | parse conversion | XML004 | error | continue |
  | FATAL_ERROR | — | error | stop |

- **Before shipping:** this changes which documents load. Characterize it in task 3.1 against the fixtures (Q6), and get a maintainer decision (BREAKING if it is an error by default).

### R2 (D5, high): code at the end of the first line

- **Form:** `<source>:<line>:<col>: <severity>: <message> [<code>]`.
- **Why:**
  - It keeps the GNU prefix.
  - It matches GCC's and Clang's `[-Wflag]` placement.
  - It matches the GCC regex documented by VS Code. `error[POL203]:` does not match that regex.
- **Change:** update the spec requirement "Compiler-like text format".
- **If the rustc look is kept instead:** ship problem matchers for GitHub and VS Code with `xacmlc`.

### R3 (D1, high): model changes

- **Span:** `SourceSpan` (start, optional end, optional offset). SARIF and LSP use ranges, rustc uses spans, and GitHub annotations take `endColumn`.
- **Column unit:** 1-based UTF-16, as SAX and `javax.tools.Diagnostic` use. Write `columnKind` in SARIF. The caret printer alone expands tabs to stops of 8, as GNU does.
- **Typed children:** `Note(kind NOTE|HELP, message, span?, suggestion?)`, as rustc's children are.
- **Typed logical location:** `(kind, name)`.
  - XML path: kind element, attribute, or text (SARIF §3.33.7).
  - JSON: a JSON Pointer (RFC 6901, SARIF's example), not Gson's `$.a[0]` form.
- **`DiagnosticCode` as an interface:** id, name, family, default severity, fatal flag, template, help markdown, helpUri. The built-in enum implements it, other modules add codes, and SARIF `rules[]` is generated from it.
- **Serializable:** `SyntaxException` is a `RuntimeException` (`CoreException.java:32`). The diagnostic records must implement Serializable.
- **Source name:** take it from `ParseOptions` only, never from `Locator.getSystemId()`. That value can be an absolute `file:` URL, which would break the spec's home-path rule (`specs/parse-diagnostics/spec.md:9-11`).

### R4 (D3/D5, high): element positions

- **Problem:** SAX and `-Xlocator` report the end of the start tag (Locator: "the first character after the text associated with the document event").
- **With source text:** scan back to `<`, and for attribute errors to the attribute value (FunctionId, DataType).
- **Without source text** (DOM or caller readers): report the parser's point and say so.
- **Spec change:** scenario "Unknown function in a policy" names which column counts.

### R5 (D5, medium-high): a built-in SARIF writer

- **Why not the D8 provider:**
  - no new dependency in the diagnostics package;
  - a stable key order for golden tests;
  - no reflection configuration for the native `xacmlc`.
- **Content:**
  - `version` "2.1.0" and `$schema`;
  - `tool.driver` (name, semanticVersion, `rules` from the catalogue with name, shortDescription, `help.markdown`, `defaultConfiguration.level`);
  - each result with an explicit `level` (otherwise "warning" is assumed);
  - one physical location with a relative URI, plus `logicalLocations`;
  - `columnKind`.
- **Suppressed-errors note (DIA901):** write it as a `toolExecutionNotification` at level warning, not as a result.
- **GitHub limits:** stay inside them (see the consumer table).
- **Schema check in tests:** needs a test-scope validator (dependency approval).

### R6 (D5, medium): "did you mean" algorithm

1. First an exact case-insensitive match (rustc).
2. Restricted Damerau-Levenshtein (GCC, rustc).
3. Measure over the full id with limit max(2, ⌊last segment / 3⌋); segments split at `:` `/` `#`. This equals the spec rule and sits between rustc, GCC, and Clang.
4. Then retry on the last segment alone (rustc's multi-lookup).
5. Search the same registry kind and XACML version first. Use an exact match in the other version as a specific note.
6. Break ties deterministically (GCC: first wins).
7. Give one suggestion only.
8. Registries: functions, data types, algorithms, categories, status codes, and the expected element names from JAXB events.

### R7 (D2, medium): exception and options

- **Constructor:** `SyntaxException(List<Diagnostic>)` stores a snapshot (`List.copyOf`), not the collector.
- **Message:** the first diagnostic's first line only, never the excerpt. The message becomes the Status message (`CoreException.java:43-50`), which a PDP can return, and excerpts can hold attribute values.
- **Error cap:** 100 is fine (Clang 20, GCC unlimited).
- **Per-code severity overrides:** GCC `-Werror=`, Clang, SARIF configuration overrides. A promoted warning keeps its code and gets a note naming the option.
- **`retainSource` option:**
  - on for `byte[]` input (no copy needed);
  - off for library `InputStream` input, because a full copy costs time and memory on valid input (spec "No cost on valid input");
  - `xacmlc` reads files as bytes.

### R8 (D4, medium): cascades and order

- Mark skipped definitions as poisoned and suppress follow-on errors.
- Sort the final list by (source, line, column, sequence), so XSD-filter errors and mapper errors appear in document order.
- These stop the parse at once: XML syntax, LIM, JSON syntax, and XSD102. SAX `fatalError` semantics support this.

### R9 (D5, medium): message style and templates

- Messages follow rustc style: lowercase start, no trailing period, identifiers in quotes.
- Each code has one template, and a test checks that no placeholder is left unfilled.

### R10 (D1, medium): families

- Add VAL6nn: the same value or identifier check gives the same code in policy, request, XML, and JSON.
- Add RSP7nn: responses are parsed today (`Xacml30RequestContextFromJaxbToObjectModelMapper.java:370-373`, `ResultAdapter.java:88`).
- Add DIA9nn: notes about the diagnostics themselves.
- JSN covers JSON syntax and JSON-profile structure only. Model errors found in JSON use the POL, REQ, RSP, or VAL codes.

### R11 (D10, low-medium): later CLI adapters

- `--format=github` for workflow commands.
- A GitLab Code Quality JSON adapter.

### R12 (D5/D10): explanations and code stability

- Add `xacmlc --explain CODE` from each code's Markdown help (rustc).
- Never reuse a code. Use SARIF `deprecatedIds` when a code is renamed.

## Proposed code catalogue (families and first codes)

Severity is the default severity. "Stop" means the parse stops at once.

| Code | Name | Severity, stop | Current source anchor |
| --- | --- | --- | --- |
| XML001 | xml-not-well-formed | error, stop | `BaseJAXBUnmarshaller.java:146-158,197-205` |
| XML002 | unexpected-element | error*, continue | `:87-93` (event dropped today) |
| XML003 | unexpected-text | warning | same |
| XML004 | invalid-xml-attribute-value | error | `BooleanAttributeValidationFilter.java:78`, JAXB conversion events |
| XSD101 | schema-violation (note carries the `cvc-…` id) | error, continue | D7 |
| XSD102 | no-schema-for-namespace | error, stop | D7 |
| POL201 | unknown-policy-root | error, stop | design.md:16-17; mapper `:199` |
| POL202 | missing-required-attribute | error | mapper `:578`, XACML 2.0 sites |
| POL203 | unknown-function (with hint) | error | D5 example |
| POL204 | unknown-rule-combining-algorithm | error | `PolicyUnmarshallerSupport.java:91` |
| POL205 | unknown-policy-combining-algorithm | error | — |
| POL206 | function-argument-mismatch | error | `FunctionSpecBuilder.validateParametersAndThrow` |
| POL207 | undefined-variable | error (suppressed when the definition was poisoned) | — |
| POL208 | cyclic-variable-reference | error | `VariableManager.java:61` |
| POL209 | empty-policy-reference | error | mapper `:249,:263` |
| POL210 | match-without-value | error | mapper `:527` |
| POL211 | invalid-version-pattern | error | `VersionMatch.java` |
| POL212 | unsupported-attribute-reference | error | mapper `:593` |
| REQ301 | unknown-request-root | error, stop | — |
| REQ302 | missing-required-attribute | error | `Xacml30RequestContextFromJaxbToObjectModelMapper.java:519`, `Xacml20RequestContextUnmarshaller.java:254`, JSON `AttributeDeserializer.java:60` |
| REQ303 | invalid-category | error; design Open Question 3 decides warning or note for valid custom URIs | `:472` |
| REQ304 | attributes-reference-without-id | error | `:502,:508` |
| VAL601 | invalid-value-for-data-type | error | `TypeToXacml30` (16 sites), types/* |
| VAL602 | unknown-data-type (with hint) | error | `:268`, `Xacml20RequestContextUnmarshaller.java:296` |
| RSP701 | invalid-status-code (with hint) | error | `:370-373` |
| RSP702 | invalid-decision | error | `ResultAdapter.java:88` |
| LIM401 | doctype-not-allowed | error, stop | spec parse-security |
| LIM402 | caller-parser-rejected | error, stop | same |
| LIM410 to LIM417 | document size, depth, elements, attributes per element, attribute length, text or string length, name length, number length | error, stop; message names the limit and value; help names the option | D6 |
| JSN501 | json-not-well-formed | error, stop | — |
| JSN502 | duplicate-member | error, stop | spec parse-security |
| JSN503 | wrong-json-type | error | — |
| JSN504 | unknown-member | severity open | XACML JSON profile rule not checked |
| DIA901 | errors-suppressed | note | SARIF notification |

\* XML002 as an error by default depends on the maintainer's decision (R1).

## Findings in current code (report only; for task 3.1)

- **JAXB validation events are dropped** (R1). JAXB RI behavior checked in master source; a test in task 3.1 confirms it for 2.3.x.
- **Broken message templates:**
  - `SyntaxException.java:136-149` uses `{}` placeholders with `String.format` (`CoreException.java:78-80`). The arguments are dropped, for example at `DOMUtil.java:217,360`.
  - `:121-125` ends its template with `s%`. `String.format` throws on it (by reading; UNVERIFIED by test), so the caller at `DefaultResolverRegistry.java:108` gets a format exception instead of a SyntaxException.
  - `:111-113` invalid status code says "Invalid XACML type identifier".
  - `:96-103` passes 5 arguments to 4 placeholders.
  - `Xacml30PolicyFromJaxbToObjectModelMapper.java:527-528` contains a literal `%s`.
- **JSON adapters work on Gson trees** (`JsonRequestContextUnmarshaller.java:57-69`), so they have no positions. Positions need the D8 streaming path.

## Risks and open questions

1. Unknown or misplaced elements: error (BREAKING for documents that load today) or warning? (R1)
2. Text form: GCC placement (R2), or rustc placement with shipped matchers?
3. REQ303 severity (design Open Question 3), and the JSON-profile rule for unknown members (JSN504; UNVERIFIED).
4. With Jackson byte sources, the column may count bytes (UNVERIFIED; for the Q4 lane). The model needs UTF-16 columns.
5. Schema-validating the SARIF output in tests needs a test-scope dependency (approval).
6. Hints can name a valid but different id, for example `date-…` vs `dateTime-…`. They must stay "help:" text, never an automatic fix.

## Verification (this lane)

- **Read:**
  - the change's proposal (including Approval), design, tasks, both specs, and `research/README.md`;
  - `SyntaxException`, `CoreException`, `BaseJAXBUnmarshaller`, the error sites of the mappers and JSON adapters, and the `Unmarshaller` interface.
- **Commands:** `rtk rg` and `rtk proxy sed` only.
- **Fetched:** the external sources above (Tavily extract; DuckDuckGo fetch after two Tavily 429 responses).
- **Not run:** Maven, tests, benchmarks, git or br writes. No files written.

## Residual risk

- Two SARIF details are UNVERIFIED (see Confidence).
- The JAXB RI behavior and the `s%` exception are inferred from source reading, not tests.
- The consumer compatibility claims rest on the documented regexes, not on runs in each tool.
