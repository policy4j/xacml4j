<!-- Lane BR1 of the authorization-model brainstorm swarm, Claude Opus 5.5, read-only; scope: ALFA and the Axiomatics-style domain model; report from the NTM pane, paths made repository-relative (artagon-uri paths relative to the Artagon projects folder). -->

FINAL REPORT (BR1 ALFA and domain-model lane, Claude Opus 5.5)

## Summary

- **ALFA is not a standard.**
  - The OASIS text is "Abbreviated Language for Authorization Version 1.0, Working Draft 01, 10 March 2015". It was never voted to a Committee Draft, and `docs.oasis-open.org/xacml/` has no ALFA directory.
  - "ALFA 2.0" was an individual IETF Internet-Draft (`draft-brossard-alfa-authz-00`, 22 July 2024). It is now expired and is a skeleton with no grammar.
  - In practice ALFA is what Axiomatics' tools do. Rock Solid Knowledge's Enforcer also uses it.
- **No permissively licensed, complete ALFA grammar exists.**
  - The only open compiler found is `a2x` (Rust, pest), and it is GPL-3.0-or-later.
  - The ALFA 2.0 repository has `Alfa2.g4`, but it is a 99-line token stub under IETF terms.
  - A grammar for xacml4j would have to be written fresh. Its basis would be WD01 (OASIS notice) plus the documented Axiomatics behavior.
- **Axiomatics' domain model ("dictionary") is plain data.**
  - Each attribute is a short name in a namespace, plus an XACML id URI, a category, a data type, and an optional constraint (enumeration, pattern, range).
  - The authoritative file today is `attributes.yaml`. `attributes.alfa` and a Java dictionary are generated from it. Validation happens when the ALFA compiler resolves names and checks types.
- **XACML 4.0 (ACAL) CSD01 (18 Feb 2026) ships a machine-readable `ShortIdSet`.**
  - It has 321 short names: categories, 17 data types, 17 standard attributes, 4 status codes, 7 combining algorithms, 268 functions.
  - It is a ready seed for a "4.0 model". The names use the same last-segment convention as xacml4j's function short ids.
- **xacml4j already has most of the pieces.**
  - Short names exist for categories, attributes, data types, and functions.
  - Builders exist for every policy element, and a policy-tree visitor exists.
  - The marshalling SPI is pluggable. It falls short in two places: the closed `MediaType` enum, and a single-root `PolicyUnmarshaller`.
- **Recommendation:**
  1. A format-neutral `xacml-model` module (the authorization model, or dictionary), with built-in packs for XACML 2.0, 3.0, and 4.0-CSD01, generated from xacml4j's own registries.
  2. A separate `xacml-alfa` module: a clean-room ANTLR 4 grammar, semantic checks against the model, lowering to the existing builders, a reverse printer, and diagnostics with source spans.
  3. Both opt-in, with no change to the PDP. "Do not do it" (use Axiomatics' tools and import the XACML XML) remains a valid fallback.

## Research tools (2026-10-27)

| Tool | Label | Note |
| --- | --- | --- |
| GitHub connector or MCP, `gh` CLI | Not used | Packet rule. Public GitHub pages were read anonymously over HTTPS. |
| Tavily search | PARTIAL | 2 calls worked; the 3rd returned HTTP 429. |
| DuckDuckGo search | PARTIAL | One query returned "No results" (bot detection). |
| DuckDuckGo fetch | SUPPORTED | Axiomatics documentation pages. |
| WebSearch | SUPPORTED | Fallback after the Tavily 429. |
| WebFetch | SUPPORTED | Axiomatics, OASIS, IETF datatracker, docs.rs, github.com pages. |
| `curl` (anonymous, read in memory, nothing written) | SUPPORTED | OASIS WD01 `.doc`, XACML 4.0/ACAL zips, docs.oasis-open.org listings, raw.githubusercontent.com, Maven Central metadata. |
| Local read-only (Read, `rtk rg`, Python) | SUPPORTED | xacml4j sources; artagon-uri `docs/wiki/02-type-system`. |

## 1. Facts with sources

### 1.1 ALFA standards status (fetched)

- **OASIS WD01**
  - Cover text from https://groups.oasis-open.org/higherlogic/ws/public/download/55228/alfa-for-xacml-v1.0-wd01.doc (text extracted in memory): "Abbreviated Language for Authorization Version 1.0, Working Draft 01, 10 March 2015". Technical Committee: OASIS XACML TC. Editors: Pablo Giambiagi, Srijith K. Nair, David Brossard (Axiomatics).
  - Status text: it "has not yet been voted on by the TC or [approved] as a Committee Draft". Notice: "Copyright © OASIS Open 2015. All Rights Reserved."
  - Document record: OASIS document 55228, status "Draft", submitted by Erik Rissanen on 2015-03-10 (https://groups.oasis-open.org/higherlogic/ws/public/document?document_id=55228).
  - Sections: §3 language structure (identifiers, comments, namespace, scoping, policy, policy set, targets, conditions, constants, attributes and designators, atomic values and bags, obligations and advice, operator declarations with inverses and bag overloading, precedence, and declarations of functions, combining algorithms, categories, and attributes); §4 "Compiling into XACML" (still editor notes); §5 coverage (deviations; unsupported: combiner parameters, variable definitions, attribute selectors, the XPath data type, combining-algorithm parameters); Appendix B, default identifiers.
  - It contains no formal grammar: neither "EBNF" nor "::=" appears in the text.
- **Axiomatics position:** "ALFA is not yet a part of the OASIS series of standards". Blog post of 2016-07-05, updated 2023-10-19 (https://axiomatics.com/blog/is-alfa-a-part-of-the-oasis-xacml-technical-committee-series-of-standards).
- **No OASIS publication:** the `docs.oasis-open.org/xacml/` listing fetched on 2026-10-27 has no ALFA directory.
- **ALFA 2.0 (IETF)**
  - `draft-brossard-alfa-authz-00`, "ALFA 2.0 - the Abbreviated Language for Authorization", 22 July 2024, by D. Brossard (Axiomatics), A. Clymer (Rock Solid Knowledge), T. Dimitrakos (University of Kent). The header says "Intended status: Standards Track".
  - Datatracker: expired, no stream, last updated 2025-01-23 (https://datatracker.ietf.org/doc/draft-brossard-alfa-authz/).
  - The text (https://www.ietf.org/archive/id/draft-brossard-alfa-authz-00.txt, 9,780 characters) has only Introduction, Conventions, Security, IANA, and References. It contains no syntax.

### 1.2 ALFA as used today

Source: the Axiomatics VS Code ALFA documentation (https://axiomatics.github.io/alfa-vscode-doc/, "© Axiomatics 2025"; "Copyright © 2012-2025 Axiomatics AB. All Rights Reserved."). It says the extension implements "Abbreviated Language for Authorization v. 1.0".

- **Namespaces and imports:** `namespace A { namespace B { policy p {...} } import B.p }`, and `import A.B.*`. Every declaration must sit in a namespace. A URI can override the default id: `policy p = "urn:example:policy:p" {...}`.
- **Declarations** (all standard ones are in `system.alfa`, which is copied into projects and whose `System` namespace is imported implicitly):
  - types: `type string = "http://www.w3.org/2001/XMLSchema#string"`;
  - categories: `category subjectCat = "urn:…:access-subject"`, plus `resourceCat`, `actionCat`, `environmentCat`;
  - attributes: `attribute subjectId { id = "urn:…:subject-id" type = string category = subjectCat }`. Standard attributes are in `standard-attributes.alfa`;
  - functions: `function stringEqual = "urn:…:string-equal" : string string -> boolean`. Argument kinds are a declared type, `bag[T]`, `anyAtomic`, `bag[anyAtomic]`, `anyAtomicOrBag`, and `function`; the last argument may carry a `*` repetition marker;
  - operators: `infix allowbags comm (==) = { "<function-id>" : string string -> boolean … }`. They can declare inverses, can be commutative (`comm`), and can apply to bags (`allowbags`). Precedence is fixed by the operator's first character, from loosest to tightest: `|`, `&`, `= < > $`, `@ ^`, `+ -`, `* / %`;
  - combining algorithms: `ruleCombinator permitUnlessDeny = "…"`, `policyCombinator denyOverrides = "…"`;
  - notices: `obligation myObligation1 = "…"`, `advice myAdvice1 = "…"`.
- **Policies**
  - Elements: `policyset name = "uri" { target … condition … apply <alg> <children> }`, `policy …`, and `rule name { permit|deny target … condition … on permit { obligation X { attr = expr } } on deny { advice Y {…} } }`.
  - Targets: `target clause a or b clause c`. Clauses are ANDed, `or` inside a clause, `and` inside each branch.
  - Designators: `Attributes.role[mustbepresent issuer="x"]`.
  - Typed constants: `"127.0.0.1":ipAddress`.
  - Function references: `function[stringEqual]`.
  - Unnamed rules, policies, and policy sets get generated ids. Named rules referenced inside a policy are inlined.
- **Mapping to XACML 3.0** (pages "Targets", "Conditions", "XACML file generation and naming"):

  | ALFA | XACML 3.0 output |
  | --- | --- |
  | `target clause …` | `<Target>` with one `AnyOf` per clause, `AllOf` per `or` branch, `Match` per `and` term. The compiler reorders a match to "value first" using the operator's inverse or commutativity, or reports an error. |
  | `condition expr` | `<Condition><Apply …>`. Operator overloads resolve by argument types; an `allowbags` operator becomes an any-of style call. |
  | `condition` on a policy or policy set | The compiler generates "a rule to contain the condition and a couple of extra policies". This is a documented deviation from XACML. |
  | Policy or policy set directly under a namespace | Its own XACML file. Default id `http://axiomatics.com/alfa/identifier/<qualified name>` (example `A.B.p`). |
  | Rules | Always inlined; RuleId = qualified name. |

- **Limitations** (VS Code documentation): "ALFA does not support combiner parameters or variable definition/declarations". The constant syntax cannot express the XPath data type.

### 1.3 Grammars and compilers, with licenses

| Artifact | What it is | License | Reusable by an LGPL v3 project? |
| --- | --- | --- | --- |
| OASIS WD01 (`.doc`) | Prose specification, no formal grammar | OASIS notice: copies and derivative works that "assist in its implementation" are allowed if the notice is kept (same text as the 3.0 and 4.0 notices) | Yes, as the basis for a clean-room grammar, keeping the notice (inference). |
| Axiomatics VS Code extension, Eclipse plugin, docs, `system.alfa` | Proprietary tools. The Eclipse plugin needs registration (https://www.alfa.guide/alfa-authorization-ide-plugin). | "All Rights Reserved"; extension license not found (UNVERIFIED) | No copying of text or files. Facts about the syntax only. |
| `axiomatics/policies` (https://github.com/axiomatics/policies) | Demo `.alfa` policies plus attribute files | No license file → all rights reserved | No. |
| `a2x` (https://docs.rs/a2x, v0.1.2; source https://sr.ht/~gheartsfield/a2x/) | Rust, pest parser; "ALFA 1.0 → XACML 3.0"; implements WD01 and follows the Axiomatics VS Code behavior | GPL-3.0-or-later | No code reuse. Usable only as an external black box for differential tests. |
| `davidjbrossard/alfa-authorization-language`, `Grammer/Alfa2.g4` | ANTLR 4; 99 lines; lexer tokens plus one parser rule `body: NAMESPACE;` | IETF contributions under BCP 78/79 and the TLP (CONTRIBUTING.md; code components under Simplified BSD) | Not worth reusing; a stub. |
| ANTLR 4 (https://github.com/antlr/antlr4) | Parser generator and runtime; `antlr4-runtime` 4.13.2 is the latest on Maven Central (metadata `lastUpdated` 2024-08-03) | BSD-3-Clause (`LICENSE.txt`, 2012–2022) | Yes. |
| Rock Solid Knowledge Enforcer (.NET) | Commercial ALFA engine ("ALFA … is the language Enforcer uses", https://www.identityserver.com/documentation/enforcer/alfa/IntroducingAlfa, search snippet) | Commercial (UNVERIFIED) | No. |

### 1.4 The Axiomatics domain model, or attribute dictionary (fetched)

- **Axiomatics Services Manager 7.8** (patch 7.8.4) (https://docs.axiomatics.com/axiomatics-services-manager/projects/dictionaries/attribute-dictionary/)
  - Fields: Name, Namespace (dot-separated; not part of XACML), ID (a URI; generated as namespace.name when missing), Description, Category (access-subject, resource, action, environment, or custom), Data type (17 XACML types), Constraint.
  - Constraints: enumeration for string or integer; pattern facets for string (`length`, `minLength`, `maxLength`, `pattern`, `whiteSpace`); range for integer (`min/maxInclusive`, `min/maxExclusive`).
  - Identity: ID + name + namespace + category + type. The same ID may appear in several categories.
  - Exchange formats: `attribute.ttl` (for the PAP client) and `attribute.xml` (re-importable).
- **Axiomatics Policy DevOps 26.1** (patch 26.1.7) (https://docs.axiomatics.com/axiomatics-policy-devops/using-apd/basic-workflow/create-the-dictionary/)
  - `src/authorizationDomain/attributes.yaml` is authoritative. Each key is a dotted full name with `xacmlId`, `category` (`AccessSubject`, `Resource`, `Action`, `Environment`), and `datatype` (a shorthand code from the JSON Profile).
  - `./gradlew generateAlfaDictionaryFromYaml generateJavaDictionaryFromYaml` generates `attributes.alfa` and `src/test/java/apd/Dictionary.java`.
  - Rules: attributes must be in the dictionary before ALFA, tests, or connectors use them. `id` is a reserved word in ALFA. `extraAttributeIdCheck false` relaxes the name/id rule.
- **Authorization Hub 26.1** (https://docs.axiomatics.com/authorization-hub/projects/dictionary/): per attribute, name, description, namespace, data type, category, cache profile (TTL, max items), and connector mappings. It generates `attribute-dictionary.alfa` and has a REST API (Swagger at `/api/hub-service/swagger-ui/index.html`).
- **Inference:** validation means the ALFA compiler resolves names and checks types and overloads against the declarations. I found no separate "policy against dictionary" validator in the fetched pages.

### 1.5 Baselines for "2.0, 3.0, and 4.0 models with all profiles" (fetched)

- **XACML 4.0 / ACAL**
  - Packages dated 18 Feb 2026 at https://docs.oasis-open.org/xacml/acal/: `acal-all-v1.0-csd01.zip`, `jacal-all-v1.0-csd01.zip`, `xacml-all-v4.0-csd01.zip`.
  - XACML 4.0 is "XML Representation of ACAL Version 1.0, Committee Specification Draft 01, 18 February 2026". Annex A.2 carries the same OASIS notice.
  - Machine-readable files: `acal-core-xml-v4.0-identifiers.xml` and `acal-core-json-v1.0-identifiers.json`. The XML file is a `ShortIdSet` `urn:oasis:names:tc:acal:1.0:core:identifiers` with 321 `ShortId`s (counted):
    - 8 categories (3 attribute and 5 subject categories);
    - 17 data types (including `entity`, without `xpathExpression`);
    - 17 standard attributes (9 subject, 2 resource, 3 action, 3 environment);
    - 4 status codes;
    - 7 combining algorithms;
    - 268 functions, with names such as `string-equal` and `hexBinary-one-and-only`.
  - The ShortIdSet maps names to URIs only. It does not give category or data type for attributes.
  - Key changes listed in the repository README (https://github.com/oasis-tcs/xacml-spec): Target becomes a boolean `Apply`; `PolicySet` merges into `Policy`; `NoticeExpression` replaces obligations and advice; XPath and JSONPath move to profiles; DataType is declared once per Attribute; `ShortIdSet`; `QuantifiedExpression`; `Bundle`; the `urn:oasis:names:tc:acal:` namespace.
- **XACML 3.0 profile documents** (`docs.oasis-open.org/xacml/3.0/` listing): administration and delegation, dsig, export compliance (ec-us), hierarchical resource, IPC, multiple decision, privacy, RBAC, SAML 2.0. Separate directories: combalgs (2014), dlp-nac (2015), duties (2024), dyn-attr (2022), related-entities (2024), time-extensions (2020), json-http (2019), map-authz (2015), rest (2019), saml-profile (2014).
- **XACML 2.0 profiles:** hierarchical resource, multiple resource, privacy, RBAC, SAML 2.0, dsig (`docs.oasis-open.org/xacml/2.0/`).

### 1.6 xacml4j repository facts (local, this branch)

- **Marshalling SPI**
  - `MarshallingProvider` (`xacml-core/src/main/java/org/xacml4j/v30/marshal/MarshallingProvider.java:35-81`) is discovered with `ServiceLoader` and keyed by `MediaType`. `newPolicyUnmarshaller(FunctionProvider, DecisionCombiningAlgorithmProvider)` is at `:42-57`.
  - `MediaType.of(String)` only searches the closed enum `Type` (`MediaType.java:36-42`). The constants are `XACML20_XML`, `XACML30_XML`, `XACML30_JSON`, `XACML30_PROTO` (`:44-65`).
  - `PolicyUnmarshaller extends Unmarshaller<CompositeDecisionRule>` (`PolicyUnmarshaller.java:27-29`; `Unmarshaller.java:30-33`), so it returns one root.
  - Only xacml-xml implements providers (`Xacm20MarshallingProvider.java:38-47`, `Xacm30MarshallingProvider.java:40-54`). xacml-json has request and response classes only, no policy JSON.
- **Name lookup helpers for a compiler:** `PolicyUnmarshallerSupport.lookUpFunction` (`:67`), `createRuleCombiningAlgorithm` (`:86`), `lookUpPolicyCombiningAlgorithm` (`:107`).
- **Builders to compile into**
  - `Policy.builder(String)` (`Policy.java:85`); `PolicySet.builder(String)` (`PolicySet.java:81`); `Rule.builder(String, Effect)` (`Rule.java:48`).
  - `Target`, `MatchAnyOf`, `MatchAllOf`, `Match` builders (`Target.java:56`, `MatchAnyOf.java:53`, `MatchAllOf.java:57`, `Match.java:80`).
  - `Apply.builder(FunctionSpec)` and `Apply.builder(String, Function<String, Optional<FunctionSpec>>)` (`Apply.java:214,218`).
  - `AttributeDesignator.builder` (`:65`), `AttributeSelector.builder` (`:56`).
  - `ObligationExpression.builder(id, Effect)` (`:46`), `AdviceExpression.builder` (`:65`), `AttributeAssignmentExpression.builder` (`:62`).
  - `PolicyIDReference` and `PolicySetIDReference` builders (`:46/:50`, `:48/:52`).
- **Reverse direction and type checks:** `PolicyTreeVisitor` (`policy/PolicyTreeVisitor.java:31`, 35 default visit methods). Match-time type checks with `FunctionParamSpec.isValidParamType` (`Match.java:58-74`).
- **Short names already exist**
  - `SemanticIdentifier.abbreviatedId` (`SemanticIdentifier.java:54-76`).
  - `CategoryId`: 13 constants (`CategoryId.java:43-55`). `WellKnownAttributeIds`: 20 constants (constructor at `:132`).
  - `XacmlTypes`: 19 types with short names, including `JPATH` (`:143`) and `ENTITY` (`:157`).
  - Functions: `XacmlFuncSpec.shortId` (`XacmlFuncSpec.java:46`) and `FunctionSpec.getShortId` (`FunctionSpec.java:60`). The default is the last URN segment (`FunctionSpecBuilder.java:90-98`), the same convention as ACAL ShortIdSet names. There are 275 `@XacmlFuncSpec` annotations in 25 files; 33 give an explicit `shortId`.
- **Related and Nested Entities support:** `QuantifiedExpression` (`policy/QuantifiedExpression.java:37-42`, links the related-entities csprd01) with ForAll, ForAny, Map, Select; `DynamicType` (`types/DynamicType.java:51`); `TypeAssertion`; `Entity` (`types/Entity.java:45`).
- **No ALFA or Cedar code exists:** `rtk rg` found only unrelated hits.
- **Documentation drift:** AGENTS.md names xacml-core packages `identifier`, `model`, and `schemas`. None exists under `xacml-core/src/main` on this branch.

### 1.7 ValueBox (artagon-uri)

`docs/wiki/02-type-system/00-valuebox-module-as-built.md` says, as of its 2026-09-23 source check, that `artagon-valuebox` is not in the Gradle build and fails with 695 javac errors in 76 files. Chapters 01–07 are a proposed CEL-Valhalla design. Chapter 05 covers entities and identity; chapter 06 covers refinement types, with numeric and string restrictions. Not usable as a dependency now.

## 2. Design options (ideas; each has trade-offs)

**Q1. ALFA parser technology**

- **A. ANTLR 4.13.2 grammar in a new `xacml-alfa` module.**
  - The grammar lives at `src/main/antlr4/org/xacml4j/alfa/syntax/Alfa.g4`, built with `antlr4-maven-plugin`.
  - User-defined `infix` operators: the lexer emits operator tokens classified by their first character; a left-recursive expression rule with `<assoc=right>` reproduces the fixed precedence.
  - Pros: the grammar is readable documentation; error recovery and line/column positions come built in; other targets later (an LSP or TypeScript); BSD-3.
  - Cons: a runtime dependency, generated code, and operator tokenization needs care.
- **B. Hand-written recursive descent with a Pratt parser for expressions.**
  - Pros: no dependency; the most natural fit for user-declared operators; full control of messages.
  - Cons: more code to own; no declarative grammar.
- **C. Xtext, Parboiled, JavaCC/CongoCC.**
  - Xtext is EPL, which is weak copyleft and not GPL-compatible without a secondary licence; it also pulls in a heavy runtime.
  - JavaCC/CongoCC: viable, but less tooling and fewer users (license not checked).
- **D. Do not do it.** Authors use Axiomatics' tools to produce XACML 3.0 XML, and xacml4j imports the XML as it does today. Zero cost, but it depends on a proprietary tool chain and gives no round trip.

**Q2. Compile target and round trip**

- **A. ALFA AST → semantic checks → the xacml4j builders (1.6).** XML output then comes from the existing marshaller.
  - Pros: one model; reuses `Apply` and `Match` type checks and the provider lookups.
  - Cons: compiler errors must be mapped from builder exceptions back to spans.
- **B. ALFA → XACML 3.0 XML text → the existing unmarshaller** (the a2x and Axiomatics approach).
  - Pros: easy golden-file comparison with other compilers.
  - Cons: double work at load time; source positions are lost.
- **C. Reverse direction: model → ALFA through `PolicyTreeVisitor`, with an explicit coverage report.** VariableDefinition, AttributeSelector, combiner parameters, the XPath type, `PolicyDefaults`, `Description`, quantified expressions, and the entity type do not exist in ALFA. Choose one:
  - reject them with diagnostics;
  - inline variables (evaluation-count difference only);
  - an opt-in "xacml4j ALFA extensions" dialect. This diverges from Axiomatics.
- **D. Do not do the reverse direction.** ALFA becomes import-only.

**Q3. Error reporting with positions**

- **A. Spans carried in the ALFA layer only.**
  - Types: `SourceSpan(path, startLine, startColumn, endLine, endColumn)` and `Diagnostic(code, Severity, message, SourceSpan, List<SourceSpan> related)`, with codes `ALFA-SYN-nnn` and `ALFA-SEM-nnn` and "did you mean" from the symbol tables.
  - `AlfaCompilation.sourceMap()` maps XACML ids to spans, so decision traces and `PolicyIdentifierList` lead back to ALFA lines. No change to the policy model.
- **B. Add optional `SourceLocation` metadata to `PolicyElement`.** Simpler lookups, but a public API change in core (BREAKING-adjacent).
- **Output:** human `file:line:col`, plus SARIF 2.1.0 JSON for CI (SARIF facts not fetched).

**Q4. Where the dictionary (authorization model) lives**

- **A. A new `xacml-model` module, format-neutral.**
  - Package `org.xacml4j.model`. Records `AuthorizationModel`, `AttributeDef(qualifiedName, URI id, CategoryId, ValueType, Optional issuer, Cardinality, Optional<Constraint>, Conformance, profile, description)`, `CategoryDef`, `DataTypeDef`, `FunctionDef` (signature taken from `FunctionSpec`), `CombiningAlgorithmDef`, `NoticeDef`, `EntityTypeDef`. Sealed `Constraint` with `Enumeration`, `Pattern`, `Range` (the ASM model).
  - Packs in `org.xacml4j.model.packs`: `xacml-2.0`, `xacml-3.0`, `acal-1.0-csd01`, plus one per profile.
  - Seeds: generated from `XacmlTypes`, `CategoryId`, `WellKnownAttributeIds`, `FunctionProvider`, the algorithm providers, and the ACAL `ShortIdSet`.
  - I/O in `org.xacml4j.model.io`: JSON (Gson is already used) with a JSON Schema; optional YAML import compatible with APD's `attributes.yaml`.
- **B. Put it into xacml-core.** No new module, but it grows core and couples authoring metadata to the runtime.
- **C. Build it on ValueBox or CEL types.** Richer refinement and entity types, but the module does not compile; defer.
- **D. Do not do it.** The dictionary is just ALFA declaration files compiled per project, as Axiomatics does.

**Q5. SPI integration**

- **A. An `AlfaMarshallingProvider` with its own non-enum `MediaType`, plus a fix so `MediaType.of` also consults discovered providers.**
- **B. Add an `ALFA` constant to `MediaType.Type`.**
- **C. A separate `AlfaCompiler` API outside the SPI.**
- Either way a multi-root result is needed: `AlfaCompilation { List<CompositeDecisionRule> roots; Map<String, CompositeDecisionRule> byId; List<Diagnostic> diagnostics; sourceMap }`. ALFA yields one file per top-level policy, and WD01 §4 only says a vendor "MAY mark a policy as main".

**Q6. Scope of the 4.0 model**

- **A. Ship 2.0 and 3.0 packs now, and 4.0 as a pack marked "csd01".**
- **B. Wait for an ACAL Committee Specification.**
- **C. Target 4.0 only.** Not possible yet: the engine is 3.0-shaped. 4.0 targets are `Apply`, and `Policy` absorbs `PolicySet`.

## 3. Recommendation (idea)

1. **Now:** no code. Record an OpenSpec proposal for "authorization model and ALFA front end" as future work. Keep XACML XML as the interchange format.
2. **Phase 1, `xacml-model`** (Q4-A, Q6-A):
   - The API and packs generated at build time from xacml4j's own registries, so names and signatures always match the engine.
   - Import of the ACAL `ShortIdSet`, keeping the OASIS notice.
   - JSON format and schema first; YAML import later.
   - Optional "policy lint" (a warning for designators missing from the model) and optional, opt-in request validation in xacml-pdp. No decision changes by default.
3. **Phase 2, `xacml-alfa`** (Q1-A, Q2-A, Q3-A, Q5-A plus C):
   - A clean-room ANTLR grammar from WD01 and the documented behavior. Never copy Axiomatics files.
   - Our own generated `system.alfa`. ALFA uses camelCase names (for example `stringEqual`); generate them deterministically from the existing short ids (`string-equal` → `stringEqual`). Do not change `getShortId()`.
   - A configurable default id prefix, neutral by default, with an Axiomatics-compatible mode.
   - Golden tests: our ALFA samples → normalized XACML.
   - Optional differential runs against a2x as a black box, which is fine for a GPL tool that is not linked or distributed.
   - Round-trip property tests on the 3.0 conformance policies for the subset ALFA supports.
4. **Phase 3:** the reverse printer (Q2-C, with a coverage report) and an optional language server.

**Why:** ALFA has no stable standard, so the durable asset is the format-neutral model. The same model can feed ALFA, a Cedar schema (BR lanes), and ACAL. ANTLR gives positions and recovery cheaply under a compatible license. Keeping everything in new modules and opt-in protects decisions and the public API.

## 4. Risks and what would be BREAKING

- **Not breaking:** new modules (`xacml-model`, `xacml-alfa`), new APIs (`AlfaCompiler`, a multi-root compilation, a non-enum `MediaType`), and opt-in lint or validation.
- **Breaking or near-breaking:**
  - Adding a constant to `MediaType.Type`: exhaustive `switch` code in clients fails to recompile.
  - Changing the `PolicyUnmarshaller` return type.
  - Making dictionary validation mandatory at policy load or in the PDP: it rejects today's policies or changes decisions.
  - Moving `CategoryId` or `WellKnownAttributeIds` to dictionary-backed instances: affects equality and serialization.
  - Renaming function short ids to the ALFA camelCase form.
  - Adding source metadata to `PolicyElement` (Q3-B).
- **Dialect drift:** the target is a moving proprietary behavior. WD01 is incomplete, ALFA 2.0 expired, and there is no ALFA conformance suite.
- **Lossy round trip:** variables, selectors, combiner parameters, XPath, entity, and quantified expressions do not exist in ALFA. ALFA's generated wrappers for conditions on policies do not map back to the same source.
- **Legal:** Axiomatics material is all rights reserved. Write grammar and system files clean-room. The a2x code is GPL. The trademark status of "ALFA" is UNVERIFIED (Axiomatics® is a registered trademark per the Notices page). Name the module neutrally ("ALFA-compatible").
- **Draft churn:** XACML 4.0/ACAL is CSD01 (a draft); its identifiers may change.
- **Maintenance:** a grammar, a type checker, and a printer form a second front end to keep in step with the engine.

## 5. Open questions for the maintainer

1. Which ALFA dialect: OASIS WD01, current Axiomatics behavior, or the ALFA 2.0 draft? Do we accept xacml4j-only extensions (variables, entities, quantified expressions)?
2. Is a new runtime dependency (ANTLR 4.13.2, BSD-3) acceptable in a new module, or do you prefer a hand-written parser?
3. Default generated-id scheme: neutral (`urn:xacml4j:alfa:<qualified name>`?) or Axiomatics-compatible (`http://axiomatics.com/alfa/identifier/<qualified name>`)?
4. Dictionary file format priority: ALFA declarations, JSON plus schema, APD-style YAML, or several? Should the PDP ever validate requests against it, opt-in only?
5. Should the 4.0 pack ship now from CSD01, or wait for a Committee Specification?
6. Should the model be shared with the Cedar front end (entity types, actions)? Should ValueBox refinement types become the constraint system later?
7. Should AGENTS.md be corrected for the missing `identifier`, `model`, and `schemas` packages?

## Verification run (read-only)

- Read `xacml-core` marshal, policy, function, and type sources, with line numbers cited above.
- Read ValueBox chapter 00 and the type-system README.
- Fetched Axiomatics documentation pages, OASIS document pages, the WD01 `.doc` and the ACAL zips (in memory), IETF datatracker and draft text, docs.rs a2x, and anonymous GitHub pages and raw files (`Alfa2.g4`, CONTRIBUTING.md, ANTLR LICENSE).
- No file written, no Maven, no git writes, no GitHub connector or `gh`.

## Residual risk

- The Axiomatics VS Code extension license was not found.
- The Rock Solid Knowledge Enforcer license was not checked.
- The WD01 text was extracted heuristically from a binary `.doc`, so section text may be incomplete.
- ALFA syntax facts come from the current Axiomatics documentation and may differ from WD01.
- Other ALFA parsers in other ecosystems may exist: one DuckDuckGo search failed and Tavily was rate-limited.
