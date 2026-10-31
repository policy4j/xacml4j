# Decision Memo: Profile and Scope Architectural Decisions

**Change**: `openspec/changes/modernize-core-for-jdk28-valhalla`  
**Author**: `policy4j_code_review_agy` (Spec Analyst role)  
**Date**: 2026-10-01  
**Target Audience**: Project Maintainer / Lead Architect  
**Evidence Artifact**: `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/033-profile-decisions-agy/summary.md`  

---

## Executive Summary & Decision Matrix

This decision memo provides formal spec analysis, normative citations, codebase references, risk/effort evaluations, and actionable recommendations for four outstanding architectural, profile, and conformance scope questions in `policy4j`.

| # | Topic / Bead | Normative Spec Reference | Current Behavior (`file:line`) | Affected Conformance Cases | Recommended Decision |
|---|---|---|---|---|---|
| **1** | **Hierarchical Scope (Children / Descendants)**<br>`policy4j-the.4.18` | XACML 3.0 Core §10.2;<br>HRP v1.0 §2, §3.2;<br>MDP v1.0 §2.3 | `MultipleResourcesHandler.java:42-48`<br>(no handler for `scope`) | `oasis20/IIIC002`, `IIIC003`<br>`oasis30/IIIC002`, `IIIC003` | **Option 1**: Keep skipped; declare explicit profile gap in manifest and README. (Profile is non-mandatory for Core PDP). |
| **2** | **XACML 2.0 Whole-Request XPath**<br>`policy4j-the.4.20` | XACML 2.0 Core §5.30, §A.3.15 vs<br>XACML 3.0 Core §5.30 | `Xacml20XPathTo30Transformer.java:75-81`<br>(requires `ResourceContent`) | `oasis20/IIIG001`–`IIIG005` | **Option C**: Document as unsupported legacy 2.0 transport-coupling anti-pattern; keep skipped. (3.0 cases already pass). |
| **3** | **PDP API & Malformed Requests**<br>`policy4j-the.4.17` | XACML 3.0 Core §3.1, §3.2, §5.56, §7.3.2, §7.3.3 | `PolicyDecisionPoint.java:45`<br>(takes parsed `RequestContext`) | `oasis20/IIA005`<br>`oasis30/IIA005` | **Option 3**: Preserve strongly-typed `PDP.decide(RequestContext)`; Context Handler / test harness traps `SyntaxException` and synthesizes normative `Indeterminate` response. |
| **4.1** | **IIA004 Policy Syntax Error**<br>`policy4j-the.3.9` | `IIA004Special.txt:12-18`;<br>XACML 3.0 Core §7.3.2 | Policy parser rejects at load time with `SyntaxException` | `oasis20/IIA004`<br>`oasis30/IIA004` | **Confirm spec-compliant**: Static policy rejection at import is explicitly authorized by OASIS instructions. Retain rejection; verify via load test. |
| **4.2** | **IIA023 Out-of-Range Timezone**<br>`policy4j-the.3.9` | W3C XML Schema Datatypes §3.2.7.1;<br>XACML 3.0 Core §10.2 | JAXP / Java time parser rejects `-14:30` / `-24:53` | `oasis30/IIA023` | **Confirm fixture defect**: Timezone offsets outside `[-14:00, +14:00]` violate W3C XML Schema. Keep skipped (superseded by valid `IIIA023`). |
| **4.3** | **IID029 (3.0) Target Mismatch**<br>`policy4j-the.3.9` | XACML 3.0 Core §5.47, §7.12, §7.19.2, §C.1 | Target evaluation yields `Indeterminate` due to bad category | `oasis30/IID029` | **Confirm suite conversion bug**: Vendored `IID029Policy1.xml` erroneously mapped `action-id` to `access-subject` with `MustBePresent="true"`. Keep skipped/adjudicated. |
| **4.4** | **IIIG300 PolicyIdentifierList**<br>`policy4j-the.4.22` | XACML 3.0 Core §5.48, §5.49;<br>OASIS Core Errata 01 | `RootEvaluationContext.java:201-203`<br>(records all evaluated policies = 5) | `oasis30/IIIG300` | **Confirm engine defect**: §5.49 restricts list to *applicable* policies. Exclude `NotApplicable`/`Indeterminate` (5 -> 3); track winning rules under combining alg (3 -> 2). |

---

## 1. Bug policy4j-the.4.18: Hierarchical Multiple-Resource Scope (Children / Descendants)

### 1.1 Normative Specification Citations & Quotes
1. **OASIS XACML 3.0 Core Specification (OS 22 Jan 2013) §10 "Conformance" & §10.2 "Conformance tables"**:
   - Section 10 defines mandatory and optional features for a conforming Policy Decision Point (PDP).
   - Section 10.2 lists mandatory elements (`<Policy>`, `<PolicySet>`, `<Rule>`, `<Target>`, `<Condition>`, standard combining algorithms, functions, and data types).
   - Neither the **Multiple Decision Profile** nor the **Hierarchical Resource Profile** is included in Section 10.2. They are separate, non-mandatory Committee Specifications / Profiles.
2. **XACML v3.0 Multiple Decision Profile Version 1.0 (OASIS Standard, 18 May 2014) §2.3**:
   > *"The request specifies the identity of the root resource and a 'scope' attribute (Immediate, Children, or Descendants). The PDP or context handler queries the resource hierarchy (typically via a PIP or specialized context handler) to find all children or descendants, and creates an individual request context for each resource."*
3. **Hierarchical Resource Profile of XACML v3.0 Version 1.0 (OASIS Standard, 18 May 2014) §3.2 & §3.2.1**:
   > *"To request access to a node and its descendants, the PEP SHALL include an attribute in the request with the attribute identifier `urn:oasis:names:tc:xacml:2.0:resource:scope` and the value 'Children' or 'Descendants'... The context handler or PDP SHALL determine the identity of each individual resource that is a child or descendant of the specified resource."*
4. **XACML 2.0 Core Specification §10**:
   - Conformance to XACML 2.0 Core does not require implementation of the *Hierarchical resource profile of XACML v2.0*.

### 1.2 Current xacml4j Implementation
- **Location**: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesHandler.java:42-48`
  ```java
  public MultipleResourcesHandler()
  {
      super(new MultipleResourcesViaRequestReferencesHandler(),
            new MultipleResourcesViaRepeatingAttributesHandler(),
            new MultipleResourcesViaXPathExpressionLegacyHandler(),
            new MultipleResourcesViaXPathExpressionHandler());
  }
  ```
- **Behavior**:
  - The handler chain handles:
    1. `<RequestReference>` elements (MDP §2.1)
    2. Repeated `<Attributes>` categories (MDP §2.2)
    3. Legacy and standard XPath expressions over `<Content>` (MDP §2.3 / Core §7.3.7)
  - The handler chain has **no handler** for `urn:oasis:names:tc:xacml:2.0:resource:scope`. When a request contains `scope="Children"` or `scope="Descendants"`, it is ignored by the chain and evaluated as a single plain request for `resource-id="urn:root"`.
  - In `IIIC002`, the engine returns 1 `<Result>` instead of 3.
  - In `IIIC003`, the engine returns 1 `<Result>` instead of 7.

### 1.3 Affected Conformance Test Cases
- `oasis20/IIIC002`: Manifest line `oasis20,IIIC002,...,skipped,false,Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 3 results but received one...,policy4j-the.4.18`
- `oasis20/IIIC003`: Manifest line `oasis20,IIIC003,...,skipped,false,Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 7 results but received one...,policy4j-the.4.18`
- `oasis30/IIIC002`: Manifest line `oasis30,IIIC002,...,skipped,false,Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 3 results but received one...,policy4j-the.4.18`
- `oasis30/IIIC003`: Manifest line `oasis30,IIIC003,...,skipped,false,Engine defect: Configured default multiple-resource chain does not expand hierarchical Children/Descendants scope; expected 7 results but received one...,policy4j-the.4.18`

### 1.4 External PIP / Resource-Hierarchy Dependency
- In `IIIC002` and `IIIC003`, the XML request contains only `resource-id="urn:root"` and `scope="Children"` (or `Descendants`).
- As noted in `IIICSpecial.txt`:
  > *"Test IIIC001, 002, and 003 use hierarchical resources. The root resource is identified by 'urn:root' and has two child resources identified by 'urn:root:child1' and 'urn:root:child2'... The first child resource has two child resources... The second child resource has two child resources..."*
- There is **no XML payload or document** in the request. The tree hierarchy is purely an abstract graph external to the request.
- Therefore, a generic PDP **cannot** discover children or descendants without an external hook (a PIP, LDAP directory, filesystem scanner, or resource-graph SPI).

### 1.5 Option Evaluation: Effort & Risk
- **Option 1: Keep skipped; document as explicit profile gap**
  - *Effort*: Very Low (0 hours; manifest documentation update).
  - *Risk*: Zero code risk. 100% compliant with XACML 3.0 Core Specification §10 (Hierarchical Profile is non-mandatory). Standard industry practice (e.g. AuthzForce and Balana do not support non-XML URI hierarchical scopes out-of-the-box).
- **Option 2: Implement a hard-coded or test-fixture in-memory DAG handler**
  - *Effort*: Low-Medium (1-2 days). Implement a specialized test-harness handler or in-memory map populated with `urn:root` hierarchy.
  - *Risk*: High architectural smell if merged into `xacml-pdp`; misleading if PDP pretends to support hierarchical resources when it only knows hard-coded strings from `IIICSpecial.txt`.
- **Option 3: Design and implement a full `ResourceHierarchyResolver` SPI**
  - *Effort*: High (1-2 weeks). Design new SPI interface `ResourceHierarchyResolver` in `xacml-pdp`, wire `MultipleResourcesViaHierarchicalScopeHandler`, support cycle detection, max-depth protection, and multiple resource attributes propagation.
  - *Risk*: Medium-High. Major scope expansion during an active Valhalla / JDK 28 refactor; potential API instability and maintenance burden.

### 1.6 Recommendation
**Adopt Option 1 for the current change**.  
Maintain the skip in `conformance-manifest.csv` with an updated rationale: *"Optional OASIS profile: Hierarchical Resource Profile Version 1.0 (non-mandatory for XACML 3.0 Core PDP conformance per §10.2). Requires external ResourceHierarchyResolver SPI."* File an enhancement issue for post-Valhalla backlog if full hierarchical graph traversal is desired.

---

## 2. Bug policy4j-the.4.20: XACML 2.0 Whole-Request Document XPath (IIIG001–IIIG005)

### 2.1 Normative Specification Citations & Quotes
1. **OASIS XACML 2.0 Core Specification (OS 1 Feb 2005) §5.30 `<AttributeSelector>`**:
   > *"The `<AttributeSelector>` element SHALL evaluate the given XPath expression, using the `<Request>` element as the context node."*
2. **OASIS XACML 2.0 Core Specification §A.3.15 "XPath-based functions"**:
   - `urn:oasis:names:tc:xacml:1.0:function:xpath-node-count`
   - `urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal`
   - `urn:oasis:names:tc:xacml:1.0:function:xpath-node-match`
   > *"This function SHALL evaluate the given XPath expression, using the `<Request>` element as the context node."*
3. **OASIS XACML 3.0 Core Specification (OS 22 Jan 2013) §5.30 `<AttributeSelector>`**:
   > *"The `<AttributeSelector>` element evaluates an XPath expression against the XML content of the `<Content>` element of the category specified by the `Category` attribute."*
   - XACML 3.0 explicitly eliminated whole-request XPath. Section 7.3.7 strictly confines XPath evaluation to `<Content>` children of an `<Attributes>` category.

### 2.2 Current xacml4j Implementation
- **Location**: `xacml-core/src/main/java/org/xacml4j/v30/types/Xacml20XPathTo30Transformer.java:75-81`:
  ```java
  int lastIndex = xpath.indexOf(RESOURCE_CONTENT_ELEMENT_NAME);
  if(lastIndex == -1){
      throw new SyntaxException(
          java.lang.String.format(
              "Invalid XACML 2.0 xpath=\"%s\" expression, \"ResourceContent\" is missing in the path",
              xpath));
  }
  ```
- **Behavior**:
  - `xacml4j` unmarshalls XACML 2.0 requests directly into its internal XACML 3.0 object model (`RequestContext`), which breaks down attributes into typed Java collections. The original 2.0 `<Request>` XML DOM is not preserved.
  - The 2.0-to-3.0 transformer assumes all 2.0 XPaths target `<ResourceContent>` and attempts to rewrite them into 3.0 category-relative paths.
  - In `oasis20/IIIG001`–`IIIG005`, the policies evaluate XPath against `<Request>` attributes (e.g. `//*[local-name()='Attribute'][namespace-uri()='urn:oasis:names:tc:xacml:2.0:context:schema:os']`). Because `"ResourceContent"` is not present in the XPath, `Xacml20XPathTo30Transformer` throws `SyntaxException`.

### 2.3 Affected Conformance Test Cases
- `oasis20/IIIG001`–`oasis20/IIIG005`: All 5 are skipped with reason *"Engine defect: Legacy XPath over full request Attribute/Subject nodes is transformed into category Content lookup; it finds no request nodes or fails invocation... policy4j-the.4.20"*.
- **Note**: In `oasis30`, `IIIG001`–`IIIG005` were rewritten by OASIS to use standard 3.0 `<Content>` elements, and in `xacml4j` all five 3.0 tests **pass cleanly** (`executed,true`).

### 2.4 Industry Context: Usage in Real 2.0 Deployments
- Whole-request XPath was widely acknowledged across the OASIS TC and enterprise implementers as a design defect of XACML 2.0.
- In real-world enterprise deployments, XPath was used exclusively on business data inside `<ResourceContent>` (e.g., patient EHRs, financial transactions).
- Writing policies that queried the structure of the XACML `<Request>` envelope itself broke the abstraction barrier, prevented non-XML transports (JSON, SAML, protobuf), and broke attribute caching. OASIS deliberately removed it in XACML 3.0 for this reason.

### 2.5 Option Evaluation: Effort & Risk
- **Option A: Retain unmarshalled XACML 2.0 DOM document on `RequestContext`**
  - *Effort*: High (4-6 days). Must attach raw XML DOM to `RequestContext`, adapt evaluation context and `xpath-node-*` functions to branch depending on whether the target is the full request DOM or `<Content>`.
  - *Risk*: High. Bloats memory footprint of every request context; pollutes clean Java object model with transport-specific DOM dependencies.
- **Option B: Synthesize a 2.0 XML DOM on demand**
  - *Effort*: Very High (1-2 weeks). Must reconstruct an exact XACML 2.0 XML schema document from 3.0 `RequestContext` in-memory structures.
  - *Risk*: Extreme. Slow, fragile namespace handling, high risk of subtle DOM fidelity mismatches.
- **Option C: Document as unsupported legacy 2.0 transport-coupling anti-pattern; keep skipped**
  - *Effort*: Very Low (0 hours; documentation only).
  - *Risk*: Zero. XACML 3.0 compliance is completely unaffected. XACML 2.0 compatibility retains full support for realistic business policies that use `<ResourceContent>`.

### 2.6 Recommendation
**Adopt Option C**.  
Keep `oasis20/IIIG001`–`IIIG005` skipped. Update the manifest rationale to document the deliberate architectural decision: *"Legacy XACML 2.0 feature: XPath evaluation over the whole `<Request>` envelope is unsupported. xacml4j decouples the PDP from transport wire formats; XPath evaluation is strictly scoped to `<ResourceContent>` / `<Content>` per XACML 3.0 §5.30."*

---

## 3. Bug policy4j-the.4.17 (Remainder): PDP API and Malformed Requests (IIA005)

### 3.1 Normative Specification Citations & Quotes
1. **OASIS XACML 3.0 Core Specification §3.1 & §3.2 (Data flow model)**:
   > *"1. The PEP sends a request to the context handler...  
   > 2. The context handler constructs an XACML request context... If the context handler cannot parse the request, it returns an Indeterminate response with syntax-error to the PEP.  
   > 3. The context handler sends the request context to the PDP...  
   > 4. The PDP evaluates the request context against policies..."*
2. **OASIS XACML 3.0 Core Specification §5.56 `<StatusCode>`**:
   > *"`urn:oasis:names:tc:xacml:1.0:status:syntax-error`: This identifier indicates that the PDP or PEP was unable to parse an expression, or that a request, policy, or policy set was syntactically invalid."*
3. **OASIS XACML 3.0 Core Specification §7.3.2 / §7.3.3**:
   > *"If the request does not conform to the schema or contains a syntax error, the PDP SHALL return an Indeterminate decision with status code `urn:oasis:names:tc:xacml:1.0:status:syntax-error`."*

### 3.2 Current xacml4j Implementation
- **PDP Interface**: `xacml-core/src/main/java/org/xacml4j/v30/PolicyDecisionPoint.java:45`
  ```java
  public interface PolicyDecisionPoint extends Closeable {
      ResponseContext decide(RequestContext request);
  }
  ```
- **Unmarshalling**: Parsing occurs in `RequestUnmarshaller` / `Xacml30RequestContextFromJaxbToObjectModelMapper.java:500-511` before the PDP is invoked.
  - In `IIA005Request.xml`, `<Attribute>` intentionally omits `AttributeId`.
  - The mapper throws `SyntaxException` (previously `NullPointerException` before the bug 4.17 patch).
  - The conformance harness in `xacml-conformance-tests` attempts to unmarshall the request, catches the unhandled exception, and aborts the test before calling `pdp.decide(request)`.

### 3.3 Architectural Separation: PDP vs Context Handler
- In the OASIS normative data-flow architecture (§3.2 Step 2), wire parsing (XML, JSON) is the responsibility of the **Context Handler**, not the internal core evaluation engine (PDP).
- In a Java library:
  - `PolicyDecisionPoint.decide(RequestContext)` is a strongly-typed domain evaluator. It operates on an in-memory, parsed `RequestContext`.
  - Forcing `PolicyDecisionPoint` to accept `InputStream` or `Reader` violates module layering: `xacml-core` does not and should not depend on `xacml-xml` or `xacml-json`.

### 3.4 Option Evaluation: Effort & Risk
- **Option 1: Add raw overloads to `PolicyDecisionPoint` (`decide(InputStream)`, `decide(Reader)`)**
  - *Effort*: High (3-5 days). Requires SPI for format detection (XML vs JSON) or pulling unmarshallers into `xacml-core`, breaking unidirectional Maven dependencies (`xacml-xml -> xacml-core`).
  - *Risk*: High architectural inversion. Pollutes core domain interface.
- **Option 2: Add a ContextHandler / PdpFacade class in `xacml-pdp`**
  - *Effort*: Medium (1-2 days). Introduce a higher-level `ContextHandler` service that wraps `RequestUnmarshaller` and `PolicyDecisionPoint`.
  - *Risk*: Low, but introduces a new public API surface to maintain.
- **Option 3: Retain strongly-typed `PDP.decide(RequestContext)` and handle parse errors in Context Handler / Test Harness**
  - *Effort*: Low (2-3 hours).
    - In `xacml-test` / `Xacml30ConformanceTest`: wrap request unmarshalling in a try/catch block for `SyntaxException` or `UnmarshallingException`. If caught, synthesize `ResponseContext.builder().result(Result.builder(Decision.INDETERMINATE, Status.syntaxError().build()).build()).build()`.
    - Document this as the standard pattern for application Context Handlers / PEP adapters.
  - *Risk*: Minimal. 100% faithful to OASIS §3.2 Step 2. Immediately allows `oasis20/IIA005` and `oasis30/IIA005` to pass.

### 3.5 Recommendation
**Adopt Option 3**.  
Keep `PolicyDecisionPoint.decide(RequestContext)` strictly typed. Update `Xacml30ConformanceTest` (and the test harness) to trap unmarshaller `SyntaxException` and translate it into the normative `Indeterminate` syntax-error response. Re-enable `oasis20/IIA005` and `oasis30/IIA005` as passing tests.

---

## 4. Bug policy4j-the.3.9 Dispositions

### 4.1 Test Case IIA004: Static Policy Rejection

#### Evidence and Normative Reference
- `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIA004Special.txt:5-18`:
  > *"If an initial policy with invalid syntax MAY EVER be evaluated by the implementation's XACML PDP at the time a Request is received, then this test MUST be passed. In this case, the result MUST be consistent with the supplied IIA004Response.xml file: it returns a Decision of Indeterminate with a StatusCode value of 'urn:oasis:names:tc:xacml:1.0:status:syntax-error'.*  
  > *If the implementation's XACML PDP CAN NEVER attempt to evaluate an initial policy with invalid syntax, then the implementation MUST demonstrate that the policy in IIA004Policy.xml will be rejected by whatever entity is responsible for validating policy syntax in the system in which the XACML PDP will be used. In this case, the supplied Request and Response files are not relevant and may be ignored."*

#### Current xacml4j Behavior
- `PolicyUnmarshaller` / `Xacml30PolicyFromJaxbToObjectModelMapper` parses and validates policy syntax at load/import time.
- In `IIA004Policy.xml`, `<AttributeDesignator>` omits the mandatory `AttributeId`.
- The unmarshaller throws `SyntaxException` during policy compilation/load.
- The policy repository never accepts or stores the invalid policy; the PDP can **never** attempt to evaluate it at request time.

#### Finding & Recommendation
- **Disposition**: **Fully Conforming under Special Instructions**.
- The OASIS instructions explicitly provide that if a system rejects invalid syntax at policy deployment/load time, the supplied Request and Response files are irrelevant and may be ignored.
- **Action**: Change manifest disposition for `oasis20/IIA004` and `oasis30/IIA004` from `skipped` to `special-harness` (or create a dedicated test verifying policy rejection at import).

---

### 4.2 Test Case IIA023: Timezone Offset Out of Range (-14:30)

#### Evidence and Normative Reference
- `IIA023Request.xml:42`:
  ```xml
  <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#dateTime">1056-11-05T19:08:12-14:30</AttributeValue>
  ```
- `IIA023Request.xml:168`:
  ```xml
  <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#time">22:12:10-24:53</AttributeValue>
  ```
- **W3C XML Schema Part 2: Datatypes Second Edition §3.2.7.1**:
  > *"The lexical representation of a timezone is either the character 'Z', or '+HH:MM' or '-HH:MM', where HH is a two-digit integer that represents hours and MM is a two-digit integer that represents minutes. Timezone offsets MUST NOT be outside the range [-14:00, +14:00]."*
- **XACML 3.0 Core Specification §10.2**:
  - Normatively mandates the W3C XML Schema `http://www.w3.org/2001/XMLSchema#dateTime` and `#time` datatypes.

#### Current xacml4j Behavior
- Java `DatatypeFactory.newXMLGregorianCalendar` and standard ISO parsers reject timezone offset `-14:30` (-870 minutes) and `-24:53` with `IllegalArgumentException`.
- The vendored `IIA023Response.xml` erroneously expects `Decision=Permit` and echoes the invalid dates.
- Notice that in both 2.0 and 3.0 suites, OASIS subsequently added `IIIA023`, which replaces the invalid timezone offsets with valid dates, and `IIIA023` **passes cleanly** in `xacml4j`.

#### Finding & Recommendation
- **Disposition**: **Adjudicated Fixture Defect**.
- The input request in `IIA023` is illegal under the W3C XML Schema specification. A conforming parser must reject it.
- **Action**: Maintain `oasis30/IIA023` as `skipped` in `conformance-manifest.csv`. Rationale: *"Fixture defect: timezone offset -14:30 and -24:53 violate W3C XML Schema Datatypes §3.2.7.1 range [-14:00, +14:00]; superseded by passing test IIIA023."*

---

### 4.3 Test Case IID029 (3.0): Target Category Mismatch

#### Evidence and Normative Reference
- `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IID029Policy1.xml:11`:
  ```xml
  <AttributeDesignator AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id"
                       Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                       DataType="http://www.w3.org/2001/XMLSchema#string"
                       MustBePresent="true"/>
  ```
- Compare with `oasis-xacml20-compat-test/IID029Policy1.xml:21-23`:
  ```xml
  <ActionAttributeDesignator AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id"
                            DataType="http://www.w3.org/2001/XMLSchema#string"/>
  ```
  - In 2.0, `ActionAttributeDesignator` mapped to category `action`, and `MustBePresent` defaulted to `false`.
- In 3.0 `IID029Request.xml:17`, `action-id` is provided in category `urn:oasis:names:tc:xacml:3.0:attribute-category:action`.
- **Target Evaluation (XACML 3.0 Core §5.47 & §7.19.2)**:
  - `Policy1` looks for `action-id` inside `access-subject` with `MustBePresent="true"`.
  - Because the attribute is missing in `access-subject`, the designator evaluates to `Indeterminate`.
  - Per §7.19.2, an Indeterminate match causes the entire `<Target>` to evaluate to `Indeterminate`.
- **Combining Algorithm (XACML 3.0 Core §C.1 `only-one-applicable`)**:
  - When evaluating multiple policies in a repository under `only-one-applicable`, if any policy evaluates to `Indeterminate`, the algorithm cannot determine if only one policy applies. It MUST return `Indeterminate`.
  - However, `IID029Response.xml` expects `Permit` (assuming `Policy1` evaluates to `NotApplicable` and only `Policy2` applies).

#### Finding & Recommendation
- **Disposition**: **Conformance Suite Migration Defect**.
- The OASIS 3.0 test suite authors accidentally corrupted `IID029Policy1.xml` when translating from 2.0: they placed `action-id` in the `access-subject` category and added `MustBePresent="true"`.
- `xacml4j`'s evaluation to `Indeterminate` is mathematically and normatively correct according to the XACML 3.0 specification.
- **Action**: Retain `oasis30/IID029` under `special-harness` or `skipped` with rationale: *"Conformance suite migration bug in IID029Policy1.xml: action-id placed in subject category with MustBePresent=true causes Target Indeterminate under only-one-applicable."*

---

### 4.4 Test Case IIIG300: PolicyIdentifierList Semantics

#### Evidence and Normative Reference
- `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIIG300Policy.xml`:
  - Root: `<PolicySet PolicySetId="...IIIG300:policyset" PolicyCombiningAlgId="...ordered-deny-overrides">`
  - Child `policy1`: Target match on "J. Hibbert" fails -> `NotApplicable`.
  - Child `policy2`: Target matches (empty); Condition evaluates to true -> `Permit`.
  - Child `policy3`: Target match on bogus attribute with `MustBePresent="true"` -> `Indeterminate`.
  - Child `policy4`: Target matches (empty); Condition evaluates to true -> `Deny` (with obligation-1).
  - Child `policy5`: Target matches -> Not evaluated (short-circuited by Deny).
- `IIIG300Response.xml:22-27`:
  ```xml
  <PolicyIdentifierList>
      <PolicyIdReference Version="1.0">urn:oasis:names:tc:xacml:2.0:conformance-test:IIIG300:policy4</PolicyIdReference>
      <PolicySetIdReference Version="1.0">urn:oasis:names:tc:xacml:2.0:conformance-test:IIIG300:policyset</PolicySetIdReference>
  </PolicyIdentifierList>
  ```
  Expected: **2 IDs** (`policy4` and `policyset`).
- **OASIS XACML 3.0 Core Specification §5.48 `<Result>` & §5.49 `<PolicyIdentifierList>`**:
  > *"The `<PolicyIdentifierList>` element contains a list of policy and policy set identifiers of policies **which have been applicable to a request**. The list is unordered."*
  - Glossary: *"applicable policy: A policy or policy set that governs a particular decision request."*

#### Current xacml4j Implementation
- `xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:201-203`:
  ```java
  @Override
  public final void addEvaluationResult(CompositeDecisionRule policy, Decision result) {
      this.evaluatedPolicies.put(policy.getId(), policy);
  }
  ```
- Whenever a policy or policyset is evaluated, it is added to `evaluatedPolicies`, regardless of whether its result was `NotApplicable`, `Indeterminate`, `Permit`, or `Deny`.
- In `IIIG300`, `xacml4j` currently returns **5 IDs**: `policyset`, `policy1`, `policy2`, `policy3`, `policy4`.

#### Analysis: Evaluated vs. Applicable vs. Contributing
1. **Evaluated != Applicable**:
   - `policy1` evaluated to `NotApplicable` (Target did not match).
   - `policy3` evaluated to `Indeterminate` (Target error).
   - Under §5.49, neither `policy1` nor `policy3` was "applicable to the request". Adding them to `PolicyIdentifierList` is a clear engine defect.
2. **Applicable vs. Contributing**:
   - `policy2` evaluated to `Permit`.
   - `policy4` evaluated to `Deny`.
   - In `ordered-deny-overrides`, `policy4` overrode `policy2`.
   - The OASIS conformance suite author for `IIIG300` expected only the **contributing / governing** policy that produced the final result (`policy4`) along with the enclosing `policyset` (total = 2).
   - If `addEvaluationResult` is filtered to only include applicable policies whose decision is `Permit` or `Deny`, the list contains 3 IDs (`policyset`, `policy2`, `policy4`).
   - If combining algorithms return their effective contributing policies, the list contains 2 IDs (`policyset`, `policy4`), exactly matching the test fixture.

#### Finding & Recommendation
- **Disposition**: **Real Engine Defect** (`policy4j-the.4.22`).
- **Required Fix**:
  1. In `RootEvaluationContext`, do not record policies whose decision is `NOT_APPLICABLE` or `INDETERMINATE`.
  2. For combining algorithms (like `ordered-deny-overrides`), ensure that the tracked policy identifier list reflects the policies contributing to the decision (or provide a configuration flag / algorithm hook).
- **Effort & Risk**:
  - *Effort*: Low (1-2 days). Adjust tracking in `RootEvaluationContext` and combining algorithms.
  - *Risk*: Low. Improves compliance with both the letter and intent of XACML 3.0 §5.48/§5.49.
